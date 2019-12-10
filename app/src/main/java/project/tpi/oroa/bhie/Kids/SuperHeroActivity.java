package project.tpi.oroa.bhie.Kids;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import project.tpi.oroa.bhie.R;

public class SuperHeroActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_hero);
        getActionBar().hide();
        String nombre = getIntent().getStringExtra("nombre");
        TextView text2 = findViewById(R.id.textViewSuperHeroe);
        String old = text2.getText().toString();
        String newOne = old + " " + nombre;
        text2.setText(newOne);
    }

    public void onClickChooseGame(View v)
    {
        try {
            Intent k = new Intent(SuperHeroActivity.this, ChooseGameActivity.class);
            startActivity(k);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
