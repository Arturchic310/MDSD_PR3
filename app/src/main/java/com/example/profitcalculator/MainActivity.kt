package com.example.profitcalculator

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.Double
import kotlin.math.*

class MainActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val inputPc = findViewById<EditText>(R.id.inputPc)
        val inputSigma = findViewById<EditText>(R.id.inputSigma)
        val inputB = findViewById<EditText>(R.id.input_B)
        val resultText = findViewById<TextView>(R.id.resultText)
        val calcButton = findViewById<Button>(R.id.calcButton)

        calcButton.setOnClickListener {
            try {
                val Pc = inputPc.text.toString().toDouble()
                val after_Sigma = inputSigma.text.toString().toDouble()
                val B = inputB.text.toString().toDouble()

                val before_Sigma = 1.0

                //частка енергії, що генерується без небалансів
                val before_Del = Delta(Pc, before_Sigma) //~0.2
                val after_Del = Delta(Pc, after_Sigma)   //~0.68

                val profit_before = calcProfit(Pc, before_Del, B)
                val profit_after = calcProfit(Pc, after_Del, B)

                resultText.text = """
                    -> Прибуток до вдосконалення системи прогнозу (похибка %.2f МВт): %.1f тис. грн
                    
                    ~> Прибуток після вдосконалення системи прогнозу (похибка %.2f МВт): %.1f тис. грн 
                """.trimIndent().format(before_Sigma, profit_before, after_Sigma, profit_after)

            } catch (e: Exception) {
                Toast.makeText(this, "Перевірте введені дані", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Функція нормального розподілу потужності Pd(P)
    private fun Pd(P: Double, Pc: Double, sigma: Double): Double {
        return (1 / (sigma * sqrt(2 * Math.PI))) * exp(-((P - Pc).pow(2)) / (2 * sigma.pow(2)))
    }

    private fun Delta(Pc: Double, sigma: Double): Double {
        val step = 0.001       // крок інтегрування
        val delta = Pc * 0.05
        var sum = 0.0
        var P = Pc - delta
        while (P <= Pc + delta) {
            sum += Pd(P, Pc, sigma) * step
            P += step
        }
        val deltaEta = sum
        return deltaEta
    }

    private fun calcProfit(Pc: Double, Del: Double, B: Double): Double {
        val W1 = Pc * 24 * Del      //обсяг енергії без небалансів
        val W2 = Pc * 24 * (1-Del)  //обсяг енергії з небалансами
        val Ps = W1 * B             //початковий прибуток
        val F = W2 * B              //штраф
        return Ps - F               //фінальний прибуток
    }
}