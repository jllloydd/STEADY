package com.bonak.steady

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.components.XAxis
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.TextView
import android.widget.ProgressBar
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.viewModelScope
import java.text.SimpleDateFormat
import android.graphics.PorterDuff
import androidx.core.content.ContextCompat
import android.util.Log
import android.os.Handler
import android.os.Looper
import android.app.AlertDialog
import android.widget.ArrayAdapter
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieData
import android.graphics.Color

class Home3 : Fragment() {
    private lateinit var lineChart: LineChart
    private lateinit var pieChart: PieChart
    private lateinit var riskProgressBar: ProgressBar
    private lateinit var earthquakeRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home3, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        earthquakeRecyclerView = view.findViewById(R.id.earthquake_list)
        earthquakeRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        earthquakeRecyclerView.adapter = EarthquakeAdapter(emptyList())

        setupLandslideFrequencyChart()
        setupHighRiskAreasChart()
        setupLandslideRiskIndicator()
        setupRecentEarthquakes()
    }

    private fun setupLandslideFrequencyChart() {
        lineChart = view?.findViewById(R.id.landslide_frequency_chart)!!
        
        // Sample data for line chart
        val entries = listOf(
            Entry(0f, 20f),
            Entry(1f, 15f),
            Entry(2f, 25f),
            Entry(3f, 30f),
            Entry(4f, 28f)
        )

        val dataSet = LineDataSet(entries, "Frequency")
        dataSet.apply {
            setDrawFilled(true)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            color = ContextCompat.getColor(requireContext(), R.color.brown)
            fillColor = ContextCompat.getColor(requireContext(), R.color.text_brown)
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.brown))
            valueTextColor = Color.BLACK
            valueTextSize = 12f
        }

        lineChart.apply {
            data = LineData(dataSet)
            description.isEnabled = false
            legend.isEnabled = false
            setDrawGridBackground(false)
            xAxis.textColor = Color.BLACK
            axisLeft.textColor = Color.BLACK
            axisRight.isEnabled = false
            setPadding(16, 16, 16, 16)
            invalidate()
        }
    }

    private fun setupHighRiskAreasChart() {
        pieChart = view?.findViewById(R.id.high_risk_chart)!!
        
        val entries = listOf(
            PieEntry(30f, "Irisan"),
            PieEntry(25f, "Asin"),
            PieEntry(23f, "Kennon"),
            PieEntry(22f, "Ambiong")
        )

        val dataSet = PieDataSet(entries, "")  // Empty label since we have title above
        
        dataSet.colors = listOf(
            ContextCompat.getColor(requireContext(), R.color.brown),
            ContextCompat.getColor(requireContext(), R.color.text_brown),
            ContextCompat.getColor(requireContext(), R.color.text_light),
            ContextCompat.getColor(requireContext(), R.color.light_brown)
        )

        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueFormatter = PercentFormatter()

        pieChart.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            legend.isEnabled = true
            legend.textSize = 12f
            legend.textColor = Color.BLACK
            legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            legend.orientation = Legend.LegendOrientation.HORIZONTAL
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
            setDrawEntryLabels(false)
            centerText = ""
            invalidate()
        }
    }

    private fun setupLandslideRiskIndicator() {
        try {
            // Get current date
            val currentDate = SimpleDateFormat("MM-dd-yyyy", Locale.US).format(Date())
            
            // Find views using the safer way
            val dateText = view?.findViewById<TextView>(R.id.landslide_risk_date)
            val locationText = view?.findViewById<TextView>(R.id.landslide_risk_location)
            val riskProgress = view?.findViewById<ProgressBar>(R.id.risk_progress)
            val riskPercentageText = view?.findViewById<TextView>(R.id.risk_percentage)
            
            // Find the card view
            val cardView = view?.findViewById<View>(R.id.landslide_risk_card)

            // Define risk levels for different locations in Cordillera
            val locations = listOf(
                // Benguet Province (Higher risk due to steep terrain and urban development)
                "Baguio City" to 85,      // Urban center, steep slopes, high rainfall
                "La Trinidad" to 82,       // Valley area, dense population
                "Itogon" to 80,           // Mining area, steep terrain
                "Tuba" to 78,             // Mixed terrain, mining activities
                "Tublay" to 75,           // Mountain slopes
                
                // Mountain Province (Moderate to high risk due to terrain)
                "Bontoc" to 77,           // Provincial capital, rice terraces
                "Sagada" to 75,           // Tourist area, limestone formations
                "Bauko" to 73,            // Agricultural area
                "Besao" to 70,            // Mountain terrain
                
                // Ifugao Province (Moderate risk, terraced landscapes)
                "Banaue" to 72,           // Rice terraces, tourism
                "Kiangan" to 70,          // Historical site
                "Lagawe" to 68,           // Provincial capital
                
                // Kalinga Province (Moderate risk)
                "Tabuk City" to 65,       // Provincial capital
                "Tinglayan" to 68,        // Mountain area
                
                // Apayao Province (Lower risk due to less development)
                "Luna" to 63,             // Provincial capital
                "Kabugao" to 60,          // Forest area
                
                // Abra Province (Varied risk levels)
                "Bangued" to 65,          // Provincial capital
                "Bucay" to 68,            // Valley area
                "Bucloc" to 70            // Mountain area
            )

            // Add click listener to the card
            cardView?.setOnClickListener {
                showLocationsDialog(locations)
            }

            // Create a timer to update location every few seconds
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed(object : Runnable {
                override fun run() {
                    // Select random location
                    val (location, riskPercentage) = locations.random()

                    // Set the values safely
                    dateText?.text = currentDate
                    locationText?.text = location
                    riskProgress?.progress = riskPercentage
                    riskPercentageText?.text = "$riskPercentage%"

                    // Set progress bar colors
                    riskProgress?.progressDrawable?.setColorFilter(
                        ContextCompat.getColor(requireContext(), R.color.brown),
                        PorterDuff.Mode.SRC_IN
                    )
                    
                    // Schedule the next update
                    handler.postDelayed(this, 3000)
                }
            }, 2000)

        } catch (e: Exception) {
            Log.e("LandslideRisk", "Error setting up landslide risk indicator", e)
        }
    }

    private fun showLocationsDialog(locations: List<Pair<String, Int>>) {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Landslide Risk Levels")
            .setAdapter(
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_list_item_2,
                    android.R.id.text1,
                    locations.map { "${it.first} - ${it.second}%" }
                )
            ) { dialog, which ->
                // Update the main display with selected location
                val (location, riskPercentage) = locations[which]
                view?.let { view ->
                    view.findViewById<TextView>(R.id.landslide_risk_location)?.text = location
                    view.findViewById<ProgressBar>(R.id.risk_progress)?.progress = riskPercentage
                    view.findViewById<TextView>(R.id.risk_percentage)?.text = "$riskPercentage%"
                }
                dialog.dismiss()
            }
            .setNegativeButton("Close", null)
            .create()

        dialog.show()
    }

    private fun setupRecentEarthquakes() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://earthquake.usgs.gov/fdsnws/event/1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(EarthquakeService::class.java)

        lifecycleScope.launch {
            try {
                val response = service.getEarthquakes()
                val earthquakes = response.features.map {
                    EarthquakeData(
                        time = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(it.properties.time)),
                        magnitude = "M${it.properties.mag}",
                        distance = it.properties.place
                    )
                }
                earthquakeRecyclerView.adapter = EarthquakeAdapter(earthquakes)
            } catch (e: Exception) {
                Log.e("Home3", "Error fetching earthquake data: ${e.message}")
            }
        }
    }

}

