package com.fastcampus.kwave.android.rxjava7;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.fastcampus.kwave.android.rxjava7.domain.Data;
import com.fastcampus.kwave.android.rxjava7.domain.Row;
import com.fastcampus.kwave.android.rxjava7.domain.Weather;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
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
    public List<Weather> weather = new ArrayList<>();
    WeatherAdapter adapter = null;
    Button btnFind;
    EditText editFind;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        btnClick();
    }

    private void initView(){
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        btnFind = (Button) findViewById(R.id.btnFind);
        editFind = (EditText) findViewById(R.id.editFind);


        adapter = new WeatherAdapter(weather,getBaseContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    public void btnClick() {
        btnFind.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // 1. 레트로핏 생성
                Retrofit client = new Retrofit.Builder()
                        .baseUrl(SERVER)        // retrofit을 연결할 주소
                        .addConverterFactory(GsonConverterFactory.create())         // Json Parser 추가
                        // Observable을 리턴 타입으로 사용하기 위해서 RxJava2CallAdapterFactory를 어댑터로 사용
                        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())  //RxJava2를 사용하기 위해 추가
                        .build();               //인터페이스 연결

                // 2. 서비스 생성
                IWeather service = client.create(IWeather.class);

                // 3. 옵저버블 생성
                Observable<Data> observable = service.getData(SERVER_KEY, 1,10, editFind.getText().toString());
                                                                                                 // editFind.getText().toString() : 입력 받을 텍스트
                // 4. 발행 시작
                observable.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        // 구독 시작
                        .subscribe(
                                data -> {
                                    Row rows[] = data.getRealtimeWeatherStation().getRow();
//                                    weather.clear();      // 이전에 입력한 리스트를 없애고 현재 입력한 리스트만 나오게 함
                                    for(Row row : rows){
                                        Log.i("rows","=================rows개수 : "+rows.length);
                                        Weather weatherInfo = new Weather();
                                        weatherInfo.STN_NM = row.getSTN_NM();
                                        weatherInfo.SAWS_TA_AVG = row.getSAWS_TA_AVG();
                                        weatherInfo.SAWS_HD = row.getSAWS_HD();
                                        weather.add(weatherInfo);

//                                Log.i("Weather","지역명"+row.getSTN_NM());
//                                Log.i("Weather","온도"+row.getSAWS_TA_AVG()+"도");
//                                Log.i("Weather","습도"+row.getSAWS_HD()+"%");
                                    }
                                    editFind.setText("");
                                    adapter.notifyDataSetChanged();
                                }
                        );
            }
        });
    }

    public class WeatherAdapter extends RecyclerView.Adapter<Holder>{
        public List<Weather> data = null;
        private LayoutInflater inflater = null;

        public WeatherAdapter(List<Weather> data, Context context) {
            this.data = data;
            inflater =  LayoutInflater.from(context);
        }

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.item_list_weather, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(Holder holder, int position) {
            Weather weather = data.get(position);
            Log.d("weather","----------------------------------------weather : "+weather);
            holder.setTextName(weather.STN_NM);
            holder.setTextTemperture(weather.SAWS_TA_AVG);
            holder.setTextHumidity(weather.SAWS_HD);
            holder.setPosition(position);
            Log.d("holder","----------------------------------------holder : "+holder);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public Weather getItem(int position){
            return data.get(position);
        }

        public void setData(List<Weather> weather) {
            this.data = weather;
        }
    }


    public class Holder extends RecyclerView.ViewHolder{
        int position;
        private TextView textName;
        private TextView textTemperture;
        private TextView textHumidity;

        public Holder(View itemView) {
            super(itemView);
            textName = (TextView) itemView.findViewById(R.id.textName);
            textTemperture = (TextView) itemView.findViewById(R.id.textTemperture);
            textHumidity = (TextView) itemView.findViewById(R.id.textHumidity);
        }

        public String getTextName() {
            return textName.getText().toString();
        }

        public void setTextName(String Name) {
            textName.setText(Name+"구");
        }

        public String  getTextTemperture() {
            return textTemperture.getText().toString();
        }

        public void setTextTemperture(String Temperture) {
            textTemperture.setText("온도 : " +Temperture+"도");
        }

        public String getTextHumidity() {
            return textHumidity.getText().toString();
        }

        public void setTextHumidity(String Humidity) {
            textHumidity.setText("습도 : " +Humidity+"%");
        }

        public void setPosition(int position) {
            this.position = position;
        }
    }
}

interface IWeather{     // Retrofit interface 선언
    @GET("{key}/json/RealtimeWeatherStation/{start}/{count}/{name}")
    Observable<Data> getData(@Path("key") String server_key
                           , @Path("start") int begin_index
                           , @Path("count") int offset
                           , @Path("name") String gu);
}
