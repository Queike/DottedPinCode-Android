package com.queike.dottedpincode

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import kotlinx.android.synthetic.main.dotted_pin_code_layout.view.*

private const val PIN_CODE_MAX_LENGTH = 6

class DottedPinCode(context: Context, attrs: AttributeSet): ConstraintLayout(context, attrs) {

    private val _isInputComplete = MutableLiveData<Boolean>()

    val isInputComplete: LiveData<Boolean>
        get() = _isInputComplete

    private val pinCodeDots
        get() = listOf(
            pinDot1ImageView,
            pinDot2ImageView,
            pinDot3ImageView,
            pinDot4ImageView,
            pinDot5ImageView,
            pinDot6ImageView
        )

    init {
        View.inflate(context, R.layout. dotted_pin_code_layout, this)
        setOnClickListeners()
        observePinCodeEditTextContent()
    }

    private fun setOnClickListeners() {
        pinCodeInputBackgroundImageView.setOnClickListener {
            pinCodeHiddenEditText.isFocusableInTouchMode = true
            pinCodeHiddenEditText.requestFocus()
            showSoftKeyboard(pinCodeHiddenEditText)
        }
    }

    private fun showSoftKeyboard(view: View) {
        if (view.requestFocus()) {
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    fun hideSoftKeyboard(view: View) {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    private fun fillNextPinCodeDot() {
        val dotToChange = pinCodeDots.firstOrNull {
            it?.drawable?.constantState == ContextCompat.getDrawable(
                context!!,
                R.drawable.gray_round_filled
            )?.constantState
        }
        dotToChange?.setImageResource(R.drawable.dark_gray_round_filled)
    }

    private fun clearLastPinCodeDot() {
        val dotToChange = pinCodeDots.lastOrNull {
            it?.drawable?.constantState == ContextCompat.getDrawable(
                context!!,
                R.drawable.dark_gray_round_filled
            )?.constantState
        }
        dotToChange?.setImageResource(R.drawable.gray_round_filled)
    }

    private fun clearAllPinCodeDots() {
        pinCodeDots.forEach {
            it.setImageResource(R.drawable.gray_round_filled)
        }
    }

    private fun observePinCodeEditTextContent() {
        pinCodeHiddenEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = Unit
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(pinCode: CharSequence?, start: Int, before: Int, count: Int) {
                if (isAnotherSignEntered(pinCode)) {
                    fillNextPinCodeDot()
                    if (hasPinCodeMaxLength(pinCode)) {
                        hideSoftKeyboard(pinCodeHiddenEditText)
                        _isInputComplete.value = true
                    }
                } else if (isAnotherSignDeleted(pinCode)) {
                    clearLastPinCodeDot()
                }
            }
        })
    }

    private fun isAnotherSignEntered(pinCode: CharSequence?): Boolean {
        return pinCode!!.length > getNumberOfHighlightedPinDots()
    }

    private fun hasPinCodeMaxLength(pinCode: CharSequence?): Boolean {
        return pinCode!!.length == PIN_CODE_MAX_LENGTH
    }

    private fun isAnotherSignDeleted(pinCode: CharSequence?): Boolean {
        return pinCode!!.length < getNumberOfHighlightedPinDots()
    }

    private fun getNumberOfHighlightedPinDots(): Int {
        return pinCodeDots.count {
            it?.drawable?.constantState == ContextCompat.getDrawable(
                context!!,
                R.drawable.dark_gray_round_filled
            )?.constantState
        }
    }

    fun getText(): String {
        return pinCodeHiddenEditText.text.toString()
    }

    fun clearEditText() {
        pinCodeHiddenEditText.text.clear()
        clearAllPinCodeDots()
        _isInputComplete.value = false
    }

    fun clearAndAnimatePinCode() {
        YoYo.with(Techniques.Shake).duration(500).repeat(0).playOn(pinDotsHolderLinearLayout)
        pinCodeHiddenEditText.text.clear()
        clearAllPinCodeDots()
    }
}