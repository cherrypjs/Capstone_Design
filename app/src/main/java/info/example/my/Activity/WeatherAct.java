package info.example.my.Activity;

import static android.util.Log.ERROR;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
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
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


import info.example.my.Fragment.AlarmSettingsFragment;
import info.example.my.DAO.WeatherDAO;
import info.example.my.R;
import info.example.my.etc.GpsTracker;
import info.example.my.etc.Tmdata;
import info.example.my.etc.Weatherdata;
import info.example.my.func.AlarmReceiver;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;


public class WeatherAct extends AppCompatActivity {


    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private TextView test_gps;
    private TextView tv_tmp, tv_wind, tv_sky, tv_rain, tv_time, tv_pop, tv_sno, tv_tmn, tv_tmx, tv_reh;

    private List<String> searchList;
    String ttsData="";

    private GpsTracker gpsTracker;
    private String nx = "", ny = "", address = "";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private static final int REQUEST_ALARM_PERMISSION = 1324;
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    private Calendar savedCalendar;
    private String savedMessage;
    private TextToSpeech tts;



    private Handler handler = new Handler(Looper.getMainLooper());

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_activity);
        searchItem();
        RelativeLayout mainContainer = findViewById(R.id.mainContainer);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        preferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        editor = preferences.edit();


        tv_sky = findViewById(R.id.tv_cloud);
        tv_tmp = findViewById(R.id.tv_current_temperature);
        tv_tmn = findViewById(R.id.tv_tmn);
        tv_tmx = findViewById(R.id.tv_tmx);
        tv_time=findViewById(R.id.tv_time);
        tv_wind=findViewById(R.id.tv_wind);
        tv_rain=findViewById(R.id.tv_rain);
        tv_sno=findViewById(R.id.tv_sno);
        tv_pop=findViewById(R.id.tv_pop);
        tv_reh=findViewById(R.id.tv_reh);

        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();
        } else {
            checkRunTimePermission();
        }

        gpsTracker = new GpsTracker(WeatherAct.this);
        double latitude = gpsTracker.getLatitude();  // x좌표
        double longitude = gpsTracker.getLongitude();// y좌표

        // 현재 위치 주소 가져오기
        String address = getCurrentAddress(latitude, longitude);
        test_gps = findViewById(R.id.test_gps); // test_gps 초기화

        String[] local = address.split(" ");
        String localName0 = local[1];
        String localName1 = local[2];
        String localName2 = local[3];
        Log.d("localname", localName0+localName1+localName2);


        readExcel(localName0,localName1, localName2);
        String currentaddress = local[1] + " " +  local[2] + " " + local[3];
        test_gps.setText(currentaddress);
        Toast.makeText(WeatherAct.this, "현재위치 \n위도 " + latitude + "\n경도 " + longitude, Toast.LENGTH_LONG).show();
        // 검색한 위치 주소 가져오기




        // 별도의 스레드에서 날씨 데이터 가져오기
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
                ttsData = currentaddress+"의 현재 하늘은"+weatherArray[0]+"이고 기온은"+weatherArray[2]+"도입니다.";
                Log.d("tts",ttsData);
                speak(ttsData);
            });
        }).start();
        // 새로운 스레드 시작
    }
    private void speak(String text) {
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != ERROR){
                    int result = tts.setLanguage(Locale.KOREA); // 언어 선택
                    if(result == TextToSpeech.LANG_NOT_SUPPORTED || result == TextToSpeech.LANG_MISSING_DATA){
                        Log.e("TTS", "This Language is not supported");
                    }else{
                        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                    }
                }else{
                    Log.e("TTS", "Initialization Failed!");
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(tts!=null){ // 사용한 TTS객체 제거
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
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
    public String getCurrentAddress(double latitude, double longitude) {
        // 지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 7);
        } catch (IOException ioException) {
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";
        }

        Address address = addresses.get(0);
        return address.getAddressLine(0).toString() + "\n";
    }

    private void showDialogForLocationServiceSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(WeatherAct.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n" + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE:
                if (checkLocationServicesStatus()) {
                    checkRunTimePermission();
                    return;
                }
                break;
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    void checkRunTimePermission() {
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(WeatherAct.this, Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(WeatherAct.this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            // 위치 값을 가져올 수 있음
        } else {
            ActivityCompat.requestPermissions(WeatherAct.this, REQUIRED_PERMISSIONS, GPS_ENABLE_REQUEST_CODE);
        }
    }

    // 알람 권한 요청
    private void requestAlarmPermission(Calendar calendar, String message) {
        savedCalendar = calendar;
        savedMessage = message;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestPermissions(new String[]{Manifest.permission.SCHEDULE_EXACT_ALARM}, REQUEST_ALARM_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {
        super.onRequestPermissionsResult(permsRequestCode, permissions, grandResults);

        // 위치 권한 처리
        if (permsRequestCode == GPS_ENABLE_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {
            boolean check_result = true;
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if (check_result) {
                Toast.makeText(this, "위치 권한이 승인되었습니다.", Toast.LENGTH_SHORT).show();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {
                    Toast.makeText(this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다.", Toast.LENGTH_LONG).show();
                }
            }
        }

        // 알람 권한 처리
        if (permsRequestCode == REQUEST_ALARM_PERMISSION) {
            if (grandResults.length > 0 && grandResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "알람 권한이 승인되었습니다.", Toast.LENGTH_SHORT).show();
                // 알람 설정 호출
                scheduleAlarm(savedCalendar, savedMessage);
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SCHEDULE_EXACT_ALARM)) {
                    Toast.makeText(this, "알람 권한이 거부되었습니다. 다시 요청하세요.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "알람 권한이 거부되었습니다. 설정(앱 정보)에서 권한을 허용해야 합니다.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    // 알람 권한 요청 메서드 (별도로 호출할 필요 없으면 삭제 가능)
    private void requestAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12 이상에서 필요
            requestPermissions(new String[]{Manifest.permission.SCHEDULE_EXACT_ALARM}, REQUEST_ALARM_PERMISSION);
        }
    }


    public void readExcel(String localName0, String localName1, String localName2) {
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
                        String cell0Contents = sheet.getCell(0, row).getContents().trim();
                        Log.i("EXCEL_DEBUG", "cell0Contents: " + cell0Contents);

                        // localName0이 포함된 경우
                        if (cell0Contents.contains(localName0)) {
                            String cell1Contents = sheet.getCell(1, row).getContents().trim();  // 공백 제거
                            String cell2Contents = sheet.getCell(2, row).getContents().trim();  // 공백 제거
                            Log.i("EXCEL_DEBUG", "cell1Contents: " + cell1Contents);
                            Log.i("EXCEL_DEBUG1", "cell2Contents: " + cell2Contents);

                            // localName1이 있는지 먼저 확인
                            if (cell1Contents.equalsIgnoreCase(localName1)) {
                                // localName2가 있는지 확인
                                if (cell2Contents.equalsIgnoreCase(localName2)) {
                                    // nx, ny 값을 가져오고 루프 종료
                                    nx = sheet.getCell(3, row).getContents();  // nx
                                    ny = sheet.getCell(4, row).getContents();  // ny
                                    Log.i("READ_EXCEL1", "x = " + nx + "  y = " + ny);
                                    break; // 루프 종료
                                } else {
                                    // localName2가 없으면 localName1로 nx, ny 값을 가져옴
                                    if (!cell2Contents.isEmpty()) {
                                        nx = sheet.getCell(3, row).getContents();  // nx
                                        ny = sheet.getCell(4, row).getContents();  // ny
                                        Log.i("READ_EXCEL1_LOCALNAME1", "x = " + nx + "  y = " + ny);
                                        break; // 루프 종료
                                    } else {
                                        // localName2가 없고 cell2Contents도 비어있으면 다음 행으로 계속 진행
                                        Log.i("EXCEL_DEBUG", "cell2Contents is empty, continuing to next row");
                                        continue;
                                    }
                                }
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


    public void searchItem() {
        searchList = new ArrayList<>();
        settingList();

        AutoCompleteTextView autoCompleteTextView = findViewById(R.id.autoCompleteTextView);

        // AutoCompleteTextView에 어댑터 연결
        ArrayAdapter<String> adapter = new ArrayAdapter<>(WeatherAct.this,
                android.R.layout.simple_dropdown_item_1line, searchList);
        autoCompleteTextView.setAdapter(adapter);

        // 아이템 선택 시 처리
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                Intent intent = new Intent(WeatherAct.this, SearchActivity.class);
                intent.putExtra("selected_location", selectedItem);
                startActivity(intent);

            }
        });
    }

    private void settingList(){
        searchList.add("서울특별시 종로구");
        searchList.add("서울특별시 중구");
        searchList.add("서울특별시 용산구");
        searchList.add("서울특별시 성동구");
        searchList.add("서울특별시 광진구");
        searchList.add("서울특별시 동대문구");
        searchList.add("서울특별시 중랑구");
        searchList.add("서울특별시 성북구");
        searchList.add("서울특별시 강북구");
        searchList.add("서울특별시 도봉구");
        searchList.add("서울특별시 노원구");
        searchList.add("서울특별시 은평구");
        searchList.add("서울특별시 서대문구");
        searchList.add("서울특별시 마포구");
        searchList.add("서울특별시 양천구");
        searchList.add("서울특별시 강서구");
        searchList.add("서울특별시 구로구");
        searchList.add("서울특별시 금천구");
        searchList.add("서울특별시 영등포구");
        searchList.add("서울특별시 동작구");
        searchList.add("서울특별시 관악구");
        searchList.add("서울특별시 서초구");
        searchList.add("서울특별시 강남구");
        searchList.add("서울특별시 송파구");
        searchList.add("서울특별시 강동구");

        searchList.add("부산광역시 중구");
        searchList.add("부산광역시 서구");
        searchList.add("부산광역시 동구");
        searchList.add("부산광역시 영도구");
        searchList.add("부산광역시 부산진구");
        searchList.add("부산광역시 동래구");
        searchList.add("부산광역시 남구");
        searchList.add("부산광역시 북구");
        searchList.add("부산광역시 해운대구");
        searchList.add("부산광역시 사하구");
        searchList.add("부산광역시 금정구");
        searchList.add("부산광역시 강서구");
        searchList.add("부산광역시 연제구");
        searchList.add("부산광역시 수영구");
        searchList.add("부산광역시 사상구");
        searchList.add("부산광역시 기장군");

        searchList.add("대구광역시 중구");
        searchList.add("대구광역시 동구");
        searchList.add("대구광역시 서구");
        searchList.add("대구광역시 남구");
        searchList.add("대구광역시 북구");
        searchList.add("대구광역시 수성구");
        searchList.add("대구광역시 달서구");
        searchList.add("대구광역시 달성군");

        searchList.add("인천광역시 중구");
        searchList.add("인천광역시 동구");
        searchList.add("인천광역시 미추홀구");
        searchList.add("인천광역시 연수구");
        searchList.add("인천광역시 남동구");
        searchList.add("인천광역시 부평구");
        searchList.add("인천광역시 계양구");
        searchList.add("인천광역시 서구");
        searchList.add("인천광역시 강화군");
        searchList.add("인천광역시 옹진군");

        searchList.add("광주광역시 동구");
        searchList.add("광주광역시 서구");
        searchList.add("광주광역시 남구");
        searchList.add("광주광역시 북구");
        searchList.add("광주광역시 광산구");

        searchList.add("대전광역시 동구");
        searchList.add("대전광역시 중구");
        searchList.add("대전광역시 서구");
        searchList.add("대전광역시 유성구");
        searchList.add("대전광역시 대덕구");

        searchList.add("울산광역시 중구");
        searchList.add("울산광역시 남구");
        searchList.add("울산광역시 동구");
        searchList.add("울산광역시 북구");
        searchList.add("울산광역시 울주군");

        searchList.add("세종특별자치시 세종특별자치시");

        searchList.add("경기도 수원시장안구");
        searchList.add("경기도 수원시권선구");
        searchList.add("경기도 수원시팔달구");
        searchList.add("경기도 수원시영통구");
        searchList.add("경기도 성남시수정구");
        searchList.add("경기도 성남시중원구");
        searchList.add("경기도 성남시분당구");
        searchList.add("경기도 의정부시");
        searchList.add("경기도 안양시만안구");
        searchList.add("경기도 안양시동안구");
        searchList.add("경기도 부천시");
        searchList.add("경기도 광명시");
        searchList.add("경기도 평택시");
        searchList.add("경기도 동두천시");
        searchList.add("경기도 안산시상록구");
        searchList.add("경기도 안산시단원구");
        searchList.add("경기도 고양시덕양구");
        searchList.add("경기도 고양시일산동구");
        searchList.add("경기도 고양시일산서구");
        searchList.add("경기도 과천시");
        searchList.add("경기도 구리시");
        searchList.add("경기도 남양주시");
        searchList.add("경기도 오산시");
        searchList.add("경기도 시흥시");
        searchList.add("경기도 군포시");
        searchList.add("경기도 의왕시");
        searchList.add("경기도 하남시");
        searchList.add("경기도 용인시처인구");
        searchList.add("경기도 용인시기흥구");
        searchList.add("경기도 용인시수지구");
        searchList.add("경기도 파주시");
        searchList.add("경기도 이천시");
        searchList.add("경기도 안성시");
        searchList.add("경기도 김포시");
        searchList.add("경기도 화성시");
        searchList.add("경기도 광주시");
        searchList.add("경기도 양주시");
        searchList.add("경기도 포천시");
        searchList.add("경기도 여주시");
        searchList.add("경기도 연천군");
        searchList.add("경기도 가평군");
        searchList.add("경기도 양평군");

        searchList.add("강원도 춘천시");
        searchList.add("강원도 원주시");
        searchList.add("강원도 강릉시");
        searchList.add("강원도 동해시");
        searchList.add("강원도 태백시");
        searchList.add("강원도 속초시");
        searchList.add("강원도 삼척시");
        searchList.add("강원도 홍천군");
        searchList.add("강원도 횡성군");
        searchList.add("강원도 영월군");
        searchList.add("강원도 평창군");
        searchList.add("강원도 정선군");
        searchList.add("강원도 철원군");
        searchList.add("강원도 화천군");
        searchList.add("강원도 양구군");
        searchList.add("강원도 인제군");
        searchList.add("강원도 고성군");
        searchList.add("강원도 양양군");

        searchList.add("충청북도 청주시상당구");
        searchList.add("충청북도 청주시서원구");
        searchList.add("충청북도 청주시흥덕구");
        searchList.add("충청북도 청주시청원구");
        searchList.add("충청북도 충주시");
        searchList.add("충청북도 제천시");
        searchList.add("충청북도 보은군");
        searchList.add("충청북도 옥천군");
        searchList.add("충청북도 영동군");
        searchList.add("충청북도 증평군");
        searchList.add("충청북도 진천군");
        searchList.add("충청북도 괴산군");
        searchList.add("충청북도 음성군");
        searchList.add("충청북도 단양군");

        searchList.add("충청남도 천안시동남구");
        searchList.add("충청남도 천안시서북구");
        searchList.add("충청남도 공주시");
        searchList.add("충청남도 보령시");
        searchList.add("충청남도 아산시");
        searchList.add("충청남도 서산시");
        searchList.add("충청남도 논산시");
        searchList.add("충청남도 계룡시");
        searchList.add("충청남도 당진시");
        searchList.add("충청남도 금산군");
        searchList.add("충청남도 부여군");
        searchList.add("충청남도 서천군");
        searchList.add("충청남도 청양군");
        searchList.add("충청남도 홍성군");
        searchList.add("충청남도 예산군");
        searchList.add("충청남도 태안군");

        searchList.add("전라북도 전주시완산구");
        searchList.add("전라북도 전주시덕진구");
        searchList.add("전라북도 군산시");
        searchList.add("전라북도 익산시");
        searchList.add("전라북도 정읍시");
        searchList.add("전라북도 남원시");
        searchList.add("전라북도 김제시");
        searchList.add("전라북도 완주군");
        searchList.add("전라북도 진안군");
        searchList.add("전라북도 무주군");
        searchList.add("전라북도 장수군");
        searchList.add("전라북도 임실군");
        searchList.add("전라북도 순창군");
        searchList.add("전라북도 고창군");
        searchList.add("전라북도 부안군");

        searchList.add("전라남도 목포시");
        searchList.add("전라남도 여수시");
        searchList.add("전라남도 순천시");
        searchList.add("전라남도 나주시");
        searchList.add("전라남도 광양시");
        searchList.add("전라남도 담양군");
        searchList.add("전라남도 곡성군");
        searchList.add("전라남도 구례군");
        searchList.add("전라남도 고흥군");
        searchList.add("전라남도 보성군");
        searchList.add("전라남도 화순군");
        searchList.add("전라남도 장흥군");
        searchList.add("전라남도 강진군");
        searchList.add("전라남도 해남군");
        searchList.add("전라남도 영암군");
        searchList.add("전라남도 무안군");
        searchList.add("전라남도 함평군");
        searchList.add("전라남도 영광군");
        searchList.add("전라남도 장성군");
        searchList.add("전라남도 완도군");
        searchList.add("전라남도 진도군");
        searchList.add("전라남도 신안군");

        searchList.add("경상북도 포항시남구");
        searchList.add("경상북도 포항시북구");
        searchList.add("경상북도 경주시");
        searchList.add("경상북도 김천시");
        searchList.add("경상북도 안동시");
        searchList.add("경상북도 구미시");
        searchList.add("경상북도 영주시");
        searchList.add("경상북도 영천시");
        searchList.add("경상북도 상주시");
        searchList.add("경상북도 문경시");
        searchList.add("경상북도 경산시");
        searchList.add("경상북도 군위군");
        searchList.add("경상북도 의성군");
        searchList.add("경상북도 청송군");
        searchList.add("경상북도 영양군");
        searchList.add("경상북도 영덕군");
        searchList.add("경상북도 청도군");
        searchList.add("경상북도 고령군");
        searchList.add("경상북도 성주군");
        searchList.add("경상북도 칠곡군");
        searchList.add("경상북도 예천군");
        searchList.add("경상북도 봉화군");
        searchList.add("경상북도 울진군");
        searchList.add("경상북도 울릉군");

        searchList.add("경상남도 창원시의창구");
        searchList.add("경상남도 창원시성산구");
        searchList.add("경상남도 창원시마산합포구");
        searchList.add("경상남도 창원시마산회원구");
        searchList.add("경상남도 창원시진해구");
        searchList.add("경상남도 진주시");
        searchList.add("경상남도 통영시");
        searchList.add("경상남도 사천시");
        searchList.add("경상남도 김해시");
        searchList.add("경상남도 밀양시");
        searchList.add("경상남도 거제시");
        searchList.add("경상남도 양산시");
        searchList.add("경상남도 의령군");
        searchList.add("경상남도 함안군");
        searchList.add("경상남도 창녕군");
        searchList.add("경상남도 고성군");
        searchList.add("경상남도 남해군");
        searchList.add("경상남도 하동군");
        searchList.add("경상남도 산청군");
        searchList.add("경상남도 함양군");
        searchList.add("경상남도 거창군");
        searchList.add("경상남도 합천군");

        searchList.add("제주특별자치도 제주시");
        searchList.add("제주특별자치도 서귀포시");

    }
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater =getMenuInflater();
        inflater.inflate(R.menu.menu_activity,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_settings) { // 다이얼로그 생성 및 표시
            showSettingsDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void showSettingsDialog() {
        String[] options = {"알림 on/off", "소리 on/off", "알림 설정 ","재시작"};
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
                        case 2:
                            AlarmSettingsFragment alarmSettingsFragment = new AlarmSettingsFragment();
                            alarmSettingsFragment.show(getSupportFragmentManager(), "AlarmSettingsFragment");
                            break;
                        case 3: // 옵션 3
                            Intent intent3 = new Intent(this, FirstAct.class);//fragment 사용해서 위에 창하나 띄우는 형식으로 행ㅇ야할듯? 돌아오기 해야함
                            startActivity(intent3);
                            break;
                    }
                })
                .setNegativeButton("취소", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }
    private void showNotificationSettingsDialog() {
        Log.d("NotificationSettings", "Notification settings dialog opened"); // 디버깅용 로그
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


    // 정확한 알람 설정 메서드
    private void scheduleAlarm(Calendar calendar, String message) {
        // 권한 확인
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SCHEDULE_EXACT_ALARM) != PackageManager.PERMISSION_GRANTED) {
            // 권한 요청이 필요함
            Toast.makeText(this, "정확한 알람 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 정확한 알람을 예약할 수 있는지 확인
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this, "정확한 알람 예약을 할 수 없습니다. 설정에서 권한을 허용해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("message", message);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            Toast.makeText(this, "알람이 설정되었습니다!", Toast.LENGTH_SHORT).show();
        }
    }

}
