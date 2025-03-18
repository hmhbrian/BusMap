package com.example.busmap;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.busmap.R;

public class SettingFragment extends Fragment {

    private Switch switchNotification;
    private static final int REQUEST_CODE = 100;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        // Ánh xạ switch
        switchNotification = view.findViewById(R.id.switch_notification);

        // Kiểm tra quyền thông báo (chỉ yêu cầu cho Android 13 trở lên)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Nếu chưa cấp quyền thì switch vẫn bật và yêu cầu quyền khi bật
                switchNotification.setEnabled(true);
                boolean isNotificationEnabled = PreferenceManager.getDefaultSharedPreferences(getContext())
                        .getBoolean("notification_enabled", true);  // Mặc định là bật thông báo
                switchNotification.setChecked(isNotificationEnabled);
            } else {
                // Nếu đã cấp quyền thì bật switch và lấy trạng thái
                switchNotification.setEnabled(true);
                boolean isNotificationEnabled = PreferenceManager.getDefaultSharedPreferences(getContext())
                        .getBoolean("notification_enabled", true);
                switchNotification.setChecked(isNotificationEnabled);
            }
        } else {
            // Nếu phiên bản thấp hơn Android 13, luôn bật switch
            switchNotification.setEnabled(true);
            boolean isNotificationEnabled = PreferenceManager.getDefaultSharedPreferences(getContext())
                    .getBoolean("notification_enabled", true);
            switchNotification.setChecked(isNotificationEnabled);
        }

        // Lắng nghe sự thay đổi trạng thái của switch
        switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Kiểm tra quyền thông báo trước khi bật/tắt
                if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    // Nếu chưa cấp quyền, yêu cầu quyền
                    ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE);
                } else {
                    // Lưu trạng thái mới vào SharedPreferences khi quyền đã được cấp
                    PreferenceManager.getDefaultSharedPreferences(getContext())
                            .edit()
                            .putBoolean("notification_enabled", isChecked)
                            .apply();
                }
            } else {
                // Đối với các phiên bản Android thấp hơn 13, chỉ cần lưu trạng thái
                PreferenceManager.getDefaultSharedPreferences(getContext())
                        .edit()
                        .putBoolean("notification_enabled", isChecked)
                        .apply();
            }
        });

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Quyền thông báo đã được cấp, bật lại switch và lưu trạng thái
                switchNotification.setEnabled(true);
                PreferenceManager.getDefaultSharedPreferences(getContext())
                        .edit()
                        .putBoolean("notification_enabled", true)
                        .apply();
                Toast.makeText(getContext(), "Quyền thông báo đã được cấp!", Toast.LENGTH_SHORT).show();
            } else {
                // Người dùng từ chối quyền, tắt switch và lưu trạng thái là tắt
                switchNotification.setChecked(false);
                PreferenceManager.getDefaultSharedPreferences(getContext())
                        .edit()
                        .putBoolean("notification_enabled", false)
                        .apply();
                Toast.makeText(getContext(), "Quyền thông báo bị từ chối!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
