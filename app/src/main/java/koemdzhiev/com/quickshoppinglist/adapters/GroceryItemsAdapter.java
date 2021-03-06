package koemdzhiev.com.quickshoppinglist.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

import koemdzhiev.com.quickshoppinglist.R;
import koemdzhiev.com.quickshoppinglist.utils.Constants;

/**
 * Created by koemdzhiev on 18/06/2015.
 * this adapter will adapt the shopping list items
 */
public class GroceryItemsAdapter extends RecyclerView.Adapter<GroceryItemsAdapter.ShoppingListViewHolder> {
    private ArrayList<String> mItems;
    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    private MaterialDialog addItemdialog;
    public static String nameOfList;

    public GroceryItemsAdapter(Context context, ArrayList<String> items, SharedPreferences preferences, SharedPreferences.Editor editor, String nameOfList) {
        mItems = items;
        mContext = context;
        mSharedPreferences = preferences;
        mEditor = editor;
        this.nameOfList = nameOfList;
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

    public class ShoppingListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        public TextView mShoppingListItem;
        public ImageView mImageView;
        public TextView mEmptyTextView;

        public ShoppingListViewHolder(View itemView) {
            super(itemView);
            mShoppingListItem = (TextView) itemView.findViewById(R.id.shoppingListItem);
            mImageView = (ImageView) itemView.findViewById(R.id.shoppingListBinIcon);

            View rootView = ((Activity)mContext).getWindow().getDecorView().findViewById(android.R.id.content);

            mEmptyTextView = (TextView)rootView.findViewById(R.id.list_empty);
            mEmptyTextView.setVisibility(View.INVISIBLE);
            mImageView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        public void bindShoppingList(String item){
            mShoppingListItem.setText(item);
        }



        @Override
        public boolean onLongClick(View v) {
            final int selectedItem = getAdapterPosition();
            String itemToBeEdited = mSharedPreferences.getString(Constants.ARRAY_LIST_ITEM_KEY + GroceryItemsAdapter.nameOfList + selectedItem, null);
            //check if the selected item has added quantity and if yes -> remove space+(number)
            String formatted ="";
            int itemSavedQuantity = 1;
            assert itemToBeEdited != null;
            if(itemToBeEdited.length()-4>0 && (itemToBeEdited.charAt(itemToBeEdited.length()-1)==')')){
                //get the save quantity
                itemSavedQuantity = Integer.parseInt(itemToBeEdited.charAt(itemToBeEdited.length()-2)+"");
                //format the string by removing the space + (number)
                formatted = itemToBeEdited.substring(0, itemToBeEdited.length() - 4);

            }else{
                formatted = itemToBeEdited;
            }

            final String[] str = {""};
            final int[] userQuantityInput = {itemSavedQuantity};
//            Toast.makeText(mContext, "Long Press", Toast.LENGTH_LONG).show();
            final MaterialDialog.Builder addItemBuilder = new MaterialDialog.Builder(mContext);
            addItemBuilder.title("Edit Item");
            //addItemBuilder.widgetColor(mContext.getResources().getColor(R.color.ColorPrimaryDark));
            addItemBuilder.titleColor(ContextCompat.getColor(mContext,R.color.ColorPrimaryDark));
            addItemBuilder.inputMaxLength(30, ContextCompat.getColor(mContext,R.color.fab_material_red_900));
            addItemBuilder.content("Quantity:" + userQuantityInput[0]);
            addItemBuilder.inputType(InputType.TYPE_CLASS_TEXT);
            addItemBuilder.autoDismiss(true);
            addItemBuilder.input("Edit shopping item", "", new MaterialDialog.InputCallback() {
                @Override
                public void onInput(MaterialDialog dialog, CharSequence input) {
                    str[0] = input.toString().trim();
                    //add it to shoppingListItems and save to sharedPreferences
                    if (str[0].length() != 0) {
                        //save items
                        if (userQuantityInput[0] > 1) {
                            str[0] += " (" + userQuantityInput[0] + ")";
                        }
                        mEditor.putString(Constants.ARRAY_LIST_ITEM_KEY + GroceryItemsAdapter.nameOfList+ selectedItem, str[0]);
                        mEditor.apply();
                        //clear the content
                        mItems.clear();
                        //read again content
                        readShoppingItems(GroceryItemsAdapter.nameOfList);
                        notifyDataSetChanged();
                        dialog.dismiss();
//                        Toast.makeText(mContext, "Saved", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(mContext, "no item description!", Toast.LENGTH_LONG).show();
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
            addItemBuilder.neutralText("Edit Quantity");
            addItemBuilder.callback(new MaterialDialog.ButtonCallback() {
                @Override
                public void onNeutral(final MaterialDialog dialog) {
                    super.onNeutral(dialog);
                    addItemBuilder.autoDismiss(false);
                    MaterialDialog.Builder quantityDialogBuilder = new MaterialDialog.Builder(mContext);
                    quantityDialogBuilder.title("Edit Quantity");
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
                            userQuantityInput[0] = which + 1;
                            addItemdialog.setContent("Quantity:" + userQuantityInput[0]);
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
            if(addItemdialog.getInputEditText() != null) {
                addItemdialog.getInputEditText().setText(formatted);
            }
            addItemdialog.show();
            return true;
        }

        @Override
        public void onClick(View view) {
            mItems.remove(getAdapterPosition());
            if (getItemCount() == 0) {
                mEmptyTextView.setVisibility(View.VISIBLE);
            }
            else {
                mEmptyTextView.setVisibility(View.INVISIBLE);
            }
            saveShoppingItems(GroceryItemsAdapter.nameOfList);
            notifyItemRemoved(getAdapterPosition());
        }
    }

    private void readShoppingItems(String nameOfListToRead) {
        int arrayListSizeDefaultValue = 0;
        int size = mSharedPreferences.getInt(Constants.ARRAY_LIST_SIZE_KEY+nameOfListToRead, arrayListSizeDefaultValue);
        for(int i = 0;i< size;i++){
            mItems.add(mSharedPreferences.getString(Constants.ARRAY_LIST_ITEM_KEY + nameOfListToRead + i,null));
        }
    }

    private void saveShoppingItems(String nameOfListToRead) {
        //save array list
        mEditor.putInt(Constants.ARRAY_LIST_SIZE_KEY + nameOfListToRead, mItems.size());
        for (int i =0;i<mItems.size();i++){
            mEditor.putString(Constants.ARRAY_LIST_ITEM_KEY + nameOfListToRead + i,mItems.get(i));
        }
        mEditor.apply();
        notifyDataSetChanged();
    }

}
