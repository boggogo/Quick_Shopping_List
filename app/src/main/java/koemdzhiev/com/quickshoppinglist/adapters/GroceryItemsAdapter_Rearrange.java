package koemdzhiev.com.quickshoppinglist.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import koemdzhiev.com.quickshoppinglist.R;

/**
 * Created by koemdzhiev on 18/06/2015.
 * this adapter will adapt the shopping list items
 */
public class GroceryItemsAdapter_Rearrange extends RecyclerView.Adapter<GroceryItemsAdapter_Rearrange.ShoppingListViewHolder> {
    private ArrayList<String> mItems;
    private Context mContext;
    private SharedPreferences.Editor mEditor;
    public static String nameOfList;

    public GroceryItemsAdapter_Rearrange(Context context, ArrayList<String> items, SharedPreferences.Editor editor, String nameOfList) {
        mItems = items;
        mContext = context;
        mEditor = editor;
        this.nameOfList = nameOfList;
    }

    @Override
    public ShoppingListViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.shopping_list_item_arrange,viewGroup,false);
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

    public class ShoppingListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView mShoppingListItem;
        public TextView mEmptyTextView;

        public ShoppingListViewHolder(View itemView) {
            super(itemView);
            mShoppingListItem = (TextView) itemView.findViewById(R.id.shoppingListItem);

            View rootView = ((Activity)mContext).getWindow().getDecorView().findViewById(android.R.id.content);

            mEmptyTextView = (TextView)rootView.findViewById(R.id.list_empty);
            mEmptyTextView.setVisibility(View.INVISIBLE);
            itemView.setOnClickListener(this);
        }

        public void bindShoppingList(String item){
            mShoppingListItem.setText(item);
        }

        @Override
        public void onClick(View view) {
            Toast.makeText(mContext,"Press and hold on an item to rearrange!",Toast.LENGTH_LONG).show();
        }
    }

}
