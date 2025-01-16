package info.example.my.etc;

import android.content.Context;
import android.util.Log;
import android.widget.Toolbar;

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





public class Tmdata extends Thread {



    private static String tmx,tmn;

    private Context mContext; // Context 변수

    String numOfRows = "500";  // 한 페이지 결과 수
    // 요청 자료 형식

    public Tmdata(Context context) {
        this.mContext = context;
    }



    public String Weather(String nx,String ny) throws IOException, JSONException {

        Log.d(nx+ny, "weatherDAO123: ");
        String base_date;

        String apiUrl = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst";
        String serviceKey = "7OOR3ha2Lf%2BGOHFeDKltquZvhrK2uoOKuQkwrr%2B9LZaOcKew8L9MWRMqnVcHI%2BfmCf1mjOAzkwsPF2E3H4v3HQ%3D%3D";  // API 키
        String base_time = "0500";
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
        Log.d("@@@", result);
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

            if (category.equals("TMX")) {
                //최고기온
                tmx = fcstValue+"  ";
            }
            if (category.equals("TMN")) {
                //최저기온
                tmn = fcstValue+"  ";
            }

        }
        return tmx+tmn ;

    }

}