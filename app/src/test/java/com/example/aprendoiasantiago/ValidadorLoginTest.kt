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
}
