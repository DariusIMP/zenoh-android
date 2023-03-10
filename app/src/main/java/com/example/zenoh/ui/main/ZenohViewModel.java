package com.example.zenoh.ui.main;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

public class ZenohViewModel extends ViewModel {

    private MutableLiveData<Integer> mIndex = new MutableLiveData<>();

    private MutableLiveData<String> zenohdLogs = new MutableLiveData<>();



    private LiveData<String> mText = Transformations.map(mIndex, new Function<Integer, String>() {
        @Override
        public String apply(Integer input) {
            return "Hello world from section: " + input;
        }
    });

    public void setIndex(int index) {
        mIndex.setValue(index);
    }

    public LiveData<String> getText() {
        return mText;
    }

    public MutableLiveData<String> getZenohdLogs() {
        return zenohdLogs;
    }

    public void setZenohdLogs(MutableLiveData<String> zenohdLogs) {
        this.zenohdLogs = zenohdLogs;
    }
}