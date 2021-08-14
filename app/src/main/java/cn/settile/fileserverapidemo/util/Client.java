package cn.settile.fileserverapidemo.util;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;

import cn.settile.fileserverapidemo.api.Api;
import cn.settile.fileserverapidemo.model.DownloadParams;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Client {
    /**
     * Client单例
     */
    private static Client instance = null;
    /**
     * retrofit 实例对象
     */
    private Retrofit retrofit = null;

    private Api api = null;

    private Client(String url){
        retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(Api.class);
    }

    public static Client getInstance(String url){
        if(instance==null){
            instance = new Client(url);
        }
        return instance;
    }

    public void upload(File file, Callback<ResponseBody> callback){
        String s = MultipartBody.FORM.toString();
        RequestBody requestBody = RequestBody.create( MultipartBody.FORM,file);
        MultipartBody.Part multipartBody = MultipartBody.Part.createFormData("uploadfile", file.getName(),requestBody);
        api.upload(multipartBody).enqueue(callback);
    }

    public void getVersionInfo(Callback<ResponseBody> callback){
        api.getVersionInfo().enqueue(callback);
    }

    public void download(DownloadParams downloadParams,Callback<ResponseBody> callback){
        api.download(downloadParams).enqueue(callback);
    }

    public static String getMimeType(File file){
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        return fileNameMap.getContentTypeFor(file.getName());
    }
}
