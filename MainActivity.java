package com.example.marcoshalberstadt.projetoalarmedeproximidade;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper helper;
    private EditText distancia, longitude, latitude;

    private ListView list;

    List<Map<String, Object>> local;

    String[] de = {"id", "distancia", "longitude", "latitude"};
    int[] para = {R.id.id, R.id.distancia, R.id.longitude, R.id.latitude};


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

        distancia = findViewById(R.id.ET_distancia);
        longitude = findViewById(R.id.ET_Longitude);
        latitude = findViewById(R.id.ET_Latitude);

        list = (ListView) findViewById(R.id.list);

        helper = new DatabaseHelper(this);
        list.setClickable(true);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3){
                String idDados = String.valueOf(local.get(position).get("id"));
                SQLiteDatabase db = helper.getWritableDatabase();
                String where [] = new String[] {idDados};

                long  result = db.delete("local", "id = ?", where);
                if (result != -1) {
                    Toast.makeText(MainActivity.this, "Registro " + idDados + " excluído com sucesso!", Toast.LENGTH_SHORT).show();
                    buscar(null);
                } else {
                    Toast.makeText(MainActivity.this, "Erro ao excluir!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        buscar(null);
    }
    public void SelecionarLocal(View v){
        Intent i = new Intent(this,MapsActivity.class);
        startActivity(i);
    }

    public void salvarLocal(View v){
        if(!distancia.getText().toString().equals("")
                && !longitude.getText().toString().equals("")
                && !latitude.getText().toString().equals("")) {
            SQLiteDatabase db = helper.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("distancia", Double.parseDouble(distancia.getText().toString()));
            values.put("longitude", Double.parseDouble(longitude.getText().toString()));
            values.put("latitude", Double.parseDouble(latitude.getText().toString()));

            long result = db.insert("local", null, values);
            if (result != -1) {
                Toast.makeText(this, "Registro salvo com sucesso!", Toast.LENGTH_SHORT).show();
                distancia.setText("");
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
            double distancia = cursor.getDouble(1);
            double longitude = cursor.getDouble(2);
            double latitude = cursor.getDouble(3);
            item.put("id", id);
            item.put("distancia", distancia);
            item.put("longitude", longitude);
            item.put("latitude",  latitude);
            local.add(item);
            cursor.moveToNext();
        }
        cursor.close();
        return local;
    }
}
