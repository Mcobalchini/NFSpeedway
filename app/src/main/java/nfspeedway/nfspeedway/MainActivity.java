package nfspeedway.nfspeedway;

import android.Manifest;
import android.app.Service;
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
import java.util.List;

import nfspeedway.nfspeedway.Entidade.SpeedPessoa;
import nfspeedway.nfspeedway.Entidade.UserService;
import nfspeedway.nfspeedway.dao.SpeedDao;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static java.lang.Float.NaN;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private TextView tvVelocidade;
    private SharedPreferences conf;
	private String idFb,name,surname,medidor = "";
	float media;
	LocationManager lm;
    private UserService userService;
    Location localizacaoAnterior = null;    
    private float multiplicador;
    private ArrayList<Float> velocidades;
    private boolean iniciar = false;
	SpeedDao speedDao;
    private final String TAG = this.getClass().getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://172.30.10.48:1337/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        this.userService = retrofit.create(UserService.class);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		speedDao = new SpeedDao(this); 
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
			idFb = inBundle.get("id").toString();
            name = inBundle.get("name").toString();
            surname = inBundle.get("surname").toString();
            String imageUrl = inBundle.get("imageUrl").toString();            

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

    public class DownloadImage extends AsyncTask<String, Void, Bitmap> { //Faz uma solicita��o get para baixar a foto de perfil
        ImageView bmImage;

        public DownloadImage(ImageView bmImage){
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls){  //Baixa a imagem de perfil da conta do facebook no imageView da tela a partir da url
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
        ((Button) findViewById(R.id.btIniciar)).setText("Iniciar");
        startActivity (new Intent(this, ConfActivity.class)); //Redireciona para a tela de configura��es
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
    }


    @Override
    public void onLocationChanged(Location localizacaoAtual) {
        int i=0;
        if(iniciar) {
            float speed = 0;
            localizacaoAtual.getTime();//Pega em milisegundos a data atual para posteriormente calcular com a data futura.

            if (localizacaoAnterior != null) /*Verifica se a localização antiga já existe.
                                    Na primeira verificação sempre será nulo.
                                    Após obter a segunda localização, ele armazena a anterior nessa variavel,
                                    para assim calcular a distancia entre a atual e a anterior*/ {
				
				//Retorna a distancia aproximada em metros entre a localiza��o atual e a passada por par�metro.
                float distanciaPercorrida = localizacaoAtual.distanceTo(localizacaoAnterior);
                float tempoGasto = ((localizacaoAtual.getTime() - localizacaoAnterior.getTime()) / 1000);

                // calcula a velocidade
                if (tempoGasto > 0) {
                    speed = getVelocidadeMedia(distanciaPercorrida, tempoGasto);
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
            localizacaoAnterior = localizacaoAtual;
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
            media = soma/qtd;
            if(media > 0) {
                inserirRegistro();
            }
            Toast.makeText(MainActivity.this,media + "",Toast.LENGTH_LONG).show();

        }
    }

    public void btListarOnClick(View v){
        Intent main = new Intent(MainActivity.this, ListActivity.class);
        startActivity(main);
    }

    private float getVelocidadeMedia(float distanciaPercorrida, float tempoGasto) {
        // Faz o calculo de velocidade, atraves da distancia divida pelo tempo
        float speed = 0;
        if(distanciaPercorrida > 0) {
            float distanciaPercorridaPorSegundo = tempoGasto > 0 ? distanciaPercorrida / tempoGasto : 0; //If tern�rio
            float distanciaPercorridaPorMinuto = distanciaPercorridaPorSegundo * 60;
            float distanciaPercorridaPorHora = distanciaPercorridaPorMinuto * 60;
            speed = distanciaPercorridaPorHora > 0 ? (distanciaPercorridaPorHora / 1000) : 0; //Tern�rio
        }
        return speed;
    }
	
	public void inserirRegistro() {
        speedDao = new SpeedDao(this); // instancia o banco
        SpeedPessoa registro = new SpeedPessoa();
        registro.setCodigoFb(idFb) ;        
        registro.setNome(name);        
		registro.setVelocidade(media);
        speedDao.incluirRegistro( registro );
        createSpeedPessoaOnServer(registro);
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





    /**
     * Salva uma speedPessoa e altera se já existir (na base online)
     * @param newSpeedPessoa
     */
    private void createSpeedPessoaOnServer(SpeedPessoa newSpeedPessoa) {
        Call<SpeedPessoa> call = this.userService.createSpeedPessoa(newSpeedPessoa);
        call.enqueue(new Callback<SpeedPessoa>() {
            @Override
            public void onResponse(Call<SpeedPessoa> call, Response<SpeedPessoa> response) {
                Log.e(TAG, response.body().toString());
            }

            @Override
            public void onFailure(Call<SpeedPessoa> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Unable to create post" , Toast.LENGTH_LONG).show();
                Log.e(TAG,t.toString());
            }
        });
    }

    public void btMinimizarOnClick(View view) {
        Intent i = new Intent(this, ServiceActivity.class);
        startService(i);
        finish();
    }

}