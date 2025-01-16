package info.example.my.etc;

import android.content.Context;
import android.util.Log;
import android.widget.Toolbar;

import com.google.firebase.BuildConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;





public class Weatherdata extends Thread {




    private static String sky, temperature, wind, rain, snow, pop ,reh;
    private String current_weather_code;
    private Context mContext; // Context 변수
    BuildConfig buildConfig;
    String numOfRows = "80";  // 한 페이지 결과 수
    // 요청 자료 형식

    public Weatherdata(Context context) {
        this.mContext = context;
    }


    public String Weather(String time ,String nx,String ny) throws IOException, JSONException {

        Log.d(nx+ny, "weatherDAO: ");
        String base_date;
        String apiUrl = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst";

        String serviceKey = "7OOR3ha2Lf%2BGOHFeDKltquZvhrK2uoOKuQkwrr%2B9LZaOcKew8L9MWRMqnVcHI%2BfmCf1mjOAzkwsPF2E3H4v3HQ%3D%3D";  // API 키
        String base_time = timeChange(time);
        LocalDate base_date1 = LocalDate.now();
        Log.d( "time~ ",base_time);


        LocalDateTime timenow = LocalDateTime.now();
        String hourtime = timenow.format(DateTimeFormatter.ofPattern("HH00"));
        Log.d("time2!! ",hourtime);

        if (hourtime.equals("0000") || hourtime.equals("0100")){

            LocalDate yesterday =  base_date1.minusDays(1);
            base_date = yesterday.format(DateTimeFormatter.ofPattern("yyyyMMdd"));//base_date1.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            Log.d("base_date1", base_date);

        }else{


            base_date =timenow.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            Log.d("base_date2", base_date);
        }



        String dataType = "json";

        StringBuilder urlBuilder = new StringBuilder(apiUrl);
        urlBuilder.append("?" + URLEncoder.encode("ServiceKey", "UTF-8") + "=" + serviceKey);
        urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=" + URLEncoder.encode(numOfRows, "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("dataType", "UTF-8") + "=" + URLEncoder.encode(dataType, "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("base_date", "UTF-8") + "=" + URLEncoder.encode(base_date, "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("base_time", "UTF-8") + "=" + URLEncoder.encode(base_time, "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("nx", "UTF-8") + "=" + URLEncoder.encode(nx, "UTF-8"));
        urlBuilder.append("&" + URLEncoder.encode("ny", "UTF-8") + "=" + URLEncoder.encode(ny, "UTF-8"));


        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");


        int responseCode = conn.getResponseCode();
        System.out.println("Response Code: " + responseCode);


        BufferedReader rd;
        if (responseCode >= 200 && responseCode <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }

        // 결과 출력
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();
        String result = sb.toString();


        JSONObject jsonObj_1 = new JSONObject(result);
        String response = jsonObj_1.getString("response");
        Log.d("Response Code", result);
        // response 로 부터 body 찾기
        JSONObject jsonObj_2 = new JSONObject(response);
        String body = jsonObj_2.getString("body");


        // body 로 부터 items 찾기
        JSONObject jsonObj_3 = new JSONObject(body);
        String items = jsonObj_3.getString("items");

        // items로 부터 itemlist 를 받기
        JSONObject jsonObj = new JSONObject(items);
        JSONArray jsonArray = jsonObj.getJSONArray("item");

        Log.i("JSONArray Length", (jsonArray.toString()));
        Log.i("Itemsstring", items.toString());

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject item = jsonArray.getJSONObject(i);  // 수정
            String fcstValue = item.getString("fcstValue");
            String category = item.getString("category");
            Log.i("Category", category);
            Log.i("fcstValue", fcstValue);
            //d여기까지 데이터안옴 왜??
            if (category.equals("SKY")) {
                if (fcstValue.equals("1")) {
                    sky = "맑음"+"  ";
                    current_weather_code = "1";
                } else if (fcstValue.equals("2")) {
                    sky = "비"+"  ";
                    current_weather_code = "2";
                } else if (fcstValue.equals("3")) {
                    sky = "구름많음"+"  ";
                    current_weather_code = "3";
                } else if (fcstValue.equals("4")) {
                    sky = "흐림"+"  ";
                    current_weather_code = "4";
                }
            }

            //   POP   강수확률
            //   PTY   강수형태
            //   PCP   1시간 강수량
            //   REH   습도
            //   SNO   적설량
            //   SKY   하늘상태
            //   TMP   1시간 기온
            //   TMN   일 최저기온
            //   TMX   일 최고기온
            //   UUU   풍속(동서성분)
            //   VVV   풍속(남북성분)
            //   WAV   파고   M   8
            //   VEC   풍향   deg   10
            //   WSD   풍속   m/s   10
            if (category.equals("TMP")) {
                //기온
                temperature = fcstValue+"  ";
            }
            if (category.equals("REH")) {
                //습도
                reh = fcstValue+"  ";
            }

            if (category.equals("WSD")) {
                //풍속
                wind = fcstValue+"  " ;
            }

            if (category.equals("PCP")) {
                //강수량
                rain = fcstValue+"  ";
            }
            if (category.equals("SNO")) {
                //적설량
                snow = fcstValue+"  ";
            }
            if (category.equals("POP")) {
                //강수확률
                pop = fcstValue+"  ";
            }




        }
        String Current_Weather= sky + rain + temperature + wind + pop + snow + reh ;
        Log.d("Current_Weather", Current_Weather);
        return sky + rain + temperature + wind + pop + snow + reh ;

    }
    public String timeChange(String time)
    {
        // 현재 시간에 따라 데이터 시간 설정(3시간 마다 업데이트) //

        if (time.equals("0200") || time.equals("0300") || time.equals("0400")) {
            time = "0200";
        } else if (time.equals("0500") || time.equals("0600") || time.equals("0700")) {
            time = "0500";
        } else if (time.equals("0800") || time.equals("0900") || time.equals("1000")) {
            time = "0800";
        } else if (time.equals("1100") || time.equals("1200") || time.equals("1300")) {
            time = "1100";
        } else if (time.equals("1400") || time.equals("1500") || time.equals("1600")) {
            time = "1400";
        } else if (time.equals("1700") || time.equals("1800") || time.equals("1900")) {
            time = "1700";
        } else if (time.equals("2000") || time.equals("2100") || time.equals("2200")) {
            time = "2000";
        } else if (time.equals("2300") || time.equals("0000") || time.equals("0100")) {
            time = "2300";
        }
        return time;
    }
}