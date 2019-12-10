package project.tpi.oroa.bhie.Kids;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import project.tpi.oroa.bhie.R;

public class ChooseGameActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_game);
        getActionBar().hide();
    }

    public void onClickChooseLevel(View v)
    {
        try {
            Intent k = new Intent(ChooseGameActivity.this, ChooseLevelActivity.class);
            startActivity(k);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
