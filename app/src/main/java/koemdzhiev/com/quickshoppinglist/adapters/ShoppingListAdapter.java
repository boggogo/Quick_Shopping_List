package koemdzhiev.com.quickshoppinglist.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import koemdzhiev.com.quickshoppinglist.Item;
import koemdzhiev.com.quickshoppinglist.R;

/**
 * Created by koemdzhiev on 18/06/2015.
 */
public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ShoppingListViewHolder> {
    private Item[] mItems;
    private Context mContext;

    public ShoppingListAdapter(Context context, Item[] items) {
        mItems = items;
        mContext = context;
    }

    @Override
    public ShoppingListViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.shopping_list_item,viewGroup,false);
        ShoppingListViewHolder viewHolder = new ShoppingListViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ShoppingListViewHolder shoppingListViewHolder, int position) {
        shoppingListViewHolder.bindShoppingList(mItems[position]);
    }

    @Override
    public int getItemCount() {
        return mItems.length;
    }

    public class ShoppingListViewHolder extends RecyclerView.ViewHolder{
        public TextView mShoppingListItem;
        public CheckBox mCheckBox;

        public ShoppingListViewHolder(View itemView) {
            super(itemView);
            mShoppingListItem = (TextView) itemView.findViewById(R.id.shoppingListItem);
            mCheckBox = (CheckBox) itemView.findViewById(R.id.shoppingListCheckBox);
        }

        public void bindShoppingList(Item item){
            mShoppingListItem.setText(item.getItemDescription());
        }
    }
}
