package koemdzhiev.com.quickshoppinglist.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.software.shell.fab.ActionButton;

import java.util.ArrayList;

import koemdzhiev.com.quickshoppinglist.R;
import koemdzhiev.com.quickshoppinglist.adapters.ShoppingListsAdapter;
import koemdzhiev.com.quickshoppinglist.utils.Constants;
import koemdzhiev.com.quickshoppinglist.utils.IabHelper;
import koemdzhiev.com.quickshoppinglist.utils.IabResult;
import koemdzhiev.com.quickshoppinglist.utils.Inventory;
import koemdzhiev.com.quickshoppinglist.utils.Purchase;
import koemdzhiev.com.quickshoppinglist.utils.SimpleDividerItemDecoration;

public class ShoppingListsActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG =  ShoppingListsActivity.class.getSimpleName();
    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private ArrayList<String> mShoppingLists;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    private TextView mEmptyTextView;
    private ActionButton actionButton;
    private ShoppingListsAdapter mAdapter;
    private AdView mAdView;
    private LinearLayout adContainer;
    private IabHelper mHelper;
    private String SKU_REMOVE_ADDS = "remove_adds_sku";
    private boolean mIsRemoveAdds = false;
    private   IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        @Override
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (result.isFailure()) {
                // handle error here
                Toast.makeText(ShoppingListsActivity.this,"error",Toast.LENGTH_LONG).show();
            }
            else{
                // does the user have the premium upgrade?
                mIsRemoveAdds = inventory.hasPurchase(SKU_REMOVE_ADDS);
                if(!mIsRemoveAdds) {
                    //play ads
                    mAdView = new AdView(ShoppingListsActivity.this);
                    mAdView.setAdSize(AdSize.BANNER);
                    mAdView.setAdUnitId(getString(R.string.banner_ad_unit_id));
                    AdRequest adRequest = new AdRequest.Builder().build();
                    mAdView.loadAd(adRequest);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.gravity = Gravity.CENTER;
                    adContainer = (LinearLayout)findViewById(R.id.myAdContainer);
                    //check if the adview is already added (count ==2) otherwise added
                    int i = adContainer.getChildCount();
                    if(! (i > 1)) {
                        adContainer.addView(mAdView, params);
                    }
                    //Toast.makeText(MainActivity.this,"no premium",Toast.LENGTH_LONG).show();
                }else{
                    //remove ads
                    if(mAdView != null) {
                        adContainer.removeView(mAdView);
                    }
                    //Toast.makeText(MainActivity.this,"premium",Toast.LENGTH_LONG).show();
                }

            }
        }
    };
    private IabHelper.OnIabPurchaseFinishedListener mPurchasedFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        @Override
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (result.isFailure()) {
                Log.d(TAG, "Error purchasing: " + result);
                if(result.getResponse() == IabHelper.BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED) {
                    MaterialDialog dialog = buildItemAlreadyPurchasedDialog();
                    dialog.show();
                }
                return;
            }
            else if (purchase.getSku().equals(SKU_REMOVE_ADDS)) {
                // consume the gas and update the UI
                mIsRemoveAdds = true;
                //remove ads
                if(mAdView != null) {
                    adContainer.removeView(mAdView);
                }
                Toast.makeText(ShoppingListsActivity.this,"Purchase successful",Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_lists);
        mEmptyTextView = (TextView)findViewById(R.id.list_empty);
        mEmptyTextView.setVisibility(View.INVISIBLE);
        mSharedPreferences = getSharedPreferences(Constants.APP_NAME_KEY, MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
        actionButton = (ActionButton)findViewById(R.id.buttonFloat);
        actionButton.setButtonColor(getResources().getColor(R.color.ColorPrimary));
        actionButton.setButtonColorPressed(getResources().getColor(R.color.ColorPrimaryDark));
        actionButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.fab_plus_icon, null));
        actionButton.setOnClickListener(this);
        mToolbar = (Toolbar)findViewById(R.id.tool_bar);
        mToolbar.setTitle("Shopping Lists");
        setSupportActionBar(mToolbar);
        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        if(mShoppingLists == null){
            mShoppingLists = new ArrayList<>();
        }
        //read the array lists
        readShoppingItems();
