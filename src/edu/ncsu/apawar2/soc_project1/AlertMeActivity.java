package edu.ncsu.apawar2.soc_project1;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import android.content.Context;
import android.database.Cursor;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class AlertMeActivity extends MapActivity implements LocationListener {

  LocationManager locationManager;
  MapController mapController;
  AlertsPositionOverlay positionOverlay;

  @Override
  protected boolean isRouteDisplayed() {
    return false;
  }

  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    setContentView(R.layout.main);


    MapView myMapView = (MapView) findViewById(R.id.myMapView);
    mapController = myMapView.getController();

    // Configure the map display options
    myMapView.setSatellite(true);

    // Zoom in
    mapController.setZoom(17);

    // Add the AlertsPositionOverlay
    positionOverlay = new AlertsPositionOverlay(this);
    List<Overlay> overlays = myMapView.getOverlays();
    overlays.add(positionOverlay);

    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

    Criteria criteria = new Criteria();
    criteria.setAccuracy(Criteria.ACCURACY_FINE);
    criteria.setAltitudeRequired(false);
    criteria.setBearingRequired(false);
    criteria.setCostAllowed(true);
    criteria.setPowerRequirement(Criteria.POWER_LOW);

    String provider = locationManager.getBestProvider(criteria, true);
    locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, this);
    Location location = locationManager.getLastKnownLocation(provider);

    if (location != null)
      updateWithNewLocation(location);
  }

  /** Update UI with a new location */
  private void updateWithNewLocation(Location location) {
    TextView myLocationText = (TextView) findViewById(R.id.myLocationText);
    String latLongString= "";
    String sav = "";
    if (location != null) {
      // Update the map location.
    
      Double geoLat = location.getLatitude() * 1E6;
      Double geoLng = location.getLongitude() * 1E6;
      GeoPoint point = new GeoPoint(geoLat.intValue(), geoLng.intValue());
      
      

      mapController.animateTo(point);

      // update my position marker
      positionOverlay.setLocation(location);
      double lat = location.getLatitude();
      double lng = location.getLongitude();
      double old_dist=0;
      Cursor c =this.getContentResolver().query(AlertProvider.CONTENT_URI, null, null, null, null);
      if (c.moveToFirst()) {
                    do{
                    	double dist;
                    	dist = getDistance(lat,lng,c.getDouble(c.getColumnIndex(AlertProvider.KEY_PLACE_LAT)),c.getDouble(c.getColumnIndex(AlertProvider.KEY_PLACE_LNG)));
                      if(dist<=50)
                    	  if(old_dist<=1 || old_dist>dist)
                    	  {       
                    		  old_dist=dist;
                    	  sav="\n"+(c.getString(c.getColumnIndex(AlertProvider.KEY_ALERT)));
                    	  }
                    	   //Toast.makeText(this, "Distance:"+dist+c.getString(c.getColumnIndex(AlertProvider.KEY_ALERT)), Toast.LENGTH_SHORT).show();
                    } while (c.moveToNext());
                 }
      

      latLongString = "Lat:" + lat + "\nLong:" + lng+ sav;
     
    } else {
      latLongString = "No location found";
    }

    myLocationText.setText("Your Current Position is:\n" + latLongString);
  }

  
  /**
   * Finds distance between two coordinate pairs.
   *
   * @param lat1 First latitude in degrees
   * @param lon1 First longitude in degrees
   * @param lat2 Second latitude in degrees
   * @param lon2 Second longitude in degrees
   * @return distance in meters
   */
  public static double getDistance(double lat1, double lon1, double lat2, double lon2) {

    final double Radius = 6371 * 1E3; // Earth's mean radius

    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);
    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1))
        * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return Radius * c;
  }

  public void onLocationChanged(Location loc)
  {
	  
updateWithNewLocation(loc);

  }
  public void onProviderDisabled(String provider)
  {
   }
  public void onProviderEnabled(String provider)
  {
   }
    public void onStatusChanged(String provider, int status, Bundle extras)
  {
  }

}