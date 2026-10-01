# 🧮 Smart Calculator — Android Application

A clean, modern, and reliable Android calculator application built with Kotlin and native View-based XML layouts. It evaluates complex mathematical expressions with correct operator precedence and bracket support using the **Shunting-Yard algorithm**.

---

## ✨ Features

- **Math Operations:** Basic arithmetic (`+`, `-`, `×`, `÷`) and support for brackets `()`.
- **Advanced Parsing:** Uses Reverse Polish Notation (RPN) via the **Shunting-yard algorithm** to strictly follow mathematical priority.
- **Unary Minus & Formatting:** Supports negative numbers and auto-cleans unnecessary decimal zeroes (e.g., `5.0` ➔ `5`).
- **Error Handling:** Gracefully handles invalid expressions and division by zero using user-friendly `Toast` notifications.
- **Custom UI Design:** Stylish dark/orange interface with custom drawable button shapes and XML styles.

---

## 🛠️ Tech Stack & Architecture

- **Language:** [Kotlin](https://kotlinlang.org/)
- **UI & Layout:** XML Layouts (`ConstraintLayout`, `GridLayout`), Custom Drawables, XML Styles.
- **View Binding:** Safe interaction with UI components (`ActivityMainBinding`).
- **Logic:** Shunting-Yard Algorithm (`tokenize()`, `toRPN()`, `evalRPN()`).
