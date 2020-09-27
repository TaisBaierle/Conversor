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
    public static final Integer VERSAO_BANCO = 46;
    private SQLiteDatabase db;
    private BancoDadosHelper helper;

    public BancoDados(Context context) {
        this.context = context;
        helper = new BancoDadosHelper();
        db = helper.getWritableDatabase();
    }

    public static class TabelaCotacao implements BaseColumns {
        public static final String NOME_TABELA = "cotacao";
        public static final String COLUNA_MOEDA = "moeda";
        public static final String COLUNA_VALOR = "valor";
        public static final String COLUNA_DATA = "data";

        public static String getSQL() {

            String sql = "CREATE TABLE " + NOME_TABELA + "(" +
                    _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUNA_MOEDA + " TEXT, " +
                    COLUNA_VALOR + " REAL, " +
                    COLUNA_DATA + " TEXT)";
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


    public Cotacao maxData() {
        StringBuilder select = new StringBuilder();
        select.append("SELECT * FROM " + TabelaCotacao.NOME_TABELA + " ORDER BY " + TabelaCotacao.COLUNA_DATA + " DESC LIMIT 1");
        Cotacao cot;
        cot = new Cotacao();
        Cursor cursor = db.rawQuery(select.toString(), null);
        try {
            if ( cursor.getCount() != 0 ) {
                cursor.moveToFirst();
                cot.setMoeda(cursor.getString(cursor.getColumnIndex(TabelaCotacao.COLUNA_MOEDA)));
                cot.setValor(cursor.getDouble(cursor.getColumnIndex(TabelaCotacao.COLUNA_VALOR)));
                cot.setData(cursor.getString(cursor.getColumnIndex(TabelaCotacao.COLUNA_DATA)));

            }
            return cot;
        } catch (Exception e) {
            return null;
        }
    }

    public long insertCotacao(Cotacao c) {
        ContentValues valores = new ContentValues();
        valores.put(TabelaCotacao.COLUNA_MOEDA, c.getMoeda());
        valores.put(TabelaCotacao.COLUNA_VALOR, c.getValor());
        valores.put(TabelaCotacao.COLUNA_DATA, c.getData());
        return db.insert(TabelaCotacao.NOME_TABELA, null, valores);

    }

    public boolean verificaDuplicatas(Cotacao c) {
        String sql = "SELECT moeda, valor, data FROM cotacao WHERE moeda = " + "'" + c.getMoeda() + "' AND valor = '" + c.getValor().toString()
                + "' AND data = " + "'" + c.getData() + "'";
        Cursor cursor = db.rawQuery(sql, null);
        if ( cursor.getCount() != 0 ) {
            return true;
        } else {
            return false;

        }

    }

    public ArrayList<Cotacao> consulta(String m) {
        String sql = "SELECT _id, moeda, valor, data FROM cotacao WHERE moeda = '" + m + "' ORDER BY data";
        Cursor c = db.rawQuery(sql, null);
        ArrayList<Cotacao> cotacoes = new ArrayList<>();
        Cotacao cot;
        while (c.moveToNext()) {
            cot = new Cotacao();
            cot.setId(c.getLong(c.getColumnIndex(TabelaCotacao._ID)));
            cot.setMoeda(c.getString(c.getColumnIndex(TabelaCotacao.COLUNA_MOEDA)));
            cot.setValor(c.getDouble(c.getColumnIndex(TabelaCotacao.COLUNA_VALOR)));
            cot.setData(c.getString(c.getColumnIndex(TabelaCotacao.COLUNA_DATA)));
            cotacoes.add(cot);
        }
        return cotacoes;

    }

    public Cotacao moedaDia(String moeda, String date) {
        Cursor c = db.rawQuery("SELECT * FROM " + TabelaCotacao.NOME_TABELA + " WHERE " + TabelaCotacao.COLUNA_MOEDA + " = " + "'" + moeda + "'" + " AND "
                + TabelaCotacao.COLUNA_DATA + " = " + "'" + date + "'", null);

        Cotacao cot = new Cotacao();
        if ( c.getCount() != 0 ) {
            c.moveToFirst();
            cot.setId(c.getLong(0));
            cot.setMoeda(c.getString(1));
            cot.setValor(c.getDouble(2));
            cot.setData(c.getString(3));
        }
        return cot;

    }
}
