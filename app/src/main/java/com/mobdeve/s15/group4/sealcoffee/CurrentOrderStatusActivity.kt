package com.mobdeve.s15.group4.sealcoffee

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class CurrentOrderStatusActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_current_order_status)

        CustomerNavigation.bind(this, CustomerDestination.STATUS)
    }
}
