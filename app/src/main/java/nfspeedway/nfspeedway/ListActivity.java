package nfspeedway.nfspeedway;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gms.analytics.ExceptionReporter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import nfspeedway.nfspeedway.dao.SpeedDao;

public class ListActivity extends AppCompatActivity {

    SpeedDao db;
    private ListView lvLocal;
    DataOutputStream dout;
    DataInputStream din;
    ObjectInputStream ois;
    private Socket cliente;
    private SharedPreferences conf;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        conf = getSharedPreferences("nfspeedway.nfspeedway_preferences", Context.MODE_PRIVATE);
        listar(); 
        if (verificaConexao()) {
            new Conexao().execute("consultar");
        } else {
            listar();
        }
    }


    public void listar() {
        lvLocal = (ListView) findViewById(R.id.lvLocal); // declara vinculando com o componente visual
        db = new SpeedDao(this); // instancia o banco
        //Cursor para listar os registros
        Cursor registros = db.listarRegistros();
        //Adapter para mostrar os registros dentro do listview (titulo e descrição)
        final SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_2,
                registros,
                new String[]{"_id", "velocidade"},
                new int[]{android.R.id.text1, android.R.id.text2}
        );

        //List criado para popular o listview
        List<String> local = new ArrayList<>();

        while (registros.moveToNext()) {//enquanto houverem registros
            //adiciona na lista
            local.add(registros.getInt(registros.getColumnIndex("_id")) + " - " +
                    registros.getString(registros.getColumnIndex("velocidade")) + " - " +
                    registros.getString(registros.getColumnIndex("nome")) + " - " + "\n");
        }
        //define o adapter para o listview
        lvLocal.setAdapter(adapter);
    }

    class Conexao extends AsyncTask<String,Void,String> {
        private ProgressDialog dialog;
        String resposta;
        ArrayList <String> lista = new ArrayList<>();
        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(ListActivity.this);
            dialog.setTitle("Aguarde...");
            dialog.setMessage("Recebendo dados do servidor");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCancelable(false);
            dialog.show();
            resposta = new String();
        }

        @Override
        protected void onPostExecute(String s) {
            dialog.dismiss();
            if(resposta.equals("Listando registros do servidor")) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(ListActivity.this,
                        android.R.layout.simple_list_item_1, lista);

                lvLocal.setAdapter(adapter);
            }
            Toast.makeText(ListActivity.this,resposta,Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String doInBackground(String... params) {

            //Cursor registros = db.listarRegistros();
            try {
                if(cliente == null) {
                    cliente = new Socket();
                    cliente.connect(new InetSocketAddress(conf.getString("serverIP","192.168.2.111"), 6789),7000);
                    dout = new DataOutputStream(cliente.getOutputStream());
                    din = new DataInputStream(cliente.getInputStream());
                    ois = new ObjectInputStream(cliente.getInputStream());
                }else{
                    cliente.connect(new InetSocketAddress(conf.getString("serverIP","192.168.2.111"), 6789),7000);
                    din = new DataInputStream(cliente.getInputStream());
                }
                dout.writeUTF("consultar");
                try {
                    lista = (ArrayList<String>) ois.readObject();
                } catch (ClassNotFoundException e) {
                    return resposta = "Erro não tratado";
                }
                dout.flush();

            } catch (Exception e) {
                return "Não foi possível se conectar";
            }
            return resposta = "Listando registros do servidor";

        }


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

}