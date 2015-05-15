package koemdzhiev.com.stormy;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;

/**
 * Created by koemdzhiev on 15/05/2015.
 */
public class WIFIDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(R.string.error_title)
                .setMessage("There is no internet connection!")
                .setPositiveButton(R.string.error_ok_button_text,null);

        AlertDialog dialog = builder.create();
        return dialog;
    }
}
