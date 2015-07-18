package koemdzhiev.com.quickshoppinglist.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

import koemdzhiev.com.quickshoppinglist.R;
import koemdzhiev.com.quickshoppinglist.ui.MainActivity;
import koemdzhiev.com.quickshoppinglist.utils.Constants;

/**
 * Created by koemdzhiev on 18/07/2015.
 * this adapter will adapt the shopping lists
 */
public class ShoppingListsAdapter extends RecyclerView.Adapter<ShoppingListsAdapter.ShoppingListsViewHolder>  {
    private ArrayList<String> mShoppingListsItems;
    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    public ShoppingListsAdapter( Context context,ArrayList<String> items,SharedPreferences preferences, SharedPreferences.Editor editor) {
        mContext = context;
        mShoppingListsItems = items;
        mSharedPreferences = preferences;
        mEditor = editor;

    }

    @Override
    public ShoppingListsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.shopping_lists_item_layout,parent,false);
        ShoppingListsAdapter.ShoppingListsViewHolder viewHolder = new ShoppingListsAdapter.ShoppingListsViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ShoppingListsViewHolder holder, int position) {
        holder.bindShoppingList(mShoppingListsItems.get(position));
    }

    @Override
    public int getItemCount() {
        return mShoppingListsItems.size();
    }

    public class ShoppingListsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener {
        public TextView mShoppingList;

        public ShoppingListsViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            mShoppingList = (TextView) itemView.findViewById(R.id.shopping_list_title);
        }

        public void bindShoppingList(String item){
            mShoppingList.setText(item);
        }

        @Override
        public void onClick(View v) {
//            Toast.makeText(mContext, mShoppingList.getText().toString(), Toast.LENGTH_SHORT).show();
            String nameOfShoppingList = mShoppingList.getText().toString();
            Intent intent = new Intent(mContext, MainActivity.class);
            intent.putExtra(Constants.NAME_OF_SHOPPING_LIST,nameOfShoppingList);
            mContext.startActivity(intent);
        }

        @Override
        public boolean onLongClick(View v) {
            final String[] str = {""};
            final int selectedItem = getAdapterPosition();
            String itemToBeEdited = mSharedPreferences.getString(Constants.ARRAY_SHOPPING_LIST_KEY + selectedItem, null);

            MaterialDialog.Builder builder = new MaterialDialog.Builder(mContext);
            builder.title("Edit Shopping List Name");
            builder.positiveColor(mContext.getResources().getColor(R.color.ColorPrimary));
            builder.negativeColor(mContext.getResources().getColor(R.color.ColorPrimary));
            builder.titleColor(mContext.getResources().getColor(R.color.ColorPrimaryDark));
            builder.input("new shopping list name", itemToBeEdited, new MaterialDialog.InputCallback() {
                @Override
                public void onInput(MaterialDialog materialDialog, CharSequence input) {
                    str[0] = input.toString().trim();
                    //add it to shoppingListItems and save to sharedPreferences
                    if (str[0].length() != 0) {
                        mShoppingListsItems.set(getAdapterPosition(), str[0]);
                        copyShoppingItemsFromPrevious(ShoppingListItemAdapter.nameOfList, str[0]);
                        saveShoppingLists();
//                        notifyDataSetChanged();
//                        isListEmpty();
                    } else {
                        Toast.makeText(mContext, "no list name!", Toast.LENGTH_LONG).show();
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
            return true;
        }
    }

    private void saveShoppingLists() {
        //save array list
        mEditor.putInt(Constants.ARRAY_SHOPPING_LIST_SIZE_KEY, mShoppingListsItems.size());
        for (int i =0;i<mShoppingListsItems.size();i++){
            mEditor.putString(Constants.ARRAY_SHOPPING_LIST_KEY + i,mShoppingListsItems.get(i));
        }
        mEditor.apply();
        notifyDataSetChanged();
    }

    private void copyShoppingItemsFromPrevious(String nameOfListToRead,String nameOfListToWrite) {
        ArrayList<String> temp = new ArrayList<>();
        int arrayListSizeDefaultValue = 0;
        //copy from previous location
        int size = mSharedPreferences.getInt(Constants.ARRAY_LIST_SIZE_KEY+nameOfListToRead, arrayListSizeDefaultValue);
        for(int i = 0;i< size;i++){
            temp.add(mSharedPreferences.getString(Constants.ARRAY_LIST_ITEM_KEY + nameOfListToRead + i,null));
        }
        //delete from previous location
        for(int i = 0; i < size; i++) {
            mEditor.remove(Constants.ARRAY_LIST_ITEM_KEY + nameOfListToRead + i);
        }
        //mEditor.commit();

        //save to the new location
        mEditor.putInt(Constants.ARRAY_LIST_SIZE_KEY+nameOfListToWrite, temp.size());
        for (int i =0;i<temp.size();i++){
            mEditor.putString(Constants.ARRAY_LIST_ITEM_KEY + nameOfListToWrite + i,temp.get(i));
        }
        mEditor.commit();
        notifyDataSetChanged();
    }


}
