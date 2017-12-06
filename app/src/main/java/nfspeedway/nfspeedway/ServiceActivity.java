package nfspeedway.nfspeedway;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Matheus Eduardo on 05/12/2017.
 */

public class ServiceActivity extends Service {

    boolean flag = true;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand() {
        new ServiceThread().start();
        return START_NOT_STICKY;
    }

    class ServiceThread extends Thread {

        public void run(){

            while (flag) {

            }
        }
    }

    public void onDestroy() {
        flag = false;
    }

}
