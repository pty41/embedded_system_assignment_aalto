package com.example.jjkrs.sharephoto;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

public class UserInfoActivity extends AppCompatActivity {

    private TextView name;
    private TextView email;
    private View mRootView;

    public static Intent createIntent(Context context) {
        return new Intent(context, UserInfoActivity.class);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // Check if user is not signed in
        if (user == null) {
            // Not signed in, go to sign up form
            startActivity(SignInActivity.createIntent(this));
            finish();
            return;
        }

        setContentView(R.layout.user_info);

        // User info
        name = (TextView) findViewById(R.id.txtName);
        email = (TextView) findViewById(R.id.txtEmail);
        name.setText(user.getDisplayName());
        email.setText(user.getEmail());


        /*
         * This is for the id token for the authentication
         * and should be sent to the server. Location of
         * of this code is most likely needed elsewhere.
         */
        user.getIdToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            String idToken = task.getResult().getToken();

                            // Show the token TODO: remove later!
                            TextView tok = (TextView) findViewById(R.id.txtIdToken);
                            //tok.setText("ID Token: " + idToken.substring(0,25) + "...");
                            //tok.setText("ID Token: " + idToken);
                            // End

                            // TODO: Send token to your backend via HTTPS

                        } else {
                            throw new RuntimeException(task.getException().toString() + R.string.token_fail);
                        }
                    }
                });

    }

    // Options bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    // Options bar continue
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                logOut();
                return true;
            case R.id.delete_account_menu:
                deleteAccount();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void logOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            startActivity(SignInActivity.createIntent(UserInfoActivity.this));
                            finish();
                        } else {
                            showSnackbar(R.string.sign_out_failed);
                        }
                    }
                });
    }

    private void deleteAccount() {
        AuthUI.getInstance()
                .delete(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            startActivity(SignInActivity.createIntent(UserInfoActivity.this));
                            finish();
                        } else {
                            showSnackbar(R.string.delete_account_failed);
                        }
                    }
                });
    }

    // This is just for error messages
    @MainThread
    private void showSnackbar(@StringRes int errorMessageRes) {
        mRootView = (View) findViewById(android.R.id.content);
        Snackbar.make(mRootView, errorMessageRes, Snackbar.LENGTH_LONG).show();
    }
}
