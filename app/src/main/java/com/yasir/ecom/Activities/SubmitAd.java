package com.yasir.ecom.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.yasir.ecom.AdObserver;
import com.yasir.ecom.Adapter.SelectedImagesAdapter;
import com.yasir.ecom.Category.MainCategory;
import com.yasir.ecom.Classes.GifSizeFilter;
import com.yasir.ecom.Model.AdDetails;
import com.yasir.ecom.Model.PicturesModel;
import com.yasir.ecom.Model.SelectedAdImages;
import com.yasir.ecom.Model.User;
import com.yasir.ecom.R;
import com.yasir.ecom.Utils.CommonUtils;
import com.yasir.ecom.Utils.CompressImage;
import com.yasir.ecom.Utils.GetAdAddress;
import com.yasir.ecom.Utils.NotificationAsync;
import com.yasir.ecom.Utils.SharedPrefs;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.filter.Filter;


import java.io.File;


public class SubmitAd extends AppCompatActivity implements AdObserver {
    StorageReference mStorageRef;
    Button pickPicture, submitAd;
    EditText title, price, description;
    DatabaseReference mDatabase;

    ArrayList<String> imageUrl;
    String adId;
    Double longitude = 60.3323097, latitude = 30.0493247;

    SharedPreferences userPref;
    String username, city, phonenumber;

    List<Uri> mSelected;
    ArrayList<SelectedAdImages> selectedAdImages;
    SelectedImagesAdapter adapter;
    AdObserver adObserver;

    ArrayList<String> adCover;
    private static final int REQUEST_CODE_CHOOSE = 23;

    long time;
    EditText usernameField, phoneField, locationField;
    TextView chooseCategoryText;
    public static String mainCategory, childCategory;
    public static Activity fa;
    String adminFcmKey;
    int p1 = 0, p2 = 0;
    CheckBox checkbox;

    @Override
    protected void onPostResume() {
        if (mainCategory == null) {
            chooseCategoryText.setText("Choose mobile brand");
        } else {
            if (childCategory != null) {
//                if (subChild != null) {
//                    chooseCategoryText.setText(mainCategory + " > " + childCategory + " > " + subChild);
//                } else {
                chooseCategoryText.setText(mainCategory + " > " + childCategory);
//                }
            } else {
                chooseCategoryText.setText(mainCategory);
            }
        }
        super.onPostResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_ad);
        SubmitAd.this.setTitle("Submit a free Ad");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        adObserver = SubmitAd.this;

        getPermissions();

        fa = this;
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("admin").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    adminFcmKey = dataSnapshot.getValue(String.class);
                    SharedPrefs.setadminFcmKey(adminFcmKey);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        imageUrl = new ArrayList<String>();
        adCover = new ArrayList<String>();


        pickPicture = (Button) findViewById(R.id.pickpicture);
        submitAd = (Button) findViewById(R.id.submit);

        title = (EditText) findViewById(R.id.title);
        price = (EditText) findViewById(R.id.price);
        description = (EditText) findViewById(R.id.description);

        usernameField = (EditText) findViewById(R.id.username);
        phoneField = (EditText) findViewById(R.id.phone);
        locationField = (EditText) findViewById(R.id.location);
        checkbox = findViewById(R.id.check);

        chooseCategoryText = (TextView) findViewById(R.id.choose_category);


        chooseCategoryText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(SubmitAd.this, MainCategory.class);
                startActivity(i);

            }
        });


        submitAd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (p1 == 1 && p2 == 1) {
                    if (imageUrl.isEmpty()) {
                        CommonUtils.showToast("Please select atleast one image");
                    } else if (mainCategory == null) {
                        Toast.makeText(SubmitAd.this, "Please choose a category!", Toast.LENGTH_SHORT).show();

                    } else if (title.getText().toString().length() == 0) {
                        title.setError("Cannot be null");

                    } else if (price.getText().toString().length() == 0) {
                        price.setError("Cannot be null");
                    } else if (description.getText().toString().length() == 0) {
                        description.setError("Cannot be null");
                    } else if (!checkbox.isChecked()) {
                        checkbox.setError("");
                        CommonUtils.showToast("Please accept the terms and conditions");

                    } else {
                        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

                        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
//                            CommonUtils.showToast("disabled");
                            final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }else{
                            Intent intent = new Intent(SubmitAd.this, GPSTrackerActivity.class);
                            startActivityForResult(intent, 1);
                        }

                    }
