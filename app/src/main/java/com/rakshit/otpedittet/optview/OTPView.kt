package com.rakshit.otpedittet.optview

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.widget.addTextChangedListener
import com.rakshit.otpedittet.R

@SuppressLint("NewApi")
class OTPView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyle, defStyleRes) {

    private val itemCount: Int
    private val showCursor: Boolean
    private val inputType: Int
    private val importantForAutofillLocal: Int
    private val autofillHints: String?
    private var itemWidth: Int
    private var itemHeight: Int
    private val cursorColor: Int
    private val allCaps: Boolean
    private val marginBetween: Int
    private val isPassword: Boolean

    private val textSizeDefault: Int
    private val textColor: Int
    private val backgroundImage: Drawable?
    private val font: Typeface?

    private val highlightedTextSize: Int
    private val highlightedTextColor: Int
    private val highlightedBackgroundImage: Drawable?
    private val highlightedFont: Typeface?

    private val filledTextSize: Int
    private val filledTextColor: Int
    private val filledBackgroundImage: Drawable?
    private val filledFont: Typeface?

    private var onFinishFunction: ((String) -> Unit) = {}
    private var onCharacterUpdatedFunction: ((Boolean) -> Unit) = {}

    private val editTexts: MutableList<EditText> = mutableListOf()
    private var focusIndex = 0

