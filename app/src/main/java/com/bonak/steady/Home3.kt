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
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieData
import android.graphics.Color
import com.bonak.steady.OpenMeteoService
import com.bonak.steady.OpenMeteoResponse
import com.bonak.steady.EarthquakeService
import com.bonak.steady.EarthquakeResponse
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class Home3 : Fragment() {
    private lateinit var lineChart: LineChart
    private lateinit var pieChart: PieChart
    private lateinit var riskProgressBar: ProgressBar
    private lateinit var earthquakeRecyclerView: RecyclerView

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var updateRunnable: Runnable

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

        lineChart = view.findViewById(R.id.line_chart)

        setupRainfallRiskIndicator()
        setupHighRiskAreasChart()
        setupLandslideRiskIndicator()
        setupRecentEarthquakes()
    }

    private fun setupRainfallRiskIndicator() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val weatherService = retrofit.create(OpenMeteoService::class.java)

        lifecycleScope.launch {
            try {
                val latitude = 16.4164
                val longitude = 120.5931
                Log.d("Home3", "Requesting weather data for lat: $latitude, lon: $longitude")

                val weatherResponse = weatherService.getForecast(
                    latitude = latitude,
                    longitude = longitude,
                    hourly = "precipitation_probability",
                    timezone = "Asia/Tokyo"
                )

                val currentHourIndex = getCurrentHourIndex(weatherResponse.hourly.time)
                displayRiskForNext12Hours(weatherResponse.hourly.precipitation_probability, currentHourIndex, weatherResponse.hourly.time)

            } catch (e: Exception) {
                Log.e("Home3", "Error fetching weather data: ${e.message}")
            }
        }
    }

    private fun getCurrentHourIndex(times: List<String>): Int {
        val currentTime = SimpleDateFormat("yyyy-MM-dd'T'HH", Locale.getDefault()).format(Date())
        return times.indexOfFirst { it.startsWith(currentTime) }
    }

    private fun displayRiskForNext12Hours(probabilities: List<Int>, startIndex: Int, times: List<String>) {
        val entries = mutableListOf<Entry>()
        val labels = mutableListOf<String>()

        for (i in startIndex until startIndex + 12) {
            if (i >= probabilities.size) break
            entries.add(Entry((i - startIndex).toFloat(), probabilities[i].toFloat()))
            labels.add(times[i].substring(11, 16)) // Extract hour and minute
        }

        val dataSet = LineDataSet(entries, "Precipitation Probablity").apply {
            color = ContextCompat.getColor(requireContext(), R.color.brown)
            valueTextColor = ContextCompat.getColor(requireContext(), R.color.text_brown)
            valueFormatter = object : ValueFormatter() {
                override fun getPointLabel(entry: Entry?): String {
                    return "${entry?.y?.toInt()}%"
                }
            }
        }

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        // Configure x-axis
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.granularity = 1f
        xAxis.labelRotationAngle = -45f
        xAxis.setDrawGridLines(false)

        // Configure y-axis
        val yAxisLeft = lineChart.axisLeft
        yAxisLeft.axisMinimum = 0f
        yAxisLeft.axisMaximum = 100f
        yAxisLeft.setDrawGridLines(false)

        val yAxisRight = lineChart.axisRight
        yAxisRight.isEnabled = false

        // General chart settings
        lineChart.description.isEnabled = false
        lineChart.setExtraOffsets(10f, 10f, 10f, 15f) // Add padding
        lineChart.invalidate() // Refresh the chart
    }

    private fun setupHighRiskAreasChart() {
        pieChart = view?.findViewById(R.id.high_risk_chart)!!

        val entries = listOf(
            PieEntry(30f, "Irisan"),
            PieEntry(25f, "Asin"),
            PieEntry(23f, "Kennon"),
            PieEntry(22f, "Ambiong")
        )

        val dataSet = PieDataSet(entries, "")

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

            // Define risk levels for different locations
            val locations = listOf(
                "Baguio City" to 85,      // Urban center, steep slopes, high rainfall
                "La Trinidad" to 82,      // Valley area, dense population
                "Itogon" to 80,           // Mining area, steep terrain
                "Tuba" to 78,             // Mixed terrain, mining activities
                "Tublay" to 75,           // Mountain slopes
                "Bontoc" to 77,           // Provincial capital, rice terraces
                "Sagada" to 75,           // Tourist area, limestone formations
                "Bauko" to 73,            // Agricultural area
                "Besao" to 70,            // Mountain terrain
                "Banaue" to 72,           // Rice terraces, tourism
                "Kiangan" to 70,          // Historical site
                "Lagawe" to 68,           // Provincial capital
                "Tabuk City" to 65,       // Provincial capital
                "Tinglayan" to 68,        // Mountain area
                "Luna" to 63,             // Provincial capital
                "Kabugao" to 60,          // Forest area
                "Bangued" to 65,          // Provincial capital
                "Bucay" to 68,            // Valley area
                "Bucloc" to 70            // Mountain area
            )

            // Add click listener to the card
            val cardView = view?.findViewById<View>(R.id.landslide_risk_card)
            cardView?.setOnClickListener {
                showLocationsDialog(locations)
            }

            // Initialize the update runnable
            updateRunnable = object : Runnable {
                override fun run() {
                    if (!isAdded) return  // Ensure the fragment is attached

                    // Select random location
                    val (location, riskPercentage) = locations.random()

                    // Set the values safely
                    dateText?.text = currentDate
                    locationText?.text = location
                    riskProgress?.progress = riskPercentage
                    riskPercentageText?.text = "$riskPercentage%"

                    // Schedule the next update
                    handler.postDelayed(this, 3000)
                }
            }
            handler.postDelayed(updateRunnable, 2000)

        } catch (e: Exception) {
            Log.e("LandslideRisk", "Error setting up landslide risk indicator", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Ensure to remove callbacks to prevent memory leaks
        handler.removeCallbacksAndMessages(null)
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


        holder.timeText.text = earthquake.time
        holder.magnitudeText.text = earthquake.magnitude
        holder.distanceText.text = earthquake.distance


        if (earthquake.magnitude.length > 5) {
            holder.magnitudeText.textSize = 14f
        } else {
            holder.magnitudeText.textSize = 16f
        }


        val magnitudeValue = earthquake.magnitude.substring(1).toDoubleOrNull() ?: 0.0
        holder.magnitudeText.setTextColor(
            if (magnitudeValue >= 5.0) Color.RED else Color.BLACK
        )
    }

    override fun getItemCount() = earthquakes.size
}


