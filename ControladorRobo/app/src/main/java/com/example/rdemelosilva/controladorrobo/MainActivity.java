package com.example.rdemelosilva.controladorrobo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Handler bluetoothIn;
    TextView vlTemperatura, vlLdr, vlUmidade, vlVelocidade;
    Button btnLeft, btnRight, btnForward, btnBackward, btnStopRobot;
    SeekBar skVelocidade;

    final int handlerState = 0;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();
    private boolean fgSocketConnected = false;
    private String comando;

    private ConnectedThread mConnectedThread;

    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address
    private static String address;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //Link the buttons and textViews to respective views
        btnLeft = (Button) findViewById(R.id.btnLeft);
        btnRight = (Button) findViewById(R.id.btnRight);
        btnForward = (Button) findViewById(R.id.btnForward);
        btnBackward = (Button) findViewById(R.id.btnBackward);
        btnStopRobot = (Button) findViewById(R.id.btnStopRobot);

        skVelocidade = (SeekBar) findViewById(R.id.skVelocidade);

        vlLdr = (TextView) findViewById(R.id.vlLdr);
        vlUmidade = (TextView) findViewById(R.id.vlUmidade);
        vlTemperatura = (TextView) findViewById(R.id.vlTemperatura);
        vlVelocidade = (TextView) findViewById(R.id.vlVelocidade);

        comando = "F";

        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {
                    String readMessage = (String) msg.obj;
                    receberValorSensores(readMessage);
                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();

        // Set up onClick listeners for buttons to send 1 or 0 to turn on/off LED
        btnLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    comando = "A";
                    mConnectedThread.write();    // Send "0" via Bluetooth
                    Toast.makeText(getBaseContext(), "Virando a esquerda", Toast.LENGTH_SHORT).show();
                } else if (action == MotionEvent.ACTION_UP) {
                    comando = "E";
                    mConnectedThread.write();
                    Toast.makeText(getBaseContext(), "Parando robô", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        btnRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    comando = "B";
                    mConnectedThread.write();
                    Toast.makeText(getBaseContext(), "Virando a direita", Toast.LENGTH_SHORT).show();
                } else if (action == MotionEvent.ACTION_UP) {
                    comando = "E";
                    mConnectedThread.write();
                    Toast.makeText(getBaseContext(), "Parando robô", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        btnForward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    comando = "C";
                    mConnectedThread.write();
                    Toast.makeText(getBaseContext(), "Andando para frente", Toast.LENGTH_SHORT).show();
                } else if (action == MotionEvent.ACTION_UP) {
                    comando = "E";
                    mConnectedThread.write();
                    Toast.makeText(getBaseContext(), "Parando robô", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        btnBackward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    comando = "D";
                    mConnectedThread.write();
                    Toast.makeText(getBaseContext(), "Andando para trás", Toast.LENGTH_SHORT).show();
                } else if (action == MotionEvent.ACTION_UP) {
                    comando = "E";
                    mConnectedThread.write();
                    Toast.makeText(getBaseContext(), "Parando robô", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        btnStopRobot.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                skVelocidade.setProgress(0);
                comando = "E";
                mConnectedThread.write();
                Toast.makeText(getBaseContext(), "Parando robô", Toast.LENGTH_SHORT).show();
            }
        });

        skVelocidade.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                // TODO Auto-generated method stub

                vlVelocidade.setText(String.valueOf(progress));
                mConnectedThread.write();

            }
        });
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }

    @Override
    public void onStart() {
        super.onStart();

        //Get MAC address from DeviceListActivity via intent
        Intent intent = getIntent();

        //Get the MAC address from the DeviceListActivty via EXTRA
        address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

        //create device and set the MAC address
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket connection.
        int controle=0;

        while(controle<4){
            try
            {

                btSocket.connect();
                controle=4;
            } catch (IOException e) {
                controle++;
                if(controle==3){
                    finish();
                }
                try
                {
                    Toast.makeText(getBaseContext(), "Não foi possível conectar. Tente novamente!", Toast.LENGTH_LONG).show();
                    btSocket.close();
                } catch (IOException e2)
                {

                }
            }
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        try
        {
            btSocket.close();
        } catch (IOException e2) {

        }
    }

    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    private void checkBTState() {

        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "Aparelho não suporta bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    public void receberValorSensores(String readMessage){
        String ldr = "", umidade = "", temperatura = "";
        temperatura = readMessage.substring(readMessage.indexOf('T') + 1, readMessage.indexOf('U'));
        umidade = readMessage.substring(readMessage.indexOf('U') + 1, readMessage.indexOf('L'));
        ldr = readMessage.substring(readMessage.indexOf('L') + 1);

        vlLdr.setText(ldr);
        vlUmidade.setText(umidade);
        vlTemperatura.setText(temperatura);
    }

    //create new class for connect thread
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {

            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer); //read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
       //write method
        public void write() {
            String comandoCompleto = "";
            if(vlVelocidade.getText().toString() == "" || vlVelocidade.getText().toString() == null ){
                comandoCompleto = comando.concat(String.valueOf(0));
            }
            else{
                comandoCompleto = comando.concat(vlVelocidade.getText().toString());
            }
            byte[] msgBuffer = comandoCompleto.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "Não foi possível enviar dados para o bluetooth. Tente novamente.", Toast.LENGTH_LONG).show();
                finish();

            }
        }
    }
}