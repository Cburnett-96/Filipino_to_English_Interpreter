package com.orbotz.filipino_english_interpreter;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {
    SharedPreferences mySharedPref ;
    public SharedPref(Context context) {
        mySharedPref = context.getSharedPreferences("filename", Context.MODE_PRIVATE);
    }
    // this method will save the nightMode State : True or False
    public void setNightModeState(Boolean state) {
        SharedPreferences.Editor editor = mySharedPref.edit();
        editor.putBoolean("NightMode", state);
        editor.apply();
    }
    public void setFirstLaunch(Boolean state) {
        SharedPreferences.Editor editor = mySharedPref.edit();
        editor.putBoolean("FirstTime", state);
        editor.apply();
    }
    public void setDataLaunch(Boolean state) {
        SharedPreferences.Editor editor = mySharedPref.edit();
        editor.putBoolean("Data", state);
        editor.apply();
    }
    // this method will load the Night Mode State
    public Boolean loadNightModeState (){
        Boolean state = mySharedPref.getBoolean("NightMode",false);
        return  state;
    }
    public Boolean loadFirstState (){
        Boolean state = mySharedPref.getBoolean("FirstTime",false);
        return  state;
    }
    public Boolean loadDataState (){
        Boolean state = mySharedPref.getBoolean("Data",false);
        return  state;
    }
}
