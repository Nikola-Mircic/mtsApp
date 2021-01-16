package com.app.mtsapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.app.mtsapp.location.service.ServiceHandler;

import java.util.Calendar;

public class Settings extends AppCompatActivity {

    private LanguageManager languageManager;

    private static int[] notificationToggleIcons;
    private ToggleButton notificationOne, notificationTwo, notificationThree; //Паљење и гашење појединачних нотификација
    private SharedPreferences.Editor sharedPreferencesEditor;
    private SwitchCompat trackerSwitch; //ПКонтролише праћења локације и слања нотифиакција
    private SwitchCompat dailyNotificationsSwitch; //Контролише слање дневних нотификација са саветима

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        languageManager = new LanguageManager(this);
        languageManager.checkLocale();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button englishButton = findViewById(R.id.englishButton), serbianLatinButton = findViewById(R.id.serbianLatinButton), serbianCyrillicButton = findViewById(R.id.serbianCyrillicButton);
        //Постави језик на енглески
        englishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                languageManager.setLocale("en", true);
            }
        });
        //Постави језик на српску латиницу
        serbianLatinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                languageManager.setLocale("sr", true);
            }
        });
        //Постави језик на српску ћирилицу
        serbianCyrillicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                languageManager.setLocale("sr-rRS", true);
            }
        });

        notificationToggleIcons = new int[]{
                R.drawable.put_on_mask,
                R.drawable.disinfect_clothes,
                R.drawable.disinfect_hands,
                R.drawable.mask_notification_disabled,
                R.drawable.clothes_notification_disabled,
                R.drawable.hands_notification_disabled
        };

        //Референцирај шерд префс и тогл дугмиће за нотификације
        SharedPreferences sharedPreferences = getSharedPreferences("SharedPreferences", MODE_PRIVATE);
        sharedPreferencesEditor = sharedPreferences.edit();
        notificationOne = findViewById(R.id.notificationOne);
        notificationTwo = findViewById(R.id.notificationTwo);
        notificationThree = findViewById(R.id.notificationThree);
        trackerSwitch = findViewById(R.id.trackerSwitch);
        dailyNotificationsSwitch = findViewById(R.id.dailyNotificationsSwitch);

        loadButtonStates(); //Учита сачувана стања дугмића за нотификације сачувана у уређају

        //Сачувај промене стања коришћења нотификација у уређају и промени иконицу одговарајућег дугмета
        notificationOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPreferencesEditor.putBoolean("notificationOne", notificationOne.isChecked()).apply();
                setNotificationIcons();
            }
        });
        notificationTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPreferencesEditor.putBoolean("notificationTwo", notificationTwo.isChecked()).apply();
                setNotificationIcons();
            }
        });
        notificationThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPreferencesEditor.putBoolean("notificationThree", notificationThree.isChecked()).apply();
                setNotificationIcons();
            }
        });

        //Покрени/заустави сервис за праћење локације и слање нотификација везаних за локацију
        trackerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ServiceHandler.startTrackingService(Settings.this);
                } else {
                    ServiceHandler.stopTrackingService(Settings.this);
                }
                sharedPreferencesEditor.putBoolean("trackerSwitch", isChecked).apply();
            }
        });

        dailyNotificationsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sharedPreferencesEditor.putBoolean("sendDailyNotifications", isChecked).apply();
                Intent temp = new Intent(getApplicationContext(), MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, temp, 0);
                if (isChecked) {
                    Calendar c = Calendar.getInstance();

                    c.set(Calendar.HOUR_OF_DAY, 20);
                    c.set(Calendar.MINUTE, 0);
                    c.set(Calendar.SECOND, 0);

                    AlarmManager manager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);

                    manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
                    Log.i("AlarmSchedule", "Alarm manager is set!!");
                } else {
                    AlarmManager manager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
                    manager.cancel(pendingIntent);
                }
            }
        });
    }

    //Учита сачувана стања дугмића за нотификације сачувана у уређају и промени стања дугмића, као и прекидача за сервис за праћење
    private void loadButtonStates() {
        SharedPreferences sharedPreferences = getSharedPreferences("SharedPreferences", MODE_PRIVATE);

        notificationOne.setChecked(sharedPreferences.getBoolean("notificationOne", true));
        notificationTwo.setChecked(sharedPreferences.getBoolean("notificationTwo", true));
        notificationThree.setChecked(sharedPreferences.getBoolean("notificationThree", true));
        setNotificationIcons();

        trackerSwitch.setChecked(sharedPreferences.getBoolean("trackerSwitch", false));
        dailyNotificationsSwitch.setChecked(sharedPreferences.getBoolean("sendDailyNotifications", false));
    }

    //Питај корисника да ли жели да изађе са тренутног екрана када притисне дугме за враћање назад
    @Override
    public void onBackPressed() {
        //Прикажи упозорење
        InfoPopup infoPopup = new InfoPopup(Settings.this, false, false);
        infoPopup.showDialog();
    }

    //Постави одговарајуће иконице за слике дугмића за паљење и гашење одређених обавештења
    private void setNotificationIcons() {
        if (notificationOne.isChecked()) {
            notificationOne.setBackgroundResource(notificationToggleIcons[0]);
        } else {
            notificationOne.setBackgroundResource(notificationToggleIcons[3]);
        }

        if (notificationTwo.isChecked()) {
            notificationTwo.setBackgroundResource(notificationToggleIcons[1]);
        } else {
            notificationTwo.setBackgroundResource(notificationToggleIcons[4]);
        }

        if (notificationThree.isChecked()) {
            notificationThree.setBackgroundResource(notificationToggleIcons[2]);
        } else {
            notificationThree.setBackgroundResource(notificationToggleIcons[5]);
        }
    }
}