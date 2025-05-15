package com.example.eparking.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.eparking.R
import com.example.eparking.model.ParkingSpot
import com.example.eparking.model.ParkingStatus

class ParkingSpotAdapter(
    private var spots: List<ParkingSpot>
) : RecyclerView.Adapter<ParkingSpotAdapter.ParkingSpotViewHolder>() {

    class ParkingSpotViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view as CardView
        val labelText: TextView = view.findViewById(R.id.spotLabel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParkingSpotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.parking_spot_item, parent, false)
        return ParkingSpotViewHolder(view)
    }

    override fun onBindViewHolder(holder: ParkingSpotViewHolder, position: Int) {
        val spot = spots[position]
        holder.labelText.text = spot.label
        
        // Set background color based on status
        val colorRes = when (spot.status) {
            ParkingStatus.AVAILABLE -> R.color.available_green
            ParkingStatus.OCCUPIED -> R.color.occupied_red
            ParkingStatus.UNKNOWN -> R.color.unknown_gray
        }
        holder.cardView.setCardBackgroundColor(
            ContextCompat.getColor(holder.itemView.context, colorRes)
        )

        // Set click listener to cycle through statuses
        holder.itemView.setOnClickListener {
            val currentStatus = spot.status
            spot.status = when (currentStatus) {
                ParkingStatus.AVAILABLE -> ParkingStatus.OCCUPIED
                ParkingStatus.OCCUPIED -> ParkingStatus.UNKNOWN
                ParkingStatus.UNKNOWN -> ParkingStatus.AVAILABLE
            }
            notifyItemChanged(position)
        }
    }

    override fun getItemCount() = spots.size

    fun updateSpots(newSpots: List<ParkingSpot>) {
        spots = newSpots
        notifyDataSetChanged()
    }
} 