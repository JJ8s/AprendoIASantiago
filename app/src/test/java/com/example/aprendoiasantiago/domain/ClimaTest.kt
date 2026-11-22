package com.example.aprendoiasantiago.domain

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ClimaTest : StringSpec({

    "los códigos WMO se agrupan en condiciones" {
        CondicionClima.desdeCodigo(0) shouldBe CondicionClima.DESPEJADO
        CondicionClima.desdeCodigo(2) shouldBe CondicionClima.PARCIAL
        CondicionClima.desdeCodigo(3) shouldBe CondicionClima.NUBLADO
        CondicionClima.desdeCodigo(45) shouldBe CondicionClima.NIEBLA
        CondicionClima.desdeCodigo(53) shouldBe CondicionClima.LLOVIZNA
        CondicionClima.desdeCodigo(63) shouldBe CondicionClima.LLUVIA
        CondicionClima.desdeCodigo(81) shouldBe CondicionClima.LLUVIA
        CondicionClima.desdeCodigo(73) shouldBe CondicionClima.NIEVE
        CondicionClima.desdeCodigo(86) shouldBe CondicionClima.NIEVE
        CondicionClima.desdeCodigo(95) shouldBe CondicionClima.TORMENTA
        CondicionClima.desdeCodigo(1234) shouldBe CondicionClima.DESCONOCIDA
    }

    "con lluvia el consejo es llevar paraguas aunque haga frío" {
        ClimaPunto(5.0, 2.0, 10.0, CondicionClima.LLUVIA).consejo shouldBe ConsejoClima.PARAGUAS
    }

    "sin lluvia, la sensación térmica baja pide abrigo" {
        ClimaPunto(10.0, 8.0, 10.0, CondicionClima.NUBLADO).consejo shouldBe ConsejoClima.ABRIGO
    }

    "con 30 grados o más pide hidratarse" {
        ClimaPunto(31.0, 33.0, 5.0, CondicionClima.DESPEJADO).consejo shouldBe ConsejoClima.HIDRATARSE
    }

    "un día templado es buen momento para recorrer" {
        ClimaPunto(20.0, 20.0, 5.0, CondicionClima.PARCIAL).consejo shouldBe ConsejoClima.TODO_BIEN
    }
})
