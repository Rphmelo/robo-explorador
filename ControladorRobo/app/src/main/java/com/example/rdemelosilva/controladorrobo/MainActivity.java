package com.example.rdemelosilva.controladorrobo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import android.app.Activity;
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
import android.widget.TextView;
import android.widget.Toast;

import com.example.rdemelosilva.controladorrobo.R;
import com.example.rdemelosilva.controladorrobo.DeviceListActivity;

public class MainActivity extends AppCompatActivity {

    Handler bluetoothIn;
    TextView vlTemperatura, vlLdr, vlHumidade;
    Button btnLeft, btnRight, btnForward, btnBackward, btnStopRobot;

    final int handlerState = 0;                        //used to identify handler message
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();
    private boolean fgSocketConnected = false;

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

        vlLdr = (TextView) findViewById(R.id.vlLdr);
        vlHumidade = (TextView) findViewById(R.id.vlHumidade);
        vlTemperatura = (TextView) findViewById(R.id.vlTemperatura);

        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {                                     //if message is what we want
                    String readMessage = (String) msg.obj;                                                                // msg.arg1 = bytes from connect thread
                    recDataString.append(readMessage);                                      //keep appending to string until ~
                    int endOfLineIndex = recDataString.indexOf("~");                    // determine the end-of-line
                    if (endOfLineIndex > 0) {                                           // make sure there data before ~
                        String dataInPrint = recDataString.substring(0, endOfLineIndex);
                        int dataLength = dataInPrint.length();                          //get length of data received

                        if (recDataString.charAt(0) == '#')                             //if it starts with # we know it is what we are looking for
                        {
                            String sensor0 = recDataString.substring(1, 5);             //get sensor value from string between indices 1-5
                            String sensor1 = recDataString.substring(6, 10);            //same again...
                            String sensor2 = recDataString.substring(11, 15);
                            String sensor3 = recDataString.substring(16, 20);

                        }
                        recDataString.delete(0, recDataString.length());                    //clear all string data
                        // strIncom =" ";
                        dataInPrint = " ";
                    }
                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();

        // Set up onClick listeners for buttons to send 1 or 0 to turn on/off LED
        btnLeft.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("A");    // Send "0" via Bluetooth
                Toast.makeText(getBaseContext(), "Virando a esquerda", Toast.LENGTH_SHORT).show();
            }
        });

        btnRight.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("B");    // Send "0" via Bluetooth
                Toast.makeText(getBaseContext(), "Virando a direita", Toast.LENGTH_SHORT).show();
            }
        });

        btnForward.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("C");    // Send "0" via Bluetooth
                Toast.makeText(getBaseContext(), "Andando para frente", Toast.LENGTH_SHORT).show();
            }
        });

        btnBackward.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("D");    // Send "0" via Bluetooth
                Toast.makeText(getBaseContext(), "Andando para trás", Toast.LENGTH_SHORT).show();
            }
        });

        btnStopRobot.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("E");    // Send "0" via Bluetooth
                Toast.makeText(getBaseContext(), "Parando robô", Toast.LENGTH_SHORT).show();
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

//        //Get the MAC address from the DeviceListActivty via EXTRA
        address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

        //create device and set the MAC address
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket connection.
        try
        {
            btSocket.connect();
            fgSocketConnected = true;
        } catch (IOException e) {
            try
            {
                btSocket.close();
                fgSocketConnected = false;
            } catch (IOException e2)
            {
                fgSocketConnected = false;
                //insert code to deal with this
            }
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        mConnectedThread.write("x");
    }

    @Override
    public void onStop()
    {
        super.onStop();
        try
        {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
            fgSocketConnected = false;
        } catch (IOException e2) {
            //insert code to deal with this
            fgSocketConnected = false;
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
            } catch (IOException e) { }

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
                    //bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                    vlLdr.setText(readMessage.charAt(0));
                    vlHumidade.setText(readMessage.charAt(1));
                    vlTemperatura.setText(readMessage.charAt(2));
                } catch (IOException e) {
                    break;
                }
            }
        }
        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
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