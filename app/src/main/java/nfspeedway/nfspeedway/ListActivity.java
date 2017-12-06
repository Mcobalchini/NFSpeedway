package nfspeedway.nfspeedway;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.net.Socket;
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

public class ListActivity extends AppCompatActivity {

    SpeedDao db;
    private ListView lvLocal;
    private SharedPreferences conf;
    private UserService userService;
    public List<SpeedPessoa> speeds = new ArrayList<>();
    private final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        conf = getSharedPreferences("nfspeedway.nfspeedway_preferences", Context.MODE_PRIVATE);
        lvLocal = (ListView) findViewById(R.id.lvLocal);
        ArrayAdapter<SpeedPessoa> adapter = new ArrayAdapter<>(ListActivity.this,
                android.R.layout.simple_list_item_1, speeds);
        lvLocal.setAdapter(adapter);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://" + conf.getString("serverIP","172.30.10.48") + ":1337/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        this.userService = retrofit.create(UserService.class);
        listar();
        if (verificaConexao()) {
            getAllSpeedPessoa();
        } else {
            listar();
        }
    }


    public void listar() {
        lvLocal = (ListView) findViewById(R.id.lvLocal); // declara vinculando com o componente visual
        db = new SpeedDao(this); // instancia o banco
        //Cursor para listar os registros
        Cursor registros = db.listarRegistros();
        //Adapter para mostrar os registros dentro do listview (titulo e descri��o)
        final SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_2,
                registros,
                new String[]{"_id", "velocidade"},
                new int[]{android.R.id.text1, android.R.id.text2}
        );

        //List criado para popular o listview
        List<SpeedPessoa> local = new ArrayList<>();
        SpeedPessoa speedPessoa = new SpeedPessoa();
        while (registros.moveToNext()) {//enquanto houverem registros
            speedPessoa.setCodigoFb("teste");
            speedPessoa.setNome("teste");
            speedPessoa.setVelocidade(registros.getColumnIndex("velocidade"));
            local.add(speedPessoa);
        }
        //define o adapter para o listview
        lvLocal.setAdapter(adapter);
    }

    public  boolean verificaConexao() {
        boolean isConnected;
        ConnectivityManager conectivtyManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (conectivtyManager.getActiveNetworkInfo() != null
                && conectivtyManager.getActiveNetworkInfo().isAvailable()
                && conectivtyManager.getActiveNetworkInfo().isConnected()) {
            isConnected = true;
        } else {
            isConnected = false;
        }
        return isConnected;
    }

    /**
     * Busca toas as speed na base online.
     */
    private void getAllSpeedPessoa() {

        Call<List<SpeedPessoa>> getAllSpeedPessoasCall = this.userService.getAllSpeedPessoa();

        getAllSpeedPessoasCall.enqueue(new Callback<List<SpeedPessoa>>() {
            @Override
            public void onResponse(Call<List<SpeedPessoa>> call, Response<List<SpeedPessoa>> response) {
                speeds = response.body();
            }

            @Override
            public void onFailure(Call<List<SpeedPessoa>> call, Throwable t) {
                Log.e(TAG, "Erro ao enviar registro.");
            }
        });
    }
}