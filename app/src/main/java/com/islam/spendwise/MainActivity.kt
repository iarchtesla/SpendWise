package com.islam.spendwise

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.islam.spendwise.ui.navigation.SpendWiseNavHost
import com.islam.spendwise.ui.theme.SpendWiseTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SpendWiseTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    SpendWiseNavHost()
                }
            }
        }
    }
}
