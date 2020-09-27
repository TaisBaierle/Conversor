package com.example.conversor;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    Button sair, converter, listar, atualizar;
    BancoDados db;
    ProgressBar bar;
    TextView aguarde, resposta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sair = findViewById(R.id.sair);
        converter = findViewById(R.id.converte);
        listar = findViewById(R.id.historico);
        atualizar = findViewById(R.id.atualiza);
        bar = findViewById(R.id.progressBar);
        aguarde = findViewById(R.id.text_aguarde);
        resposta = findViewById(R.id.text_resposta);
        db = new BancoDados(getBaseContext());
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
            builder.setTitle("                  CREDITOS");
            textView.setText("Aplicativo de conversão de moedas desenvolvido para a disciplina de programação 4 - 2019/02\nAutoria Tais Baierle\nProfessor Gabriel");
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
        return super.onOptionsItemSelected(item);
    }

    public void sair(View v) {
        finishAffinity();
    }

    public void atualizar(View v) throws ParseException {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        Calendar cal = Calendar.getInstance();
        Date dataAtual = cal.getTime();

        Date diaMesAnterior = diasAnteriores(dataAtual);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(diaMesAnterior);

        //if ( db.maxData() == null ) {
        List<String> urlList = new ArrayList<>();
        while (cal.getTime().after(calendar.getTime())) {
            String url = "https://api.exchangeratesapi.io/" + format.format(cal.getTime()) + "?base=BRL\n";
            urlList.add(url);
            Log.d("SAIDA: ", url);
            cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);

        }
        new DownloadJson().execute(urlList.toArray(new String[urlList.size()]));

    } /*else {
            cal = Calendar.getInstance();//data atual
            calendar = Calendar.getInstance();
            calendar.setTime(format.parse(db.maxData()));//ultima data do banco
            while (calendar.getTime().before(cal.getTime())) {
                String url = "https://api.exchangeratesapi.io/" + format.format(calendar.getTime()) + "?base=BRL\n";
                new DownloadJson().execute(url);
                calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
            }
        }
    }*/

    private static Date diasAnteriores(Date data) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(data);
        gc.add(Calendar.MONTH, -1);
        return gc.getTime();
    }


    class DownloadJson extends AsyncTask<String, Integer, String> {
        String diretorio;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            atualizar.setEnabled(false);
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
                    diretorio = Environment.getExternalStorageDirectory().toString() + "/Download/" + nomeArquivo + ".txt";
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

            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
            bar.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String saida) {

            atualizar.setEnabled(true);
            Toast.makeText(getBaseContext(), "Atualização Concluída", Toast.LENGTH_LONG).show();
            aguarde.setText(" ");
            bar.setProgress(0);


            try {
                String dir = Environment.getExternalStorageDirectory().toString() + "/Download/";
                File file = new File(dir);

                for (String arquivo : file.list()) {
                    if ( arquivo.endsWith(".txt") ) {
                        Cotacao c1 = new Cotacao();
                        Cotacao c2 = new Cotacao();
                        Cotacao c3 = new Cotacao();
                        Cotacao c4 = new Cotacao();
                        dir = Environment.getExternalStorageDirectory().toString() + "/Download/" + arquivo;
                        FileReader fileReader = new FileReader(dir);
                        BufferedReader bufferedReader = new BufferedReader(fileReader);

                        String txt;
                        txt = bufferedReader.readLine();
                        JSONObject object = new JSONObject();


                            if ( object.has("BRL") ) {
                                c1.setMoeda("BRL");
                                c1.setValor(object.getDouble("BRL"));
                                c1.setData(object.getString("date"));
                                Long add1 = db.insertCotacao(c1);

                            }
                           /*if ( object.has("EUR") ) {
                                c2.setMoeda("EUR");
                                c2.setValor(object.getDouble("EUR"));
                                c2.setData(object.getString("date"));
                                Long add2 = db.insertCotacao(c2);
                            }

                            if ( object.has("GBP") ) {
                                c3.setMoeda("GBP");
                                c3.setValor(object.getDouble("GBP"));
                                c3.setData(object.getString("date"));
                                Long add3 = db.insertCotacao(c3);
                            }
                            if ( object.has("USD") ) {
                                c4.setMoeda("USD");
                                c4.setValor(object.getDouble("USD"));
                                c4.setData(object.getString("date"));
                                Long add4 = db.insertCotacao(c4);


                            }*/
                        }
                    }

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}










