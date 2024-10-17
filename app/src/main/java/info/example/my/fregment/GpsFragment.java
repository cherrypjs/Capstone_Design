//package info.example.my.fregment;
//
//import android.Manifest;
//import android.app.AlertDialog;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.net.Uri;
//import android.os.Bundle;
//import android.provider.Settings;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//import androidx.fragment.app.Fragment;
//
//import org.json.JSONException;
//
//import java.io.IOException;
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//import java.time.ZonedDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.concurrent.Executors;
//
//import info.example.my.R;
//import info.example.my.DAO.WeatherDAO;
//import info.example.my.etc.apiTest;
//
//public class GpsFragment extends Fragment {
//
//    private TextView weatherTextView; // 날씨 정보를 표시할 TextView
//
//    public GpsFragment() {
//        // Required empty public constructor
//    }
//
//    public static GpsFragment newInstance(String param1, String param2) {
//        GpsFragment fragment = new GpsFragment();
//        Bundle args = new Bundle();
//        args.putString("param1", param1);
//        args.putString("param2", param2);
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            // 여기에 필요한 매개변수를 추가적으로 처리할 수 있습니다.
//        }
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_gps, container, false);
//
//        // TextView 초기화
//        weatherTextView = view.findViewById(R.id.tv_location); // TextView의 ID를 지정해 주세요
//
//        // 날씨 정보 가져오기
//        getWeatherInfo();
//
//        return view;
//    }
//
//    private void getWeatherInfo() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    // 네트워크 작업
//                    apiTest at = new apiTest();
//                    LocalDateTime time1 = LocalDateTime.now();
//                    ZonedDateTime utcTime = time1.atZone(ZoneId.of("UTC"));
//                    ZonedDateTime koreaTime = utcTime.withZoneSameInstant(ZoneId.of("Asia/Seoul"));
//                    String time = koreaTime.format(DateTimeFormatter.ofPattern("HH00"));
//
//                    // API 호출
//                    WeatherDAO weatherData = at.Weather(time);
//
//                    // UI 작업은 메인 스레드에서만 가능
//                    getActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            // UI 업데이트 (TextView에 날씨 정보 표시 등)
//                            if (weatherData != null) {
//                                String weatherInfo = "날씨: " + weatherData.getSky() + "\n" +
//                                        "온도: " + weatherData.getTemperature() + "\n" +
//                                        "바람: " + weatherData.getWind();
//                                weatherTextView.setText(weatherInfo);
//                            } else {
//                                weatherTextView.setText("날씨 정보를 가져오지 못했습니다.");
//                            }
//                        }
//                    });
//                } catch (IOException | JSONException e) {
//                    Log.e("WeatherError", e.getMessage());
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//    }
//}