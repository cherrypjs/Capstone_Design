package info.example.my.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import info.example.my.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AlarmSettingsFragment#} factory method to
 * create an instance of this fragment.
 */
public class AlarmSettingsFragment extends DialogFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alarm_setting, container, false);

        TimePicker timePicker = view.findViewById(R.id.timePicker);
        EditText messageInput = view.findViewById(R.id.messageInput);
        Button saveButton = view.findViewById(R.id.saveButton);

        saveButton.setOnClickListener(v -> {
            // 알람 시간 및 메시지 저장 로직
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();
            String message = messageInput.getText().toString();

            if (message.isEmpty()) {
                Toast.makeText(getContext(), "메시지를 입력하세요!", Toast.LENGTH_SHORT).show();
                return;
            }

            // 알람 예약 로직 (Activity와 통신 필요 시 Listener 사용)
            Toast.makeText(getContext(), "알람이 설정되었습니다!", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
