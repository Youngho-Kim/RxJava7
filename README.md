###Retrofit을 사용한 RxJava 활용
[전체소스코드](https://github.com/Youngho-Kim/RxJava7/blob/master/app/src/main/java/com/fastcampus/kwave/android/rxjava7/MainActivity.java)  


##### Retrofit 적용
1. Gradle에 라이브러리 추가
```java
    implementation 'com.squareup.retrofit2:retrofit:2.3.0'      // 레트로핏
    implementation 'com.squareup.retrofit2:converter-gson:2.3.0'        // gson 컨버터
    implementation 'com.squareup.retrofit2:adapter-rxjava2:2.3.0'   // rxjava adapter
```

2. 받아올 Json 데이터의 형식에 맞게 바꾸어준다.
여기서는 
```java
.addConverterFactory(GsonConverterFactory.create())         // Json Parser 추가
 // Observable을 리턴 타입으로 사용하기 위해서 RxJava2CallAdapterFactory를 어댑터로 사용
 ```
 
 
 3. interface를 만든다
 ```java
 interface IWeather{     // Retrofit interface 선언
     @GET("{key}/json/RealtimeWeatherStation/{start}/{count}/{name}")
     Observable<Data> getData(@Path("key") String server_key
                            , @Path("start") int begin_index
                            , @Path("count") int offset
                            , @Path("name") String gu);
 }
```

4. 소스에 적용
```java
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
```

5. Callback을 구현 - 여기서는 Observable을 사용하고 있으므로 
```java
 .addCallAdapterFactory(RxJava2CallAdapterFactory.create()) 
```
 을 사용하여 Observable을 리턴 타입으로 사용하게 한다.