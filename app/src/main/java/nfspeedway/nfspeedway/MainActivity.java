package nfspeedway.nfspeedway;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;

import static nfspeedway.nfspeedway.R.id.tvTempo;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private TextView tvVelocidade;
    private SharedPreferences conf;
    LocationManager lm;
    Location previousLocation = null;
    String medidor = "";
    private float multiplicador;
    private ArrayList<Float> velocidades;
    private boolean iniciar = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        conf = getSharedPreferences("nfspeedway.nfspeedway_preferences",Context.MODE_PRIVATE);
        retornaTipoMedidor();
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            AlertDialog.Builder alerta = new AlertDialog.Builder(this);
            alerta.setTitle("Atenção");
            alerta.setMessage("O GPS é necessário para a aplicação e não está habilitado. Deseja Habilitar??");
            alerta.setCancelable(false);
            alerta.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(i);
                }
            });
            alerta.setNegativeButton("Cancelar", null);
            alerta.show();
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);


        tvVelocidade = (TextView) findViewById(R.id.tvVelocidade);
        if (AccessToken.getCurrentAccessToken() == null) {
            goLoginScreen();
        }else{

            Bundle inBundle = getIntent().getExtras();
            String name = inBundle.get("name").toString();
            String surname = inBundle.get("surname").toString();
            String imageUrl = inBundle.get("imageUrl").toString();
            String id = inBundle.get("id").toString();

            TextView nameView = (TextView) findViewById(R.id.tv_Name);
            if(!name.equals("") || name != null || name != ""){
                name = "Nome: " + name+" "+surname ;
            }else{
                name = "Ainda sem informações de nome\nTente relogar-se";
            }

            nameView.setText(name);

            new MainActivity.DownloadImage((ImageView) findViewById(R.id.profileImage)).execute(imageUrl);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        retornaTipoMedidor();
    }

    public class DownloadImage extends AsyncTask<String, Void, Bitmap> { //Faz uma solicitação get para baixar a foto de perfil
        ImageView bmImage;

        public DownloadImage(ImageView bmImage){
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls){
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try{
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            }catch (Exception e){
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result){
            bmImage.setImageBitmap(result);
        }

    }

    private void goLoginScreen() { // Redireciona para a tela de login
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void logout(View view) { // Retira o token de login e redireciona para a tela de login
        LoginManager.getInstance().logOut();
        goLoginScreen();
    }

    public void btConfigOnClick(View view) {
        iniciar = false;
        tvVelocidade.setText("0,00");
        ((TextView) findViewById(tvTempo)).setText("Tempo: 0.0"); //Tempo em segundos
        ((TextView) findViewById(R.id.tvDistancia)).setText("Distancia: 0.0"); //Escreve em tela
        ((Button) findViewById(R.id.btIniciar)).setText("Iniciar");
        startActivity (new Intent(this, ConfActivity.class));
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        tvVelocidade.setText("0,00");
        ((TextView) findViewById(tvTempo)).setText("GPS desabilitado"); //Tempo em segundos
        ((TextView) findViewById(R.id.tvDistancia)).setText("GPS desabilitado"); //Escreve em tela
    }


    @Override
    public void onLocationChanged(Location currentLocation) {
        int i=0;
        if(iniciar) {
            float speed = 0;
            currentLocation.getTime();//Pega em milisegundos a data atual para posteriormente calcular com a data futura.

            if (previousLocation != null) /*Verifica se a localização antiga já existe.
                                    Na primeira verificação sempre será nulo.
                                    Após obter a segunda localização, ele armazena a anterior nessa variavel,
                                    para assim calcular a distancia entre a atual e a anterior*/ {

                float distance = currentLocation.distanceTo(previousLocation);
                ((TextView) findViewById(R.id.tvDistancia)).
                        setText("Distancia: " + distance); //Escreve em tela

                float timeTaken = ((currentLocation.getTime() - previousLocation.getTime()) / 1000);
                ((TextView) findViewById(tvTempo)).setText("Tempo: " + timeTaken); //Tempo em segundos

                // calcula a velocidade
                if (timeTaken > 0) {
                    speed = getAverageSpeed(distance, timeTaken);
                }

                if (speed >= 0) {
                    if(multiplicador != 1.0){ //Se for m/s divide pelos 3,6 se nao nao faz nada
                        speed = speed / multiplicador;
                    }

                    DecimalFormat df = new DecimalFormat("#.##");
                    ((TextView) findViewById(R.id.tvVelocidade)). setText(df.format(speed)); //Escreve em tela
                    velocidades.add(speed);
                    i++;
                }
            }
            previousLocation = currentLocation;
        }

    }
    public void btIniciarOnClick(View view) {
        if(!iniciar){
            iniciar = true;
            ((Button) findViewById(R.id.btIniciar)).setText("Pausar");
            velocidades = new ArrayList<>();

        }else{
            iniciar = false;
            ((Button) findViewById(R.id.btIniciar)).setText("Iniciar");

            float qtd = 0f;
            float soma = 0f;
            if (velocidades != null) {

                for (int i = 0; i < velocidades.size(); i++){
                    soma += velocidades.get(i);
                    qtd++;
                }

            }
            float media = soma/qtd;
            Toast.makeText(MainActivity.this,media + "",Toast.LENGTH_LONG).show();

        }
    }

    private float getAverageSpeed(float distance, float timeTaken) {
        // Faz o calculo de velocidade, através da distancia divida pelo tempo
        float speed = 0;
        if(distance > 0) {
            float distancePerSecond = timeTaken > 0 ? distance/timeTaken : 0;
            float distancePerMinute = distancePerSecond*60;
            float distancePerHour = distancePerMinute*60;
            speed = distancePerHour > 0 ? (distancePerHour/1000) : 0;
        }
        return speed;
    }

    public void retornaTipoMedidor(){
        if(Float.parseFloat(conf.getString("tipoVelo","1.0f")) == 1.0){
            medidor = "Km/h";
            multiplicador = (float) 1.0;
        }else{
            medidor = "M/s";
            multiplicador = (float) 3.6;
        }
        ((TextView) findViewById(R.id.tvMedidor)). setText(medidor); //Escreve em tela
    }


}





