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
        
        // Initialize all components
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
        earthquakeRecyclerView = view?.findViewById(R.id.earthquake_list)!!
        earthquakeRecyclerView.layoutManager = LinearLayoutManager(context)
        
        // Updated mock data with varied locations
        val earthquakes = listOf(
            EarthquakeData(
                time = "2 days ago",
                magnitude = "M2.8",
                distance = "32km from Baguio"  // Near La Trinidad
            ),
            EarthquakeData(
                time = "4 days ago",
                magnitude = "M3.2",
                distance = "45km from Tublay"  // Tublay, Benguet
            ),
            EarthquakeData(
                time = "5 days ago",
                magnitude = "M2.5",
                distance = "68km from Sagada"  // Sagada area
            ),
            EarthquakeData(
                time = "1 week ago",
                magnitude = "M3.5",
                distance = "85km from Bontoc"  // Mountain Province
            ),
            EarthquakeData(
                time = "2 weeks ago",
                magnitude = "M4.1",
                distance = "95km from Banaue"  // Ifugao area
            ),
            EarthquakeData(
                time = "2 weeks ago",
                magnitude = "M2.7",
                distance = "73km from Tabuk"  // Kalinga area
            ),
            EarthquakeData(
                time = "3 weeks ago",
                magnitude = "M3.0",
                distance = "120km from Luna"  // Apayao area
            ),
            EarthquakeData(
                time = "1 month ago",
                magnitude = "M3.8",
                distance = "145km from Bangued"  // Abra region
            ),
            EarthquakeData(
                time = "1 month ago",
                magnitude = "M2.6",
                distance = "55km from Itogon"  // Itogon area
            ),
            EarthquakeData(
                time = "1 month ago",
                magnitude = "M3.3",
                distance = "88km from Tuba"  // Tuba area
            )
        )

        earthquakeRecyclerView.adapter = EarthquakeAdapter(earthquakes)
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
        holder.timeText.text = earthquake.time
        holder.magnitudeText.text = earthquake.magnitude
        holder.distanceText.text = earthquake.distance
    }

    override fun getItemCount() = earthquakes.size
}

// Add interface for USGS API
interface USGSService {
    @GET("query")
    suspend fun getEarthquakes(
        @Query("format") format: String = "geojson",
        @Query("latitude") latitude: Double = 16.4023,  // Baguio City coordinates
        @Query("longitude") longitude: Double = 120.5960,
        @Query("maxradiuskm") radius: Int = 150,  // Focus on CAR region
        @Query("starttime") startTime: String = getLastMonthDate(),
        @Query("minmagnitude") minMagnitude: Double = 2.0,  // Lowered to show more earthquakes
        @Query("orderby") orderBy: String = "time",  // Order by most recent
        @Query("limit") limit: Int = 50  // Show more results
    ): EarthquakeResponse
}

// Add data classes for API response
data class EarthquakeResponse(
    val features: List<Feature>
)

data class Feature(
    val properties: Properties,
    val geometry: Geometry
)

data class Geometry(
    val coordinates: List<Double>
)

data class Properties(
    val mag: Double,
    val time: Long,
    val place: String
)

// Add this helper function
private fun getLastMonthDate(): String {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.MONTH, -3)  // Go back 3 months instead of 1
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)
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