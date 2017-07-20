package com.fastcampus.kwave.android.rxjava7;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.fastcampus.kwave.android.rxjava7.domain.Data;
import com.fastcampus.kwave.android.rxjava7.domain.Row;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

public class MainActivity extends AppCompatActivity {

    // http://openAPI.seoul.go.kr:8088/(인증키)/xml/RealtimeWeatherStation/1/5/중구
    // 4a704a59546b776138307975426366

    public static final String SERVER = "http://openAPI.seoul.go.kr:8088/";
    public static final String SERVER_KEY = "4a704a59546b776138307975426366";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. 레트로핏 생성
        Retrofit client = new Retrofit.Builder()
                .baseUrl(SERVER)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        // 2. 서비스 생성
        IWeather service = client.create(IWeather.class);

        // 3. 옵저버블 생성
        Observable<Data> observable = service.getData(SERVER_KEY, 1,10, "서초");

        // 4. 발행 시작
        observable.subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                // 구독 시작
                .subscribe(
                        data -> {
                            Row rows[] = data.getRealtimeWeatherStation().getRow();
                            for(Row row : rows){
                                Log.i("Weather","지역명"+row.getSTN_NM());
                                Log.i("Weather","온도"+row.getSAWS_TA_AVG()+"도");
                                Log.i("Weather","습도"+row.getSAWS_HD()+"%");
                            }
                        }
                );
    }
}

interface IWeather{
    @GET("{key}/json/RealtimeWeatherStation/{start}/{count}/{name}")
    Observable<Data> getData(@Path("key") String server_key
                           , @Path("start") int begin_index
                           , @Path("count") int offset
                           , @Path("name") String gu);
}
