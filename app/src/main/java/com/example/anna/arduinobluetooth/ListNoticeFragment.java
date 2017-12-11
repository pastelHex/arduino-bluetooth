package com.example.anna.arduinobluetooth;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by Anna on 08.12.2017.
 */

public class ListNoticeFragment extends DialogFragment {

    ListNoticeListener mListener;

    public interface ListNoticeListener{
        public void onListClick(ListNoticeFragment dialogFragment, int i);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (ListNoticeListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()+ " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        //String[] list = savedInstanceState.getStringArray("list");
        Bundle b = getArguments();
        String[] list = b.getStringArray("list");
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Avalible devices").setItems(list, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mListener.onListClick(ListNoticeFragment.this, i);
            }
        });
        return builder.create();
    }
}
