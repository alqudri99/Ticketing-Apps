package com.pandec.ticketingapps

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener
import com.pandec.ticketingapps.Constants.MAX_STEP_FOR_DISCRETE_EFFECT
import com.pandec.ticketingapps.Constants.ZERO
import com.pandec.ticketingapps.databinding.ActivityMainBinding
import java.math.BigDecimal
import java.math.BigInteger

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    var currentProgress = 0
    private var threshold = 5
    private var progressDirection = ZERO
    val max = 13000000
    val min = 1000000
    val multiplier = 1000000
    val isFirstMultiplierEligible = max % multiplier == ZERO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.fu.setOnFocusChangeListener(object : View.OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                if (!hasFocus) {
                    val amountInPercentage = (binding.fu.text.toString().toFloat().minus(min) /
                            max.toFloat()) * getMaxStep().plus(1).toFloat()
                    binding.sl.value = amountInPercentage
                }
            }

        })

        binding.sl.apply {
            valueTo = getMaxStep().toFloat()

            addOnChangeListener { slider, value, fromUser ->
                if (fromUser) {
                    anotherOnSlideChanged(value, {
                        binding.fu.setText("$it")
                    }, {
                        slider.value = it
                    })
                }
                currentProgress = value.toInt()
            }
        }
    }

    private fun anotherOnSlideChanged(
        value: Float,
        onSlided: (BigInteger?) -> Unit,
        setSliderProgress: (Float) -> Unit
    ) {
        val modulusFactor = 1f
        val modulusValue = value % modulusFactor
        var isNeedDiscreteEffect = getMaxStep() < MAX_STEP_FOR_DISCRETE_EFFECT

        val nextValue = value.plus(modulusFactor - (value % modulusFactor))
        val previousValue = value.minus(modulusValue)

        if (value > progressDirection && modulusValue > threshold) {
            currentProgress = nextValue.toInt()
            onSlided(getAmount(currentProgress))
        }
        if (value.toInt() <= progressDirection && modulusValue < threshold) {
            currentProgress = previousValue.toInt()
            onSlided(getAmount(currentProgress))
        }
        progressDirection = value.toInt()
        if (isNeedDiscreteEffect) setSliderProgress(currentProgress.toFloat())
    }

    override fun onBackPressed() {
        binding.fu.clearFocus()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return super.onKeyDown(keyCode, event)
    }

    private fun getAmount(position: Int): BigInteger? = when {
        !(isFirstMultiplierEligible) && getMaxStep() < position.plus(if (isFirstMultiplierEligible) 0 else 1) ->
            max.toBigInteger()
        else -> (multiplier * position).plus(min).toBigInteger()
    }

    fun getMaxStep() = (max.minus(min) / multiplier).plus(if (isFirstMultiplierEligible) 0 else 1)
}


object Constants {
    const val ZERO = 0
    const val ZERO_FLOAT = 0f
    const val TWO = 2
    const val ONE_FLOAT = 1f
    const val MAX_STEP_FOR_DISCRETE_EFFECT = 1000
}
