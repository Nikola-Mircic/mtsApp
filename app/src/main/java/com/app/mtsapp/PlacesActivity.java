package com.app.mtsapp;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.app.mtsapp.location.LocationFinder;
import com.app.mtsapp.location.LocationSystem;
import com.app.mtsapp.location.SavedLocation;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.List;

public class PlacesActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowLongClickListener {

    private final int locationRequestCode = 100; //Код за захтевање дозволе коришћења локације уређаја
    private final String[] locationRequests = new String[]{"Manifest.permission.ACCESS_FINE_LOCATION", "Manifest.permission.ACCESS_COARSE_LOCATION"};
    private final int gpsRequestCode = 200; //Код за паљење GPS локације уређаја

    private GoogleMap googleMap;

    private Location currentLocation; //Тренутна локација уређаја
    private String currentLocationName; //Име које се чува на маркеру при чувању локације
    private String unnamedLocationName; //Име које се додели локацији када корисник не унесе ниједно

    //Toast упозорења
    private String noLocationNameToast;
    private String deniedLocationPermsToast;
    private String retryCurrentLocationToast;
    private String gpsToast;

    private List<SavedLocation> savedLocations;
    private LocationSystem locationSystem;
    private LocationFinder locationFinder;

    private boolean settingHomeLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageManager languageManager = new LanguageManager(PlacesActivity.this);
        languageManager.checkLocale();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);

        unnamedLocationName = this.getResources().getString(R.string.unnamedLocation);
        noLocationNameToast = this.getResources().getString(R.string.noLocationNameToast);
        deniedLocationPermsToast = this.getResources().getString(R.string.deniedLocationPermissionsToast);
        retryCurrentLocationToast = this.getResources().getString(R.string.retryCurrentLocationToast);
        gpsToast = this.getResources().getString(R.string.gpsToast);

        //Ако апликација има дозволе потребне за узимање локације
        if (hasLocationPermission()) {

            //Узми supportMapFragment и обавести програм кад је мапа спремна
            SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            if (supportMapFragment != null) {
                supportMapFragment.getMapAsync(this);
            }

            //Учитај сачуване локације и референцирај листу сачуваних локација
            locationSystem = new LocationSystem(this);

            locationFinder = new LocationFinder(this);
            locationFinder.run();

            ImageButton addMarkerButton = findViewById(R.id.addMarker);
            addMarkerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gpsAddLocation(); //Упали GPS локацију на уређају и ако је могуће маркира тренутну локацију на мапи
                }
            });

            ImageButton helpButton = findViewById(R.id.helpButton);
            helpButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InfoPopup infoPopup = new InfoPopup(PlacesActivity.this, false, false, true);
                    infoPopup.showDialog();
                }
            });


        } else {
            //Ако нема, затражи их
            requestLocationPermissions();
        }
    }

    @Override
    public void onMapReady(GoogleMap gMap) {
        googleMap = gMap;

        loadMarkersFromSavedLocations(); //Прикажи маркере свих сачуваних локација

        getAndZoomCurrentLocation();
        //Ако није детектована тренутна локација, зумирај на кућну лоакцију или ако она не постоји на прву сачувану локацију
        if (currentLocation == null) {
            //Ако постоји бар 1 сачувана локација
            if (savedLocations.size() > 0) {
                LatLng zoomLatLng;
                SavedLocation homeLocation = locationSystem.getLocation("home");
                if (homeLocation != null) {
                    zoomLatLng = new LatLng(homeLocation.getLatitude(), homeLocation.getLongitude()); //Координате сачуване кућне локације
                } else {
                    zoomLatLng = new LatLng(savedLocations.get(0).getLatitude(), savedLocations.get(0).getLongitude()); //Координате прве сачуване локације
                }

                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(zoomLatLng, 17)); //Зумирај мапу на изабрану локацијуу
            }
        }

        ImageButton currentLocationButton = findViewById(R.id.currentLocationButton);
        currentLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAndZoomCurrentLocation();
            }
        });


        //Зове се када се неки маркер помера
        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker arg0) {
            }

            //Кад је завршено померање маркера
            @Override
            public void onMarkerDragEnd(Marker marker) {
                try {
                    Log.d("System out", "onMarkerDragEnd..." + marker.getPosition().latitude + "..." + marker.getPosition().longitude);
                    System.out.println("[МРМИ]: Стара локација: " + locationSystem.getLocation(marker.getTitle()).getLatitude() + ", " + locationSystem.getLocation(marker.getTitle()).getLongitude());

                    //Промени координате локације која је сачувана у локцаионом систему користећи нове координате маркера
                    locationSystem.getLocation(marker.getTitle()).setLatitude(marker.getPosition().latitude);
                    locationSystem.getLocation(marker.getTitle()).setLongitude(marker.getPosition().longitude);

                    System.out.println("[МРМИ]: Померам локацију имена " + marker.getTitle());
                    System.out.println("[МРМИ]: Нова локација: " + locationSystem.getLocation(marker.getTitle()).getLatitude() + ", " + locationSystem.getLocation(marker.getTitle()).getLongitude());

                    locationSystem.saveLocations(); //Сачувај локације у систему
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onMarkerDrag(Marker arg0) {
            }
        });

        //Избриши маркер кад се дуго стисне његов назив
        googleMap.setOnInfoWindowLongClickListener(this);
    }

    //Избриши маркер и локацију из система кад корисник дуго држи прст на називу маркера
    @Override
    public void onInfoWindowLongClick(Marker marker) {
        marker.remove();
        locationSystem.removeLocation(marker.getTitle());
        locationSystem.saveLocations();
    }

    //Питај корисника да ли жели да изађе са тренутног екрана када притисне дугме за враћање назад
    @Override
    public void onBackPressed() {
        //Прикажи упозорење
        InfoPopup infoPopup = new InfoPopup(PlacesActivity.this, false, false, false);
        infoPopup.showDialog();
    }

    //=================== ПРОВЕРАВАЊЕ И ДОБИЈАЊЕ ДОЗВОЛА ЗА ЛОКАЦИЈУ И ПАЉЕЊЕ ЛОКАЦИЈЕ НА УРЕЂАЈУ =============================

    //Проверава да ли апликација има дозволе потребне за добијање локације
    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == (PackageManager.PERMISSION_GRANTED)
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == (PackageManager.PERMISSION_GRANTED);
    }

    //Захтева дозволе потребне за добијање локације
    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(this, locationRequests, locationRequestCode);
    }

    //Обрађује захтеве за дозволе
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //Ако су у питању локације
        if (requestCode == locationRequestCode) {
            if (grantResults.length > 0) {
                //Ако су дозвољене
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    //Обавести корисника да је одбио захтев за дозволе локације и врати га на главни екран (MainActivity)
                    Toast.makeText(this, deniedLocationPermsToast, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MainActivity.class));
                }
            }
        }
    }

    /*Проверава да ли је GPS локација уређаја упаљења. Ако јесте, дода маркер на тренутној локацији а ако није затражи од корисника да дозволи
    апликацији да користи GPS локацију уређаја*/
    private void gpsAddLocation() {
        //Направи захтев за проверу и добијање GPS локације уређаја
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(5000).setFastestInterval(2000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext()).checkLocationSettings(builder.build());
        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class); //Response се прави да би се проверило да ли је GPS локација већ упаљења
                    getCurrentLocation();

                    //Ако је GPS локација већ упаљења неће се throw-ати било какав exception
                    System.out.println("[MRMI]: GPS локација је већ упаљена, додајем маркер");
                    if (currentLocation != null) {
                        showLocationNamePopup();
                    } else {
                        Toast.makeText(PlacesActivity.this, retryCurrentLocationToast, Toast.LENGTH_SHORT).show();
                    }

                } catch (ApiException apiE) {
                    switch (apiE.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            //Упали мени који пита корисника да ли апликација сме да упали GPS локацију уређаја
                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) apiE;
                                resolvableApiException.startResolutionForResult(PlacesActivity.this, gpsRequestCode);
                            } catch (IntentSender.SendIntentException e) {
                                e.printStackTrace();
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            System.out.println("[MRMI]: SETTINGS_CHANGE_UNAVAILABLE");
                            break;
                    }
                }
            }
        });
    }

    //Прикаже прозор за унос имена локације
    private void showLocationNamePopup() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.add_location_popup);

        final EditText locationNameInput = dialog.findViewById(R.id.locationNameInput); //Текст за унос имена локације
        final SwitchCompat isHomeLocationSwitch = dialog.findViewById(R.id.isHomeLocationSwitch); //Дугме које одређује да ли је локација која се чува кућна или не

        //Дугме за отказивање додавања локације
        ImageButton cancelButton = dialog.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        //Дугме за чување изабране локације
        ImageButton yesButton = dialog.findViewById(R.id.yesButton);
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentLocationName = locationNameInput.getText().toString();

                //Стави назив на ? ако корисник није унео назив
                if (currentLocationName.equals("")) {
                    currentLocationName = unnamedLocationName;
                    Toast.makeText(PlacesActivity.this, noLocationNameToast, Toast.LENGTH_SHORT).show();
                } else {
                    settingHomeLocation = isHomeLocationSwitch.isChecked(); //Провери да ли је локација која се уноси кућна
                    addMarker(); //Додај маркер нове унете локације

                    dialog.dismiss();
                }
            }
        });

        dialog.show();
    }

    //Обрађује одлуке корисника при дијалогу за коришћење GPS лоакције уређаја
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == gpsRequestCode) {
            //Ако је корисник одбио коришћење GPS локације обавести га о неопходности коришћења њих за додавање тренутне локације
            if (resultCode != Activity.RESULT_OK) {
                Toast.makeText(this, gpsToast, Toast.LENGTH_LONG).show();
            } else {
                //Ако је пристао мораће да сачека пар секунди да се GPS повеже са уређајем и да поново стисне дугме за додавање тренутне локације
                System.out.println("[MRMI]: Упаљена GPS локациај уређаја");
            }
        }
    }


    //=================== ДОДАВАЊЕ МАРКЕРА И ДОБИЈАЊЕ ТРЕНУТНЕ ЛОКАЦИЈЕ =============================

    private void getCurrentLocation() {
        currentLocation = locationFinder.getCurrentLocation();
    }

    //Дода маркер на тренутној лоакцији уређаја
    private void addMarker() {
        //Ако су пронађене тренутне координате уређаја
        if (currentLocation != null) {
            LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()); //Узми тренутне координате
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17)); //Зумирај мапу на тренутну локацију
            MarkerOptions markerOptions = new MarkerOptions().position(currentLatLng).title(currentLocationName).draggable(true); //Постави подешавања маркера

            //Ако се мења кућна лоакција
            if (settingHomeLocation) {
                //Ако већ постоји кућна локација сачувана у систему
                if (locationSystem.getLocation("home") != null) {
                    //Избриши стару кућну локацију и поново прикажи маркере
                    locationSystem.removeLocation("home");
                    locationSystem.saveLocations();
                    googleMap.clear();
                    loadMarkersFromSavedLocations();
                }

                //Постави посебну иконицу кућног маркера и назови маркер "home" у систему локација ради лакшег проналажења
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("home_location", 50, 50)));
                currentLocationName = "home";
            }
            googleMap.addMarker(markerOptions); //Прикажи маркер одабраних подешавања

            System.out.println("[МРМИ]: Чувам локацију имена " + currentLocationName);
            //Додај и сачувај локацију у систему локација
            locationSystem.addLocation(currentLocationName, currentLocation);
            locationSystem.saveLocations();
        } else {
            System.out.println("[MRMI]: Не постоје тренутне координате уређаја");
        }
    }

    //Прикаже маркере сачуваних локација користећи LocationSystem
    private void loadMarkersFromSavedLocations() {

        locationSystem.loadLocations();
        savedLocations = locationSystem.getLocations();

        System.out.println("[MRMI]: Број сачуваних локација: " + savedLocations.size());

        //Направи фајл са локацијама ако не постоји
        if (savedLocations == null) {
            locationSystem.saveLocations();
            savedLocations = locationSystem.getLocations();
        } else {
            for (SavedLocation savedLocation : savedLocations) {
                LatLng currentLatLng = new LatLng(savedLocation.getLatitude(), savedLocation.getLongitude());
                MarkerOptions markerOptions = new MarkerOptions().position(currentLatLng).title(savedLocation.getName()).draggable(true); //Постави позицију, назив и особине маркера
                if (savedLocation.getName().equals("home")) {
                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("home_location", 50, 50)));
                }
                googleMap.addMarker(markerOptions); //Постави маркер учитане локације на мапу
                System.out.println("[MRMI]: Учитао локацију " + savedLocation.getName() + " на координатама " + savedLocation.getLatitude() + " , " + savedLocation.getLongitude());
            }
        }
    }

    //Преувелича иконицу маркера за локацију
    public Bitmap resizeMapIcons(String iconName, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(iconName, "drawable", getPackageName()));
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false);
    }

    private void getAndZoomCurrentLocation() {
        getCurrentLocation();
        if (currentLocation != null) {
            //Зумирај мапу на тренутну локацију
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 17));
        }
    }
}