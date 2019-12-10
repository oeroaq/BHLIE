package project.tpi.oroa.bhie.Kids;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.EditText;

import project.tpi.oroa.bhie.InitialMenuActivity;
import project.tpi.oroa.bhie.R;

public class InitialMenuKidsActivity extends Activity {
public EditText edt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_menu_kids);
        getActionBar().hide();
        edt = this.findViewById(R.id.editTextKids);
    }


    public void onClickSuperHero(View v)
    {

        String str = edt.getText().toString();

        try {
            Intent k = new Intent(InitialMenuKidsActivity.this, SuperHeroActivity.class);
            k.putExtra("nombre", str);
            startActivity(k);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }



    public void onClickChooseGame(View v)
    {
        try {
            Intent k = new Intent(InitialMenuKidsActivity.this, ChooseGameActivity.class);
            startActivity(k);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }


}
