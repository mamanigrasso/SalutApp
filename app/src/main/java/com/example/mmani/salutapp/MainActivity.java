package com.example.mmani.salutapp;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.bluelinelabs.logansquare.LoganSquare;
import com.peak.salut.Callbacks.SalutCallback;
import com.peak.salut.Callbacks.SalutDataCallback;
import com.peak.salut.Callbacks.SalutDeviceCallback;
import com.peak.salut.Salut;
import com.peak.salut.SalutDataReceiver;
import com.peak.salut.SalutDevice;
import com.peak.salut.SalutServiceData;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SalutDataCallback {

    private Salut network;
    private boolean isHost = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SalutDataReceiver dataReceiver = new SalutDataReceiver(this, this);
        SalutServiceData serviceData = new SalutServiceData("sas", 50489, "Tablename");

        network = new Salut(dataReceiver, serviceData, new SalutCallback() {
            @Override
            public void call() {
                Toast.makeText(MainActivity.this, "Sorry, this device is not supported", Toast.LENGTH_SHORT).show();
            }
        });

        Button btnHost = findViewById(R.id.btnHost);
        Button btnDiscover = findViewById(R.id.btnDiscover);
        Button btnReset = findViewById(R.id.btnReset);
        Button btnSendHello = findViewById(R.id.btnSendHello);

        btnHost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isHost = true;
                startService(network);
            }
        });

        btnDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isHost = false;
                discoverNetworks(network);
            }
        });


        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isHost) {
                    network.stopNetworkService(false);
                } else {
                    network.stopServiceDiscovery(true);
                }
            }
        });

        btnSendHello.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message message = new Message();
                message.description = "Hello!";

                network.sendToHost(message, new SalutCallback() {
                    @Override
                    public void call() {
                        Toast.makeText(MainActivity.this, "Oh shoot!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void startService(Salut network) {
        network.startNetworkService(new SalutDeviceCallback() {
            @Override
            public void call(SalutDevice salutDevice) {
                Toast.makeText(MainActivity.this, salutDevice.deviceName + " has connected.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void discoverNetworks(final Salut network) {
        network.discoverNetworkServices(new SalutDeviceCallback() {
            @Override
            public void call(SalutDevice device) {
                Toast.makeText(MainActivity.this, "A device has been found with the name " + device.deviceName,
                        Toast.LENGTH_SHORT).show();

                AlertDialog.Builder builder = buildDialog(network.foundDevices);
                builder.show();
            }
        }, false);
    }

    private AlertDialog.Builder buildDialog(ArrayList<SalutDevice> devices) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(MainActivity.this);
        builderSingle.setTitle("Select table to join");

        final ArrayAdapter<SalutDevice> arrayAdapter = new ArrayAdapter<SalutDevice>(MainActivity.this, android.R.layout
                .select_dialog_singlechoice);
        arrayAdapter.addAll(devices);

        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final SalutDevice device = arrayAdapter.getItem(which);
                AlertDialog.Builder builderInner = new AlertDialog.Builder(MainActivity.this);
                builderInner.setMessage(device.instanceName);
                builderInner.setTitle("You will join");
                builderInner.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int which) {
                        dialog.dismiss();
                        connectToHost(device);
                    }
                });
                builderInner.show();
            }
        });

        return builderSingle;
    }

    private void connectToHost(SalutDevice host) {
        network.registerWithHost(host, new SalutCallback() {
            @Override
            public void call() {
                Toast.makeText(MainActivity.this, "You registered successfully!",
                        Toast.LENGTH_LONG).show();
            }
        }, new SalutCallback() {
            @Override
            public void call() {
                Toast.makeText(MainActivity.this, "You couldn't be registered...",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDataReceived(Object o) {
        try {
            Message message = LoganSquare.parse((String)o, Message.class);
            Toast.makeText(MainActivity.this, message.description, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("Exception", e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(isHost)
            network.stopNetworkService(false);
        else
            network.unregisterClient(false);
    }
}
