package com.app.mtsapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

public class InfoPopup extends Dialog {
    private Context context;

    public InfoPopup(Context contextArg) {
        super(contextArg);
        context = contextArg;
        LanguageManager languageManager = new LanguageManager(context);
        languageManager.checkLocale();
    }

    //Приказиује прозорчић са информацијама и правилима понашања током пандемије
    public void showRulebookDialog() {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.rulebook_info_popup);

        TextView rulebookText = dialog.findViewById(R.id.rulebookText);
        rulebookText.setMovementMethod(new ScrollingMovementMethod());

        ImageButton dismissButton = dialog.findViewById(R.id.cancelButton);
        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void showHelpDialog() {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.help_popup);

        ImageButton dismissButton = dialog.findViewById(R.id.cancelButton);
        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    //Прикажe прозор који пита корисника да ли жели да изађе из апликације/врати се на главни мени (зависно од boolean-a quit)
    public void showBackButtonDialog(final boolean shouldQuitApp) {

        //Постави изглед прозорчића
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.back_alert_dialog);

        /*Промени наслов прозорчића у односу на његов намену:
        ако се позове на главном менију нек пита корисника да ли жели да изађе из апликације
        а ако се зове из другог екрана нек пита корисника да ли жели да се врати на главни екран */
        String dialogTitleText;
        if (shouldQuitApp) {
            dialogTitleText = context.getResources().getString(R.string.quitApp);
        } else {
            dialogTitleText = context.getResources().getString(R.string.backToMenu);
        }
        TextView dialogTitle = dialog.findViewById(R.id.dialogTitle);
        dialogTitle.setText(dialogTitleText);

        //Дугме за отказивање изласка са тренутног активитија
        ImageButton dismissButton = dialog.findViewById(R.id.cancelButton);
        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Ако не жели, уклони прозор
                dialog.dismiss();
            }
        });

        ImageButton continueButton = dialog.findViewById(R.id.yesButton);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Ако треба да се угаси апликација, угаси је не дозвољавајући враћање назад на изгубљене активитије у меморији
                if (shouldQuitApp) {
                    dialog.dismiss();
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    context.startActivity(intent);
                    ((Activity) context).finish();
                    System.exit(0);
                } else {
                    //Ако не треба, врати се на мени
                    dialog.dismiss();
                    context.startActivity(new Intent(context, MainActivity.class));
                    ((Activity) context).finish();
                }
            }
        });

        dialog.show();
    }
}
