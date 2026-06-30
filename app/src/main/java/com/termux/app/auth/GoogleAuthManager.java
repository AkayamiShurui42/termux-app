package com.termux.app.auth;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

public class GoogleAuthManager {
    private GoogleSignInClient mGoogleSignInClient;
    private Context context;

    public GoogleAuthManager(Context context) {
        this.context = context;
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken("YOUR_SERVER_CLIENT_ID") // Replace with actual client ID
                .requestServerAuthCode("YOUR_SERVER_CLIENT_ID")
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    public GoogleSignInClient getSignInClient() {
        return mGoogleSignInClient;
    }

    public void saveToken(String token) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            SharedPreferences secureStorage = EncryptedSharedPreferences.create(
                    context,
                    "secure_auth_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            secureStorage.edit().putString("auth_token", token).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
