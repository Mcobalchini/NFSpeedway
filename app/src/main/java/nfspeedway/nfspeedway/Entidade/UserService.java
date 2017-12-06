package nfspeedway.nfspeedway.Entidade;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface UserService {
    @GET("/user")
    Call<List<SpeedPessoa>> getAllSpeedPessoa();

    @GET("/user/{id}")
    Call<List<SpeedPessoa>> getSpeedPessoaById(@Path("id") int id);

    @POST("./")
    Call<SpeedPessoa> createSpeedPessoa(@Body SpeedPessoa speedPessoa);

    @POST("./")
    Call<SpeedPessoa> createListSpeedPessoa(@Body List<SpeedPessoa> speedPessoa);
}