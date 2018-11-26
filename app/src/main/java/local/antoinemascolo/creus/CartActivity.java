package local.antoinemascolo.creus;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.media.MediaPlayer;
import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

public class CartActivity extends AppCompatActivity {
    private final String TAG = "Cart";

    private Button pay;
    public static TextView grandTotal;

   // final MediaPlayer goodsound = MediaPlayer.create(this, R.raw.goodbeep);
    //final MediaPlayer error = MediaPlayer.create(this, R.raw.error);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        MainActivity.myCart.UpdateNullItems(); //Pour les items a 0

        final RecyclerView rv = (RecyclerView) findViewById(R.id.cartList);
        rv.setLayoutManager(new LinearLayoutManager(this));

        ArrayList<Object> objList = new ArrayList<>(MainActivity.myCart.getCartItems().values());
        CartAdapter cartAdapter = new CartAdapter(objList);
        rv.setAdapter(cartAdapter);
        //cartAdapter.notifyDataSetChanged();

        pay = (Button) findViewById(R.id.buttonPayer);
        grandTotal = (TextView) findViewById(R.id.grandTotal);
        grandTotal.setText(String.format("%.2f", MainActivity.myCart.getBalance())+ "$");

        pay.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                Log.e(TAG, "On Clicked");
                if (MainActivity.currInputStream == null){
                    Log.e(TAG, "No input stream for payment!");
                    Toast.makeText(getApplicationContext(),"Veuillez vous connecter au CREUS",Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(getApplicationContext(),"Veuillez passez votre carte pour payer",Toast.LENGTH_LONG).show();

                    //Scan pour carte
                    Boolean gotCard = false;
                    int bytes; // bytes returned from read()
                    byte[] buffer = new byte[1024];  // buffer store for the stream
                    String incomingMessage;

                    int loop = 0;
                    while (!gotCard) {
                        // Read from the InputStream
                        loop++;
                        Log.d("Loop", Integer.toString(loop));
                        if (loop >= 10000){
                            Log.e(TAG, "Payment timeout");
                            break;
                        }
                        //try {
                            Log.d(TAG, "Is connected to inputStream");

                           // bytes = MainActivity.currInputStream.read(buffer);
                            incomingMessage = "C1";//new String(buffer, 0, bytes);
                            Log.d(TAG, "Card msg received: " + incomingMessage);

                            if (incomingMessage.matches("(.*)C(.*)")) {
                                //Commande de compte qui paye
                                gotCard = true;
                                Integer.parseInt(incomingMessage.replaceAll("[\\D]", ""));
                                Log.d(TAG, incomingMessage);
                                int numCarte = Integer.parseInt(incomingMessage);

                                Log.d(TAG, "Payment avec compte: " + numCarte);
                                Account account = MainActivity.accounts.get(numCarte);
                                Boolean payed = account.pay(MainActivity.myCart.getBalance());
                                if (payed) {
                                    MainActivity.myCart.checkOut();
                                    MainActivity.writeDataToFileInventory(MainActivity.allItems);
                                    MainActivity.writeDataToFileAccount(MainActivity.accounts);
                                    Toast.makeText(getApplicationContext(), "Succès de l'achat. Merci :)", Toast.LENGTH_LONG).show();
                                    //  goodsound.start();
                                    startActivity(new Intent(CartActivity.this, ShopActivity.class));
                                } else {
                                    Toast.makeText(getApplicationContext(), "Balance insuffisante :(", Toast.LENGTH_LONG).show();
                                    //error.start();
                                }
                            }

                        /*} catch (IOException e) {
                            Log.e(TAG, "write: Error receiving card. " + e.getMessage());
                            break;
                        }*/
                    }
                }
            }
        });
    }
}
