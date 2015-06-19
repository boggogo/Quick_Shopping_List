package koemdzhiev.com.quickshoppinglist;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

import koemdzhiev.com.quickshoppinglist.adapters.ShoppingListAdapter;


public class MainActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private ArrayList<Item> shoppingListItems;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar)findViewById(R.id.tool_bar);
        setSupportActionBar(mToolbar);
        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        shoppingListItems = new ArrayList<>();
        shoppingListItems.add(new Item("Apples"));
        shoppingListItems.add(new Item("Bred"));
        shoppingListItems.add(new Item("Potatoes"));
        shoppingListItems.add(new Item("Muffins"));
        shoppingListItems.add(new Item("Crackers"));
        shoppingListItems.add(new Item("Spaghetti"));
        shoppingListItems.add(new Item("Plastic Bags"));
        shoppingListItems.add(new Item("Deodorant"));
        shoppingListItems.add(new Item("Razors"));
        shoppingListItems.add(new Item("Shampoo"));
        shoppingListItems.add(new Item("Tooth brushes"));
        shoppingListItems.add(new Item("Butter"));
        shoppingListItems.add(new Item("Bagels"));
        shoppingListItems.add(new Item("Coconut water"));
        shoppingListItems.add(new Item("Tomatoes"));

        ShoppingListAdapter adapter = new ShoppingListAdapter(this,shoppingListItems);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getApplicationContext()));
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

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
}
