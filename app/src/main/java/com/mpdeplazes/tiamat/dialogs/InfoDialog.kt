package com.mpdeplazes.tiamat.dialogs

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import com.mpdeplazes.tiamat.R

class InfoDialog {

    companion object {
        fun makeAndShow(
            context: Context,
            message: String,
            onClickFun: (dialog: DialogInterface?, which: Int) -> Unit
        ) {
            val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
                onClickFun(dialog, which)
            }

            AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton(
                    context.getString(R.string.dialog_okay),
                    dialogClickListener
                )
                .show()
        }
    }
}