//        Toast.makeText(this, ""+mSharedPreferences.getString(Constants.ARRAY_LIST_ITEM_KEY+mShoppingLists.get(0)+0,""), Toast.LENGTH_LONG).show();
//        mShoppingLists.add("TEST");
//        mShoppingLists.add("TEST");
        mAdapter = new ShoppingListsAdapter(ShoppingListsActivity.this,mShoppingLists,mSharedPreferences,mEditor);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getApplicationContext()));
        mRecyclerView.setAdapter(mAdapter);
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
                deleteShoppingListItems(viewHolder.getAdapterPosition());
                mShoppingLists.remove(viewHolder.getAdapterPosition());
                saveShoppingItems();
                isListEmpty();
                mAdapter.notifyDataSetChanged();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
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
                if (result.isSuccess()) {
                    //Horay, IAB is fully set up!
                    Log.d(TAG, "Horay, IAB is fully set up!");
                    //queryPurchasedItems;
                    mHelper.queryInventoryAsync(mGotInventoryListener);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //check if user has bought "remove adds"
        if(mHelper.isSetupDone() && !mHelper.isAsyncInProgress()) {
            mHelper.queryInventoryAsync(mGotInventoryListener);
        }
        isListEmpty();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHelper != null) mHelper.dispose();
        mHelper = null;
        if(mAdView != null){
            mAdView.destroy();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_item_lists, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.action_remove_adds){
            if(mHelper.isSetupDone()) {
                mHelper.launchPurchaseFlow(this, SKU_REMOVE_ADDS, 1, mPurchasedFinishedListener, "");
            }
        }
        //noinspection SimplifiableIfStatement
        if(id == R.id.action_about){
            //start about activity
            Intent intent = new Intent(ShoppingListsActivity.this,AboutActivity.class);
            startActivity(intent);
        }

        if(id == R.id.action_how_to_use){
            //start about activity
            Intent intent = new Intent(ShoppingListsActivity.this,HowToActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.buttonFloat){
            final String[] str = {""};
            MaterialDialog.Builder builder = new MaterialDialog.Builder(this);
            builder.title("New Shopping List");
            builder.positiveColor(getResources().getColor(R.color.ColorPrimary));
            builder.negativeColor(getResources().getColor(R.color.ColorPrimary));
            builder.titleColor(getResources().getColor(R.color.ColorPrimaryDark));
            builder.input("add shopping list", "", new MaterialDialog.InputCallback() {
                @Override
                public void onInput(MaterialDialog materialDialog, CharSequence input) {
                    str[0] = input.toString().trim();
                    //add it to shoppingListItems and save to sharedPreferences
                    if (str[0].length() != 0) {
                        if(!mShoppingLists.contains(str[0])){
                            mShoppingLists.add(str[0]);
                            saveShoppingItems();
                            isListEmpty();
                        }else {
                            MaterialDialog.Builder builder = new MaterialDialog.Builder(ShoppingListsActivity.this);
                            builder.title("Shopping list already exist!");
                            builder.content("Sorry! \nYou cannot have two shopping lists with the same name.");
                            builder.positiveText("Oh, ok...");
                            builder.show();
                        }
                    } else {
                        Toast.makeText(ShoppingListsActivity.this, "no list name!", Toast.LENGTH_LONG).show();
                    }
                }
            }).positiveText("SAVE").callback(new MaterialDialog.ButtonCallback() {
                @Override
                public void onPositive(MaterialDialog dialog) {
                    super.onPositive(dialog);
                }
            }).negativeText("CANCEL").callback(new MaterialDialog.ButtonCallback() {
                @Override
                public void onNegative(MaterialDialog dialog) {
                    super.onNegative(dialog);
                    dialog.dismiss();
                }
            }).show();
        }
    }

    private void saveShoppingItems() {
        //save array list
        mEditor.putInt(Constants.ARRAY_SHOPPING_LIST_SIZE_KEY, mShoppingLists.size());
        for (int i =0;i<mShoppingLists.size();i++){
            mEditor.putString(Constants.ARRAY_SHOPPING_LIST_KEY + i,mShoppingLists.get(i));
        }
        mEditor.apply();
        mAdapter.notifyDataSetChanged();
    }
    private void readShoppingItems() {
        int arrayListSizeDefaultValue = 0;
        int size = mSharedPreferences.getInt(Constants.ARRAY_SHOPPING_LIST_SIZE_KEY, arrayListSizeDefaultValue);
        for(int i = 0;i< size;i++){
            mShoppingLists.add(mSharedPreferences.getString(Constants.ARRAY_SHOPPING_LIST_KEY + i,null));
        }
    }

    private void deleteShoppingListItems(int position) {
        String nameOfShoppingList = mShoppingLists.get(position);
        int arrayListSizeDefaultValue = 0;
        int size = mSharedPreferences.getInt(Constants.ARRAY_LIST_SIZE_KEY + nameOfShoppingList, arrayListSizeDefaultValue);
        for(int i = 0;i< size;i++){
            mEditor.remove(Constants.ARRAY_LIST_ITEM_KEY + nameOfShoppingList + i);
            mEditor.commit();
        }
        mEditor.putInt(Constants.ARRAY_LIST_SIZE_KEY + nameOfShoppingList, 0);
        mEditor.commit();
//        Toast.makeText(this, mEditor.commit()+"",Toast.LENGTH_LONG).show();
        Toast.makeText(this, "Deleted successfully!",Toast.LENGTH_LONG).show();
    }
    private void isListEmpty() {
        if (mRecyclerView.getAdapter().getItemCount() == 0) {
            mEmptyTextView.setVisibility(View.VISIBLE);
        }
        else {
            mEmptyTextView.setVisibility(View.INVISIBLE);
        }
    }
    private MaterialDialog buildItemAlreadyPurchasedDialog(){
        MaterialDialog.Builder builder = new MaterialDialog.Builder(ShoppingListsActivity.this);
        builder.title("Item already purchased");
        builder.content("Sorry! \nYou cannot buy this product twice.");
        builder.positiveText("Oh, ok...");
        return builder.build();
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
