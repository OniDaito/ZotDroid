package computer.benjamin.zotdr0id.auth;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import computer.benjamin.zotdr0id.R;

/**
 * A screen that fires up the OAuth we need for Zotero
 */
public class LoginActivity extends AppCompatActivity  {

    private static final String TAG = "zotdr0id.LoginActivity";
    private ZoteroAuth zoteroAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        zoteroAuth = new ZoteroAuth(this);
        setContentView(R.layout.zotero_login);
        final Button mSignInButton = (Button) findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                zoteroAuth.start();
                mSignInButton.setEnabled(false);
            }
        });
    }

    public void onAuthFinish(boolean success) {
        if (success) {
            Intent resultIntent = new Intent();
            setResult(Activity.RESULT_OK, resultIntent);
            ZoteroDeets.load(this);
            finish();
        } else {
            final Button mSignInButton = (Button) findViewById(R.id.sign_in_button);
            setResult(Activity.RESULT_CANCELED);
            finish();
            mSignInButton.setEnabled(true);
        }
    }

}