//                submitAd();
                } else {
                    getPermissions();
                }
            }
        });
        time = System.currentTimeMillis();

        username = SharedPrefs.getUsername();


        mDatabase.child("users").child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    User user = dataSnapshot.getValue(User.class);
                    usernameField.setText(user.getName());
                    phoneField.setText("" + user.getPhone());
                    locationField.setText(user.getCity());
                    username = user.getUsername();
                    phonenumber = user.getPhone();
                    city = user.getCity();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        showPickedPictures();

        pickPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (p1 == 1 && p2 == 1) {
                    selectedAdImages.clear();
                    initMatisse();

                } else {
                    getPermissions();
                }
            }
        });

    }

    private void initMatisse() {
        Matisse.from(SubmitAd.this)
                .choose(MimeType.allOf())
                .countable(true)
                .maxSelectable(8)
                .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .imageEngine(new GlideEngine())
                .forResult(REQUEST_CODE_CHOOSE);
    }

    private void getPermissions() {
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,

        };

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
//        CommonUtils.showToast(PERMISSION_ALL+"");
    }

    private void showPickedPictures() {
        selectedAdImages = new ArrayList<>();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.horizontal_recycler_view);
        LinearLayoutManager horizontalLayoutManagaer
                = new LinearLayoutManager(SubmitAd.this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(horizontalLayoutManagaer);
        adapter = new SelectedImagesAdapter(SubmitAd.this, selectedAdImages);
        recyclerView.setAdapter(adapter);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        // Check which request we're responding to
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            mSelected = Matisse.obtainResult(data);

            adapter.notifyDataSetChanged();
            for (Uri img :
                    mSelected) {
                selectedAdImages.add(new SelectedAdImages("" + img));
                adapter.notifyDataSetChanged();
                CompressImage compressImage = new CompressImage(SubmitAd.this);
                imageUrl.add(compressImage.compressImage("" + img));
            }
        }
        if (requestCode == 1) {
            if (data != null) {
                Bundle extras = data.getExtras();
                longitude = extras.getDouble("Longitude");
                latitude = extras.getDouble("Latitude");
//            Toast.makeText(fa, longitude+"   "+latitude, Toast.LENGTH_SHORT).show();

                submitAd(latitude,longitude);
            }

        }

        super.onActivityResult(requestCode, resultCode, data);

    }


    void submitAd(Double lat,Double lon) {
        adId = mDatabase.child("ads").push().getKey();

        String ci = GetAdAddress.getCity(SubmitAd.this, lat, lon);
        SharedPrefs.setUserCity(ci);
        mDatabase.child("users").child(username).child("city").setValue(ci);

        String Adtitle = title.getText().toString(),
                AdDescription = description.getText().toString();
        long AdPrice = Long.parseLong(price.getText().toString());

        String username = SharedPrefs.getUsername();
        mDatabase.child("users").child(username).child("adsPosted").child("" + adId).setValue("" + adId);


        mDatabase.child("ads").child("" + adId).setValue(new AdDetails(adId, Adtitle, AdDescription, username, "" + phonenumber, ci, ""
                , "Active", mainCategory, childCategory,
                time, AdPrice, 0, latitude, longitude)).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SubmitAd.this, "Error" + e, Toast.LENGTH_SHORT).show();
            }
        });

        int count = 0;
        for (String img : imageUrl) {

            putPictures(img, "" + adId, count);
            count++;
            adObserver.onUploaded(count, imageUrl.size());

        }

        NotificationAsync notificationAsync = new NotificationAsync(SubmitAd.this);
        String NotificationTitle = "New Ad by " + username + " from " + city;
        String NotificationMessage = "Title: " + Adtitle + "   Price: " + AdPrice;
        notificationAsync.execute("ali", adminFcmKey, NotificationTitle, NotificationMessage);


        mainCategory = null;
//        subChild = null;
        childCategory = null;
//        finish();


    }

    public void putPictures(String path, final String key, final int count) {
        String imgName = Long.toHexString(Double.doubleToLongBits(Math.random()));

        ;
        Uri file = Uri.fromFile(new File(path));


        StorageReference riversRef = mStorageRef.child("Photos").child(imgName);

        riversRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    @SuppressWarnings("VisibleForTests")
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content

                        Uri downloadUrl = taskSnapshot.getDownloadUrl();

                        mDatabase.child("ads").child("" + adId).child("pictures")
                                .push()
                                .setValue(new PicturesModel("" + downloadUrl, count));

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                        Toast.makeText(SubmitAd.this, "error", Toast.LENGTH_SHORT).show();
                    }
                });


    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onUploaded(int count, int arraySize) {
//        CommonUtils.showToast(count+"     "+arraySize);
        if (count == arraySize) {
            Intent i = new Intent(SubmitAd.this, SuccessPage.class);
            startActivity(i);
            finish();
        }


    }

    public boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                } else {
                    p1 = 1;
                    p2 = 1;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("permissions", "" + permissions[0]);
        for (int a = 0; a < permissions.length; a++) {
        }
        if (permissions[0].equalsIgnoreCase("android.permission.ACCESS_FINE_LOCATION") && grantResults[0] == 0) {
//
            p1 = 1;

        } else {

            takeToSettings(permissions, 0);

        }
        if (permissions[1].equalsIgnoreCase("android.permission.WRITE_EXTERNAL_STORAGE") && grantResults[1] == 0) {
            p2 = 1;

        } else {

            takeToSettings(permissions, 1);
        }

    }

    private void takeToSettings(String[] permissions, int a) {
        boolean isgranted = true;

//            getPermissions();
        boolean showRationale = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            showRationale = shouldShowRequestPermissionRationale(permissions[a]);
        }
        if (!showRationale) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivityForResult(intent, 1);
            isgranted = false;
        }
//        CommonUtils.showToast("This app needs to access Gallery");
    }
}

