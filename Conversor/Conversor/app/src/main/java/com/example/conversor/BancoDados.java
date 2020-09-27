package com.example.conversor;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;

public class BancoDados {
    private Context context;
    public static final String NOME_BANCO = "cotacao.db";
    public static final Integer VERSAO_BANCO = 3;
    private SQLiteDatabase db;
    private BancoDadosHelper helper;

    public BancoDados(Context context) {
        this.context = context;
        helper = new BancoDadosHelper();
        db = helper.getWritableDatabase();
    }

    public static class TabelaCotacao implements BaseColumns {
        public static final String NOME_TABELA = "contacao";
        public static final String COLUNA_MOEDA = "moeda";
        public static final String COLUNA_VALOR = "valor";
        public static final String COLUNA_DATA = "data";

        public static String getSQL() {

            String sql = "CREATE TABLE " + NOME_TABELA + "(" +
                    _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUNA_MOEDA + " TEXT, " +
                    COLUNA_VALOR + " REAL, " +
                    COLUNA_DATA + " TEXT )";
            return sql;
        }
    }

    private class BancoDadosHelper extends SQLiteOpenHelper {

        BancoDadosHelper() {
            super(context, NOME_BANCO, null, VERSAO_BANCO);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(TabelaCotacao.getSQL());

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TabelaCotacao.NOME_TABELA);
            onCreate(db);
        }

        @Override
        public void onConfigure(SQLiteDatabase db) {
            super.onConfigure(db);
            db.setForeignKeyConstraintsEnabled(true);
        }
    }

    public ArrayList<Cotacao> getCotacoes() {
        String colunas[] = {TabelaCotacao._ID, TabelaCotacao.COLUNA_MOEDA, TabelaCotacao.COLUNA_VALOR, TabelaCotacao.COLUNA_DATA};
        Cursor cursor = db.query(TabelaCotacao.NOME_TABELA, colunas, null, null, null, null, TabelaCotacao.COLUNA_DATA);
        ArrayList<Cotacao> list = new ArrayList<>();
        Cotacao c;
        while (cursor.moveToNext()) {
            c = new Cotacao();
            c.setId(cursor.getLong(cursor.getColumnIndex(TabelaCotacao._ID)));
            c.setMoeda(cursor.getString(cursor.getColumnIndex(TabelaCotacao.COLUNA_MOEDA)));
            c.setValor(cursor.getDouble(cursor.getColumnIndex(TabelaCotacao.COLUNA_VALOR)));
            c.setData(cursor.getString(cursor.getColumnIndex(TabelaCotacao.COLUNA_DATA)));
        }
        return list;
    }


    public String maxData() {
        String selectQuery = "select max (data) from contacao";
        Cursor cursor = db.rawQuery(selectQuery,null);
        String date = cursor.getString(cursor.getColumnIndex(TabelaCotacao.COLUNA_DATA));
        return date;
    }
    public long insertCotacao(Cotacao c){
        ContentValues valores = new ContentValues();
        valores.put(TabelaCotacao.COLUNA_MOEDA, c.getMoeda());
        valores.put(TabelaCotacao.COLUNA_VALOR, c.getValor());
        valores.put(TabelaCotacao.COLUNA_DATA, c.getData());
        return db.insert(TabelaCotacao.NOME_TABELA, null, valores);

    }
}



