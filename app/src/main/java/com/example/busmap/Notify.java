package com.example.busmap;

import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

public class Notify {
    public static void Exit(Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("xác nhận để thoát.");
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setMessage("Bạn có muốn thoát?");
        builder.setCancelable(false);
        builder.setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                System.exit(1);
            }
        });
        builder.setNegativeButton("Không đồng ý", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
