package info.example.my.Activity;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;

import info.example.my.R;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import java.io.File;
import jxl.write.biff.RowsExceededException;

public class FirstAct extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first_activity); // activity_first.xml 레이아웃 파일을 사용합니다.


        fetchDataAndTransition();
    }

    public void writeExcel(String date, String tmx, String tmn) {
        try {
            // 파일 경로 설정
            File file = new File(getExternalFilesDir(null), "tmdata.xls");

            WritableWorkbook workbook;

            // 파일 존재 여부 확인 후 열기
            if (file.exists()) {
                Workbook existingWorkbook = Workbook.getWorkbook(file);
                workbook = Workbook.createWorkbook(file, existingWorkbook);
            } else {
                workbook = Workbook.createWorkbook(file);
            }

            WritableSheet sheet = workbook.getSheet(0);
            if (sheet == null) {
                sheet = workbook.createSheet("Sheet1", 0);
            }

            // 현재 행 수를 구하여 마지막 행 아래에 추가
            int newRow = sheet.getRows(); // 현재 사용 중인 행 수를 가져옴

            // 데이터 추가
            sheet.addCell(new Label(0, newRow, date));
            sheet.addCell(new Label(1, newRow, tmx));
            sheet.addCell(new Label(2, newRow, tmn));

            // 파일에 변경 사항 쓰기
            workbook.write();

            // 작업 후 파일 닫기
            workbook.close();

        } catch (WriteException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (BiffException e) {
            throw new RuntimeException(e);
        }
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

