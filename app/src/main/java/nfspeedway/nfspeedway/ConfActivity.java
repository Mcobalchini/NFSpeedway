package br.edu.utfpr.ondeestou;
import android.content.SharedPreferences;
import android.preference.PreferenceActivity;
import android.os.Bundle;

public class ConfActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.activity_conf);
    }
}
