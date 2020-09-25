package com.elearn.trainor.HelperClasses;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;
import android.view.KeyEvent;

import com.elearn.trainor.R;

public class AlertDialogManager {

    public static Dialog showDialog(Context context, String title, String message, boolean showNegativeButton, final IClickListener iClickListener) {
        Dialog alert = null;
        if (!((Activity) context).isFinishing()) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setTitle(title);
            dialog.setCancelable(false);
            dialog.setMessage(message);
            dialog.setPositiveButton(context.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (iClickListener != null) {
                        iClickListener.onClick();
                        dialog.dismiss();
                    } else {
                        dialog.dismiss();
                    }
                }
            });
            if (showNegativeButton) {
                dialog.setNegativeButton(context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
            }
            alert = dialog.create();
            alert.setCanceledOnTouchOutside(false);
            alert.show();
        }
        return alert;
    }

    public static void showCustomDialog(Context context, String title, String message, boolean showNegativeButton, final IClickListener iClickListener, final IClickListener iClickListenerCancel, String OK_Button_Text, String Cancel_ButtonText, String textColor) {
        if (!((Activity) context).isFinishing()) {
            AlertDialog.Builder dialog;
            if (!textColor.equals("")) {
                dialog = new AlertDialog.Builder(context, R.style.AlertDialogCustom);
            } else {
                dialog = new AlertDialog.Builder(context);
            }
            dialog.setTitle(title);
            dialog.setMessage(message);
            dialog.setPositiveButton(OK_Button_Text, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (iClickListener != null) {
                        dialog.dismiss();
                        iClickListener.onClick();
                    } else {
                        dialog.dismiss();
                    }
                }
            });
            if (showNegativeButton)
                dialog.setNegativeButton(Cancel_ButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (iClickListenerCancel != null) {
                            dialog.dismiss();
                            iClickListenerCancel.onClick();
                        } else {
                            dialog.dismiss();
                        }
                    }
                });

            dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    return keyCode == event.KEYCODE_BACK;
                }
            });
            AlertDialog alert = dialog.create();
            alert.setCanceledOnTouchOutside(false);
            alert.show();
        }
    }

    public static void alternateCustomDialog(Context context, String title, String message, boolean enoughSpace, boolean showNegativeButton, final IClickListener iClickListener, final IClickListener iClickListenerCancel, String OK_Button_Text, String Cancel_ButtonText, String textColor, String From) {
        if (!((Activity) context).isFinishing()) {
            AlertDialog.Builder dialog;
            if (!textColor.equals("")) {
                dialog = new AlertDialog.Builder(context, R.style.AlternateAlertDialogCustom);
            } else {
                dialog = new AlertDialog.Builder(context);
            }
            dialog.setTitle(title);
            if (From.equals("CourseDownload")) {
                if (enoughSpace) {
                    dialog.setMessage(message);
                } else {
                    dialog.setMessage(Html.fromHtml(message + "<br>" + "<font color=#FF0000>" + context.getResources().getString(R.string.not_enough_space) + "</font>"));
                }
            } else {
                dialog.setMessage(message);
            }
            dialog.setPositiveButton(OK_Button_Text, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (iClickListener != null) {
                        dialog.dismiss();
                        iClickListener.onClick();
                    } else {
                        dialog.dismiss();
                    }
                }
            });
            if (showNegativeButton)
                dialog.setNegativeButton(Cancel_ButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (iClickListenerCancel != null) {
                            dialog.dismiss();
                            iClickListenerCancel.onClick();
                        } else {
                            dialog.dismiss();
                        }
                    }
                });

            dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    return keyCode == event.KEYCODE_BACK;
                }
            });
            AlertDialog alert = dialog.create();
            alert.setCanceledOnTouchOutside(false);
            alert.show();
        }
    }
}
