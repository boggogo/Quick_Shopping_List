package koemdzhiev.com.quickshoppinglist;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.gc.materialdesign.views.ButtonFloat;

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
    private ButtonFloat mFabButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEmptyTextView = (TextView)findViewById(R.id.list_empty);
        mEmptyTextView.setVisibility(View.INVISIBLE);
        mSharedPreferences = getPreferences(MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();

        mFabButton = (ButtonFloat)findViewById(R.id.buttonFloat);
        mFabButton.setOnClickListener(new View.OnClickListener() {
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

        ShoppingListAdapter adapter = new ShoppingListAdapter(this,shoppingListItems,mSharedPreferences,mEditor);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getApplicationContext()));
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

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
    }

    private void buildAlertDialog() {
        final String[] str = {""};
//        final EditText editText = new EditText(this);
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setMessage("Enter Grocery Item");
//        builder.setTitle("Add Item");
//        builder.setView(editText);
//        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                String str = editText.getText().toString();
//                //add it to shoppingListItems and save to sharedPreferences
//                shoppingListItems.add(str);
//                saveShoppingItems();
//                isListEmpty();
//            }
//        });
//        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                // do sth
//            }
//        });
//
//        AlertDialog dialog = builder.create();
//        dialog.show();
        // using libary
        MaterialDialog builder = new MaterialDialog.Builder(this)
                .title("Add Item")
                .widgetColor(getResources().getColor(R.color.ColorPrimaryDark))
                .inputMaxLength(30,R.color.material_blue_grey_950)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("add shopping item", "", new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                         str[0] = input.toString();
                        //add it to shoppingListItems and save to sharedPreferences
                        shoppingListItems.add(str[0]);
                        saveShoppingItems();
                        isListEmpty();
                    }
                }).negativeText("Cancel").show();


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
            //mRecyclerView.setVisibility(View.GONE);
            mEmptyTextView.setVisibility(View.VISIBLE);
        }
        else {
            //mRecyclerView.setVisibility(View.GONE);
            mEmptyTextView.setVisibility(View.INVISIBLE);
        }
    }
}
