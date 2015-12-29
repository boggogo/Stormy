package koemdzhiev.com.stormy.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;

import koemdzhiev.com.stormy.R;

/**
 * Created by koemdzhiev on 29/12/15.
 */
public class WhatsNewDialogCreator {
    AlertDialog.Builder mBuilder;
    AlertDialog mDialog;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    public WhatsNewDialogCreator(final Context context, SharedPreferences preferences) {
        sharedPref = preferences;
        editor = preferences.edit();
        mBuilder =
                new AlertDialog.Builder(context).
                        setTitle("What's new in 2.0 update").
        setPositiveButton("Next", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(context).
                                setTitle("What's new in 2.0 update").
                                setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        editor.putInt(context.getString(R.string.saved_if_whats_new_seen), 0);
                                        editor.apply();
                                        dialog.dismiss();
                                    }
                                }).
                                setView(R.layout.alert_dialog_whats_new_swipe_refresh_layout).
                setMessage("Swipe down to refresh...");
                builder.create().show();
            }
        }).setMessage("Swipe left/right to change screens").
                setView(R.layout.alert_dialog_whats_new_swipe_left_right_layout)
                        .setNeutralButton("Done", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                editor.putInt(context.getString(R.string.saved_if_whats_new_seen), 0);
                                editor.apply();
                                mDialog.dismiss();
            }
        });
        mDialog = mBuilder.create();
    }

    public void show(){
        mDialog.show();
    }
}