data class EarthquakeData(
    val time: String,
    val magnitude: String,
    val distance: String
)

class EarthquakeAdapter(private val earthquakes: List<EarthquakeData>) :
    RecyclerView.Adapter<EarthquakeAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val timeText: TextView = view.findViewById(R.id.time_text)
        val magnitudeText: TextView = view.findViewById(R.id.magnitude_text)
        val distanceText: TextView = view.findViewById(R.id.distance_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_earthquake, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val earthquake = earthquakes[position]

        // Set text properties dynamically
        holder.timeText.text = earthquake.time
        holder.magnitudeText.text = earthquake.magnitude
        holder.distanceText.text = earthquake.distance

        // Example of dynamically changing text size based on content
        if (earthquake.magnitude.length > 5) {
            holder.magnitudeText.textSize = 14f
        } else {
            holder.magnitudeText.textSize = 16f
        }

        // Example of changing text color based on magnitude
        val magnitudeValue = earthquake.magnitude.substring(1).toDoubleOrNull() ?: 0.0
        holder.magnitudeText.setTextColor(
            if (magnitudeValue >= 5.0) Color.RED else Color.BLACK
        )
    }

    override fun getItemCount() = earthquakes.size
}


// Add the calculateDistance function
private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371 // Earth's radius in kilometers
    val latDistance = Math.toRadians(lat2 - lat1)
    val lonDistance = Math.toRadians(lon2 - lon1)
    val a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return r * c
}