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

class Home3 : Fragment() {
    private lateinit var lineChart: LineChart
    private lateinit var pieChart: PieChart
    private lateinit var riskProgressBar: ProgressBar
    private lateinit var earthquakeRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home3, container, false)
        
        // Initialize views
        lineChart = view.findViewById(R.id.landslide_frequency_chart)
        pieChart = view.findViewById(R.id.high_risk_chart)
        riskProgressBar = view.findViewById(R.id.risk_progress)
        earthquakeRecyclerView = view.findViewById(R.id.earthquake_list)

        setupLandslideFrequencyChart()
        setupHighRiskAreasChart()
        setupLandslideRiskIndicator()
        setupRecentEarthquakes()

        return view
    }

    private fun setupLandslideFrequencyChart() {
        // Sample data for line chart
        val entries = listOf(
            Entry(0f, 20f),
            Entry(1f, 15f),
            Entry(2f, 25f),
            Entry(3f, 30f),
            Entry(4f, 28f)
        )

        val dataSet = LineDataSet(entries, "Frequency")
        dataSet.setDrawFilled(true)
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER

        lineChart.data = LineData(dataSet)
        lineChart.description.isEnabled = false
        lineChart.legend.isEnabled = false
        lineChart.setDrawGridBackground(false)
        lineChart.invalidate()
    }

    private fun setupHighRiskAreasChart() {
        val entries = listOf(
            PieEntry(25f, "Irisan"),
            PieEntry(25f, "Asin"),
            PieEntry(25f, "Kennon"),
            PieEntry(25f, "Ambiong")
        )

        val dataSet = PieDataSet(entries, "High Risk Areas")
        pieChart.data = PieData(dataSet)
        pieChart.description.isEnabled = false
        pieChart.invalidate()
    }

    private fun setupLandslideRiskIndicator() {
        riskProgressBar.progress = 55 // Set to 55%
    }

    private fun setupRecentEarthquakes() {
        val earthquakes = listOf(
            EarthquakeData("5 days ago", "M5.5", "39km away"),
            EarthquakeData("5 days ago", "M5.5", "39km away"),
            EarthquakeData("5 days ago", "M5.5", "39km away"),
            EarthquakeData("5 days ago", "M5.5", "39km away")
        )

        earthquakeRecyclerView.layoutManager = LinearLayoutManager(context)
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