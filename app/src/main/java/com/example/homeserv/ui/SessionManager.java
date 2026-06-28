package com.example.homeserv.ui;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.homeserv.data.User;

public class SessionManager {
    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences("homeserv_session", Context.MODE_PRIVATE);
    }

    public void saveUser(User user) {
        prefs.edit().putInt("user_id", user.id).apply();
    }

    public int getUserId() {
        return prefs.getInt("user_id", -1);
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
