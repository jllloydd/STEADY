package com.bonak.steady

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import androidx.preference.PreferenceManager
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import android.location.Geocoder
import android.location.Address
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.text.Editable
import android.text.TextWatcher
import org.osmdroid.views.overlay.Overlay
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent

class Home1 : Fragment() {

    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val mapViewModel: MapViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home1, container, false)

        val locSelectBtn: Button = view.findViewById(R.id.loc_select_btn)

        locSelectBtn.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Search Location")

            val input = AutoCompleteTextView(requireContext())
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)

            val adapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line)
            input.setAdapter(adapter)

            input.setOnItemClickListener { _, _, position, _ ->
                val selectedSuggestion = adapter.getItem(position)
                performSearch(selectedSuggestion ?: "")
            }

            input.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    // No action needed
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val suggestions = NominatimApi.service.searchLocations(s.toString())
                            withContext(Dispatchers.Main) {
                                adapter.clear()
                                adapter.addAll(suggestions.map { it.display_name })
                                adapter.notifyDataSetChanged()
                            }
                        } catch (e: Exception) {
                            // Handle exceptions
                        }
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                    // No action needed
                }
            })

            builder.setPositiveButton("OK") { dialog, _ ->
                val searchText = input.text.toString()
                performSearch(searchText)
                dialog.dismiss()
            }
            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }

            builder.show()
        }

        val ctx: Context = requireActivity().applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        Configuration.getInstance().userAgentValue = "com.bonak.steady"

        mapView = view.findViewById(R.id.map)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val currentLocationOverlay = CurrentLocationOverlay {
            resetToCurrentLocation()
        }
        mapView.overlays.add(currentLocationOverlay)

        val mapController = mapView.controller
        mapController.setZoom(mapViewModel.mapZoomLevel)
        mapController.setCenter(mapViewModel.mapCenter ?: GeoPoint(48.8583, 2.2944))

        if (mapViewModel.mapCenter == null) {

            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {

                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val userLocation = GeoPoint(location.latitude, location.longitude)
                        mapController.setZoom(18.0)
                        mapController.setCenter(userLocation)

                        val startMarker = Marker(mapView)
                        startMarker.position = userLocation
                        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        startMarker.title = "You are here"
                        mapView.overlays.add(startMarker)


                        mapViewModel.mapCenter = userLocation
                        mapViewModel.mapZoomLevel = 18.0
                    }
                }
            } else {

                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1
                )
            }
        }

        return view
    }

    private fun performSearch(query: String) {
        val geocoder = Geocoder(requireContext())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val addresses: List<Address>? = geocoder.getFromLocationName(query, 1)
                withContext(Dispatchers.Main) {
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        val geoPoint = GeoPoint(address.latitude, address.longitude)

                        mapView.overlays.clear()

                        val startMarker = Marker(mapView)
                        startMarker.position = geoPoint
                        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        startMarker.title = "Location Selected"

                        mapView.overlays.add(startMarker)
                        val currentLocationOverlay = CurrentLocationOverlay {
                            resetToCurrentLocation()
                        }
                        mapView.overlays.add(currentLocationOverlay)

                        val mapController = mapView.controller
                        mapController.setCenter(geoPoint)
                        mapController.setZoom(18.0)

                        mapViewModel.mapCenter = geoPoint
                        mapViewModel.mapZoomLevel = 18.0
                    } else {
                    }
                }
            } catch (e: Exception) {
            }
        }
    }

    private fun resetToCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val geoPoint = GeoPoint(it.latitude, it.longitude)

                mapView.overlays.clear()

                val startMarker = Marker(mapView)
                startMarker.position = geoPoint
                startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                startMarker.title = "Current Location"
                mapView.overlays.add(startMarker)

                val currentLocationOverlay = CurrentLocationOverlay {
                    resetToCurrentLocation()
                }
                mapView.overlays.add(currentLocationOverlay)

                val mapController = mapView.controller
                mapController.setCenter(geoPoint)
                mapController.setZoom(18.0)

                mapViewModel.mapCenter = geoPoint
                mapViewModel.mapZoomLevel = 18.0
            }
        }
    }

    class CurrentLocationOverlay(private val onClick: () -> Unit) : Overlay() {
        private val outerCirclePaint = Paint().apply {
            color = android.graphics.Color.BLUE
            style = Paint.Style.FILL
            alpha = 50 // Semi-transparent
        }
        private val innerCirclePaint = Paint().apply {
            color = android.graphics.Color.BLUE
            style = Paint.Style.FILL
        }
        private val borderPaint = Paint().apply {
            color = android.graphics.Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        private val buttonRect = RectF(10f, 10f, 110f, 110f)

        override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
            if (!shadow) {

                canvas.drawOval(buttonRect, outerCirclePaint)

                val centerX = buttonRect.centerX()
                val centerY = buttonRect.centerY()
                val radius = (buttonRect.width() / 4)
                canvas.drawCircle(centerX, centerY, radius, innerCirclePaint)

                canvas.drawOval(buttonRect, borderPaint)
            }
        }

        override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
            if (buttonRect.contains(e.x, e.y)) {
                onClick()
                return true
            }
            return false
        }
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()

        mapViewModel.mapCenter = mapView.mapCenter as GeoPoint
        mapViewModel.mapZoomLevel = mapView.zoomLevelDouble
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDetach()
    }
}