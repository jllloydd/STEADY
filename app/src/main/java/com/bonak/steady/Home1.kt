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
import android.widget.TextView
import android.widget.EditText
import android.util.Log
import androidx.lifecycle.lifecycleScope
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.Priority
import androidx.fragment.app.activityViewModels
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import org.osmdroid.config.Configuration
import androidx.preference.PreferenceManager
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.util.*
import android.location.Geocoder
import android.location.Address
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Job
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.text.Editable
import android.text.TextWatcher
import org.osmdroid.views.overlay.Overlay
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import com.bonak.steady.shelterLocations
import com.bonak.steady.safeLocations
import com.bonak.steady.dangerLocations
import com.bonak.steady.OsrmApiService
import com.bonak.steady.OsrmResponse
import android.speech.tts.TextToSpeech

class Home1 : Fragment() {

    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val mapViewModel: MapViewModel by activityViewModels()

    private lateinit var locationCallback: LocationCallback
    private var currentLocationMarker: Marker? = null
    private lateinit var textToSpeech: TextToSpeech

    private var destinationGeoPoint: GeoPoint? = null

    private var referenceLocation: GeoPoint? = null

    private var searchJob: Job? = null

    private var isNavigationActive: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home1, container, false)

        mapView = view.findViewById(R.id.map)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val currentGeoPoint = GeoPoint(location.latitude, location.longitude)
                    updateCurrentLocationOnMap(currentGeoPoint)
                    destinationGeoPoint?.let { destination ->
                        updateRouteToDestination(currentGeoPoint, destination)
                    }
                }
            }
        }

        textToSpeech = TextToSpeech(requireContext()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.language = Locale.US
            }
        }

        checkLocationPermission()

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            setInitialLocation()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }

        val locSelectBtn: Button = view.findViewById(R.id.loc_select_btn)

        val locEcenterBtn: Button = view.findViewById(R.id.loc_ecenter_btn)

        locEcenterBtn.setOnClickListener {
            val nearestEvacuationCenter = findNearestEvacuationCenter()
            nearestEvacuationCenter?.let {
                showDirectionsToLocation(it.geoPoint)
            }
        }

        val locHospitalBtn: Button = view.findViewById(R.id.loc_hospital_btn)

        locHospitalBtn.setOnClickListener {
            val nearestHospital = findNearestHospital()
            nearestHospital?.let {
                showDirectionsToLocation(it.geoPoint)
            }
        }

        locSelectBtn.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Search Location")

            val input = AutoCompleteTextView(requireContext())
            input.inputType = InputType.TYPE_CLASS_TEXT
            builder.setView(input)

            val adapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line)
            input.setAdapter(adapter)

            input.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    searchJob?.cancel()
                    searchJob = CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val suggestions = NominatimApi.service.searchLocations(s.toString())
                            withContext(Dispatchers.Main) {
                                if (suggestions.isNotEmpty()) {
                                    adapter.clear()
                                    adapter.addAll(suggestions.map { it.display_name })
                                    adapter.notifyDataSetChanged()
                                } else {

                                }
                            }
                        } catch (e: Exception) {

                        }
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
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

        val shelterBtn: Button = view.findViewById(R.id.shelter_btn)
        shelterBtn.setOnClickListener {
            addMarkersToMap(shelterLocations, "Shelter")
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val currentLocationOverlay = CurrentLocationOverlay {
            resetToCurrentLocation()
        }
        mapView.overlays.add(currentLocationOverlay)

        val mapController = mapView.controller
        mapController.setZoom(mapViewModel.mapZoomLevel)
        mapController.setCenter(mapViewModel.mapCenter ?: GeoPoint(48.8583, 2.2944))


        return view
    }

    private fun setInitialLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val geoPoint = GeoPoint(it.latitude, it.longitude)
                    referenceLocation = geoPoint
                    updateNearestLocationsUI()


                    if (currentLocationMarker == null) {
                        currentLocationMarker = Marker(mapView).apply {
                            position = geoPoint
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            title = "Current Location"
                            mapView.overlays.add(this)
                        }
                    } else {
                        currentLocationMarker?.position = geoPoint
                    }

                    val mapController = mapView.controller
                    mapController.setCenter(geoPoint)
                    mapController.setZoom(18.0)

                    mapViewModel.mapCenter = geoPoint
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

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(2000)
                .build()

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    private fun updateCurrentLocationOnMap(currentGeoPoint: GeoPoint) {
        CoroutineScope(Dispatchers.Main).launch {
            if (currentLocationMarker == null) {
                currentLocationMarker = Marker(mapView).apply {
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = "Current Location"
                    mapView.overlays.add(this)
                }
            }
            currentLocationMarker?.position = currentGeoPoint
            mapView.controller.setCenter(currentGeoPoint)
            mapView.invalidate()
        }
    }

    private fun showDirectionsToLocation(destination: GeoPoint) {
        destinationGeoPoint = destination
        isNavigationActive = true
        referenceLocation?.let { startLocation ->
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val directions = fetchDirections(startLocation, destination)
                    withContext(Dispatchers.Main) {
                        if (isNavigationActive) {
                            startLocationUpdates()
                            displayRouteOnMap(directions, startLocation, destination)
                            provideTurnByTurnInstructions(directions)
                            setupCancelNavigationOverlay()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Home1", "Error fetching directions: ${e.message}")
                }
            }
        }
    }

    private fun setupCancelNavigationOverlay() {

        if (mapView.overlays.none { it is CancelNavigationOverlay }) {
            val cancelOverlay = CancelNavigationOverlay {
                clearRouteAndMarkers()
            }
            mapView.overlays.add(cancelOverlay)
        }
    }

    private fun clearRouteAndMarkers() {
        Log.d("Home1", "Clearing route and markers")
        mapView.overlays.clear()

        isNavigationActive = false // Reset navigation state

        referenceLocation?.let { currentLocation ->
            val currentMarker = Marker(mapView)
            currentMarker.position = currentLocation
            currentMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            currentMarker.title = "Current Location"
            mapView.overlays.add(currentMarker)
        }

        mapView.invalidate()
        Log.d("Home1", "Map reset complete")
    }

    private suspend fun fetchDirections(start: GeoPoint, end: GeoPoint): List<GeoPoint> {
        val startString = "${start.longitude},${start.latitude}"
        val endString = "${end.longitude},${end.latitude}"

        val response = NetworkModule.osrmApiService.getRoute(startString, endString)

        val route = response.routes.firstOrNull()
        return if (route != null) {
            decodePolyline(route.geometry)
        } else {
            emptyList()
        }
    }

    private fun displayRouteOnMap(route: List<GeoPoint>, current: GeoPoint, destination: GeoPoint) {
        mapView.overlays.clear()


        val polyline = Polyline()
        polyline.setPoints(route)
        polyline.title = "Route"
        mapView.overlays.add(polyline)


        val currentMarker = Marker(mapView)
        currentMarker.position = current
        currentMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        currentMarker.title = "Current Location"
        mapView.overlays.add(currentMarker)


        val destinationMarker = Marker(mapView)
        destinationMarker.position = destination
        destinationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        destinationMarker.title = "Destination"
        mapView.overlays.add(destinationMarker)


        mapView.invalidate()
    }

    private fun provideTurnByTurnInstructions(route: List<GeoPoint>) {

        // Optionally, use TextToSpeech for voice guidance
        textToSpeech.speak("Follow the route to your destination.", TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun findNearestHospital(): LocationData? {
        return referenceLocation?.let { refLoc ->
            hospitalLocations.minByOrNull { it.geoPoint.distanceToAsDouble(refLoc) }
        }
    }

    private fun findNearestEvacuationCenter(): LocationData? {
        return referenceLocation?.let { refLoc ->
            shelterLocations.minByOrNull { it.geoPoint.distanceToAsDouble(refLoc) }
        }
    }

    private fun updateNearestLocationsUI() {
        val nearestHospital = findNearestHospital()
        nearestHospital?.let {
            view?.findViewById<TextView>(R.id.nearest_hospital_txt)?.text = it.name
            view?.findViewById<TextView>(R.id.hospital_loc_txt)?.text = it.address
        }

        val nearestEvacuationCenter = findNearestEvacuationCenter()
        nearestEvacuationCenter?.let {
            view?.findViewById<TextView>(R.id.evacuation_center_txt)?.text = it.name
        }
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

                        referenceLocation = geoPoint
                        updateNearestLocationsUI()

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
                    }
                }
            } catch (e: Exception) {
            }
        }
    }

    private fun updateRouteToDestination(currentLocation: GeoPoint, destination: GeoPoint) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val directions = fetchDirections(currentLocation, destination)
                withContext(Dispatchers.Main) {
                    if (isNavigationActive) {
                        displayRouteOnMap(directions, currentLocation, destination)
                        setupCancelNavigationOverlay()
                    }
                }
            } catch (e: Exception) {
                Log.e("Home1", "Error updating route: ${e.message}")
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

                referenceLocation = geoPoint
                updateNearestLocationsUI()

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

    private fun addMarkersToMap(locations: List<LocationData>, label: String) {
        mapView.overlays.clear()

        for (location in locations) {
            val marker = Marker(mapView)
            marker.position = location.geoPoint
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker.title = location.name


            marker.setIcon(resources.getDrawable(org.osmdroid.library.R.drawable.marker_default, null))

            mapView.overlays.add(marker)
        }


        val textOverlay = TextOverlay("Showing $label locations")
        mapView.overlays.add(textOverlay)


        val currentLocationOverlay = CurrentLocationOverlay {
            resetToCurrentLocation()
        }
        mapView.overlays.add(currentLocationOverlay)


        if (locations.isNotEmpty()) {
            val mapController = mapView.controller
            mapController.setCenter(locations[0].geoPoint)
            mapController.setZoom(15.0)
        }
    }

    class CurrentLocationOverlay(private val onClick: () -> Unit) : Overlay() {
        private val outerCirclePaint = Paint().apply {
            color = android.graphics.Color.BLUE
            style = Paint.Style.FILL
            alpha = 50
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

        fusedLocationClient.removeLocationUpdates(locationCallback)
        textToSpeech.stop()

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
        textToSpeech.shutdown()
    }
}