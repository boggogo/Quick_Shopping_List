package koemdzhiev.com.quickshoppinglist.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import koemdzhiev.com.quickshoppinglist.R;
import koemdzhiev.com.quickshoppinglist.utils.Constants;

public class IsNewUserActivity extends AppCompatActivity {
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    private Button mShowHowToUseButton;
    private Button mSkipButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_is_new_user);

        mSharedPreferences = getSharedPreferences(Constants.APP_NAME_KEY, MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();

        mSkipButton = (Button)findViewById(R.id.skip_how_to_use_btn);
        mSkipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(IsNewUserActivity.this,ShoppingListsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                //set the variable to false
                setTheUserAsNotNew();
                startActivity(intent);
            }
        });

        mShowHowToUseButton = (Button)findViewById(R.id.how_to_use_btn);
        mShowHowToUseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(IsNewUserActivity.this,HowToActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                //set the variable to false
                setTheUserAsNotNew();
                startActivity(intent);
            }
        });

    }

    private void setTheUserAsNotNew() {
        mEditor.putBoolean(Constants.IS_NEW_USER, false);
        mEditor.apply();
    }
}
