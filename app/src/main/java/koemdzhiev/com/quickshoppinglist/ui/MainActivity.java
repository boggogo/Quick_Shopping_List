package koemdzhiev.com.quickshoppinglist.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.software.shell.fab.ActionButton;

import java.util.ArrayList;

import koemdzhiev.com.quickshoppinglist.R;
import koemdzhiev.com.quickshoppinglist.adapters.ShoppingListAdapter;
import koemdzhiev.com.quickshoppinglist.utils.Constants;
import koemdzhiev.com.quickshoppinglist.utils.IabHelper;
import koemdzhiev.com.quickshoppinglist.utils.IabResult;
import koemdzhiev.com.quickshoppinglist.utils.Inventory;
import koemdzhiev.com.quickshoppinglist.utils.Purchase;
import koemdzhiev.com.quickshoppinglist.utils.SimpleDividerItemDecoration;


public class MainActivity extends AppCompatActivity {
    private static final String TAG =  MainActivity.class.getSimpleName();
    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private ArrayList<String> shoppingListItems;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    private TextView mEmptyTextView;
    private ShoppingListAdapter adapter;
    private ActionButton actionButton;
    private MaterialDialog addItemdialog = null;
    private AdView mAdView;
    private IabHelper mHelper;
    private String SKU_REMOVE_ADDS = "remove_adds_sku";
    private boolean mIsRemoveAdds = false;
    private IabHelper.OnIabPurchaseFinishedListener mPurchasedFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        @Override
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (result.isFailure()) {
                Log.d(TAG, "Error purchasing: " + result);
                return;
            }
            else if (purchase.getSku().equals(SKU_REMOVE_ADDS)) {
                // consume the gas and update the UI
                mIsRemoveAdds = true;
                mAdView.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this,"Purchase successful",Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEmptyTextView = (TextView)findViewById(R.id.list_empty);
        mEmptyTextView.setVisibility(View.INVISIBLE);
        mSharedPreferences = getPreferences(MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();

        queryPurchasedItems();

        actionButton = (ActionButton)findViewById(R.id.buttonFloat);
        actionButton.setButtonColor(getResources().getColor(R.color.ColorPrimary));
        actionButton.setButtonColorPressed(getResources().getColor(R.color.ColorPrimaryDark));
        actionButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.fab_plus_icon,null));
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildAlertDialog();
            }
        });
        mToolbar = (Toolbar)findViewById(R.id.tool_bar);
        setSupportActionBar(mToolbar);
        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        if(shoppingListItems == null){
              shoppingListItems = new ArrayList<>();
        }

        //read the array lists
        readShoppingItems();
        adapter = new ShoppingListAdapter(this,shoppingListItems,mSharedPreferences,mEditor);
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
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    actionButton.setShowAnimation(ActionButton.Animations.SCALE_UP);
                    actionButton.show();
                }
            }
        });
        //Swiping to remove item from the list--------
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT){

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                shoppingListItems.remove(viewHolder.getAdapterPosition());
                saveShoppingItems();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
        //Swiping to remove item from the list----code end----
        //check weather to show the empty text view
        isListEmpty();

        //set up billing
        String s1 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtQb2DVEXk0rdUSFd/pxgEERrWEtnWbX5fLHN";
        String s2 = "wN3hUcNnI8o6+86qdmEIgw89nG8KIbmN8Uc7JyT1P09e2BWi2pOdqUqSE1rFcUJBUzSudWQgts6YUZ6g";
        String s3 = "7ck/qDUHZznhABmp11OlRXKq9aWmrxKRObv9x6o8+zD8bcI+6J8WYdhDXAQ2RRA+XJX8h+BZ7Aew2c";
        String s4 = "Vq9RrvxYIr/rrswlx0CFi0h0mluDaOnc3TMlXmT9BNJOljTwv73Iss3L5GHxcdSVysCg9LfmYS0nCciP1kUVeLHizykrHwJI";
        String s5 = "Ra6ejXOdTVLolwJA3M0kt4ZhPxWOkb2NrG9J82DXAte1JchgWUfQIDAQAB";
        String publicKey = s1+s2+s3+s4+s5;

        mHelper = new IabHelper(this,publicKey);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    //error
                    Log.d(TAG, "Proglem setting up in-app Billing: " + result);
                }
                    //Horay, IAB is fully set up!
                    Log.d(TAG, "Horay, IAB is fully set up!");
            }
        });
    }

    private void queryPurchasedItems() {
        //check if user has bought "remove adds"
        IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
            @Override
            public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
                if (result.isFailure()) {
                    // handle error here
                    Toast.makeText(MainActivity.this,"error",Toast.LENGTH_LONG).show();
                }
                else{
                    // does the user have the premium upgrade?
                    mIsRemoveAdds = inventory.hasPurchase(SKU_REMOVE_ADDS);
                    if(!mIsRemoveAdds) {
                        Toast.makeText(MainActivity.this,"no premium",Toast.LENGTH_LONG).show();
                        mAdView = (AdView) findViewById(R.id.adView);
                        AdRequest adRequest = new AdRequest.Builder().build();
                        mAdView.loadAd(adRequest);
                    }else{
                        mAdView.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this,"premium",Toast.LENGTH_LONG).show();
                    }

                }
            }
        };
        mHelper.queryInventoryAsync(mGotInventoryListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        queryPurchasedItems();
        isListEmpty();
    }

    @Override
    protected void onResume() {
        super.onResume();
        queryPurchasedItems();
        isListEmpty();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHelper != null) mHelper.dispose();
        mHelper = null;
        mAdView.destroy();
    }

    private void readShoppingItems() {
        int arrayListSizeDefaultValue = 0;
        int size = mSharedPreferences.getInt(Constants.ARRAY_LIST_SIZE_KEY, arrayListSizeDefaultValue);
        for(int i = 0;i< size;i++){
            shoppingListItems.add(mSharedPreferences.getString(Constants.ARRAY_LIST_ITEM_KEY + i,null));
        }
    }

    private void saveShoppingItems() {
        //save array list
        mEditor.putInt(Constants.ARRAY_LIST_SIZE_KEY, shoppingListItems.size());
        for (int i =0;i<shoppingListItems.size();i++){
            mEditor.putString(Constants.ARRAY_LIST_ITEM_KEY + i,shoppingListItems.get(i));
        }
        mEditor.apply();
        adapter.notifyDataSetChanged();
    }

    private void buildAlertDialog() {
        final int[] choosenQuantity = {1};
        final String[] str = {""};
        final MaterialDialog.Builder addItemBuilder = new MaterialDialog.Builder(this);
        addItemBuilder.title("Add Item");
        addItemBuilder.inputMaxLength(30, R.color.material_blue_grey_950);
        addItemBuilder.content("Quantity:" + choosenQuantity[0]);
        //addItemBuilder.widgetColor(getResources().getColor(R.color.ColorPrimary));
        addItemBuilder.positiveColor(getResources().getColor(R.color.ColorPrimary));
        addItemBuilder.negativeColor(getResources().getColor(R.color.ColorPrimary));
        addItemBuilder.neutralColor(getResources().getColor(R.color.ColorPrimary));
        addItemBuilder.titleColor(getResources().getColor(R.color.ColorPrimaryDark));
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
                    saveShoppingItems();
                    isListEmpty();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            String allShoppingItems = getAllShoppingItemsToSend();
            Intent sendIntent = new Intent(Intent.ACTION_VIEW);
            sendIntent.setData(Uri.parse("sms:"));
            sendIntent.putExtra("sms_body","Shopping list:\n\n" + allShoppingItems);
            startActivity(sendIntent);
            return true;
        }
        if(id == R.id.action_send_email){
            String allShoppingItems = getAllShoppingItemsToSend();
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", "", null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Shopping list");
            emailIntent.putExtra(Intent.EXTRA_TEXT, allShoppingItems);
            startActivity(Intent.createChooser(emailIntent, "Send email..."));
        }
        if(id == R.id.action_clearAll){
            MaterialDialog.Builder builder = new MaterialDialog.Builder(this);
            builder.title("Clear All");
            builder.content("Are you sure that you want to remove all shopping items from the list?");
            builder.positiveText("YES");
            builder.negativeText("NO");
            builder.callback(new MaterialDialog.ButtonCallback() {
                @Override
                public void onPositive(MaterialDialog dialog) {
                    super.onPositive(dialog);
                    shoppingListItems.clear();
                    saveShoppingItems();
                }
            });
            MaterialDialog dialog = builder.build();
            dialog.show();
        }
        if(id == R.id.action_about){
            //start about activity
            Intent intent = new Intent(MainActivity.this,AboutActivity.class);
            startActivity(intent);
        }
        if(id == R.id.action_remove_adds){
            mHelper.launchPurchaseFlow(this,SKU_REMOVE_ADDS,1,mPurchasedFinishedListener,"");
        }

        return super.onOptionsItemSelected(item);
    }

    private String getAllShoppingItemsToSend() {
        String allShoppingItems = "";
        int arrayListSizeDefaultValue = 0;
        int size = mSharedPreferences.getInt(Constants.ARRAY_LIST_SIZE_KEY, arrayListSizeDefaultValue);
        for(int i = 0;i<size;i++){
            allShoppingItems += shoppingListItems.get(i)+"\n";
        }
        return allShoppingItems;
    }
    private void saveIsRemoveAdds(boolean b) {
        mEditor.putBoolean(Constants.IS_REMOVE_ADDS,b);
    }
    private void isListEmpty() {
        if (mRecyclerView.getAdapter().getItemCount() == 0) {
            mEmptyTextView.setVisibility(View.VISIBLE);
        }
        else {
            mEmptyTextView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            //setContentView(R.layout.activity_main_adds);
            super.onActivityResult(requestCode, resultCode, data);
        }
        else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
            //setContentView(R.layout.activity_main_adds);
        }
    }
}
