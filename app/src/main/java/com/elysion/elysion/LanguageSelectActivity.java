package com.elysion.elysion;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.elysion.elysion.SimpleClasses.ApiRequest;
import com.elysion.elysion.SimpleClasses.Callback;
import com.elysion.elysion.SimpleClasses.Functions;
import com.elysion.elysion.SimpleClasses.Variables;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LanguageSelectActivity extends Activity implements LanguageAdapter.OnLanguageSelectionListener {

    RecyclerView languageList;
    LinearLayoutManager linearLayoutManager;
    LanguageAdapter languageAdapter;
    List<String> languageNameList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_langauge_select);

        Variables.sharedPreferences = getSharedPreferences(Variables.pref_name, MODE_PRIVATE);
        String selectedLanguage = Variables.sharedPreferences.getString(Variables.selectedLanguage, "");

        languageList = (RecyclerView) findViewById(R.id.languageList);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        languageList.setHasFixedSize(true);

        // use a linear layout manager
        linearLayoutManager = new LinearLayoutManager(this);
        languageList.setLayoutManager(linearLayoutManager);

        languageNameList.add("English");
        languageNameList.add("Hindi");
        languageNameList.add("Tamil");
        languageNameList.add("Telugu");
        languageNameList.add("Marathi");
        languageNameList.add("Gujarati");
        languageNameList.add("Kannada");
        languageNameList.add("Bengali");

        // specify an adapter (see also next example)
        languageAdapter = new LanguageAdapter(languageNameList, this);
        languageList.setAdapter(languageAdapter);
        if (!selectedLanguage.isEmpty()) {
            languageAdapter.setSelection(selectedLanguage);
        }
    }

    @Override
    public void onLanguageSelected(String language, int position) {
        SharedPreferences.Editor editor = Variables.sharedPreferences.edit();
        editor.putString(Variables.selectedLanguage, language);
        editor.apply();
        editor.commit();

        String fbId = Variables.sharedPreferences.getString(Variables.u_id, "");
        updateLanguage(fbId, language);
    }


    private void updateLanguage(String id,
                                String languageName) {


        JSONObject parameters = new JSONObject();
        try {

            parameters.put("fb_id", id);
            parameters.put("language", "" + languageName);


        } catch (JSONException e) {
            e.printStackTrace();
        }

        Functions.Show_loader(this, false, false);
        ApiRequest.Call_Api(this, Variables.updateLanguage, parameters, new Callback() {
            @Override
            public void Responce(String resp) {
                Functions.cancel_loader();
                finish();

            }
        });

    }
}
