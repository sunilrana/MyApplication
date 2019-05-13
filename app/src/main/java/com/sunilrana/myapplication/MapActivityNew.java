package com.sunilrana.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.DataBufferUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.RuntimeRemoteException;
import com.google.android.gms.tasks.RuntimeExecutionException;
import com.google.android.gms.tasks.Tasks;
import com.google.android.libraries.places.compat.AutocompleteFilter;
import com.google.android.libraries.places.compat.AutocompletePrediction;
import com.google.android.libraries.places.compat.AutocompletePredictionBufferResponse;
import com.google.android.libraries.places.compat.GeoDataClient;
import com.google.android.libraries.places.compat.Place;
import com.google.android.libraries.places.compat.PlaceBufferResponse;
import com.google.android.libraries.places.compat.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class MapActivityNew extends AppCompatActivity implements OnMapReadyCallback,GoogleApiClient.OnConnectionFailedListener{

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;

    private static final String TAG = "MapActivityNew";

    private AutoCompleteTextView mSearchText ;
    private ImageView mGps,mInfo;
    private Spinner mSpinner;


    private GoogleMap mMap;
    private Boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private GeoDataClient mGoogleApiClient;
    private PlaceInfo mPlace;
    private Marker mMarker;
    private Polyline mCurrentPolyline;
    private Location mCurrentLocation;

    private LocationRequest mLocationRequest;


    private DrawerLayout dl;
//    private ActionBarDrawerToggle t;
  //  private NavigationView nv;


    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */



    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(-40,-168),new LatLng(71,136)
    );

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        mMap = googleMap;

        if (mLocationPermissionGranted) {
            getLDeviceLocation();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            //"Show My location"(GPS icon) button removed, because our custom search bar will block its view
           // mMap.getUiSettings()

            init();
            // hello feature

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_new);
        // getSupportActionBar().hide();

        dl = (DrawerLayout)findViewById(R.id.activity_main);

       // t.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");




        mSpinner = (Spinner) findViewById(R.id.spinner1);
        List<String> list = new ArrayList<String>();
        list.add("Select location on map");
        list.add("Select location from saved places");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,  android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(R.layout.spinner_item_layout);
        mSpinner.setAdapter(adapter);

        mSearchText = (AutoCompleteTextView) findViewById(R.id.inputSearch) ;
        mGps = (ImageView) findViewById(R.id.ic_gps) ;
        mInfo= (ImageView) findViewById(R.id.place_info);


        getLocationPermission();
//        FirebaseApp.initializeApp(this);

        startLocationUpdates();

    }

    private void startLocationUpdates() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

//        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
//                    @Override
//                    public void onLocationResult(LocationResult locationResult) {
//                        // do work here
//                        if (locationResult.getLastLocation()!=null && mAuth.getCurrentUser()!= null)
//                        {
//                            RWLocation loc = new RWLocation(locationResult.getLastLocation().getLatitude(),locationResult.getLastLocation().getLongitude());
//                            mDatabase.child("Users").child("User:"+mAuth.getCurrentUser().getUid()).child("currentLocation").setValue(loc);
//                        }
//                    }
//                },
//                Looper.myLooper());
    }


//    @Override
//    public void onTaskDone(Object... values) {
//        if(mCurrentPolyline!=null){
//            mCurrentPolyline.remove();
//        }
//        mCurrentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

