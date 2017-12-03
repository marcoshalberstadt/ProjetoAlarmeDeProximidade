package com.example.marcoshalberstadt.projetoalarmedeproximidade;

import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Marcos Halberstadt on 12/2/2017.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String BANCO_DE_DADOS = "local";
    private static int VERSAO = 3;

    public DatabaseHelper (Context context){
        super(context, BANCO_DE_DADOS, null, VERSAO );
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE local (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "distancia DOUBLE, " +
                "longitude DOUBLE, " +
                "latitude DOUBLE);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS local");
    }
}
