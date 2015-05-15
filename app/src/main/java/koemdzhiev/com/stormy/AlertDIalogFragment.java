package koemdzhiev.com.stormy;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;

/**
 * Created by koemdzhiev on 15/05/2015.
 */
public class AlertDIalogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context mContext = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                .setTitle(mContext.getString(R.string.error_title))
                .setMessage(mContext.getString(R.string.error_massage))
                .setPositiveButton(mContext.getString(R.string.error_ok_button_text), null);
        AlertDialog dialog = builder.create();
        return dialog;
    }
}