    init {
        LayoutInflater.from(context).inflate(R.layout.otp_view_layout, this, true)

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.OTPView,
            0, 0
        )
            .apply {
                try {
                    itemCount = getInteger(R.styleable.OTPView_otp_itemCount, 1)
                    showCursor = getBoolean(R.styleable.OTPView_otp_showCursor, false)
                    inputType = getInteger(R.styleable.OTPView_android_inputType, 0)
                    importantForAutofillLocal =
                        getInteger(R.styleable.OTPView_android_importantForAutofill, 0)
                    autofillHints = getString(R.styleable.OTPView_android_autofillHints)
                    itemWidth = getDimensionPixelSize(R.styleable.OTPView_otp_itemWidth, 44)
                    itemHeight = getDimensionPixelSize(R.styleable.OTPView_otp_itemHeight, 44)
                    cursorColor = getColor(R.styleable.OTPView_otp_cursorColor, Color.BLACK)
                    allCaps = getBoolean(R.styleable.OTPView_otp_allcaps, false)
                    marginBetween = getDimensionPixelSize(
                        R.styleable.OTPView_otp_marginBetween,
                        8.dpTopx
                    )
                    isPassword = getBoolean(R.styleable.OTPView_otp_ispassword, false)

                    textSizeDefault =
                        getDimensionPixelSize(R.styleable.OTPView_otp_textSize, 14.dpTopx)
                    textColor = getInteger(R.styleable.OTPView_otp_textColor, Color.BLACK)
                    backgroundImage =
                        getDrawable(R.styleable.OTPView_otp_backgroundImage) ?: customBackground()
                    font = getFont(R.styleable.OTPView_otp_Font)

                    highlightedTextSize = getDimensionPixelSize(
                        R.styleable.OTPView_otp_highlightedTextSize,
                        textSizeDefault
                    )
                    highlightedTextColor = getInteger(
                        R.styleable.OTPView_otp_highlightedTextColor,
                        textColor
                    )
                    highlightedBackgroundImage =
                        getDrawable(R.styleable.OTPView_otp_highlightedBackgroundImage)
                            ?: backgroundImage
                    highlightedFont = getFont(R.styleable.OTPView_otp_highlightedFont) ?: font

                    filledTextSize = getDimensionPixelSize(
                        R.styleable.OTPView_otp_filledTextSize,
                        textSizeDefault
                    )
                    filledTextColor = getInteger(R.styleable.OTPView_otp_filledTextColor, textColor)
                    filledBackgroundImage =
                        getDrawable(R.styleable.OTPView_otp_filledBackgroundImage)
                            ?: backgroundImage
                    filledFont = getFont(R.styleable.OTPView_otp_filledFont) ?: font

                    initEditTexts()
                } finally {
                    recycle()
                }
            }
    }

    private var disableEditListener: Boolean = false

    // region Init

    private fun initEditTexts() {
        for (x in 0 until itemCount) {
            addEditText(x)
            addListenerForIndex(x)
        }

        styleEditTexts()
        val et = editTexts[0]
        et.postDelayed({
            val editText = editTexts[focusIndex]
            editText.requestFocus()
            styleEditTexts()
            showKeyboard(true, editText)
        }, 100)
    }

    private fun addListenerForIndex(index: Int) {
        editTexts[index].addTextChangedListener {
            if (!disableEditListener) {
                when {
                    editTexts[index].text.isEmpty() -> {
                        changeFocus(false)
                    }
                    editTexts[index].text.length > 1 -> {
                        // Only Taking the last char
                        editTexts[index].setText(it?.first().toString())
                    }
                    else -> {
                        changeFocus(true)
                    }
                }
            }
        }
        editTexts[index].setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DEL &&
                event.action == KeyEvent.ACTION_DOWN
            ) {
                disableEditListener = true
                editTexts[index].setText("")
                changeFocus(false)
                //if(index-1 >= 0)
                //editTexts[index - 1].setText("")
                disableEditListener = false
            }
            if (event.action == KeyEvent.ACTION_DOWN &&
                keyCode == KeyEvent.KEYCODE_ENTER
            ) {
                if (isFilled())
                    onFinishFunction(getStringFromFields())
            }
            return@setOnKeyListener false
        }
        editTexts[index].setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus)
                focusIndex = index
            styleEditTexts()
            v.post {
                if (focusIndex < editTexts.size)
                    editTexts[focusIndex].setSelection(0)
            }
        }

        if (isPassword) {
            editTexts.forEach {
                it.transformationMethod =
                    AsteriskPasswordTransformationMethod()
            }
        }
    }

    private fun changeFocus(increment: Boolean) {
        if (increment) focusIndex++ else focusIndex--

        when {
            focusIndex < 0 -> focusIndex = 0
            focusIndex < editTexts.size -> {
                editTexts[focusIndex].requestFocus()
            }
            else -> {
                editTexts.forEach {
                    it.clearFocus()
                }
                showKeyboard(false, editTexts.last())
                if (isFilled())
                    onFinishFunction(getStringFromFields())
            }
        }
        onCharacterUpdatedFunction(isFilled())
        styleEditTexts()
    }

    private fun addEditText(index: Int) {
        val et = EditText(context)

        et.isCursorVisible = showCursor
        et.inputType = inputType
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            et.importantForAutofill = importantForAutofillLocal
            et.setAutofillHints(autofillHints)
        }
        val params = LayoutParams(
            itemWidth,
            itemHeight
        )

        et.isAllCaps = allCaps

        val leftDp = if (index == 0) 8.dpTopx else 0.dpTopx

        params.setMargins(
            leftDp,
            8.dpTopx,
            marginBetween,
            8.dpTopx
        )
        et.layoutParams = params
        et.gravity = Gravity.CENTER
        styleDefault(et)

        et.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                et.post { et.setSelection(0) }
            }
        }

        editTexts.add(et)

        val linearLayout: LinearLayout = findViewById(R.id.otp_wrapper)
        linearLayout.addView(et)
    }

    private fun styleEditTexts() {
        for (x in 0 until editTexts.size) {
            val et = editTexts[x]
            when {
                x < focusIndex -> {
                    styleFilled(et)
                }
                x == focusIndex -> {
                    styleHighlighted(et)
                }
                x > focusIndex -> {
                    styleDefault(et)
                }
            }
        }
    }

    private fun styleDefault(editText: EditText) {
        editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizeDefault.toFloat())
        editText.setTextColor(textColor)
        editText.background = backgroundImage
        editText.typeface = font
    }

    private fun styleHighlighted(editText: EditText) {
        editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, highlightedTextSize.toFloat())
        editText.setTextColor(highlightedTextColor)
        editText.background = highlightedBackgroundImage
        editText.typeface = highlightedFont
    }

    private fun styleFilled(editText: EditText) {
        editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, filledTextSize.toFloat())
        editText.setTextColor(filledTextColor)
        editText.background = filledBackgroundImage
        editText.typeface = filledFont
    }

    private val Int.dpTopx: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    private fun customBackground(): Drawable {
        val shape = GradientDrawable()
        shape.shape = GradientDrawable.RECTANGLE
        shape.cornerRadius = 8.dpTopx.toFloat()
        shape.setColor(Color.WHITE)
        shape.setStroke(2.dpTopx, Color.BLACK)
        return shape
    }

    private fun showKeyboard(show: Boolean, editText: EditText) {

        val imm: InputMethodManager? =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        if (show) {
            imm?.showSoftInput(editText, 0)
        } else {
            imm?.hideSoftInputFromWindow(editText.applicationWindowToken, 0)
        }
    }

    private fun isFilled(): Boolean {
        editTexts.forEach {
            if (it.text.isNullOrBlank()) return false
        }
        return true
    }

    private fun getStringFromFields(): String {
        var str = ""
        editTexts.forEach {
            str += it.text.firstOrNull()
        }
        return str
    }

    fun setOnFinishListener(func: (String) -> Unit) {
        onFinishFunction = func
    }

    fun setOnCharacterUpdatedListener(func: (Boolean) -> Unit) {
        onCharacterUpdatedFunction = func
    }

    fun setText(str: String) {
        disableEditListener = true
        for (x in 0 until editTexts.size) {
            if (x < str.length) {
                editTexts[x].setText(str[x].toString())
            } else {
                editTexts[x].setText("")
            }
        }
        if (str.count() < editTexts.size) {
            focusIndex = str.count()
            disableEditListener = false
            showKeyboard(true, editTexts[focusIndex])
        } else {
            editTexts.forEach {
                it.clearFocus()
            }
            focusIndex = editTexts.size
            disableEditListener = false
            showKeyboard(false, editTexts.last())
        }
        styleEditTexts()
    }

    fun clearText(showKeyboard: Boolean) {
        disableEditListener = true
        for (x in 0 until editTexts.size) {
            editTexts[x].setText("")
        }
        focusIndex = 0
        disableEditListener = false
        showKeyboard(showKeyboard, editTexts[focusIndex])
    }

    fun fitToWidth(width: Int) {
        val outerMargin = 8.dpTopx
        var dividedSpace = (width - (outerMargin * 2)) / editTexts.size
        dividedSpace -= marginBetween
        itemWidth = dividedSpace
        itemHeight = (itemWidth * 1.25f).toInt()

        val params = LayoutParams(
            itemWidth,
            itemHeight
        )

        editTexts.forEachIndexed { index, editText ->
            val leftDp = if (index == 0) 8.dpTopx else 0.dpTopx

            params.setMargins(
                leftDp,
                8.dpTopx,
                marginBetween,
                8.dpTopx
            )
            editText.layoutParams = params
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        editTexts.forEach {
            it.isEnabled = enabled
        }
    }
}

private class AsteriskPasswordTransformationMethod : PasswordTransformationMethod() {
    override fun getTransformation(source: CharSequence?, view: View?): CharSequence {
        return PasswordCharSequence(source!!)
    }
}

private class PasswordCharSequence(private var mSource: CharSequence) : CharSequence {

    override val length: Int
        get() = mSource.length

    override fun get(index: Int): Char = '*'

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence =
        mSource.subSequence(startIndex, endIndex)
}