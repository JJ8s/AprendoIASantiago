package com.example.aprendoiasantiago

import com.example.aprendoiasantiago.domain.ErrorPerfil
import com.example.aprendoiasantiago.domain.ValidadorPerfil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValidadorPerfilTest {
    @Test
    fun datosCorrectos_sinCorreoNiTelefono_sonValidos() {
        assertNull(ValidadorPerfil.validarDatos("Camila", "", ""))
    }

    @Test
    fun nombreVacio_oSoloEspacios_esError() {
        assertEquals(ErrorPerfil.NOMBRE_VACIO, ValidadorPerfil.validarDatos("   ", "", ""))
    }

    @Test
    fun nombreMuyLargo_esError() {
        val largo = "a".repeat(ValidadorPerfil.LARGO_MAXIMO_NOMBRE + 1)
        assertEquals(ErrorPerfil.NOMBRE_LARGO, ValidadorPerfil.validarDatos(largo, "", ""))
    }

    @Test
    fun correoConFormatoCorrecto_esValido() {
        assertNull(ValidadorPerfil.validarDatos("Ana", "ana@correo.cl", ""))
    }

    @Test
    fun correoSinArrobaODominio_esError() {
        assertEquals(ErrorPerfil.CORREO_INVALIDO, ValidadorPerfil.validarDatos("Ana", "ana.correo.cl", ""))
        assertEquals(ErrorPerfil.CORREO_INVALIDO, ValidadorPerfil.validarDatos("Ana", "ana@correo", ""))
    }

    @Test
    fun telefonoChilenoConPrefijo_esValido() {
        assertNull(ValidadorPerfil.validarDatos("Ana", "", "+56 9 1234 5678"))
    }

    @Test
    fun telefonoConLetrasOPocosDigitos_esError() {
        assertEquals(ErrorPerfil.TELEFONO_INVALIDO, ValidadorPerfil.validarDatos("Ana", "", "12ab5678"))
        assertEquals(ErrorPerfil.TELEFONO_INVALIDO, ValidadorPerfil.validarDatos("Ana", "", "1234"))
    }

    @Test
    fun cambioDeClave_validaCadaCampoEnOrden() {
        assertEquals(ErrorPerfil.CLAVE_ACTUAL_VACIA, ValidadorPerfil.validarClave("", "nueva", "nueva"))
        assertEquals(ErrorPerfil.CLAVE_NUEVA_VACIA, ValidadorPerfil.validarClave("vieja", "", ""))
        assertEquals(ErrorPerfil.CLAVE_NUEVA_IGUAL, ValidadorPerfil.validarClave("misma", "misma", "misma"))
        assertEquals(ErrorPerfil.CLAVES_NO_COINCIDEN, ValidadorPerfil.validarClave("vieja", "nueva", "otra"))
        assertNull(ValidadorPerfil.validarClave("vieja", "nueva", "nueva"))
    }
}
