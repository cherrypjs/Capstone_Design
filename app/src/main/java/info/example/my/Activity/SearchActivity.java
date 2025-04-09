package info.example.my.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.Calendar;
import java.util.Date;



import info.example.my.R;

import info.example.my.etc.Tmdata;
import info.example.my.etc.Weatherdata;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class SearchActivity extends AppCompatActivity {
    private TextView tv_tmp, tv_wind, tv_sky, tv_rain,tv_time, tv_pop, tv_sno,tv_tmn,tv_tmx,tv_reh,test_gps;
    private String nx = "", ny = "", address = "";
    private Handler handler = new Handler(Looper.getMainLooper());

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);

        preferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        editor = preferences.edit();






        tv_sky = findViewById(R.id.tv_cloud);
        tv_tmp = findViewById(R.id.tv_current_temperature);
        tv_tmn = findViewById(R.id.tv_tmn);
        tv_tmx = findViewById(R.id.tv_tmx);
        tv_time = findViewById(R.id.tv_time);
        tv_wind = findViewById(R.id.tv_wind);
        tv_rain = findViewById(R.id.tv_rain);
        tv_sno = findViewById(R.id.tv_sno);
        tv_pop = findViewById(R.id.tv_pop);
        tv_reh = findViewById(R.id.tv_reh);
        test_gps =findViewById(R.id.test_gps);

        Intent intent = getIntent();
        String selectedLocation = null;
        if (intent != null && intent.hasExtra("selected_location")) {
            selectedLocation = intent.getStringExtra("selected_location");
            Log.d("SelectedLocation", selectedLocation);  // 로그로 값 확인
        }
        test_gps.setText(selectedLocation);
        String[] local = selectedLocation.split(" ");

        String localName0 = local[0];
        String localName1 = local[1];
        readExcel(localName0, localName1);
        new Thread(() -> {
            // Weatherdata 및 Tmdata 인스턴스 생성

            Weatherdata at = new Weatherdata(this); // Context 올바르게 전달
            Tmdata tm = new Tmdata(this );

            // 현재 시간 가져오기
            LocalDateTime time1 = LocalDateTime.now();
            String tvtime1 = time1.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            String HHtime = time1.format(DateTimeFormatter.ofPattern("HH00"));

            // UI 시간 설정
            handler.post(() -> tv_time.setText(tvtime1));

            String weatherData = "";
            String tmData = "";

            try {
                // API 호출
                weatherData = String.valueOf(at.Weather(HHtime, nx, ny));
                tmData = String.valueOf(tm.Weather(nx, ny));
            } catch (IOException | JSONException e) {
                Log.e("WeatherError", "Error fetching data: " + e.getMessage());
            }

            // 데이터 파싱 및 UI 업데이트
            final String[] weatherArray = weatherData.split("  ");
            final String[] tmArray = tmData.split("  ");

            handler.post(() -> {
                if (tv_sky != null) {
                    tv_sky.setText(weatherArray.length > 0 ? weatherArray[0] : " ");
                }

                if (tv_rain != null) {
                    tv_rain.setText(weatherArray.length > 1 ? weatherArray[1] : "error");
                }

                if (tv_tmp != null) {
                    tv_tmp.setText(weatherArray.length > 2 ? weatherArray[2] + "°C" : "error");
                }

                if (tv_wind != null) {
                    tv_wind.setText(weatherArray.length > 3 ? weatherArray[3] + "m/s" : "error");
                }

                if (tv_pop != null) {
                    tv_pop.setText(weatherArray.length > 4 ? "강수확률" + weatherArray[4] + "%" : "error");
                }

                if (tv_sno != null) {
                    tv_sno.setText(weatherArray.length > 5 ? weatherArray[5] : "error");
                }

                if (tv_reh != null) {
                    tv_reh.setText(weatherArray.length > 6 ? weatherArray[6] + "%" : "error");
                }

                if (tv_tmx != null) {
                    tv_tmx.setText(tmArray.length > 0 ? tmArray[0] + "C" : "error");
                }

                if (tv_tmn != null) {
                    tv_tmn.setText(tmArray.length > 1 ? tmArray[1] + "C" : "error");
                }
                updateWeatherUI(weatherArray);
            });
        }).start();
        // 새로운 스레드 시작
    }

    // 현재 시간을 가져오는 메소드
    private String getCurrentTime() {
        long now = System.currentTimeMillis();
        Date mDate = new Date(now);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH00");
        return simpleDateFormat.format(mDate);

    }
    public void readExcel(String localName0, String localName1) {
        try {
            InputStream is = getBaseContext().getResources().getAssets().open("local_name.xls");
            Workbook wb = Workbook.getWorkbook(is);

            if (wb != null) {
                Sheet sheet = wb.getSheet(0);   // 시트 불러오기
                if (sheet != null) {
                    int colTotal = sheet.getColumns();    // 전체 컬럼
                    int rowIndexStart = 1;                  // row 인덱스 시작
                    int rowTotal = sheet.getColumn(colTotal - 1).length;

                    for (int row = rowIndexStart; row < rowTotal; row++) {
                        String cell0Contents = sheet.getCell(0, row).getContents();
                        Log.i("EXCEL_DEBUG", "cell0Contents: " + cell0Contents);

                        if (cell0Contents.contains(localName0)) {
                            String cell1Contents = sheet.getCell(1, row).getContents();
                            Log.i("EXCEL_DEBUG1", "cell0Contents: " + cell1Contents);
                            if (cell1Contents.contains(localName1)) {
                                // nx, ny 값을 가져오고 루프 종료
                                nx = sheet.getCell(3, row).getContents();  // nx
                                ny = sheet.getCell(4, row).getContents();
                            }
                        }
                    }
                }
                wb.close(); // 워크북 닫기
            }
        } catch (IOException e) {
            Log.i("READ_EXCEL1", e.getMessage());
            e.printStackTrace();
        } catch (BiffException e) {
            Log.i("READ_EXCEL1", e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateWeatherUI(String[] weatherArray) {
        String skyCondition = weatherArray[0]; // 하늘 상태
        String rainCondition = weatherArray[1]; // 강수량

        // 시간대에 따른 처리 (아침, 점심, 저녁)
        String timeOfDay = getTimeOfDay(); // 아침/점심/저녁 반환 메서드

        RelativeLayout mainContainer = findViewById(R.id.mainContainer);

        if (skyCondition.equals("맑음") && rainCondition.equals("강수없음")) {
            // 맑은 날씨, 비 없음
            if (timeOfDay.equals("아침")) {
                mainContainer.setBackgroundResource(R.drawable.morning_background);
            } else if (timeOfDay.equals("점심")) {
                mainContainer.setBackgroundResource(R.drawable.noontime_background);
            } else {
                mainContainer.setBackgroundResource(R.drawable.evening_background);
            }
        } else if (skyCondition.equals("구름많음") || skyCondition.equals("흐림")) {
            // 구름 낀 날
            if (timeOfDay.equals("아침")) {
                mainContainer.setBackgroundResource(R.drawable.morning_cloud_background);
            } else if (timeOfDay.equals("점심")) {
                mainContainer.setBackgroundResource(R.drawable.noontime_cloud_background);
            } else {
                mainContainer.setBackgroundResource(R.drawable.evening_cloud_background);
            }
        } else if (!rainCondition.equals("강수없음")) {
            // 비 오는 날
            if (timeOfDay.equals("아침")) {
                mainContainer.setBackgroundResource(R.drawable.morning_rain_background);
            } else if (timeOfDay.equals("점심")) {
                mainContainer.setBackgroundResource(R.drawable.noontime_rain_background);
            } else {
                mainContainer.setBackgroundResource(R.drawable.evening_rain_background);
            }
        } else if (skyCondition.equals("눈")) {
            // 눈 오는 날
            if (timeOfDay.equals("아침")) {
                mainContainer.setBackgroundResource(R.drawable.morning_snow_background);
            } else if (timeOfDay.equals("점심")) {
                mainContainer.setBackgroundResource(R.drawable.noontime_snow_background);
            } else {
                mainContainer.setBackgroundResource(R.drawable.evening_snow_background);
            }
        } else {
            // 기본 배경
            mainContainer.setBackgroundResource(R.drawable.noontime_cloud_background);
        }

    }
    private String getTimeOfDay() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= 6 && hour < 12) {
            return "아침";
        } else if (hour >= 12 && hour < 18) {
            return "점심";
        } else {
            return "저녁";
        }
    }
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater =getMenuInflater();
        inflater.inflate(R.menu.menu_activity,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_settings) {// 다이얼로그 생성 및 표시
            showSettingsDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSettingsDialog() {
        String[] options = {"알림 설정", "소리 설정", "옵션 3"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("설정")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // 알림 설정
                            showNotificationSettingsDialog();
                            break;
                        case 1: // 소리 설정
                            showSoundSettingsDialog();
                            break;
                        case 2: // 옵션 3
                            Intent intent3 = new Intent(this, FirstAct.class);//fragment 사용해서 위에 창하나 띄우는 형식으로 행ㅇ야할듯? 돌아오기 해야함
                            startActivity(intent3);
                            break;
                    }
                })
                .setNegativeButton("취소", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }
    private void showNotificationSettingsDialog() {
        boolean isNotificationEnabled = preferences.getBoolean("notifications_enabled", false); // 기본값: false

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("알림 설정")
                .setMessage("현재 알림 상태: " + (isNotificationEnabled ? "켜짐" : "꺼짐"))
                .setPositiveButton(isNotificationEnabled ? "알림 끄기" : "알림 켜기", (dialog, which) -> {
                    // 상태 변경 및 저장
                    editor.putBoolean("notifications_enabled", !isNotificationEnabled);
                    editor.apply();
                    Toast.makeText(this, "알림이 " + (!isNotificationEnabled ? "켜졌습니다." : "꺼졌습니다."), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("닫기", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void showSoundSettingsDialog() {
        boolean isSoundEnabled = preferences.getBoolean("sound_enabled", false); // 기본값: false

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("소리 설정")
                .setMessage("현재 소리 상태: " + (isSoundEnabled ? "켜짐" : "꺼짐"))
                .setPositiveButton(isSoundEnabled ? "소리 끄기" : "소리 켜기", (dialog, which) -> {
                    // 상태 변경 및 저장
                    editor.putBoolean("sound_enabled", !isSoundEnabled);
                    editor.apply();
                    Toast.makeText(this, "소리가 " + (!isSoundEnabled ? "켜졌습니다." : "꺼졌습니다."), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("닫기", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }
}


