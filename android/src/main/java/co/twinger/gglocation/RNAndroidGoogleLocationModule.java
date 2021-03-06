
package co.twinger.gglocation;

import android.location.Location;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;


public class RNAndroidGoogleLocationModule extends ReactContextBaseJavaModule {
  // React Class Name as called from JS
  public static final String REACT_CLASS = "RNAndroidGoogleLocation";
  // Unique Name for Log TAG
  public static final String TAG = RNAndroidGoogleLocationModule.class.getSimpleName();
  // Save last Location Provided
  private Location mLastLocation;
  // The Google Play Services Location Provider
  private LocationProvider mLocationProvider;
  // The React Native Context
  ReactApplicationContext mReactContext;

  // Constructor Method as called in Package
  public RNAndroidGoogleLocationModule(ReactApplicationContext reactContext) {
    super(reactContext);
    // Save Context for later use
    mReactContext = reactContext;

    Log.i(TAG, "Initializing Google Location Module:");
    
    // Get Location Provider from Google Play Services
    mLocationProvider = new LocationProvider(mReactContext.getApplicationContext(), this);
  
    Log.i(TAG, "Done Initializing Google Location Module:");
  }

  @Override
  public String getName() {
    return REACT_CLASS;
  }

  // class RNAndroidGoogleLocationCallback extends LocationCallback {
  //   @Override
  //   public void onLocationResult(LocationResult locationResult) {
  //       Log.i(TAG, "onLocationResult");
  //       super.onLocationResult(locationResult);
        
  //       RNAndroidGoogleLocationModule.this.mLastLocation = locationResult.getLastLocation();

  //       Log.i(TAG, "Location Received: " + RNAndroidGoogleLocationModule.this.mLastLocation.toString());

  //       RNAndroidGoogleLocationModule.this.getLocation();
  //   }
  // }

  /*
   * Location Callback as defined by LocationProvider
   */

  // @Override
  public void handleNewLocation(Location location) {
    if (location != null) {
      mLastLocation = location;
      Log.i(TAG, "handleNewLocation..." + location.toString());
      getLocation();
    }
  }

  /*
   * Location Provider as called by JS
   */
  @ReactMethod
  public void getLocation() {
    Log.i(TAG, "Triggering getLocation()");

    if (mLastLocation != null) {
      try {
        double Longitude;
        double Latitude;

        // Receive Longitude / Latitude from (updated) Last Location
        Longitude = mLastLocation.getLongitude();
        Latitude = mLastLocation.getLatitude();

        Log.i(TAG, "mLastLocation exists: Got location. Lng: " + Longitude + " Lat: " + Latitude);

        // Create Map with Parameters to send to JS
        WritableMap params = Arguments.createMap();
        params.putDouble("Longitude", Longitude);
        params.putDouble("Latitude", Latitude);

        // Send Event to JS to update Location
        sendEvent(mReactContext, "updateLocation", params);
      } catch (Exception e) {
        e.printStackTrace();
        Log.i(TAG, "Location services disconnected.");
      }
    }
    else {
      mLocationProvider.initializeBuilder();
    }

    Log.i(TAG, "Done Triggering getLocation()");
  }

  /*
   * Internal function for communicating with JS
   */
  private void sendEvent(ReactContext reactContext, String eventName, @Nullable WritableMap params) {
    if (reactContext.hasActiveCatalystInstance()) {
      reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
    } else {
      Log.i(TAG, "Waiting for CatalystInstance...");
    }
  }

  @Override
  protected void finalize() throws Throwable {
    // If Location Provider is connected, disconnect.
    if (mLocationProvider != null) {
      Log.i(TAG, "Disconnecting LocationProvider...");
      mLocationProvider.disconnect();
    }

    super.finalize();
  }
}