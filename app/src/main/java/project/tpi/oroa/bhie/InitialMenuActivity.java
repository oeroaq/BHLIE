package project.tpi.oroa.bhie;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import project.tpi.oroa.bhie.Kids.InitialMenuKidsActivity;

public class InitialMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_menu);
    }

    public void onClickLittleSuperHero(View v)
    {
        try {
            Intent k = new Intent(InitialMenuActivity.this, InitialMenuKidsActivity.class);
            startActivity(k);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
