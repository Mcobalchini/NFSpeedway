package nfspeedway.nfspeedway;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;



public class LoginActivity extends AppCompatActivity {
    private LoginButton loginButton;
    private CallbackManager callbackManager;
    public String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);



        callbackManager = CallbackManager.Factory.create();
        loginButton = (LoginButton) findViewById(R.id.login_button);
        //leitura de permissões do facebook para a aplicação
        loginButton.setReadPermissions("user_friends", "public_profile", "email");

        //Aqui no callback definimos o que fazer nos 3 casos (sucesso, cancelamento e falha)
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Profile profile = Profile.getCurrentProfile(); //Pega o perfil atual logado
                Toast.makeText(LoginActivity.this, "Logando na aplicação...", Toast.LENGTH_SHORT).show();
                telaPrincipal(profile); //Passa os dados para recuperarmos na outra tela
            }

            @Override
            public void onCancel() {
                Toast.makeText(getApplicationContext(), "Operação cancelada", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(getApplicationContext(), "Ocorreu algum erro ao logar na aplicação", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Facebook login
        Profile profile = Profile.getCurrentProfile();
        telaPrincipal(profile);
        //Voltar a aplicação, deve fazer com que o usuario seja redicionado para esta tela
    }

    private void telaPrincipal(Profile profile) {
        if (profile != null) {
            Intent main = new Intent(LoginActivity.this, MainActivity.class);
            main.putExtra("name", profile.getFirstName()); //Informações pertinentes ao perfil do usuario
            main.putExtra("surname", profile.getLastName());
            main.putExtra("id", profile.getId());
            main.putExtra("imageUrl", profile.getProfilePictureUri(200, 200).toString());
            main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(main);
        }
    }

    //Cada atividade ou fragmento que for integrada ao login ou compartilhamento do FacebookSDK
    // deve encaminhar o onActivityResult ao callbackManager.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

}