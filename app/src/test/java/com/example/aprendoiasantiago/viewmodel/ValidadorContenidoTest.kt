package com.example.aprendoiasantiago.ui.admin

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class ValidadorContenidoTest : StringSpec({

    "un punto con datos correctos no tiene error (acepta coma decimal)" {
        ValidadorContenido.punto("Plaza", "Centro", "-33,4372", "-70.6506", "540").shouldBeNull()
    }

    "un punto sin nombre o descripción se rechaza" {
        ValidadorContenido.punto(" ", "d", "0", "0", "0") shouldBe "Escribe el nombre."
        ValidadorContenido.punto("N", "", "0", "0", "0") shouldBe "Escribe la descripción."
    }

    "latitud y longitud fuera de rango se rechazan" {
        ValidadorContenido.punto("N", "d", "91", "0", "0") shouldBe "La latitud debe estar entre -90 y 90."
        ValidadorContenido.punto("N", "d", "0", "-181", "0") shouldBe "La longitud debe estar entre -180 y 180."
        ValidadorContenido.punto("N", "d", "abc", "0", "0") shouldBe "La latitud debe estar entre -90 y 90."
    }

    "la altitud debe ser un número" {
        ValidadorContenido.punto("N", "d", "0", "0", "alto") shouldBe "La altitud debe ser un número."
    }

    "una cápsula válida no tiene error" {
        ValidadorContenido.capsula("IA", "AIJS-008", "10", 1L).shouldBeNull()
    }

    "una cápsula necesita título, QR, puntos entre 1 y 1000 y un punto" {
        ValidadorContenido.capsula("", "Q", "10", 1L) shouldBe "Escribe el título."
        ValidadorContenido.capsula("T", " ", "10", 1L) shouldBe "Escribe el código QR."
        ValidadorContenido.capsula("T", "Q", "0", 1L) shouldBe "Los puntos deben ser un número entre 1 y 1000."
        ValidadorContenido.capsula("T", "Q", "mil", 1L) shouldBe "Los puntos deben ser un número entre 1 y 1000."
        ValidadorContenido.capsula("T", "Q", "10", null) shouldBe "Primero crea un punto turístico."
    }
})
