package com.fp.funny.video.call.in_app_purchase

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fp.funny.video.call.MainActivity
import com.fp.funny.video.call.InAppPurchases
import com.fp.funny.video.call.R

class PremiumActivity : AppCompatActivity() {

    private lateinit var subscribeButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_premium)

        subscribeButton = findViewById(R.id.subscribe_now_rectangle_premium)


        // Set up click listener for the subscribe button
        subscribeButton.setOnClickListener {
            try {
                if (InAppPurchases.isBpClientReady && InAppPurchases.month1detail != null) {
                    InAppPurchases.month1detail?.let {
                        InAppPurchases.launch_Subscription_billing_flow(
                            this,
                            it
                        )
                    }
                    Log.d("InApp_activity_check", "life time click")
                } else {
                    Toast.makeText(
                        this,
                        "Try again in a moment.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (er: Exception) {
            }
        }
    }


    @Deprecated("This method has been deprecated in favor of using the OnBackPressedDispatcher.")
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}



