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
import android.view.View;

import com.example.zenoh.ui.main.SectionsPagerAdapter;
import com.example.zenoh.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ZenohdService zenohdService;
    boolean mBound = false;

    private ZenohViewModel viewModel;

    private Intent serviceIntent;

    /** Defines callbacks for service binding, passed to bindService() */
    private final ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ZenohdService.ZenohdBinder binder = (ZenohdService.ZenohdBinder) service;
            zenohdService = binder.getService();
            mBound = true;
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

        com.example.zenoh.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = binding.viewPager;
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = binding.tabs;
        tabs.setupWithViewPager(viewPager);
        FloatingActionButton fab = binding.fab;

        viewModel = new ViewModelProvider(this).get(ZenohViewModel.class);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!zenohdService.isRunning()) {
                    startZenohdService();
                    viewModel.setZenohdLogs(zenohdService.getZenohdLogs());
                } else {
                    zenohdService.stopForeground(true);
                }
            }
        });
    }

    private void stopZenohdService() {
        zenohdService.stopService();
    }

    private void startZenohdService() {
        Context context = getApplicationContext();
        context.startForegroundService(serviceIntent);
    }
}