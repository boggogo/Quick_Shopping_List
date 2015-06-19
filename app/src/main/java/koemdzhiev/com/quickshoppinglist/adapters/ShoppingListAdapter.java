package koemdzhiev.com.quickshoppinglist.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;

import koemdzhiev.com.quickshoppinglist.Constants;
import koemdzhiev.com.quickshoppinglist.R;

/**
 * Created by koemdzhiev on 18/06/2015.
 */
public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ShoppingListViewHolder> {
    private ArrayList<String> mItems;
    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;

    public ShoppingListAdapter(Context context, ArrayList<String> items, SharedPreferences preferences,SharedPreferences.Editor editor) {
        mItems = items;
        mContext = context;
        mSharedPreferences = preferences;
        mEditor = editor;
    }

    @Override
    public ShoppingListViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.shopping_list_item,viewGroup,false);
        ShoppingListViewHolder viewHolder = new ShoppingListViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ShoppingListViewHolder shoppingListViewHolder, int position) {
        shoppingListViewHolder.bindShoppingList(mItems.get(position));
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class ShoppingListViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener, View.OnClickListener{
        public TextView mShoppingListItem;
        public CheckBox mCheckBox;

        public ShoppingListViewHolder(View itemView) {
            super(itemView);
            mShoppingListItem = (TextView) itemView.findViewById(R.id.shoppingListItem);
            mCheckBox = (CheckBox) itemView.findViewById(R.id.shoppingListCheckBox);
            mCheckBox.setOnCheckedChangeListener(this);
            itemView.setOnClickListener(this);
        }

        public void bindShoppingList(String item){
            mShoppingListItem.setText(item);
            mCheckBox.setChecked(false);
        }


        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked){
                mItems.remove(getAdapterPosition());
                saveShoppingItems();
                notifyItemRemoved(getAdapterPosition());

            }
        }

        @Override
        public void onClick(View v) {

        }
        //Method to save items to shared preferences
        private void saveShoppingItems() {
            //save array list
            mEditor.putInt(Constants.ARRAY_LIST_SIZE_KEY, mItems.size());
            for (int i =0;i<mItems.size();i++){
                mEditor.putString(Constants.ARRAY_LIST_ITEM_KEY + i,mItems.get(i));
            }
            mEditor.apply();
        }
    }

}
