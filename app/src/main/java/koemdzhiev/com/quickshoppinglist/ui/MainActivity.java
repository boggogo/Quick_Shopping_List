package koemdzhiev.com.quickshoppinglist.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.software.shell.fab.ActionButton;

import java.util.ArrayList;
import java.util.Collections;

import koemdzhiev.com.quickshoppinglist.R;
import koemdzhiev.com.quickshoppinglist.adapters.GroceryItemsAdapter;
import koemdzhiev.com.quickshoppinglist.adapters.GroceryItemsAdapter_Rearrange;
import koemdzhiev.com.quickshoppinglist.utils.Constants;
import koemdzhiev.com.quickshoppinglist.utils.SimpleDividerItemDecoration;


public class MainActivity extends AppCompatActivity {
    private static final String TAG =  MainActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private String name_of_shopping_list;
    private ArrayList<String> shoppingListItems;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    private TextView mEmptyTextView;
    private boolean isArrangeEnabled = false;
    private GroceryItemsAdapter adapter;
    private GroceryItemsAdapter_Rearrange adapter_rearrange;
    private ActionButton actionButton;
    private MaterialDialog addItemdialog = null;
    private MaterialDialog voiceInputDialog = null;

    private boolean mIsVoiceEnabled = false;
    private AudioManager mAudioManager;
    private int mStreamVolume = 0;
    private SpeechRecognizer recognizer;
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mIsVoiceEnabled) {
                showVoiceInputDialog();
            }else{
                showAlertAddShoppingItemDialog();
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mEmptyTextView = (TextView)findViewById(R.id.list_empty);
        mEmptyTextView.setVisibility(View.INVISIBLE);
        mSharedPreferences = getSharedPreferences(Constants.APP_NAME_KEY,Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
        //checks if the speech recognition is available on the device
        if(SpeechRecognizer.isRecognitionAvailable(this)) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(this);
        }
        actionButton = (ActionButton)findViewById(R.id.buttonFloat);
        actionButton.setButtonColor(ContextCompat.getColor(this,R.color.ColorPrimary));
        actionButton.setButtonColorPressed(ContextCompat.getColor(this,R.color.ColorPrimaryDark));
        actionButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.fab_plus_icon, null));
        //read if voice is enabled
        mIsVoiceEnabled = mSharedPreferences.getBoolean(Constants.IS_VOICE_ENABLED,false);
        actionButton.setOnClickListener(mOnClickListener);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        //get the name of the list to read/save
        name_of_shopping_list = getIntent().getStringExtra(Constants.NAME_OF_SHOPPING_LIST);
        toolbar.setTitle(name_of_shopping_list + " list");
        //Toast.makeText(this,name_of_shopping_list,Toast.LENGTH_SHORT).show();
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        if(shoppingListItems == null){
              shoppingListItems = new ArrayList<>();
        }
        //read the array lists
        readShoppingItems(name_of_shopping_list);
        adapter = new GroceryItemsAdapter(this,shoppingListItems,mSharedPreferences,mEditor,name_of_shopping_list);
        adapter_rearrange = new GroceryItemsAdapter_Rearrange(this,shoppingListItems,mEditor,name_of_shopping_list);

        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getApplicationContext()));
        mRecyclerView.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                actionButton.setHideAnimation(ActionButton.Animations.SCALE_DOWN);
                actionButton.hide();
                //if not currently scrolling
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    actionButton.setShowAnimation(ActionButton.Animations.SCALE_UP);
                    actionButton.show();
                }
            }
        });
        //Swiping to remove item from the list--------
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT){

            @Override
            public boolean isLongPressDragEnabled() {
                return isArrangeEnabled;
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder selected, RecyclerView.ViewHolder target) {
                final int from = selected.getAdapterPosition();
                final int to = target.getAdapterPosition();
                Collections.swap(shoppingListItems, from, to);
                if(isArrangeEnabled){
                    adapter_rearrange.notifyItemMoved(from,to);
                }else {
                    adapter.notifyItemMoved(from, to);
                }
                saveShoppingItems(name_of_shopping_list);
                adapter.notifyDataSetChanged();
                return isArrangeEnabled;
            }
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                shoppingListItems.remove(viewHolder.getAdapterPosition());
                saveShoppingItems(name_of_shopping_list);
                notifyAnAdapter();
                triggerEmptyListView();
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
        //Swiping to remove item from the list----code end----
        //check weather to show the empty text view
        triggerEmptyListView();


    }



    @Override
    protected void onResume() {
        super.onResume();
        triggerEmptyListView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(recognizer != null) {
            recognizer.cancel();
        }
        if(voiceInputDialog != null) {
            voiceInputDialog.hide();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(recognizer != null){
            recognizer.destroy();
        }
    }

    private void readShoppingItems(String nameOfListToRead) {
        int arrayListSizeDefaultValue = 0;
        int size = mSharedPreferences.getInt(Constants.ARRAY_LIST_SIZE_KEY+nameOfListToRead, arrayListSizeDefaultValue);
        for(int i = 0;i< size;i++){
            shoppingListItems.add(mSharedPreferences.getString(Constants.ARRAY_LIST_ITEM_KEY + nameOfListToRead + i, null));
        }
    }

    private void saveShoppingItems(String nameOfListToRead) {
        //save array list
        mEditor.putInt(Constants.ARRAY_LIST_SIZE_KEY + nameOfListToRead, shoppingListItems.size());
        for (int i =0;i<shoppingListItems.size();i++){
            mEditor.putString(Constants.ARRAY_LIST_ITEM_KEY + nameOfListToRead + i, shoppingListItems.get(i));
        }
        mEditor.commit();
    }

    private void showAlertAddShoppingItemDialog() {
        final int[] choosenQuantity = {1};
        final String[] str = {""};
        final MaterialDialog.Builder addItemBuilder = new MaterialDialog.Builder(this);
        addItemBuilder.title("Add Item");
        addItemBuilder.inputMaxLength(30, ContextCompat.getColor(this,R.color.fab_material_red_900));
        addItemBuilder.content("Quantity:" + choosenQuantity[0]);
        //addItemBuilder.widgetColor(getResources().getColor(R.color.ColorPrimary));
        addItemBuilder.positiveColor(ContextCompat.getColor(this, R.color.ColorPrimary));
        addItemBuilder.negativeColor(ContextCompat.getColor(this, R.color.ColorPrimary));
        addItemBuilder.neutralColor(ContextCompat.getColor(this, R.color.ColorPrimary));
        addItemBuilder.titleColor(ContextCompat.getColor(this,R.color.ColorPrimaryDark));
        addItemBuilder.inputType(InputType.TYPE_CLASS_TEXT);
        addItemBuilder.autoDismiss(true);
        addItemBuilder.input("add shopping item", "", new MaterialDialog.InputCallback() {
            @Override
            public void onInput(MaterialDialog dialog, CharSequence input) {
                str[0] = input.toString().trim();
                //add it to shoppingListItems and save to sharedPreferences
                if (str[0].length() != 0) {
                    if (choosenQuantity[0] > 1) {
                        shoppingListItems.add(str[0] + " (" + choosenQuantity[0] + ")");
                    } else {
                        shoppingListItems.add(str[0]);
                    }
                    saveShoppingItems(name_of_shopping_list);
                    notifyAnAdapter();
                    triggerEmptyListView();
                    dialog.dismiss();
                } else {
                    Toast.makeText(MainActivity.this, "no item description!", Toast.LENGTH_LONG).show();
                }

            }
        });
        addItemBuilder.negativeText("Cancel");
        addItemBuilder.callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onNegative(MaterialDialog dialog) {
                super.onNegative(dialog);
                dialog.dismiss();
            }
        });
        addItemBuilder.neutralText("Add Quantity");
        addItemBuilder.callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onNeutral(final MaterialDialog dialog) {
                super.onNeutral(dialog);
                addItemBuilder.autoDismiss(false);
                MaterialDialog.Builder quantityDialogBuilder = new MaterialDialog.Builder(MainActivity.this);
                quantityDialogBuilder.title("Add Quantity");
                quantityDialogBuilder.negativeText("CANCEL").callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        addItemBuilder.autoDismiss(true);
                        //refresh dialog to show keyboard
                        addItemdialog.dismiss();
                        addItemdialog.show();
                    }
                });
                quantityDialogBuilder.items(R.array.Quantaty_array);
                quantityDialogBuilder.itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        choosenQuantity[0] = which + 1;
                        addItemdialog.setContent("Quantity:" + choosenQuantity[0]);
                        addItemBuilder.autoDismiss(true);
                        //refresh dialog to show keyboard
                        addItemdialog.dismiss();
                        addItemdialog.show();
                    }
                });
                quantityDialogBuilder.cancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        addItemBuilder.autoDismiss(true);
                        //refresh dialog to show keyboard
                        addItemdialog.dismiss();
                        addItemdialog.show();
                    }
                });
                quantityDialogBuilder.show();
            }
        });
        addItemdialog = addItemBuilder.build();
        addItemdialog.show();
    }

    private void showVoiceInputDialog() {
        //final boolean[] voiceInput_error_flag = {false};
        //check if there is network first
        if(isNetworkConnected()){
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                    "koemdzhiev.com.quickshoppinglist");
            RecognitionListener recognitionListener = new RecognitionListener() {
                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> voiceResults = results
                            .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (voiceResults == null) {
                        Log.e(TAG, "No voice results");
                    } else {
                        Log.d(TAG, "Printing matches: ");
                        for (String match : voiceResults) {
                            Log.d(TAG, match);
                        }
                        String s = voiceResults.get(0);
                        //Capitalize the first letter
                        String cap = s.substring(0,1).toUpperCase();
                        shoppingListItems.add(cap+s.substring(1));
                        triggerEmptyListView();
                        saveShoppingItems(name_of_shopping_list);
                        notifyAnAdapter();
                    }
                }

                @Override
                public void onReadyForSpeech(Bundle params) {
                    Log.d(TAG, "Ready for speech");
                    voiceInputDialog.show();
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mStreamVolume, 0); // again setting the system volume back to the original, un-mutting
                }

                @Override
                public void onError(int error) {
                    Log.d(TAG, "Error listening for speech: " + error);
//                    if(!voiceInput_error_flag[0]) {
//                        Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
//                    }
//                    voiceInput_error_flag[0] = true;
                    //hide the voice dialog
                    voiceInputDialog.hide();
                }

                @Override
                public void onBeginningOfSpeech() {
                    Log.d(TAG, "Speech starting");
                }

                @Override
                public void onBufferReceived(byte[] buffer) {
                    // TODO Auto-generated method stub
                    Log.d(TAG, "onBufferReceived");

                }

                @Override
                public void onEndOfSpeech() {
                    // TODO Auto-generated method stub
                    voiceInputDialog.hide();
                    Log.d(TAG, "onEndOfSpeech");

                }

                @Override
                public void onEvent(int eventType, Bundle params) {
                    // TODO Auto-generated method stub
                    Log.d(TAG, "onEvent");

                }

                @Override
                public void onPartialResults(Bundle partialResults) {
                    // TODO Auto-generated method stub
                    Log.d(TAG, "OnPartialResults");
                }

                @Override
                public void onRmsChanged(float rmsdB) {
                    // TODO Auto-generated method stub
                    Log.d(TAG, "onRmsChanged");

                }
            };
            if(recognizer != null) {
                recognizer.setRecognitionListener(recognitionListener);
                recognizer.startListening(intent);
                mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                mStreamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC); // getting system volume into var for later un-muting
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0); // setting system volume to zero, muting
                voiceInputDialog = new MaterialDialog.Builder(MainActivity.this)
                        .title("Voice input")
                        .content("Pronounce the shopping list item")
                        .progress(true, 0).negativeText("Cancel").callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                super.onNegative(dialog);
                                dialog.hide();
                                recognizer.stopListening();
                            }
                        })
                        .progressIndeterminateStyle(true)
                        .build();
            }else{
                //if it is null it must be unavailable on the device
                Toast.makeText(this,"Speech Recognition is not available on this device!",Toast.LENGTH_LONG).show();
            }
        }else{
            //build error dialog
            MaterialDialog.Builder builder = new MaterialDialog.Builder(MainActivity.this)
                    .title("Ops...")
                    .content(getString(R.string.error_voice_recognition));
            builder.positiveText("OK");
            builder.show();
        }

    }

    private void showNoListItemsAlertDialog(){
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this).title("The list is empty!").content("No shopping list items!")
                .positiveText("OK").callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                    }
                });
        builder.show();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.action_switch);
        if (item != null) {
            Switch action_bar_switch = (Switch) item.getActionView().findViewById(R.id.action_switch);
            if (action_bar_switch != null) {
                int resId = mIsVoiceEnabled ? R.string.enabled : R.string.disabled;
                action_bar_switch.setText("Voice: "+getResources().getString(resId));
                action_bar_switch.setChecked(mIsVoiceEnabled);
                action_bar_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                        mIsVoiceEnabled = !mIsVoiceEnabled;
                        int resId = mIsVoiceEnabled ? R.string.enabled : R.string.disabled;
                        buttonView.setText("Voice: " + getResources().getString(resId));
                        actionButton.setOnClickListener(mOnClickListener);

                        if (isChecked) {
                            mEditor.putBoolean(Constants.IS_VOICE_ENABLED, true);
                        } else {
                            mEditor.putBoolean(Constants.IS_VOICE_ENABLED, false);
                        }
                        mEditor.apply();
                        //Toast.makeText(MainActivity.this, "Switch", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send_sms) {
            if(!ifListEmpty()) {
                String allShoppingItems = getAllShoppingItemsToSend();
                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                sendIntent.setData(Uri.parse("sms:"));
                sendIntent.putExtra("sms_body", "Shopping list:\n\n" + allShoppingItems);
                startActivity(sendIntent);
            }else{
                showNoListItemsAlertDialog();
            }
        }
        if(id == R.id.action_send_email){
            if(!ifListEmpty()) {
                String allShoppingItems = getAllShoppingItemsToSend();
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", "", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Shopping list");
                emailIntent.putExtra(Intent.EXTRA_TEXT, allShoppingItems);
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
            }else{
                showNoListItemsAlertDialog();
            }
        }
        if(id == R.id.action_clearAll){
            MaterialDialog.Builder builder = new MaterialDialog.Builder(this);
            builder.title(R.string.dialog_message_clear_all_title);
            builder.content(R.string.dialog_message_clear_all_message);
            builder.positiveText("YES");
            builder.negativeText("NO");
            builder.callback(new MaterialDialog.ButtonCallback() {
                @Override
                public void onPositive(MaterialDialog dialog) {
                    super.onPositive(dialog);
                    if(!ifListEmpty()) {
                        shoppingListItems.clear();
                        saveShoppingItems(name_of_shopping_list);
                        notifyAnAdapter();
                        triggerEmptyListView();
                    }else{
                        showNoListItemsAlertDialog();
                    }
                }
            });
            MaterialDialog dialog = builder.build();
            dialog.show();
        }
        if(id == R.id.action_remind_me){
            String itemsToBeSend = getAllShoppingItemsToSend();

            if(itemsToBeSend.equals("")){
                showNoListItemsAlertDialog();
            }else {
                Intent intent = new Intent(Intent.ACTION_INSERT)
                        .setData(CalendarContract.Events.CONTENT_URI)
                        .putExtra(CalendarContract.Events.TITLE, "Buy Groceries")
                        .putExtra(CalendarContract.Events.DESCRIPTION, "Buy these groceries:\n" + itemsToBeSend)
                        .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_FREE);
                startActivity(intent);
            }
        }
        if(id == R.id.action_about){
            //start about activity
            Intent intent = new Intent(MainActivity.this,AboutActivity.class);
            startActivity(intent);
        }
        if(id == R.id.action_how_to_use){
            //start about activity
            Intent intent = new Intent(MainActivity.this,HowToActivity.class);
            startActivity(intent);
        }
        if(id == R.id.action_sortAlphabetically){
            if(!ifListEmpty()) {
                Collections.sort(shoppingListItems, String.CASE_INSENSITIVE_ORDER);
                saveShoppingItems(name_of_shopping_list);
                notifyAnAdapter();
            }else{
                showNoListItemsAlertDialog();
            }
        }

        if(id == R.id.action_rearrange){
            if(isArrangeEnabled){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    item.setIcon(getResources().getDrawable(R.drawable.reorder_not_active, getTheme()));
                } else {
                    item.setIcon(getResources().getDrawable(R.drawable.reorder_not_active));
                }

                isArrangeEnabled = false;
                mRecyclerView.setAdapter(adapter);

            }else{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    item.setIcon(getResources().getDrawable(R.drawable.reorder_icon_active, getTheme()));
                } else {
                    item.setIcon(getResources().getDrawable(R.drawable.reorder_icon_active));
                }

                isArrangeEnabled = true;
                mRecyclerView.setAdapter(adapter_rearrange);
                showHowToDragDialog();
            }

        }
        return super.onOptionsItemSelected(item);
    }

    private void showHowToDragDialog() {
        AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater adbInflater = LayoutInflater.from(MainActivity.this);
        View checkboxView = adbInflater.inflate(R.layout.checkbox,null);
        final CheckBox dontShowAgain = (CheckBox) checkboxView.findViewById(R.id.skip);
        adb.setView(checkboxView);
        adb.setTitle(getString(R.string.dialog_message_rearrange_title));
        adb.setMessage(getString(R.string.dialog_message_rearrange_message));
        adb.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Boolean checkBoxResult = false;
                if (dontShowAgain.isChecked()) {
                    checkBoxResult = true;
                }

                mEditor.putBoolean(Constants.IF_SKIP_MESSAGE, checkBoxResult);
                // Commit the edits!
                mEditor.commit();
            }
        });

        Boolean skipMessage = mSharedPreferences.getBoolean(Constants.IF_SKIP_MESSAGE, false);
        if (!skipMessage)
            adb.show();
    }

    private void notifyAnAdapter() {
        if(isArrangeEnabled){
            adapter_rearrange.notifyDataSetChanged();
        }else {
            adapter.notifyDataSetChanged();
        }
    }

    private String getAllShoppingItemsToSend() {
        String allShoppingItems = "";
        int arrayListSizeDefaultValue = 0;
        int size = mSharedPreferences.getInt(Constants.ARRAY_LIST_SIZE_KEY + name_of_shopping_list, arrayListSizeDefaultValue);
        for(int i = 0;i<size;i++){
            allShoppingItems += shoppingListItems.get(i)+"\n";
        }
        return allShoppingItems;
    }

    private void triggerEmptyListView() {
        if (mRecyclerView.getAdapter().getItemCount() == 0) {
            mEmptyTextView.setVisibility(View.VISIBLE);
        }
        else {
            mEmptyTextView.setVisibility(View.INVISIBLE);
        }
    }

    private  boolean ifListEmpty(){
        return shoppingListItems.size() == 0;
    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm.getActiveNetworkInfo() != null);
    }

}
