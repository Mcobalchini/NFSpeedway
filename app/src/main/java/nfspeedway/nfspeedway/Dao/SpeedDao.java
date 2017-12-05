package nfspeedway.nfspeedway.dao;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import nfspeedway.nfspeedway.connection.DbConn;
import nfspeedway.nfspeedway.ondeestou.entidade.SpeedPessoa;

/**
 * Created by Matheus Eduardo on 05/12/2017.
 */
 
public class SpeedDao {

    private static final String TABLE_NAME = "SpeedPessoa";
    private SQLiteDatabase db;

    public SpeedDao(Context c) {
        db = DbConn.getConnection(c);
    }

    public void incluirRegistro(SpeedPessoa speedPessoa) {
        ContentValues registro = new ContentValues();        
        registro.put("codigo", speedPessoa.getCodigo());
        registro.put("nome", speedPessoa.getNome()); 
		registro.put("velocidade", speedPessoa.Getvelocidade());

        db.insert(TABLE_NAME, null, registro);

    }

    public boolean verifExists(int id) {
        Cursor registro = db.rawQuery("select * from SpeedPessoa where id_ = ?", new int {"id"});
        if(registro.moveToNext()){
            return true;
        }else{
            return false;
        }
    }

    public void deletaRegistros(){
        db.delete(TABLE_NAME,null,null);
    }

    public Cursor listarRegistros() {
        //Cursor para percorrer os registros
        Cursor registros = db.query(TABLE_NAME,
                null, null, null, null, null, null);
            return registros;
        }
}