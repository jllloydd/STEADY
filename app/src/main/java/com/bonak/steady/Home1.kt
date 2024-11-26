package com.bonak.steady

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

class Home1 : Fragment() {

    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val mapViewModel: MapViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home1, container, false)

        val ctx: Context = requireActivity().applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        Configuration.getInstance().userAgentValue = "com.bonak.steady"

        mapView = view.findViewById(R.id.map)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())


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