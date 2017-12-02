package nfspeedway.nfspeedway;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

import java.io.InputStream;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private static final String[] INITIAL_PERMS = {
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private static final String[] LOCATION_PERMS = {
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private TextView tvVelocidade;


    LocationManager lm;
    LocationListener ll;

    Location previousLocation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);




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
            TextView tvId = (TextView) findViewById(R.id.tv_id);
            nameView.setText("Nome: " + name + " " + surname);
            tvId.setText("Código: "+id);
            new MainActivity.DownloadImage((ImageView) findViewById(R.id.profileImage)).execute(imageUrl);
        }

    }

    @Override
    public void onLocationChanged(Location currentLocation) {

        float speed = 0;

        ((TextView) findViewById(R.id.updated)).
                setText("Updated: "+currentLocation.getTime());

        if(previousLocation != null) {
            // distance travelled (meters)
            float distance = currentLocation.distanceTo(previousLocation);
            ((TextView) findViewById(R.id.distance)).
                    setText("Distance: "+distance);

            // time taken (in seconds)
            float timeTaken = ((currentLocation.getTime()
                    - previousLocation.getTime())/1000);
            ((TextView) findViewById(R.id.time)).
                    setText("Time: "+timeTaken);

            // calculate speed
            if(timeTaken > 0) {
                speed = getAverageSpeed(distance, timeTaken);
            }

            if(speed >= 0) {
                DecimalFormat df = new DecimalFormat("#.##");
                ((TextView) findViewById(R.id.tvVelocidade)).
                        setText("Speed: "+df.format(speed));
            }
        }
        previousLocation = currentLocation;

    }

    private float getAverageSpeed(float distance, float timeTaken) {
        //float minutes = timeTaken/60;
        //float hours = minutes/60;
        float speed = 0;
        if(distance > 0) {
            float distancePerSecond = timeTaken > 0 ? distance/timeTaken : 0;
            float distancePerMinute = distancePerSecond*60;
            float distancePerHour = distancePerMinute*60;
            speed = distancePerHour > 0 ? (distancePerHour/1000) : 0;
        }

        return speed;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    public class DownloadImage extends AsyncTask<String, Void, Bitmap> {
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

    private void goLoginScreen() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void logout(View view) {
        LoginManager.getInstance().logOut();
        goLoginScreen();
    }






}
