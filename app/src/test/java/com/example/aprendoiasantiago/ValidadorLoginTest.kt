package com.example.aprendoiasantiago

import com.example.aprendoiasantiago.domain.ErrorLogin
import com.example.aprendoiasantiago.domain.ValidadorLogin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValidadorLoginTest {
    @Test
    fun usuarioVacio_devuelveErrorDeUsuario() {
        assertEquals(ErrorLogin.USUARIO_VACIO, ValidadorLogin.validar("", "demo1234"))
    }

    @Test
    fun usuarioSoloConEspacios_cuentaComoVacio() {
        assertEquals(ErrorLogin.USUARIO_VACIO, ValidadorLogin.validar("   ", "demo1234"))
    }

    @Test
    fun claveVacia_devuelveErrorDeClave() {
        assertEquals(ErrorLogin.CLAVE_VACIA, ValidadorLogin.validar("demo", ""))
    }

    @Test
    fun ambosVacios_reportaPrimeroElUsuario() {
        assertEquals(ErrorLogin.USUARIO_VACIO, ValidadorLogin.validar("", ""))
    }

    @Test
    fun datosCompletos_noDevuelveError() {
        assertNull(ValidadorLogin.validar("demo", "demo1234"))
    }

    @Test
    fun registro_conTodoCorrecto_noDevuelveError() {
        assertNull(ValidadorLogin.validarRegistro("María", "maria", "abc123", "abc123"))
    }

    @Test
    fun registro_sinConfirmacion_pideConfirmar() {
        assertEquals(
            ErrorLogin.CONFIRMACION_VACIA,
            ValidadorLogin.validarRegistro("María", "maria", "abc123", "")
        )
    }

    @Test
    fun registro_conClavesDistintas_avisaQueNoCoinciden() {
        assertEquals(
            ErrorLogin.CLAVES_NO_COINCIDEN,
            ValidadorLogin.validarRegistro("María", "maria", "abc123", "abc124")
        )
    }

    @Test
    fun registro_laConfirmacionDistingueMayusculas() {
        assertEquals(
            ErrorLogin.CLAVES_NO_COINCIDEN,
            ValidadorLogin.validarRegistro("María", "maria", "Abc123", "abc123")
        )
    }

    @Test
    fun registro_primeroSeReportanLosCamposObligatorios() {
        assertEquals(ErrorLogin.NOMBRE_VACIO, ValidadorLogin.validarRegistro("", "maria", "abc", "xyz"))
        assertEquals(ErrorLogin.USUARIO_VACIO, ValidadorLogin.validarRegistro("María", "", "abc", "xyz"))
        assertEquals(ErrorLogin.CLAVE_VACIA, ValidadorLogin.validarRegistro("María", "maria", "", "xyz"))
    }
}
