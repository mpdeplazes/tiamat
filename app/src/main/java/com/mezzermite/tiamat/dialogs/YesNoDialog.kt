package com.mezzermite.tiamat.dialogs

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import com.mezzermite.tiamat.R

class YesNoDialog {
    companion object {
        val YES = DialogInterface.BUTTON_POSITIVE
        val NO = DialogInterface.BUTTON_NEGATIVE

        fun makeAndShow(
            context: Context,
            message: String,
            yesText: String = context.getString(R.string.dialog_yes),
            noText: String = context.getString(R.string.dialog_no),
            onClickFun: (dialog: DialogInterface?, which: Int) -> Unit
        ) {
            val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
                onClickFun(dialog, which)
            }

            AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton(yesText, dialogClickListener)
                .setNegativeButton(noText, dialogClickListener)
                .show()
        }
    }
}