//        if(t.onOptionsItemSelected(item))
//            return true;

        return super.onOptionsItemSelected(item);
    }

    private void geolocate() {
        Log.d(TAG,"geoLocate: geoLocating");
        String searchString = mSearchText.getText().toString();
        Geocoder geocoder = new Geocoder(MapActivityNew.this);
        List<Address> list = new ArrayList<>();

        try{
            list = geocoder.getFromLocationName(searchString,1);
        }
        catch (IOException e) {
            Log.e(TAG,"geoLocate: IOException"+ e.getMessage());
        }

        if(list.size()>0){
            Address address = list.get(0);

            Log.d(TAG,"geoLocate: found a location : "+ address.toString());
            //
            moveCamera(new LatLng(address.getLatitude(),address.getLongitude()),DEFAULT_ZOOM,address.getAddressLine(0));
        }
    }

    private void init(){
        Log.d(TAG,"init: initializing");

//        mGoogleApiClient = new GoogleApiClient
//                .Builder(this)
//                .addApi(Places.GEO_DATA_API)
//                .addApi(Places.PLACE_DETECTION_API)
//                .enableAutoManage(this, this)
//                .build();


        mGoogleApiClient = Places.getGeoDataClient(this,null);
        mSearchText.setOnItemClickListener(mAutoCompleteClickListener);

        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter( this,mGoogleApiClient,LAT_LNG_BOUNDS,null);
        mSearchText.setAdapter(mPlaceAutocompleteAdapter);
        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {

                if(actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){

                    //execute method for searching
                    geolocate();
                }

                return false;
            }

        });

        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"onClick: Clicked gps icon");
                getLDeviceLocation();
            }
        });

        mInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                   if(mMarker.isInfoWindowShown()){
                       mMarker.hideInfoWindow();
                   }
                   else
                   {
                       mMarker.showInfoWindow();
                   }
                }
                catch (NullPointerException ex){

                }
            }
        });

        hideSoftKeyboard();
    }

    private void initMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapActivityNew.this);
    }

    private void getLocationPermission(){
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){


            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionGranted =true;
                initMap();
            }
            else{
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }


        }
        else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }


    private void getLDeviceLocation(){
        Log.d(TAG,"getDeviceLocation: getting the devices current location");
        mFusedLocationProviderClient = getFusedLocationProviderClient(this);

        try{
            if(mLocationPermissionGranted){
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Log.d(TAG,"onComplete: found location !");
                            Location currentLocation = (Location) task.getResult();
                            mCurrentLocation = currentLocation;
                            moveCamera( new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()),DEFAULT_ZOOM,"My location");
                        }
                        else{
                            Log.d(TAG,"onComplete: current location is null");
                            Toast.makeText(MapActivityNew.this,"Unable to get Current location",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        }catch (SecurityException e){
            Log.e(TAG,"getLDeviceLocation: SecurityException: "+e.getMessage());
        }

    }

    private void moveCamera(LatLng latLng, float zoom,PlaceInfo placeinfo){
        Log.d(TAG,"moveCamer: moving the camera to: lat:"+latLng.latitude+" long:"+latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));

        mMap.clear();
        if(placeinfo !=null){
            try{
                String snippet = "Address" + placeinfo.getAddress() +"\n"
                        +"Phone Number" + placeinfo.getPhonenumber() +"\n"
                        +"Website" + placeinfo.getWebSite() +"\n"
                        +"Rating" + placeinfo.getRating() +"\n";

                MarkerOptions options  = new MarkerOptions()
                        .position(latLng)
                        .title(placeinfo.getName())
                        .snippet(snippet);
                mMarker = mMap.addMarker(options);

                MarkerOptions options1 = new MarkerOptions()
                        .position(new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude()))
                        .title("My Location");
                mMap.addMarker(options1);

                showDirections(new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude())
                        ,latLng
                        , "driving");

            }
            catch (NullPointerException ex){
                Log.e(TAG,"moveCamera: NullPointerException "+ex.getMessage());
            }

        }else
        {
            mMap.addMarker(new MarkerOptions().position(latLng));
        }
        hideSoftKeyboard();
    }

    private void moveCamera(LatLng latLng, float zoom,String title){
        Log.d(TAG,"moveCamer: moving the camera to: lat:"+latLng.latitude+" long:"+latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));

        if(title != "My location"){

            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            mMap.addMarker(options);
        }

        hideSoftKeyboard();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length>0){
                    for(int i=0;i< grantResults.length;i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionGranted = true;
                    initMap();
                }
            }
        }
    }

