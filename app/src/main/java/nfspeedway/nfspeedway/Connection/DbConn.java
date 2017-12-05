package nfspeedway.nfspeedway.connection;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Matheus Eduardo on 05/12/2017.
 */

public class DbConn {

    private static SQLiteDatabase db;

    public static SQLiteDatabase getConnection(Context c){
        if(db == null){
            db = c.openOrCreateDatabase("database",Context.MODE_PRIVATE,null);
            db.execSQL( "CREATE TABLE IF NOT EXISTS SpeedPessoa ( _id INTEGER PRIMARY KEY AUTOINCREMENT, nome TEXT, velocidade REAL, codigo TEXT)" );
        }
        return db;
    }
}