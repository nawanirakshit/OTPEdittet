package com.rakshit.otpedittet

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.rakshit.otpedittet.optview.OTPView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val otpView: OTPView = findViewById(R.id.otp_view)

        otpView.setOnFinishListener {
            //when all input fields are completely filled
            Log.i("MainActivity", it)
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        }

        otpView.setOnCharacterUpdatedListener {
            if (it) {
                //Enable Submit button
                Log.i("MainActivity", "The view is filled")
            } else {
                //Disable Submit button
                Log.i("MainActivity", "The view is NOT Filled")
            }
        }
    }
}