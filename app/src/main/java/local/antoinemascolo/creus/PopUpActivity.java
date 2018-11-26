package local.antoinemascolo.creus;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;

public class PopUpActivity extends Activity {
    private Button recherche;
    private Button add;
    private TextView titre;
    private TextView description;
    private boolean connection;
    private MediaPlayer sound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop_up);

        //Parametre pour que l'activity ne prenne pas tout l'écran
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width * 0.85), (int) (height * 0.40));

        Intent intent = getIntent();
        final String nom = intent.getStringExtra("nom");
        final String desc = intent.getStringExtra("description");


        add = (Button) findViewById(R.id.ButtonAddToCart);
        recherche = (Button) findViewById(R.id.ButtonSearch);
        titre = (TextView) findViewById(R.id.titrePopUp);
        description = (TextView) findViewById(R.id.textViewDeesciption);

        titre.setText(nom);
        description.setText(desc);

        add.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                MainActivity.myCart.addItem(nom,1);
                finish();
            }
        });

        sound = MediaPlayer.create(this,R.raw.robot);

        recherche.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                if(MainActivity.currInputStream == null){
                    MainActivity.mBluetoothConnection.write(Integer.toString(MainActivity.allItems.get(nom).getItemPlace()).getBytes(Charset.forName("UTF-8")));
                    Toast.makeText(getApplicationContext(),"Veuillez suivre le CREUS!",Toast.LENGTH_LONG).show();
                    sound.start();
                }else{
                    Toast.makeText(getApplicationContext(),"Veuillez vous connecter au CREUS!",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
    }
}
