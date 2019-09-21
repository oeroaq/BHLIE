package project.tpi.oroa.bhie.Kids;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;

import project.tpi.oroa.bhie.InitialMenuActivity;
import project.tpi.oroa.bhie.R;

public class InitialMenuKidsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_menu_kids);
    }

    public void onClickCreateSuperHero(View v)
    {
        try {
            Intent k = new Intent(InitialMenuKidsActivity.this, CreateSuperHeroActivity.class);
            startActivity(k);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void onClickSuperHero(View v)
    {
        try {
            Intent k = new Intent(InitialMenuKidsActivity.this, SuperHeroActivity.class);
            startActivity(k);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }


}
