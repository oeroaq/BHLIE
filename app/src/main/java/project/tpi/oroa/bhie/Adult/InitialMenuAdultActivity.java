package project.tpi.oroa.bhie.Adult;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import project.tpi.oroa.bhie.Kids.ChooseGameActivity;
import project.tpi.oroa.bhie.Kids.CreateSuperHeroActivity;
import project.tpi.oroa.bhie.Kids.SuperHeroActivity;
import project.tpi.oroa.bhie.R;

public class InitialMenuAdultActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_menu_adult);
        getActionBar().hide();
    }



}
