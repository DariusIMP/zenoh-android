package com.example.zenoh;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;

import com.example.zenoh.ui.main.ZenohViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.os.IBinder;
import android.util.Log;
import android.view.View;

import com.example.zenoh.ui.main.SectionsPagerAdapter;
import com.example.zenoh.databinding.ActivityMainBinding;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();

    private ZenohdService zenohdService;
    boolean mBound = false;

    private ZenohViewModel viewModel;

    private Intent serviceIntent;

    /** Defines callbacks for service binding, passed to bindService() */
    private final ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            ZenohdService.ZenohdBinder binder = (ZenohdService.ZenohdBinder) service;
            zenohdService = binder.getService();
            mBound = true;
            zenohdService.setZenohdLogs(viewModel.getZenohdLogs());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        serviceIntent = new Intent(this, ZenohdService.class);
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ZenohViewModel.class);
        com.example.zenoh.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = binding.viewPager;
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = binding.tabs;
        tabs.setupWithViewPager(viewPager);
        FloatingActionButton fab = binding.fab;

        fab.setOnClickListener(view -> {
//                new Thread(MainActivity.this::executeZenohd).start();
            if (!zenohdService.isRunning()) {
                startZenohdService();
            } else {
                zenohdService.stopForeground(true);
            }
        });
    }

    private void executeZenohd() {
        try {
            Log.d(TAG, "Starting zenohd service.");
            Process zenohdProcess = Runtime.getRuntime().exec(getApplicationInfo().nativeLibraryDir + "/zenohd");
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(zenohdProcess.getErrorStream())
            );
            String line = "";
            StringBuilder builder = new StringBuilder();
            Log.d(TAG, "AAA");
            while (zenohdProcess.isAlive() && (line = bufferedReader.readLine()) != null) {
                builder.append(line);
                String log = builder.toString();
                Log.d(TAG, log);
//                zenohdLogs.postValue(log);
            }
            Log.d(TAG, "Zenohd execution stopped.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void stopZenohdService() {
        zenohdService.stopService();
    }

    private void startZenohdService() {
        Context context = getApplicationContext();
        context.startForegroundService(serviceIntent);
    }
}