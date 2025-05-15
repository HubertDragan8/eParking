package com.example.eparking

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eparking.adapter.ParkingSpotAdapter
import com.example.eparking.model.ParkingSpot
import com.example.eparking.model.ParkingStatus

class MainActivity : AppCompatActivity() {
    private var backPressedTime: Long = 0
    private lateinit var parkingAdapter: ParkingSpotAdapter
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        authManager = AuthManager(this)

        // Initialize RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.parkingGrid)
        
        // Set up GridLayoutManager with 3 columns and equal spacing
        val layoutManager = GridLayoutManager(this, 3)
        recyclerView.layoutManager = layoutManager
        
        // Add item decoration for equal spacing
        val spacing = resources.getDimensionPixelSize(R.dimen.grid_spacing)
        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: android.graphics.Rect,
                view: android.view.View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                outRect.set(spacing, spacing, spacing, spacing)
            }
        })

        // Create sample parking spots
        val parkingSpots = createSampleParkingSpots()
        
        // Initialize and set adapter
        parkingAdapter = ParkingSpotAdapter(parkingSpots)
        recyclerView.adapter = parkingAdapter

        // Set up logout button
        findViewById<Button>(R.id.logoutButton).setOnClickListener {
            // Clear login state
            authManager.logout()
            
            // Return to login screen
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun createSampleParkingSpots(): List<ParkingSpot> {
        val spots = mutableListOf<ParkingSpot>()
        
        // Create a 3x4 grid of parking spots
        for (row in 'A'..'C') {
            for (col in 1..4) {
                val label = "$row$col"
                spots.add(
                    ParkingSpot(
                        id = "spot_$label",
                        label = label,
                        status = ParkingStatus.UNKNOWN
                    )
                )
            }
        }
        
        return spots
    }

    override fun onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed()
        } else {
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show()
        }
        backPressedTime = System.currentTimeMillis()
    }
}
