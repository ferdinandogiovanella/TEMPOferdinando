package com.example.climating;
//37
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import android.Manifest;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {
    boolean Interface_Criada = false;
    boolean local_atual;
    String retorno = "";
    Boolean tratar_dias = false;
    int dia_tela_atual = 0;
    int dia_tela_anterior = 0;
    int quant_view = 0;

    private LinearLayout novos_dias;
    private TextView dia_atual;
    private TextView MostrarCidade;
    private ScrollView menu;
    private ImageView iconImage;
    private TextView SetTemperatura;
    private TextView min_max;
    private EditText cidade;
    private TextView vento;
    private TextView precipdia;
    private LinearLayout mContent;
    private ImageView local;
    private Snackbar EnderecoInv;
    private static final int REQUEST_LOCATION_PERMISSIONS = 1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Snackbar ErroDePosicao = Snackbar.make(findViewById(R.id.back), "Verifique sua localização e reinicie o app.",  Snackbar.LENGTH_SHORT);
        Snackbar PermissaoNegada = Snackbar.make(findViewById(R.id.back), "Você precisa aceitar a permissão para que o aplicativo acesse sua posição.",  Snackbar.LENGTH_LONG);
        EnderecoInv = Snackbar.make(findViewById(R.id.back),"Cidade não encontrada, verifique a escrita",Snackbar.LENGTH_LONG);

        local = (ImageView) findViewById(R.id.local);
        MostrarCidade = (TextView) findViewById(R.id.cidadeMostra);
        iconImage = (ImageView) findViewById(R.id.iconImage);
        mContent = (LinearLayout) findViewById(R.id.linear);
        novos_dias = (LinearLayout) findViewById(R.id.novos_dias);
        SetTemperatura = (TextView) findViewById(R.id.temp);
        min_max = (TextView) findViewById(R.id.min_max);
        vento = (TextView) findViewById(R.id.vento);
        precipdia = (TextView) findViewById(R.id.precipdia);
        menu = (ScrollView) findViewById(R.id.back);
        cidade = (EditText) findViewById(R.id.cidade);
        dia_atual = (TextView) findViewById(R.id.dia_semana);

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (!isConnected) {
            MostrarCidade.setText("Sem internet");
            return;
        }

        //verifica se possui um local salvo anteriormente
        RecuperaLocal();

        //Faz o pedido para o usuário fornecer as permissões necessárias
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSIONS);
        }

        //Quando clicado, tenta pegar a localização local do dispositivo
        local.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override

            public void onClick(View view) {
                fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                    if (location != null) {
                        // Obtenha a latitude e longitude da localização
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        // Faça o que quiser com a localização
                        try {

                            CompletableFuture<String> future = Conexao.pegarJson(latitude+","+longitude);

                            retira_teclado();

                            if (Interface_Criada == false) {
                                cria_interface(true);
                                Interface_Criada = true;
                            }

                            retorno = future.get();
                            local_atual=true;

                            dia_tela_anterior=0;
                            dia_tela_atual=0;
                            LinearLayout a = (LinearLayout) findViewById(dia_tela_atual+200);
                            a.setBackground(getDrawable(R.drawable.borda_arredondada));
                            cria_interface(true);
                            tratarFuturosDias();
                            TratarDados(retorno,true);

                        } catch (ExecutionException e) {
                            throw new RuntimeException(e);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                    } else {
                        ErroDePosicao.show();
                        System.out.println("Erro");
                    }
                });
