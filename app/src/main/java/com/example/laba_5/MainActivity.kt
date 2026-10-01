package com.example.laba_5

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.laba_5.databinding.ActivityMainBinding
import java.util.*
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var expr: String = ""     // рядок виразу, що показується

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ініціалізація view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Початкове значення
        setExpression("0")

        // Слухачі для цифр 0..9
        val digits = listOf(
            binding.btn0, binding.btn1, binding.btn2, binding.btn3, binding.btn4,
            binding.btn5, binding.btn6, binding.btn7, binding.btn8, binding.btn9
        )
        for (b in digits) {
            b.setOnClickListener { appendToExpression(b.text.toString()) }
        }

        binding.btnDot.setOnClickListener { appendToExpression(".") }
        binding.btnAdd.setOnClickListener { appendToExpression("+") }
        binding.btnSub.setOnClickListener { appendToExpression("-") }
        binding.btnMul.setOnClickListener { appendToExpression("×") } // відображення ×
        binding.btnDiv.setOnClickListener { appendToExpression("÷") } // відображення ÷
        binding.btnOpen.setOnClickListener { appendToExpression("(") }
        binding.btnClose.setOnClickListener { appendToExpression(")") }

        binding.btnAC.setOnClickListener { clearAll() }
        binding.btnDel.setOnClickListener { deleteLast() }

        binding.btnEqual.setOnClickListener {
            if (expr.isBlank()) return@setOnClickListener
            try {
                // Для обчислення замінимо символи ×, ÷ на внутрішні * та /
                val processedExpr = expr.replace('×', '*').replace('÷', '/')
                val result = evaluateExpression(processedExpr)
                val out = formatResult(result)
                setExpression(out)
            } catch (e: ArithmeticException) {
                Toast.makeText(this, "Помилка: ${e.message}", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Неправильний вираз", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setExpression(s: String) {
        expr = s
        binding.tvExpression.text = s
    }

    private fun appendToExpression(s: String) {
        if (expr == "0" && s.matches(Regex("[0-9.]"))) {
            setExpression(s)
            return
        }

        if (expr.isNotEmpty()) {
            val last = expr.last()
            val isNewOp = s.matches(Regex("[+\\-×÷*/]"))
            val isLastOp = isOperator(last)

            if (isLastOp && isNewOp) {
                // Дозволяємо унарний мінус після '(' або іншого оператора
                if (s == "-" && (last == '(' || isOperator(last))) {
                    expr += s
                    setExpression(expr)
                    return
                }
                // Замінюємо оператор (щоб уникнути ++, *- і т.д.)
                expr = expr.dropLast(1) + s
                setExpression(expr)
                return
            }
        }
        expr += s
        setExpression(expr)
    }

    private fun clearAll() {
        setExpression("0")
    }

    private fun deleteLast() {
        if (expr.length <= 1) {
            setExpression("0")
        } else {
            expr = expr.dropLast(1)
            setExpression(expr)
        }
    }

    private fun isOperator(c: Char): Boolean {
        return c == '+' || c == '-' || c == '×' || c == '÷' || c == '*' || c == '/'
    }

    private fun formatResult(v: Double): String {
        val formatted = String.format(Locale.US, "%.12f", v).trimEnd('0').trimEnd('.')
        return if (formatted.isEmpty()) "0" else formatted
    }

    // ---------------- EVALUATOR (tokenize -> shunting-yard -> eval RPN) ----------------

    private sealed class Token {
        data class Number(val value: Double) : Token()
        data class Op(val op: String) : Token() // + - * / u (u = unary minus)
        object LParen : Token()
        object RParen : Token()
    }

    @Throws(Exception::class)
    private fun evaluateExpression(input: String): Double {
        val tokens = tokenize(input)
        val rpn = toRPN(tokens)
        return evalRPN(rpn)
    }

    private fun tokenize(s: String): List<Token> {
        val out = ArrayList<Token>()
        var i = 0
        while (i < s.length) {
            val c = s[i]
            when {
                c.isWhitespace() -> i++
                c.isDigit() || c == '.' -> {
                    val sb = StringBuilder()
                    while (i < s.length && (s[i].isDigit() || s[i] == '.')) {
                        sb.append(s[i]); i++
                    }
                    val num = sb.toString()
                    val d = num.toDoubleOrNull() ?: throw Exception("Невірне число: $num")
                    out.add(Token.Number(d))
                }
                c == '(' -> { out.add(Token.LParen); i++ }
                c == ')' -> { out.add(Token.RParen); i++ }
                c == '+' || c == '-' || c == '*' || c == '/' -> {
                    if (c == '-') {
                        val prev = out.lastOrNull()
                        val isUnary = (prev == null) || (prev is Token.Op) || (prev is Token.LParen)
                        if (isUnary) {
                            out.add(Token.Op("u")) // unary minus
                            i++
                            continue
                        }
                    }
                    out.add(Token.Op(c.toString()))
                    i++
                }
                else -> throw Exception("Невідомий символ: $c")
            }
        }
        return out
    }

    private fun precedence(op: String): Int = when (op) {
        "u" -> 4
        "*", "/" -> 3
        "+", "-" -> 2
        else -> 0
    }

    private fun isRightAssociative(op: String): Boolean = (op == "u")

    private fun toRPN(tokens: List<Token>): List<Token> {
        val out = ArrayList<Token>()
        val stack = Stack<Token>()
        for (t in tokens) {
            when (t) {
                is Token.Number -> out.add(t)
                is Token.Op -> {
                    val curOp = t.op
                    while (stack.isNotEmpty() && stack.peek() is Token.Op) {
                        val top = stack.peek() as Token.Op
                        val topOp = top.op
                        if ((isRightAssociative(curOp) && precedence(curOp) < precedence(topOp)) ||
                            (!isRightAssociative(curOp) && precedence(curOp) <= precedence(topOp))
                        ) {
                            out.add(stack.pop())
                        } else break
                    }
                    stack.push(t)
                }
                is Token.LParen -> stack.push(t)
                is Token.RParen -> {
                    while (stack.isNotEmpty() && stack.peek() !is Token.LParen) {
                        val popped = stack.pop()
                        if (popped is Token.Op) out.add(popped) else throw Exception("Невірні дужки")
                    }
                    if (stack.isEmpty() || stack.peek() !is Token.LParen) throw Exception("Невідповідність дужок")
                    stack.pop()
                }
            }
        }
        while (stack.isNotEmpty()) {
            val p = stack.pop()
            if (p is Token.LParen || p is Token.RParen) throw Exception("Невідповідність дужок")
            out.add(p)
        }
        return out
    }

    private fun evalRPN(rpn: List<Token>): Double {
        val stack = Stack<Double>()
        for (t in rpn) {
            when (t) {
                is Token.Number -> stack.push(t.value)
                is Token.Op -> {
                    when (t.op) {
                        "u" -> {
                            val v = if (stack.isEmpty()) throw Exception("Некоректний вираз") else stack.pop()
                            stack.push(-v)
                        }
                        "+", "-", "*", "/" -> {
                            if (stack.size < 2) throw Exception("Некоректний вираз")
                            val b = stack.pop()
                            val a = stack.pop()
                            val res = when (t.op) {
                                "+" -> a + b
                                "-" -> a - b
                                "*" -> a * b
                                "/" -> {
                                    if (abs(b) < 1e-12) throw ArithmeticException("Ділення на нуль")
                                    a / b
                                }
                                else -> 0.0
                            }
                            stack.push(res)
                        }
                        else -> throw Exception("Невідомий оператор ${t.op}")
                    }
                }
                else -> throw Exception("Невідомий токен у RPN")
            }
        }
        if (stack.size != 1) throw Exception("Некоректний вираз")
        return stack.pop()
    }
}
