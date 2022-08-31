package com.olehel.cameraview

import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.core.widget.addTextChangedListener

fun EditText.onKeyboardDoneClick(onEvent: () -> Unit) {
    this.setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            onEvent()
            true
        } else false
    }
}
fun EditText.textChanged(onEvent: (isCorrect: Boolean) -> Unit) {
    this.addTextChangedListener (
        beforeTextChanged = {s, start, before, count->

        },
        onTextChanged = {s, start, before, count->

        },
        afterTextChanged = {editable->
             editable?.isNotBlank()?.let { onEvent(it) } ?: onEvent(false)
        })
    }


