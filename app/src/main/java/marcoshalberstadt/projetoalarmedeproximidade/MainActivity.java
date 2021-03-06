package marcoshalberstadt.projetoalarmedeproximidade;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;


import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private Vibrator v;
    private DatabaseHelper helper;
    private EditText name, distance, longitude, latitude;
    private SensorManager sensorManager;
    private double currentLat = 0;
    private double currentLong = 0;

    private ListView list;

    List<Map<String, Object>> local;

    String[] de = {"id", "name", "distance", "longitude", "latitude"};
    int[] para = {R.id.idTextView, R.id.nameTextView, R.id.distanceTextView, R.id.longitudeTextView, R.id.latitudeTextView};

    protected LocationManager locationManager;

    /*@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        helper = new DatabaseHelper(this);
        helper.getWritableDatabase().execSQL("DROP TABLE IF EXISTS local");

    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        name = findViewById(R.id.nameEditText);
        distance = findViewById(R.id.distanceEditText);
        longitude = findViewById(R.id.longitudeEditText);
        latitude = findViewById(R.id.latitudeEditText);

        list = findViewById(R.id.list);

        helper = new DatabaseHelper(this);
        list.setClickable(true);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                String idDados = String.valueOf(local.get(position).get("id"));
                SQLiteDatabase db = helper.getWritableDatabase();
                String where[] = new String[]{idDados};

                long result = db.delete("local", "id = ?", where);
                if (result != -1) {
                    Toast.makeText(MainActivity.this, "Registro " + idDados + " excluído com sucesso!", Toast.LENGTH_SHORT).show();
                    buscar(null);
                } else {
                    Toast.makeText(MainActivity.this, "Erro ao excluir!", Toast.LENGTH_SHORT).show();
                }
            }
        });


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this,"Permissão para utilizar sensor negado pelo usuário!",Toast.LENGTH_LONG).show();
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        buscar(null);
    }


    @Override
    public void onLocationChanged(Location location) {
        //Inserir código para calculo da distãncia
        //Toast.makeText(this, location.getLatitude() + "," + location.getLongitude(), Toast.LENGTH_SHORT).show();
        currentLat = location.getLatitude();
        currentLong = location.getLongitude();
        for(Map<String, Object> m: local){

            double distance = (distance(location.getLatitude(), location.getLongitude(), (double) m.get("latitude"), (double) m.get("longitude"))*1000);
            if(distance <= (double) m.get("distance")){

                // Vibrate for 500 milliseconds
                v.vibrate(500);
            }
        }

    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }


    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude","disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude","enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude","status");
    }

    public void SelecionarLocal(View v){
        Intent i = new Intent(this, MapsActivity.class);
        i.putExtra("currentLat" , currentLat);
        i.putExtra("currentLong" , currentLong);
        startActivityForResult(i, 999);
    }

    public void salvarLocal(View v){
        try {
            validateFields();
            SQLiteDatabase db = helper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("name", name.getText().toString());
            values.put("distance", Double.parseDouble(distance.getText().toString()));
            values.put("longitude", Double.parseDouble(longitude.getText().toString()));
            values.put("latitude", Double.parseDouble(latitude.getText().toString()));

            long result = db.insert("local", null, values);
            if (result != -1) {
                Toast.makeText(this, "Registro salvo com sucesso!", Toast.LENGTH_SHORT).show();
                name.setText("");
                distance.setText("");
                longitude.setText("");
                latitude.setText("");
                buscar(null);
            } else {
                Toast.makeText(this, "Erro ao salvar!", Toast.LENGTH_SHORT).show();
            }
        }catch(Exception ex){
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void validateFields() throws Exception{
        if(name.getText().toString().isEmpty()){
           throw new Exception("Nome vazio!");
        }

        if(distance.getText().toString().isEmpty()){
            throw new Exception("Distância vazia!");
        }


        if(longitude.getText().toString().isEmpty()){
            throw new Exception("Longitude vazia!");
        }

        if(latitude.getText().toString().isEmpty()){
            throw new Exception("Latitude vazia!");
        }
    }

    @Override
    protected void onDestroy() {
        helper.close();
        super.onDestroy();
    }

    public void buscar(View v){
        local = listarLocais("SELECT * FROM local");
        SimpleAdapter adapter = new SimpleAdapter(this, local, R.layout.list_line, de, para);
        list.setAdapter(adapter);
    }

    private List<Map<String,Object>> listarLocais(String querry){
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery(querry, null);
        cursor.moveToFirst();
        local = new ArrayList<Map<String, Object>>();
        for(int i = 0; i<cursor.getCount(); i++){
            Map<String, Object> item = new HashMap<String, Object>();
            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            double distance = cursor.getDouble(2);
            double longitude = cursor.getDouble(3);
            double latitude = cursor.getDouble(4);
            item.put("id", id);
            item.put("name", name);
            item.put("distance", distance);
            item.put("longitude", longitude);
            item.put("latitude",  latitude);
            local.add(item);
            cursor.moveToNext();
        }
        cursor.close();
        return local;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

            if (resultCode == RESULT_OK && requestCode == 999) {

                LatLng latlng = (LatLng) data.getExtras().get("latlong");

                Toast.makeText(this, String.valueOf(distance(latlng.latitude, latlng.longitude, currentLat, currentLong)*1000) + " metros", Toast.LENGTH_SHORT).show();

                latitude.setText(String.valueOf(latlng.latitude));
                longitude.setText(String.valueOf(latlng.longitude));
            }
    }
}
