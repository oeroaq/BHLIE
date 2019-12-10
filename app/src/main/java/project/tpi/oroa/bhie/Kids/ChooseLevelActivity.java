package project.tpi.oroa.bhie.Kids;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import project.tpi.oroa.bhie.MainActivity;
import project.tpi.oroa.bhie.R;

public class ChooseLevelActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_level);
        getActionBar().hide();
    }

    public void onClickLevel(View v)
    {
        try {
            Intent k = new Intent(ChooseLevelActivity.this, MainActivity.class);
            k.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(k);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