private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private AdapterView.OnItemClickListener mAutoCompleteClickListener  = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            hideSoftKeyboard();
            final AutocompletePrediction item = mPlaceAutocompleteAdapter.getItem(i);
            final String placeId = item.getPlaceId();

            Task<PlaceBufferResponse> placeResult = mGoogleApiClient.getPlaceById(placeId);
            placeResult.addOnCompleteListener(mUpdatePlaceDetailsCallback);

        }
    };

    private OnCompleteListener<PlaceBufferResponse> mUpdatePlaceDetailsCallback
            = new OnCompleteListener<PlaceBufferResponse>() {
        @Override
        public void onComplete(Task<PlaceBufferResponse> task) {
            try {
                PlaceBufferResponse places = task.getResult();

                // Get the Place object from the buffer.
                final Place place = places.get(0);

                try{
                    mPlace = new PlaceInfo() ;
                    mPlace.setAddress(place.getAddress().toString());
                    mPlace.setName(place.getName().toString());
                    mPlace.setPhonenumber(place.getPhoneNumber().toString());
                    mPlace.setLatlng(place.getLatLng());
                    mPlace.setRating(place.getRating());
                    mPlace.setWebSite(place.getWebsiteUri());
                    mPlace.setId(place.getId());

                }catch (NullPointerException ex){
                    Log.e(TAG,"onComplete: NullPointerException " + ex.getMessage());
                }

                moveCamera(new LatLng(place.getViewport().getCenter().latitude,
                        place.getViewport().getCenter().longitude),DEFAULT_ZOOM,mPlace);
                places.release();
            } catch (RuntimeRemoteException e) {

                Log.e(TAG, "Place query did not complete.", e);

                return;
            }
        }
    };

    private void showDirections(LatLng origin,LatLng destination, String travelMode){
        String url = getDirectionsUrl(origin,destination,travelMode);
      //  new FetchURL(MapActivityNew.this).execute(url,"driving");
    }

    private String getDirectionsUrl(LatLng origin,LatLng destination, String travelMode ){

        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + destination.latitude + "," + destination.longitude ;
        String mode = "mode="+travelMode;
        String params  = str_origin+"&"+str_dest+"&"+mode;
        String output = "json";

        String url =  "https://maps.googleapis.com/maps/api/directions/"+output+"?"+params+"&key=";
        //+ BuildConfig.ApiKey;

        return url;
    }

    @Override
    public void onBackPressed() {
//        if(mAuth.getCurrentUser() == null ){
//            Intent newActivityLoad = new Intent(MapActivityNew.this,Home.class);
//            startActivity(newActivityLoad);
//        }
//        else {
//            Intent startMain = new Intent(Intent.ACTION_MAIN);
//            startMain.addCategory(Intent.CATEGORY_HOME);
//            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(startMain);
//        }


    }

    /**
     * Adapter that handles Autocomplete requests from the Places Geo Data Client.
     * {@link AutocompletePrediction} results from the API are frozen and stored directly in this
     * adapter. (See {@link AutocompletePrediction#freeze()}.)
     */
    public static class PlaceAutocompleteAdapter
            extends ArrayAdapter<AutocompletePrediction> implements Filterable {

        private static final String TAG = "PlaceAutocompleteAdapter";
        private static final CharacterStyle STYLE_BOLD = new StyleSpan(Typeface.BOLD);
        /**
         * Current results returned by this adapter.
         */
        private ArrayList<AutocompletePrediction> mResultList;

        /**
         * Handles autocomplete requests.
         */
        private GeoDataClient mGeoDataClient;

        /**
         * The bounds used for Places Geo Data autocomplete API requests.
         */
        private LatLngBounds mBounds;

        /**
         * The autocomplete filter used to restrict queries to a specific set of place types.
         */
        private AutocompleteFilter mPlaceFilter;

        /**
         * Initializes with a resource for text rows and autocomplete query bounds.
         *
         * @see ArrayAdapter#ArrayAdapter(android.content.Context, int)
         */
        public PlaceAutocompleteAdapter(Context context, GeoDataClient geoDataClient,
                                        LatLngBounds bounds, AutocompleteFilter filter) {
            super(context, android.R.layout.simple_expandable_list_item_2, android.R.id.text1);
            mGeoDataClient = geoDataClient;
            mBounds = bounds;
            mPlaceFilter = filter;
        }

        /**
         * Sets the bounds for all subsequent queries.
         */
        public void setBounds(LatLngBounds bounds) {
            mBounds = bounds;
        }

        /**
         * Returns the number of results received in the last autocomplete query.
         */
        @Override
        public int getCount() {
            return mResultList.size();
        }

        /**
         * Returns an item from the last autocomplete query.
         */
        @Override
        public AutocompletePrediction getItem(int position) {
            return mResultList.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = super.getView(position, convertView, parent);

            // Sets the primary and secondary text for a row.
            // Note that getPrimaryText() and getSecondaryText() return a CharSequence that may contain
            // styling based on the given CharacterStyle.

            AutocompletePrediction item = getItem(position);

            TextView textView1 = (TextView) row.findViewById(android.R.id.text1);
            TextView textView2 = (TextView) row.findViewById(android.R.id.text2);
            textView1.setText(item.getPrimaryText(STYLE_BOLD));
            textView2.setText(item.getSecondaryText(STYLE_BOLD));

            return row;
        }

        /**
         * Returns the filter for the current set of autocomplete results.
         */
        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();

                    // We need a separate list to store the results, since
                    // this is run asynchronously.
                    ArrayList<AutocompletePrediction> filterData = new ArrayList<>();

                    // Skip the autocomplete query if no constraints are given.
                    if (constraint != null) {
                        // Query the autocomplete API for the (constraint) search string.
                        filterData = getAutocomplete(constraint);
                    }

                    results.values = filterData;
                    if (filterData != null) {
                        results.count = filterData.size();
                    } else {
                        results.count = 0;
                    }

                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {

                    if (results != null && results.count > 0) {
                        // The API returned at least one result, update the data.
                        mResultList = (ArrayList<AutocompletePrediction>) results.values;
                        notifyDataSetChanged();
                    } else {
                        // The API did not return any results, invalidate the data set.
                        notifyDataSetInvalidated();
                    }
                }

                @Override
                public CharSequence convertResultToString(Object resultValue) {
                    // Override this method to display a readable result in the AutocompleteTextView
                    // when clicked.
                    if (resultValue instanceof AutocompletePrediction) {
                        return ((AutocompletePrediction) resultValue).getFullText(null);
                    } else {
                        return super.convertResultToString(resultValue);
                    }
                }
            };
        }

        /**
         * Submits an autocomplete query to the Places Geo Data Autocomplete API.
         * Results are returned as frozen AutocompletePrediction objects, ready to be cached.
         * Returns an empty list if no results were found.
         * Returns null if the API client is not available or the query did not complete
         * successfully.
         * This method MUST be called off the main UI thread, as it will block until data is returned
         * from the API, which may include a network request.
         *
         * @param constraint Autocomplete query string
         * @return Results from the autocomplete API or null if the query was not successful.
         * @see GeoDataClient#getAutocompletePredictions(String, LatLngBounds, AutocompleteFilter)
         * @see AutocompletePrediction#freeze()
         */
        @SuppressLint("LongLogTag")
        private ArrayList<AutocompletePrediction> getAutocomplete(CharSequence constraint) {
            Log.i(TAG, "Starting autocomplete query for: " + constraint);

            // Submit the query to the autocomplete API and retrieve a PendingResult that will
            // contain the results when the query completes.
            Task<AutocompletePredictionBufferResponse> results =
                    mGeoDataClient.getAutocompletePredictions(constraint.toString(), mBounds,
                            mPlaceFilter);

            // This method should have been called off the main UI thread. Block and wait for at most
            // 60s for a result from the API.
            try {
                Tasks.await(results, 60, TimeUnit.SECONDS);
            } catch (ExecutionException | InterruptedException | TimeoutException e) {
                e.printStackTrace();
            }

            try {
                AutocompletePredictionBufferResponse autocompletePredictions = results.getResult();

                Log.i(TAG, "Query completed. Received " + autocompletePredictions.getCount()
                        + " predictions.");

                // Freeze the results immutable representation that can be stored safely.
                return DataBufferUtils.freezeAndClose(autocompletePredictions);
            } catch (RuntimeExecutionException e) {
                // If the query did not complete successfully return null
                Toast.makeText(getContext(), "Error contacting API: " + e.toString(),
                        Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error getting autocomplete prediction API call", e);
                return null;
            }
        }
    }
}