package com.schober.vinylcast

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class PreferencesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preferences)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.preferences_container, PreferencesFragment())
                .commit()
    }
}