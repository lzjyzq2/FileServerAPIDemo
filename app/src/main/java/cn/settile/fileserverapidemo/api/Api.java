package cn.settile.fileserverapidemo.api;

import cn.settile.fileserverapidemo.model.DownloadParams;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Streaming;

public interface Api {

    @POST("upload")
    @Multipart
    Call<ResponseBody> upload(@Part MultipartBody.Part part);

    @GET("api/getversioninfo")
    Call<ResponseBody> getVersionInfo();

    @Headers("Content-Type:application/octet-stream")
    @Streaming
    @POST("api/download")
    Call<ResponseBody> download(@Body DownloadParams downloadParams);
}
