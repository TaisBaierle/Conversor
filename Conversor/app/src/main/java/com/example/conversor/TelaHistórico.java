package com.example.conversor;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class TelaHistórico extends AppCompatActivity {

    TextView result;
    BancoDados db;
    Button gerar;
    Spinner spinner;
    String moeda;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico);


        gerar = findViewById(R.id.gerar);
        result = findViewById(R.id.result);
        spinner = findViewById(R.id.spinner_moeda);

        result.setMovementMethod(new ScrollingMovementMethod());

        db = new BancoDados(getBaseContext());


        String moedas[] = {"BRL - REAL", "USD - DÓLAR", "EUR - EURO", "GBP - LIBRA"};
        ArrayAdapter arrayAdapter = new ArrayAdapter(getBaseContext(), android.R.layout.simple_spinner_item, moedas);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                String moeda = item.substring(0, 3);
                setMoeda(moeda);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }
    public String getMoeda() {
        return moeda;
    }

    public void setMoeda(String moeda1) {
        this.moeda = moeda1;
    }

    public void gerar(View v){
        result.setText("");

        List<Cotacao> cotacoes = db.consulta(getMoeda());

        for (int i = 0; i < cotacoes.size(); i++) {

            result.append("Cotação: "+cotacoes.get(i).getValor()+ "\n" + "Data: " + cotacoes.get(i).getData() + "\n\n");

        }
        Toast.makeText(getBaseContext(),"HISTORICO GERADO COM SUCESSO!!",Toast.LENGTH_LONG).show();

    }

    public void voltar(View v){
        Intent intent = new Intent(getBaseContext(),MainActivity.class);
        startActivity(intent);
    }

}

