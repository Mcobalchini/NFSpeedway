
package nfspeedway.nfspeedway.Entidade;

/**
 * Created by Matheus Eduardo on 05/12/2017.
 */
public class SpeedPessoa {

    private int id;
    private String codigoFb;
    private String nome;
    private float velocidade;
    

    public int getid() {
        return id;
    }


    public String getCodigoFb() {
        return codigoFb;
    }

    public void setCodigoFb(String codigoFb) {
        this.codigoFb = codigoFb;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public float getVelocidade() {
        return velocidade;
    }

    public void setVelocidade(float velocidade) {
        this.velocidade = velocidade;
    }
}
