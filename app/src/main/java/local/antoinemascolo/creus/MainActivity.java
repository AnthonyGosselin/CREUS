package local.antoinemascolo.creus;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    public static HashMap<String,Object> allItems = new HashMap<>();
    public static Cart myCart;
    public static ArrayList<Account> accounts = new ArrayList<>();
    public static BluetoothConnectionService mBluetoothConnection;
    private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");//("8ce255c0-200a-11e0-ac64-0800200c9a66"); // Pour le bluetooth
    BluetoothDevice mBTDevice;
    BluetoothAdapter mBluetoothAdapter;
    public static InputStream currInputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button shopButton = (Button) findViewById(R.id.buttonShop);
        Button connectButton = (Button) findViewById(R.id.buttonFindCREUS);

        //On charge l'inventaire
        loadDataFromFileInventory(allItems);
        myCart = new Cart(allItems);
        loadDataFromFileAccount(accounts);

        //Bluetooth
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothConnection = new BluetoothConnectionService(MainActivity.this);

        //START PLAYER ACTIVITY
        shopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent shopIntent = new Intent(MainActivity.this, ShopActivity.class);
                startActivity(shopIntent);
            }

        });

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "Connect CREUS clicked");

                enableDisableBT();

                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

                if (pairedDevices.size() > 0) {
                    // There are paired devices. Get the name and address of each paired device.
                    for (BluetoothDevice device : pairedDevices) {
                        String deviceName = device.getName();
                        String deviceHardwareAddress = device.getAddress(); // MAC address
                        Log.e(TAG, ("Device detected: " + deviceName + " ; " + deviceHardwareAddress));

                        if (deviceHardwareAddress.matches("(.*)00:1B:10:20:0B:9A(.*)")){
                            mBTDevice = device;
                            Log.e(TAG, "Found ROBUS bluetooth device!");
                            break;
                        }
                        else{
                            Log.e(TAG, "No match");
                        }
                    }
                }

                startConnection();
            }
        });
    }

    public static void createDefaultInventory(HashMap<String,Object> defaultMap){
        //Lorsqu'il n'y a pas de fichier 'Inventaire', cette fonction est executee pour creer l'inventaire par defaut
        Log.e("CreateDefaultInventory", "Creating default inventory...");

        defaultMap.put("Clou", new Object("Clou", 1.0, 5,R.drawable.clou,"Clou en fer forgé à la main.", 1));
        defaultMap.put("Planche", new Object("Planche", 22,10, R.drawable.planche, "2x4 en érable du Mexique.", 2));
        defaultMap.put("Marteau", new Object("Marteau", 30.0, 7,R.drawable.marteau,"Marteau avec prise ergonomique et tête solide.", 3));
        defaultMap.put("Tournevis", new Object("Tournevis", 50.75,33, R.drawable.tournevis, "Tournevis étoile tout usage.", 4));
        defaultMap.put("Washer", new Object("Washer", 0.25, 99, R.drawable.washer, "Washer en acier", 5));
        defaultMap.put("Avion", new Object("Avion", 25.67, 2, R.drawable.avion, "Boeing 737 deluxe pouvant contenir 20 passagers.", 6));
        defaultMap.put("Arbre", new Object("Arbre", 1234.56, 2134, R.drawable.arbre,"Sequoia géant.", 7));
        defaultMap.put("Trebuchet", new Object("Trebuchet", 300, 90, R.drawable.trebuchet,"Cette arme de siège suppérieur peut lancer des projectiles de 90kg à 300m.", 8));
    }

    public static void writeDataToFileInventory(HashMap<String, Object> itemList) {
        try {
            File fetchedFile = new File(Environment.getExternalStorageDirectory(), "Inventaire.txt");
            FileWriter fileWriter = new FileWriter(fetchedFile);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            Log.e("writeDataToFile", "Saving to invenrtory file...");

            for (String item : itemList.keySet()){
                Object obj = itemList.get(item);
                printWriter.print(obj.getItemName() + "#");
                printWriter.print(obj.getItemPrice() + "#");
                printWriter.print(obj.getQtyLeft() + "#");
                printWriter.print(obj.getItemImage() + "#");
                printWriter.print(obj.getDescription() + "#");
                printWriter.print(obj.getItemPlace() + "\n");
            }

            printWriter.close();
        }
        catch (IOException e) {
            System.out.println("Writing to file failed! ( " + e.toString()+" )");
            Log.e("writeDataToFile", "Writing to file failed! ( " + e.toString() + " )");
        }
    }

    public static void loadDataFromFileInventory(HashMap<String, Object> itemList) {
        try {
            //Could check and create base file when it doesn't already exists
            File fetchedFile = new File(Environment.getExternalStorageDirectory(), "Inventaire.txt");

            if (!fetchedFile.exists()) {
                Log.e("loadDataFromFile", "File doesn't exist");
               boolean success = fetchedFile.createNewFile(); //On cree un nouveau fichier
               Log.e("loadDataFromFile", "New file created: " + success);
               HashMap<String,Object> defaultItems = new HashMap<>();
               createDefaultInventory(defaultItems);
               writeDataToFileInventory(defaultItems);
               allItems = defaultItems;
            }
            else{
                Log.e("loadDataFromFile", "File exists");
             /*boolean deleted = fetchedFile.delete();
                Log.e("loadDataFromFile", "File deleted: " + deleted); //*/
            }

            Log.e("loadDataFromFile", "Creating buffer...");
            BufferedReader inFile = new BufferedReader(new FileReader(fetchedFile));
            String line = "";

          /*  Log.e("loadDataFromFile", "Avion: " + Integer.toString(R.drawable.avion));
            Log.e("loadDataFromFile", "Arbre: " + Integer.toString( R.drawable.arbre));
            Log.e("loadDataFromFile", "Clou: " + Integer.toString(R.drawable.clou));*/
            while ((line = inFile.readLine()) != null) {
                String[] parts = line.split("#");

                //Parse for info
                String name = parts[0];
                Double price = Double.parseDouble(parts[1]);
                int qty = Integer.parseInt(parts[2]);
                int img = Integer.parseInt(parts[3]);
                String description = parts[4];
                int place = Integer.parseInt(parts[5]);

                Log.e("loadDataFromFile", "Creating new obj: { name = " + name + " ; price = " + price + " ; qty = " + qty + " ; img = " + img + " ; desc: " + description + " ; place = " + place + "}");

                //Create object
                Object newItem = new Object(name, price, qty, img, description, place);
                itemList.put(name, newItem);
            }

            inFile.close();
        } catch (IOException e) {
            System.out.println("Reading file failed! ( " + e.toString() + " )");
            Log.e("loadDataFromFile", "Reading file failed! ( " + e.toString() + " )");
        }
    }

    public static void createDefaultAccounts(ArrayList<Account> defaultAccount){
        //Lorsqu'il n'y a pas de fichier 'Account', cette fonction est executee pour creer l'inventaire par defaut
        Log.e("CreateDefaultAccount", "Creating default account...");

        defaultAccount.add(new Account(1,1267.28));
        defaultAccount.add(new Account(2,5467));
        defaultAccount.add(new Account(3, 12234245.12));

    }

    public static void writeDataToFileAccount(ArrayList<Account> accountList) {
        try {
            File fetchedFile = new File(Environment.getExternalStorageDirectory(), "Account.txt");
            FileWriter fileWriter = new FileWriter(fetchedFile);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            Log.e("writeDataToFile", "Saving to account file...");

            for(int i = 0; i < accountList.size();i++){
                Account account = accountList.get(i);
                printWriter.print(account.getId() + "\t");
                printWriter.print(account.getBalance() + "\n");
                Log.d(TAG, "Account " + account.getId() + " new balance: " + account.getBalance());
            }

            printWriter.close();
        }
        catch (IOException e) {
            Log.e("writeDataToFile", "Writing to file failed! ( " + e.toString() + " )");
        }
    }

    public static void loadDataFromFileAccount(ArrayList<Account> accountList1) {
        try {
            //Could check and create base file when it doesn't already exists
            File fetchedFile = new File(Environment.getExternalStorageDirectory(), "Account.txt");

            if (!fetchedFile.exists()) {
                Log.e("loadDataFromFile", "File doesn't exist");
                boolean success = fetchedFile.createNewFile(); //On cree un nouveau fichier
                Log.e("loadDataFromFile", "New file created: " + success);
                ArrayList<Account> defaultAccounts = new ArrayList<>();
                createDefaultAccounts(defaultAccounts);
                writeDataToFileAccount(defaultAccounts);
                accounts = defaultAccounts;
            }
            else{
                Log.e("loadDataFromFile", "File exists");
                /*boolean deleted = fetchedFile.delete();
                Log.e("loadDataFromFile", "File deleted: " + deleted); //*/
            }

            Log.e("loadDataFromFile", "Creating buffer...");
            BufferedReader inFile = new BufferedReader(new FileReader(fetchedFile));
            String line = "";


            while ((line = inFile.readLine()) != null) {
                String[] parts = line.split("\t");

                //Parse for info
                int id = Integer.parseInt(parts[0]);
                Double balance = Double.parseDouble(parts[1]);

                //Create object
                Account newAccount = new Account(id, balance);
                accountList1.add(newAccount);
            }

            inFile.close();
        } catch (IOException e) {
            System.out.println("Reading file failed! ( " + e.toString() + " )");
            Log.e("loadDataFromFile", "Reading file failed! ( " + e.toString() + " )");
        }
    }

    public void enableDisableBT(){
        Log.e(TAG, "Enable/disable function");
        if(mBluetoothAdapter == null){
            Log.d(TAG, "enableDisableBT: Does not have BT capabilities.");
        }
        if(!mBluetoothAdapter.isEnabled()){
            Log.d(TAG, "enableDisableBT: enabling BT.");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

        }
        if(mBluetoothAdapter.isEnabled()){
            Log.d(TAG, "Bluetooth enabled");
        }

    }
    public void startConnection(){
        startBTConnection(mBTDevice,MY_UUID_INSECURE);
    }

    /**
     * starting chat service method
     */
    public void startBTConnection(BluetoothDevice device, UUID uuid){
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection.");

        mBluetoothConnection.startClient(device,uuid);
    }
}
