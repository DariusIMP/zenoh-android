package com.example.zenoh.ui.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.zenoh.R;

/**
 * Fragment to display the output of the logs of the router when going on.
 */
public class RouterFragment extends Fragment {

    private ZenohViewModel viewModel;

    private TextView logsTextView;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_router, container, false);
        logsTextView = view.findViewById(R.id.logsTextView);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ZenohViewModel.class);
        viewModel.getZenohdLogs().observe(requireActivity(), s -> {
            logsTextView.setText(s);
            Log.d("XXX", "Updating text");
        });
    }
}