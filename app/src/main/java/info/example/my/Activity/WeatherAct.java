package info.example.my.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.apache.log4j.Logger;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import info.example.my.func.WeatherDAO;
import info.example.my.R;
import info.example.my.etc.GpsTracker;
import info.example.my.etc.Weatherdata;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class WeatherAct extends AppCompatActivity {
    private static final Logger log = Logger.getLogger(WeatherAct.class);

    private WeatherDAO weatherData;
    private TextView tv; // TextView를 참조하기 위한 변수 선언
    private TextView test_gps;
    private TextView tv_tmp, tv_wind, tv_sky, tv_rain,tv_time, tv_pop, tv_sno,tv_tmn,tv_tmx,tv_reh;

    private List<String> searchList;


    private GpsTracker gpsTracker;
    private String nx = "", ny = "", address = "";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    private Handler handler = new Handler(Looper.getMainLooper());

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_activity);
        searchItem();
        // TextView 초기화

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

//        tv_wind = findViewById(R.id.tv_wind);
//        tv_humidity = findViewById(R.id.tv_humidity);
//        tv_sno = findViewById(R.id.tv_sno);
//        tv_rain = findViewById(R.id.tv_rain);
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
        String localName = local[3];
        Log.d(localName, "localName: ");
        readExcel(localName);
        String currentaddress = local[1] + " " +  local[2] + " " + local[3];
        test_gps.setText(currentaddress);
        Toast.makeText(WeatherAct.this, "현재위치 \n위도 " + latitude + "\n경도 " + longitude, Toast.LENGTH_LONG).show();
        // 별도의 스레드에서 날씨 데이터 가져오기
        new Thread(() -> {

            Weatherdata at = new Weatherdata(this);
            LocalDateTime time = LocalDateTime.now();
            String tvtime = time.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            String HHtime = time.format(DateTimeFormatter.ofPattern("HH00"));
            tv_time.setText(tvtime);

            Log.d("timeiswhat1", HHtime);
            String weatherData = "";

            try {
                weatherData = String.valueOf(at.Weather(HHtime,nx,ny));
//
            } catch (IOException | JSONException e) {
                Log.e("WeatherError", e.getMessage());
            }

            // UI 업데이트는 메인 스레드에서 수행
            String[] weatherarray = weatherData.split(" ");
            handler.post(() -> {
                if (tv_sky != null) {
                    if (weatherarray.length > 0) {
                        tv_sky.setText( weatherarray[0]);//하늘상태
                    } else {
                        tv_sky.setText(" ");
                    }
                }

                if (tv_rain != null) {
                    if (weatherarray.length > 1) {
                        tv_rain.setText( weatherarray[1]);//강수량
                    } else{
                        tv_rain.setText("error");
                    }
                }

                if (tv_tmp != null) {
                    if (weatherarray.length > 2) {
                        tv_tmp.setText( weatherarray[2]+"°C");//온도
                    } else {
                        tv_tmp.setText("error");
                    }
                }

                if (tv_wind != null) {
                    if (weatherarray.length > 3) {
                        tv_wind.setText(weatherarray[3]+"m/s");//풍속
                    } else {
                        tv_wind.setText("error");
                    }
                }


                if (tv_pop != null) {
                    if (weatherarray.length > 4) {
                        tv_pop.setText("강수확률"+weatherarray[4]+"%");//습도
                    } else {
                        tv_pop.setText("error");
                    }


                }
                if (tv_sno != null) {
                    if (weatherarray.length > 5) {
                        tv_sno.setText( weatherarray[5]);//적설량
                    } else {
                        tv_sno.setText("error");
                    }
                }
                if (tv_tmx != null) {
                    if (weatherarray.length > 6) {
                        tv_tmx.setText( weatherarray[6]+"°C");//최고기온
                    } else {
                        tv_tmx.setText("error");
                    }
                }
                if (tv_tmn != null) {
                    if (weatherarray.length > 7) {
                        tv_tmn.setText(weatherarray[7]+"°C");//최저기온
                    } else {
                        tv_tmn.setText("error");
                    }
                }
                if (tv_reh != null) {
                    if (weatherarray.length > 8) {
                        tv_reh.setText(weatherarray[8]+"%");//습도
                    } else {
                        tv_reh.setText("error");
                    }
                }
            });

        }).start(); // 새로운 스레드 시작
    }

    // 현재 시간을 가져오는 메소드
    private String getCurrentTime() {
        long now = System.currentTimeMillis();
        Date mDate = new Date(now);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH00");
        return simpleDateFormat.format(mDate);

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
                Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
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
            if (ActivityCompat.shouldShowRequestPermissionRationale(WeatherAct.this, REQUIRED_PERMISSIONS[0])) {
                Toast.makeText(WeatherAct.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(WeatherAct.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(WeatherAct.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {
        super.onRequestPermissionsResult(permsRequestCode, permissions, grandResults);
        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {
            boolean check_result = true;
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }
            if (check_result) {
                // 위치 값을 가져올 수 있음
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {
                    Toast.makeText(WeatherAct.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(WeatherAct.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다.", Toast.LENGTH_LONG).show();
                }
            }
        }

    }



    // 저장한 엑셀파일 읽어오기
    public void readExcel(String localName) {
        try {
            InputStream is = getBaseContext().getResources().getAssets().open("local_name.xls");
            Workbook wb = Workbook.getWorkbook(is);
            weatherData = new WeatherDAO();
            if (wb != null) {
                Sheet sheet = wb.getSheet(0);   // 시트 불러오기
                if (sheet != null) {
                    int colTotal = sheet.getColumns();    // 전체 컬럼
                    int rowIndexStart = 1;                  // row 인덱스 시작
                    int rowTotal = sheet.getColumn(colTotal - 1).length;

                    for (int row = rowIndexStart; row < rowTotal; row++) {
                        String cell0Contents = sheet.getCell(0, row).getContents();
                        String cell1Contents = sheet.getCell(1, row).getContents();

                        if (cell0Contents.contains(localName)|| cell1Contents.contains(localName)) {
                            nx = sheet.getCell(2, row).getContents();  // nx
                            ny = sheet.getCell(3, row).getContents();  // ny // ny
                            row = rowTotal;
                            Log.i("READ_EXCEL1", "x = " + nx + "  y = " + ny);
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

        AutoCompleteTextView autoCompleteTextView = findViewById(R.id.autoCompleteTextView); // 수정된 부분

        // AutoCompleteTextView에 어댑터를 연결합니다.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(WeatherAct.this,
                android.R.layout.simple_dropdown_item_1line, searchList);
        autoCompleteTextView.setAdapter(adapter);
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
}

