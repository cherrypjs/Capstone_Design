package info.example.my.Activity;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import info.example.my.R;

public class FirstAct extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first_activity); // activity_first.xml 레이아웃 파일을 사용합니다.

        // API 데이터를 가져오는 메서드를 호출하고 일정 시간 후 MainActivity로 전환
        fetchDataAndTransition();
    }

    private void fetchDataAndTransition() {
        // 예를 들어, 2초 후에 MainActivity로 전환
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(FirstAct.this, WeatherAct.class); // WeatherAct로 전환
            startActivity(intent);
            finish(); // FirstActivity 종료
        }, 3000); // 2초 후
    }
}

