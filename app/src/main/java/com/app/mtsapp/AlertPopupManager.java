package com.app.mtsapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.TextView;

public class AlertPopupManager {

    Context context;
    String alertTitle;
    Boolean quitApp; //Одређује да ли ће притискање опције "да" на прозору угасити апликацију или ће вратити корисника на мени

    //Конструктор
    public AlertPopupManager(Context c, String title, boolean quit) {
        context = c;
        alertTitle = title;
        quitApp = quit;
    }

    //Прикажe прозор који пита корисника да ли жели да изађе из апликације/врати се на главни мени (зависно од boolean-a quit)
    public void showAlertDialog() {

        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(context);

        //Постављање (дугачког) наслова прозора
        TextView titleTextView = new TextView(context);
        titleTextView.setText(alertTitle);
        titleTextView.setPadding(5, 5, 5, 5);
        titleTextView.setTextSize(20);
        builder.setCustomTitle(titleTextView);

        builder.setPositiveButton(context.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Ако треба да се угаси апликација, угаси је не дозвољавајући враћање назад на изгубљене активитије у меморији
                if (quitApp) {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    context.startActivity(intent);
                    ((Activity) context).finish();
                    System.exit(0);
                } else {
                    //Ако не треба, врати се на мени
                    context.startActivity(new Intent(context, MainActivity.class));
                    ((Activity) context).finish();
                }

            }
        }).setNegativeButton(context.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Ако не жели, уклони прозор
                dialog.cancel();
            }
        });

        builder.show();
    }
}
