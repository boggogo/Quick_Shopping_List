package koemdzhiev.com.quickshoppinglist;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.software.shell.fab.ActionButton;

import java.util.ArrayList;

import koemdzhiev.com.quickshoppinglist.adapters.ShoppingListAdapter;


public class MainActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private ArrayList<String> shoppingListItems;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    private TextView mEmptyTextView;
    private int arrayListSizeDefaultValue = 0;
    private ShoppingListAdapter adapter;
    private ActionButton actionButton;
    private MaterialDialog addItemdialog = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEmptyTextView = (TextView)findViewById(R.id.list_empty);
        mEmptyTextView.setVisibility(View.INVISIBLE);
        mSharedPreferences = getPreferences(MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();

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

        //check weather to show the empty text view
        isListEmpty();

    }

    private void readShoppingItems() {
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
        addItemBuilder.widgetColor(getResources().getColor(R.color.ColorPrimaryDark));
        addItemBuilder.inputMaxLength(30, R.color.material_blue_grey_950);
        addItemBuilder.content("Quantity:" + choosenQuantity[0]);
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
    protected void onResume() {
        super.onResume();
        isListEmpty();
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void isListEmpty() {
        if (mRecyclerView.getAdapter().getItemCount() == 0) {
            mEmptyTextView.setVisibility(View.VISIBLE);
        }
        else {
            mEmptyTextView.setVisibility(View.INVISIBLE);
        }
    }
}
