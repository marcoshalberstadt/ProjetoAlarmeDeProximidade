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
import android.widget.TextView;
import android.widget.Toast;

import com.example.marcoshalberstadt.projetoalarmedeproximidade.MapsActivity;
import com.example.marcoshalberstadt.projetoalarmedeproximidade.R;
import com.example.marcoshalberstadt.projetoalarmedeproximidade.DatabaseHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private DatabaseHelper helper;
    private EditText distance, longitude, latitude;
    private SensorManager sensorManager;

    private ListView list;

    List<Map<String, Object>> local;

    String[] de = {"id", "distance", "longitude", "latitude"};
    int[] para = {R.id.id, R.id.distanceEditText, R.id.longitudeEditText, R.id.latitudeEditText};

    protected LocationManager locationManager;
    TextView txt_loc;


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

        txt_loc = findViewById(R.id.locationTextView);
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
        txt_loc = findViewById(R.id.locationTextView);
        txt_loc.setText("Latitude:" + location.getLatitude() + ", Longitude:" + location.getLongitude());
        buscar(null);
    }
    @Override
    public void onLocationChanged(Location location) {
        txt_loc.setText("Latitude:" + location.getLatitude() + ", Longitude:" + location.getLongitude());
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
        startActivity(i);
    }

    public void salvarLocal(View v){
        if(!distance.getText().toString().equals("")
                && !longitude.getText().toString().equals("")
                && !latitude.getText().toString().equals("")) {
            SQLiteDatabase db = helper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("distance", Double.parseDouble(distance.getText().toString()));
            values.put("longitude", Double.parseDouble(longitude.getText().toString()));
            values.put("latitude", Double.parseDouble(latitude.getText().toString()));

            long result = db.insert("local", null, values);
            if (result != -1) {
                Toast.makeText(this, "Registro salvo com sucesso!", Toast.LENGTH_SHORT).show();
                distance.setText("");
                longitude.setText("");
                latitude.setText("");
                buscar(null);
            } else {
                Toast.makeText(this, "Erro ao salvar!", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, "Campos não podem estar vazios!", Toast.LENGTH_SHORT).show();
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
            double distance = cursor.getDouble(1);
            double longitude = cursor.getDouble(2);
            double latitude = cursor.getDouble(3);
            item.put("id", id);
            item.put("distance", distance);
            item.put("longitude", longitude);
            item.put("latitude",  latitude);
            local.add(item);
            cursor.moveToNext();
        }
        cursor.close();
        return local;
    }
}
