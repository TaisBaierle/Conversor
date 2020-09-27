package com.example.conversor;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    public Button sair, converter, listar, atualizar;
    public BancoDados db;
    public ProgressBar bar;
    public TextView aguarde,covertido;
    public static final int PERMISSAO = 1010;
    public Spinner spinner1, spinner2;
    public EditText valorAserConvertido;
    private String moeda1, moeda2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sair = findViewById(R.id.sair);
        converter = findViewById(R.id.converte);
        covertido = findViewById(R.id.valor_convertido);
        listar = findViewById(R.id.historico);
        atualizar = findViewById(R.id.atualiza);
        bar = findViewById(R.id.progressBar);
        aguarde = findViewById(R.id.text_aguarde);
        spinner1 = findViewById(R.id.spinner_moeda1);
        spinner2 = findViewById(R.id.spinner_moeda2);
        valorAserConvertido = findViewById(R.id.valor);

        db = new BancoDados(getBaseContext());


        String moedas[] = {"BRL - REAL", "USD - DÓLAR", "EUR - EURO", "GBP - LIBRA"};
        ArrayAdapter arrayAdapter = new ArrayAdapter(getBaseContext(), android.R.layout.simple_spinner_item, moedas);
        spinner1.setAdapter(arrayAdapter);
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                String moeda1 = item.substring(0, 3);
                setMoeda1(moeda1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinner2.setAdapter(arrayAdapter);
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                String moeda2 = item.substring(0, 3);
                setMoeda2(moeda2);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public String getMoeda1() {
        return moeda1;
    }

    public void setMoeda1(String moeda1) {
        this.moeda1 = moeda1;
    }

    public String getMoeda2() {
        return moeda2;
    }

    public void setMoeda2(String moeda2) {
        this.moeda2 = moeda2;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.creditos, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if ( item.getItemId() == R.id.credito ) {
            TextView textView = new TextView(this);
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("                  CRÉDITOS");
            textView.setText("Aplicativo de conversão de moedas desenvolvido para a disciplina de programação - 2019/02\nAPI 28 Android Pie 9.0\nAutoria Tais Baierle\n" +
                    "Professor Gabriel da Silva Simões\nUniversidade Feevale\nCiência da Computação");
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            textView.setTextSize(14);
            builder.setView(textView);
            builder.setNeutralButton("Fechar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            AlertDialog alerta = builder.create();
            alerta.show();
        }
        if (item.getItemId() == R.id.data){
            TextView textView = new TextView(this);
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            Cotacao c = db.maxData();
            if (c.getData() != null) {

                textView.setText("\nData da cotação:\n\n"+c.getData());
            }else{
                textView.setText("\n\nDados desatualizados");
            }
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            textView.setTextSize(16);
            builder.setView(textView);
            builder.setNeutralButton("Fechar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            AlertDialog alerta = builder.create();
            alerta.show();
        }
        return super.onOptionsItemSelected(item);

    }

    public Double pegarValor() {
        String valor = valorAserConvertido.getText().toString();
        Double valorDecimal = Double.parseDouble(valor);
        return valorDecimal;
    }

    public void sair(View v) {
        finishAffinity();
    }

    public void telaHistorico(View v) {
        Intent intent = new Intent(getBaseContext(), TelaHistórico.class);
        startActivity(intent);
    }

    public void conversao(View v) {
        try {
            Double valorConverter = pegarValor();//entrada do usuário para converter
            Cotacao hoje = db.maxData();
            Cotacao moedaOrigem = db.moedaDia(getMoeda1(), hoje.getData());
            Cotacao moedaDestino = db.moedaDia(getMoeda2(), hoje.getData());

            Double c = valorConverter * moedaDestino.getValor();
            Double d = c / moedaOrigem.getValor();
            DecimalFormat df = new DecimalFormat("####.##");

            covertido.setText(df.format(d));

        }catch (NumberFormatException e){
            Toast.makeText(getBaseContext(),"Preencha os dados corretamente!!",Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void atualizar(View v) throws ParseException {


        String permissoes[] = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(this, permissoes, PERMISSAO);
        if ( ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ) {

            DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

            Calendar cal = Calendar.getInstance();
            Date dataAtual = cal.getTime();

            Date diaMesAnterior = diasAnteriores(dataAtual);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(diaMesAnterior);

            Cotacao maxDate = db.maxData();
            if ( maxDate.getData() == null ) {
                List<String> urlList = new ArrayList<>();
                while (cal.getTime().after(calendar.getTime())) {
                    String url = "https://api.exchangeratesapi.io/" + format.format(cal.getTime()) + "?base=BRL\n";
                    urlList.add(url);
                    Log.d("SAIDA: ", url);
                    cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);

                }
                new DownloadJson().execute(urlList.toArray(new String[urlList.size()]));
            } else {
                Cotacao c = db.maxData();
                Calendar cal_diaAtual = Calendar.getInstance();
                Calendar cal_ultimoDia = Calendar.getInstance();

                Date dia = format.parse(c.getData());
                cal_ultimoDia.setTime(dia);

                List<String> urlList2 = new ArrayList<>();
                while (cal_diaAtual.getTime().after(cal_ultimoDia.getTime())) {
                    String url = "https://api.exchangeratesapi.io/" + format.format(cal_diaAtual.getTime()) + "?base=BRL\n";
                    urlList2.add(url);
                    Log.d("SAIDA: ", url);
                    cal_diaAtual.set(Calendar.DAY_OF_MONTH, cal_diaAtual.get(Calendar.DAY_OF_MONTH) - 1);
                }
                new DownloadJson().execute(urlList2.toArray(new String[urlList2.size()]));

            }
        }


    }


    private static Date diasAnteriores(Date data) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(data);
        gc.add(Calendar.DAY_OF_MONTH, -40);
        return gc.getTime();
    }


    class DownloadJson extends AsyncTask<String, Integer, String> {
        String diretorio;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            atualizar.setEnabled(false);
            listar.setEnabled(false);
            bar.setProgress(0);
            aguarde.setText("Atualização de dados em andamento...");
        }

        @Override
        protected String doInBackground(String... txtURL) {
            int i;
            long barProgress = 0;
            try {
                for (String txt : txtURL) {
                    URL url = new URL(txt);
                    Log.d("SAIDA: ", url.toString());

                    URLConnection connection = url.openConnection();
                    connection.connect();
                    String nomeArquivo = url.getFile().substring(url.getFile().lastIndexOf('/') + 1, url.getFile().lastIndexOf("?"));

                    InputStream inputStream = new BufferedInputStream(url.openStream(), 4096);
                    File dir = new File(Environment.getExternalStorageDirectory().toString() + "/MOEDAS/");
                    if ( !dir.exists() ) {
                        dir.mkdir();
                    }
                    diretorio = dir.toString() + "/" + nomeArquivo + ".txt";
                    OutputStream outputStream = new FileOutputStream(diretorio);

                    byte info[] = new byte[1024];

                    while ((i = inputStream.read(info)) != -1) {
                        outputStream.write(info, 0, i);
                        //fazendo a escrita do arquivo obtido no webservice, usando um imputStream

                    }
                    outputStream.flush();
                    outputStream.close();
                    inputStream.close();

                    barProgress++;
                    publishProgress((int) (((float) barProgress) / ((float) txtURL.length) * 100));

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {

                String dir = Environment.getExternalStorageDirectory().toString() + "/MOEDAS/";
                File file = new File(dir);


                for (String arquivo : file.list()) {
                    if ( arquivo.endsWith(".txt") ) {
                        dir = Environment.getExternalStorageDirectory().toString() + "/MOEDAS/" + arquivo;
                        FileReader fileReader = new FileReader(dir);
                        BufferedReader bufferedReader = new BufferedReader(fileReader);
                        String txt = bufferedReader.readLine();
                        JSONObject object = new JSONObject(txt);
                        JSONObject obj = new JSONObject(object.getString("rates"));

                        Boolean b;

                        Cotacao c1 = new Cotacao();
                        c1.setMoeda("BRL");
                        c1.setValor(obj.getDouble("BRL"));
                        c1.setData(object.getString("date"));
                        b = db.verificaDuplicatas(c1);
                        if ( !b ) {
                            Long id1 = db.insertCotacao(c1);
                        } else {
                            Log.d("Registo: ", "Duplicado");
                        }


                        Cotacao c2 = new Cotacao();
                        c2.setMoeda("EUR");
                        c2.setValor(obj.getDouble("EUR"));
                        c2.setData(object.getString("date"));
                        b = db.verificaDuplicatas(c2);
                        if ( !b ) {
                            Long id2 = db.insertCotacao(c2);
                        } else {
                            Log.d("Registo: ", "Duplicado");
                        }


                        Cotacao c3 = new Cotacao();
                        c3.setMoeda("GBP");
                        c3.setValor(obj.getDouble("GBP"));
                        c3.setData(object.getString("date"));
                        b = db.verificaDuplicatas(c3);
                        if ( !b ) {
                            Long id3 = db.insertCotacao(c3);
                        } else {
                            Log.d("Registo: ", "Duplicado");
                        }


                        Cotacao c4 = new Cotacao();
                        c4.setMoeda("USD");
                        c4.setValor(obj.getDouble("USD"));
                        c4.setData(object.getString("date"));
                        b = db.verificaDuplicatas(c4);
                        if ( !b ) {
                            Long id4 = db.insertCotacao(c4);
                        } else {
                            Log.d("Registo: ", "Duplicado");
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
            bar.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String saida) {

            atualizar.setEnabled(true);
            listar.setEnabled(true);
            Toast.makeText(getBaseContext(), "Atualização Concluída", Toast.LENGTH_LONG).show();
            aguarde.setText(" ");
            bar.setProgress(0);

        }
    }
}