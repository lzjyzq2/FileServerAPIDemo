package cn.settile.fileserverapidemo.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

import cn.settile.fileserverapidemo.databinding.ActivityMainBinding;
import cn.settile.fileserverapidemo.model.DownloadParams;
import cn.settile.fileserverapidemo.util.Client;
import cn.settile.fileserverapidemo.util.FileUtil;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_DOCUMENT_CODE = 2;
    private static final int REQUEST_PERMISSION_CODE = 2;

    private ActivityMainBinding activityMainBinding;

    private Client client = null;
    private File selectedFile = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        // 检查申请权限
        int checkResult = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        boolean hasPermission =checkResult== PackageManager.PERMISSION_GRANTED;
        if(!hasPermission){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE);
        }
        // 按钮事件
        activityMainBinding.setting.setOnClickListener(v->{
            String url = activityMainBinding.url.getText().toString();
            if(url.matches("^((http)|(https))://[a-zA-Z\\.:0-9]+/$")){
                client = Client.getInstance(url);
                getVersionInfo();
            }else {
                Toast.makeText(this,"地址错误",Toast.LENGTH_SHORT).show();
            }
        });
        activityMainBinding.choose.setOnClickListener(v -> {
            chooseFile();
        });
        activityMainBinding.upload.setOnClickListener(v -> {
            if(selectedFile!=null&&selectedFile.exists()){
                if(client!=null){
                    client.upload(selectedFile, new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if(response.isSuccessful()){
                                try {
                                    String rep = response.body().string();
                                    if(rep.equals("Suss")){
                                        Toast.makeText(MainActivity.this,"上传成功",Toast.LENGTH_SHORT).show();
                                    }else if(rep.equals("fail")){
                                        Toast.makeText(MainActivity.this,"上传失败",Toast.LENGTH_SHORT).show();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }else {
                                Toast.makeText(MainActivity.this,"上传失败",Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {

                        }
                    });
                }else {
                    Toast.makeText(this,"设置服务器地址",Toast.LENGTH_SHORT).show();
                }
            }else {
                Toast.makeText(this,"文件不存在",Toast.LENGTH_SHORT).show();
            }
        });
        activityMainBinding.download.setOnClickListener(v -> {
            String dir = activityMainBinding.downloadFileDir.getText().toString();
            String name = activityMainBinding.downloadFileName.getText().toString();
            if("".equals(dir.trim())||"".equals(name.trim())){
                Toast.makeText(this,"请填写下载文件所在文件夹及名称",Toast.LENGTH_SHORT).show();
            }else {
                String[] path = dir.split(File.separator);
                DownloadParams downloadParams = new DownloadParams(path,name);
                client.download(downloadParams, new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if(response.isSuccessful()){
                            new Thread(() -> {
                                boolean state = FileUtil.writeResponseBodyToCache(MainActivity.this,response.body(),name);
                                runOnUiThread(() -> {
                                    if(state){
                                        Toast.makeText(MainActivity.this,"下载成功！",Toast.LENGTH_SHORT).show();
                                    }else {
                                        Toast.makeText(MainActivity.this,"下载失败！",Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }).start();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e("MainActivity","下载异常",t);
                    }
                });
            }
        });
        setContentView(activityMainBinding.getRoot());
    }

    /**
     * 选择文件
     */
    private void chooseFile(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_DOCUMENT_CODE);
    }

    /**
     * 获取版本信息
     */
    public void getVersionInfo(){
        client.getVersionInfo(new Callback<ResponseBody>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()){
                    try {
                        String s = response.body().string();
                        Type type = new TypeToken<Map<String,String>>(){}.getType();
                        Map<String,String> data = new Gson().fromJson(s,type);
                        String version = data.get("version");
                        Log.d("getVersionInfo",version);
                        activityMainBinding.version.setText("version:"+version);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    /**
     * 选择文件回调
     * @param requestCode 请求码
     * @param resultCode 结果码
     * @param resultData 请求值
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        if (requestCode == PICK_DOCUMENT_CODE
                && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                // 文件URI
                uri = resultData.getData();
                String path = null;
                try {
                    // 转换为File 原理是拷贝到缓存
                    selectedFile = FileUtil.from(MainActivity.this,uri);
                    // 获取真实路径
                    path = selectedFile.getPath();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // 显示选中文件路径
                activityMainBinding.uploadFilePath.setText(path);
            }else {
                Toast.makeText(this,"获取文件路径失败",Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, resultData);
    }
}