//Caso não seja possivel fazer o a requisição da localização, mostra o erro
                fusedLocationClient.getLastLocation().addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Lide com a exceção
                        PermissaoNegada.show();
                        System.out.println("Erro ao obter os dados: " + e.getMessage());

                        return;
                    }
                });
            }
        });


        cidade.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) &&
                        (i == KeyEvent.KEYCODE_ENTER)) {
                    VerificarRequest();
                }
                return false;
            }

        });

    }

    public void VerificarRequest(){
        try {
            String endereco = String.valueOf(cidade.getText());
            CompletableFuture<String> future = Conexao.pegarJson(endereco);

            retira_teclado();

            retorno = future.get();

            System.out.println(retorno);
            if(retorno.equals("Erro")){
                EnderecoInv.show();
                return;
            }else{
                if (Interface_Criada == false) {
                    cria_interface(true);
                    Interface_Criada = true;
                }
                local_atual=false;
                dia_tela_anterior=0;
                dia_tela_atual=0;
                LinearLayout a = (LinearLayout) findViewById(dia_tela_atual+200);
                a.setBackground(getDrawable(R.drawable.borda_arredondada));
                cria_interface(true);
                tratarFuturosDias();
                TratarDados(retorno,true);
            }
        } catch (ExecutionException | JSONException | InterruptedException e) {
            System.out.println(e);
        }
    }


    //Faz a request da API e mostra os dados do dia na tela.
    public void TratarDados(String retorno,boolean hoje) throws JSONException {
        //verifica se já criou os layouts dos dias futuros
        if (tratar_dias == false) {
            tratar_dias = true;
            tratarFuturosDias();
        }


        System.out.println("Passou por tratar dados");
        //Transforma a String em JSON
        JSONObject my_obj = new JSONObject(retorno);


        if (local_atual){
            MostrarCidade.setText("Local Atual");
        }else {
            String local = my_obj.getString("resolvedAddress");

            try{
                String[] localSplit = local.split(",");

                cidade.setText("");
                MostrarCidade.setText(localSplit[0] + "," + localSplit[1]);
            }catch (Exception e){
                cidade.setText("");
                MostrarCidade.setText(my_obj.getString("resolvedAddress"));
            }

            SalvaLocal(my_obj.getString("resolvedAddress"));

        }


        //Faz o tratamento para chegar aos dados necessários
        JSONArray daysArray = my_obj.getJSONArray("days");

        //faz a escolha do dia que terá as informações visiveis
        JSONObject firstDay = daysArray.getJSONObject(dia_tela_atual);

        int temperatura = firstDay.getInt("temp");
        int min = firstDay.getInt("tempmin");
        int max = firstDay.getInt("tempmax");

        //Chama a funcao que modifica o icone grande
        modifica_icon_geral(firstDay.getString("icon"));

        String dataRaw = firstDay.getString("datetime");
        String[] dataSplit = dataRaw.split("-");
        String data = dataSplit[2] + "/" + dataSplit[1] + "/" + dataSplit[0];

        String dia = getDiaSemana(data);

        dia_atual.setText(dia);


        JSONArray hoursArray = firstDay.getJSONArray("hours");

        int j = 0;
        //Estrutura de repetição para percorrer todas as posições do array
        //Serve para adicionar a temperatura em todos os horários
        for (int i = 0; i < hoursArray.length(); i++) {
            JSONObject hourObject = hoursArray.getJSONObject(i);
            String datetime = hourObject.getString("datetime");

            //faz o tratamento das horas para aparecer apenas horas e minutos
            String[] dateSeparado = datetime.split(":");
            String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
            String[] dispositivohora = currentTime.split(":");
            int dispositivo = Integer.valueOf(dispositivohora[0]);
            int hora_dia = Integer.valueOf(dateSeparado[0]);
            int temp = hourObject.getInt("temp");
            int chuvaProb = hourObject.getInt("precipprob");

            if (hora_dia < dispositivo && hoje) {
                System.out.println("menor");
            } else if (hora_dia == dispositivo) {
                SetTemperatura.setText(new Integer(temperatura).toString() + "°");
                min_max.setText("Min. " + new Integer(min).toString() + "° Max. " + new Integer(max).toString() + "°");
                precipdia.setText(hourObject.getString("precipprob") + "%");
                double ventovelocidade = hourObject.getDouble("windspeed");
                vento.setText("" + ventovelocidade + " km/h");
            } else {

                TextView chuva = findViewById(90 + j);
                TextView temphora = findViewById(60 + j);
                TextView horario = findViewById(30 + j);
                ImageView image = findViewById(j);

                precipdia.setText(hourObject.getString("precipprob") + "%");
                double ventovelocidade = hourObject.getDouble("windspeed");
                vento.setText("" + ventovelocidade + " km/h");

                j++;
                chuva.setText(chuvaProb + "%");
                temphora.setText(temp + "°C");
                horario.setText(dateSeparado[0] + ":" + dateSeparado[1]);

                if (hourObject.getString("icon").equals("rain")) {
                    image.setBackground(getResources().getDrawable(R.drawable.chuva));
                } else if (hourObject.getString("icon").equals("partly-cloudy-day")) {
                    image.setBackground(getResources().getDrawable(R.drawable.parcial_nubl));
                } else if (hourObject.getString("icon").equals("cloudy")) {
                    image.setBackground(getResources().getDrawable(R.drawable.nublado));
                } else if (hourObject.getString("icon").equals("clear-day")) {
                    image.setBackground(getResources().getDrawable(R.drawable.sun));
                } else if (hourObject.getString("icon").equals("partly-cloudy-night")) {
                    image.setBackground(getResources().getDrawable(R.drawable.cloudy_night));
                } else if (hourObject.getString("icon").equals("clear-night")) {
                    image.setBackground(getResources().getDrawable(R.drawable.clear_night));
                } else if (hourObject.getString("icon").equals("wind")) {
                    image.setBackground(getResources().getDrawable(R.drawable.windy));
                }
            }
        }
    }


    //Retira o teclado após pesquisar a cidade
    public void retira_teclado() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager manager
                    = (InputMethodManager)
                    getSystemService(
                            Context.INPUT_METHOD_SERVICE);
            manager
                    .hideSoftInputFromWindow(
                            view.getWindowToken(), 0);
        }
    }

    //Modifica o icone principal da tela
    public void modifica_icon_geral(String icon) {
        if (icon.equals("rain")) {
            iconImage.setBackground(getResources().getDrawable(R.drawable.chuva));
        } else if (icon.equals("partly-cloudy-day")) {
            iconImage.setBackground(getResources().getDrawable(R.drawable.parcial_nubl));
        } else if (icon.equals("cloudy")) {
            iconImage.setBackground(getResources().getDrawable(R.drawable.nublado));
        } else if (icon.equals("clear-day")) {
            iconImage.setBackground(getResources().getDrawable(R.drawable.sun));
        } else if (icon.equals("partly-cloudy-night")) {
            iconImage.setBackground(getResources().getDrawable(R.drawable.cloudy_night));
        } else if (icon.equals("clear-night")) {
            iconImage.setBackground(getResources().getDrawable(R.drawable.clear_night));
        } else if (icon.equals("wind")) {
            iconImage.setBackground(getResources().getDrawable(R.drawable.windy));
        }
    }

    //Cria a interface para o dia atual
    public void cria_interface(Boolean dia_atual) {
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        String[] dispositivohora = currentTime.split(":");
        int dispositivo = Integer.valueOf(dispositivohora[0]);

        mContent.removeAllViews();

        if(dia_atual){
            quant_view = 23 - dispositivo;
        }else{
            quant_view = 23;

        }

        for (int i = 0; i < quant_view; i++) {
            TextView horario = new TextView(this);
            TextView temphora = new TextView(this);
            TextView chuva = new TextView(this);
            ImageView image = new ImageView(this);
            LinearLayout linear = new LinearLayout(this);

            //muda a cor de fundo
            temphora.setBackgroundColor(getResources().getColor(R.color.hora, getResources().newTheme()));
            chuva.setBackgroundColor(getResources().getColor(R.color.hora, getResources().newTheme()));
            //muda a cor do texto
            temphora.setTextColor(getResources().getColor(R.color.white, getResources().newTheme()));
            chuva.setTextColor(getResources().getColor(R.color.white, getResources().newTheme()));
            //tamanho do texto
            temphora.setTextSize(20);
            chuva.setTextSize(20);
            //adiciona parametros de margen
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            //params.setMargins(10,10,10,10);
            params.setMarginEnd(20);
            //----------------------------horario------------------
            //adiciona os parametros no txtItem.
            horario.setLayoutParams(params);
            horario.setPadding(10, 0, 5, 20);

            // Informamos um id
            image.setId(i);
            horario.setId(30 + i);
            temphora.setId(60 + i);
            chuva.setId(90 + i);

            //muda a cor de fundo
            horario.setBackgroundColor(getResources().getColor(R.color.hora, getResources().newTheme()));

            //muda a cor do texto
            horario.setTextColor(getResources().getColor(R.color.white, getResources().newTheme()));

            //tamanho do texto
            horario.setTextSize(20);

            //adiciona parametros de margen
            params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            //params.setMargins(10,10,10,10);
            //params.setMarginEnd(20);
            params.setMargins(10, 10, 10, 10);

            //adiciona os parametros no txtItem.
            horario.setLayoutParams(params);
            horario.setPadding(10, 0, 5, 20);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(50, 50);
            image.setLayoutParams(layoutParams);

            // Adiciona no Linear Layout
            linear.setOrientation(LinearLayout.VERTICAL);

            temphora.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            horario.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            chuva.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            layoutParams = new LinearLayout.LayoutParams(70, 70);
            layoutParams.gravity = Gravity.CENTER;
            layoutParams.setMargins(0, 15, 0, 15);

            image.setLayoutParams(layoutParams);

            //adiciona os parametros no txtItem.
            System.out.println("Gerou os Views");
            image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

            linear.setPadding(5, 5, 5, 5);
            linear.setBackground(getDrawable(R.drawable.borda_arredonda_hora));
            linear.addView(temphora);
            linear.addView(image);
            linear.addView(chuva);
            linear.addView(horario);

            mContent.addView(linear);
        }
    }

    //Salva um local na memória para que quando entre no aplicativo não
    // precise sempre colocar a localização
    public void SalvaLocal(String localizacao) {
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.cidade), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("localizacao", localizacao);
        editor.apply();
    }

    //Verifica se possui um local salvo
    public void RecuperaLocal() {
        SharedPreferences sharedPref = getSharedPreferences(
                getString(R.string.cidade), Context.MODE_PRIVATE);
        String local = sharedPref.getString("localizacao", "");
        if (local.equals("")) {
            SalvaLocal("New York");
            PossuiSalvo(local);
        } else {
            PossuiSalvo(local);
        }
    }
    //Faz a requisição para a API caso tenha um local salvo
    public void PossuiSalvo(String local) {
        try {
            CompletableFuture<String> future = Conexao.pegarJson(local);

            retira_teclado();

            cria_interface(true);

            retorno = future.get();
            TratarDados(retorno,true);
            System.out.println("Passou possui salvo");
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
    //trata os dados dos dias da semana que aparecem na tela
    public void tratarFuturosDias() throws JSONException {
        JSONObject dias = new JSONObject(retorno);
        novos_dias.removeAllViews();
        //Faz o tratamento para chegar aos dados necessários
        JSONArray daysArray = dias.getJSONArray("days");
        for (int i = 0; i < 7; i++) {
            JSONObject firstDay = daysArray.getJSONObject(i);

            String temp = firstDay.getInt("tempmin") + "°-" + firstDay.getInt("tempmax") + "°";
            int chuva = firstDay.getInt("precipprob");
            String icondia = firstDay.getString("icon");
            String dataRaw = firstDay.getString("datetime");

            String[] dataSplit = dataRaw.split("-");
            String data = dataSplit[2] + "/" + dataSplit[1] + "/" + dataSplit[0];

            String dia_atual = getDiaSemana(data);

            addDia(dia_atual, temp, i, icondia);
            //System.out.println("Temp min:"+min+" Max:"+max+"\nchuva prob:"+chuva+"%\nIcone:"+icondia+"\nData:"+data);
        }
    }

    //Adiciona as informações iniciais dos dias da semana
    public void addDia(String dia_atual, String temperatura, int posicao, String icondia) {

        LinearLayout linear = new LinearLayout(this);
        TextView dia = new TextView(this);
        TextView temp = new TextView(this);
        TextView chuva = new TextView(this);
        ImageView icon = new ImageView(this);


        //Modifica o icone do dia
        if (icondia.equals("rain")) {
            icon.setBackground(getResources().getDrawable(R.drawable.chuva));
        } else if (icondia.equals("partly-cloudy-day")) {
            icon.setBackground(getResources().getDrawable(R.drawable.parcial_nubl));
        } else if (icondia.equals("cloudy")) {
            icon.setBackground(getResources().getDrawable(R.drawable.nublado));
        } else if (icondia.equals("clear-day")) {
            icon.setBackground(getResources().getDrawable(R.drawable.sun));
        } else if (icondia.equals("partly-cloudy-night")) {
            icon.setBackground(getResources().getDrawable(R.drawable.cloudy_night));
        } else if (icondia.equals("clear-night")) {
            icon.setBackground(getResources().getDrawable(R.drawable.clear_night));
        } else if (icondia.equals("wind")) {
            icon.setBackground(getResources().getDrawable(R.drawable.windy));
        }
        linear.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(70, 70);
        layoutParams.setMargins(20, 0, 0, 0);
        icon.setLayoutParams(layoutParams);

        temp.setText(temperatura);

        if (posicao == 0) {
            dia.setText("Hoje");
            linear.setBackground(getDrawable(R.drawable.borda_dia_selecionado));
        } else {
            linear.setBackground(getDrawable(R.drawable.borda_arredonda_hora));
            dia.setText(dia_atual);
        }

        dia.setTextColor(getResources().getColor(R.color.white, getResources().newTheme()));
        temp.setTextColor(getResources().getColor(R.color.white, getResources().newTheme()));

        dia.setTextSize(30);
        temp.setTextSize(30);
        int id = 200 + posicao;
        linear.setId(id);

        temp.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        temp.setGravity(View.TEXT_ALIGNMENT_GRAVITY);

        //Cria a função em cada dia para que suas informações sejam mostradas ao clicar
        linear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LinearLayout a = findViewById(200 + dia_tela_anterior);
                a.setBackground(getDrawable(R.drawable.borda_arredonda_hora));

                int id = v.getId();
                dia_tela_atual = id - 200;
                dia_tela_anterior = dia_tela_atual;

                v.setBackground(getDrawable(R.drawable.borda_dia_selecionado));

                try {
                    if(Integer.valueOf(dia_tela_atual)==0){
                        cria_interface(true);
                        TratarDados(retorno,true);
                    }else{
                        cria_interface(false);
                        TratarDados(retorno,false);
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        temp.setPadding(20, 5, 0, 5);

        dia.setPadding(25, 0, 0, 0);

        linear.addView(icon);
        linear.addView(dia);
        linear.addView(temp);

        LinearLayout.LayoutParams linear_params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        linear_params.setMargins(10, 10, 10, 10);
        linear.setLayoutParams(linear_params);

        novos_dias.addView(linear);
    }

    //Pega o nome do dia da semana
    public static String getDiaSemana(String date) { //ex 07/03/2017
        GregorianCalendar gc = new GregorianCalendar();
        try {
            gc.setTime(new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR")).parse(date));
            String dia = new SimpleDateFormat("EEE", new Locale("pt", "BR")).format(gc.getTime()).toUpperCase();
            System.out.println(dia);
            if (dia.equals("SEG")) {
                return "Segunda-Feira";
            } else if (dia.equals("TER")) {
                return "Terça-Feira";
            } else if (dia.equals("QUA")) {
                return "Quarta-Feira";
            } else if (dia.equals("QUI")) {
                return "Quinta-Feira";
            } else if (dia.equals("SEX")) {
                return "Sexta-Feira";
            } else if (dia.equals("SÁB")) {
                return "Sábado";
            } else if(dia.equals("DOM")){
                return "Domingo";
            }else{
                return dia;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "Ocorreu algum erro";
    }

}