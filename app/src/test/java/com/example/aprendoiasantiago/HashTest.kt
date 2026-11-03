package com.example.aprendoiasantiago

import com.example.aprendoiasantiago.utils.Hash
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class HashTest {
    @Test
    fun sha256_deTextoConocido_coincideConElValorEsperado() {
        // Valor de referencia estándar de SHA-256 para "abc".
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            Hash.sha256("abc")
        )
    }

    @Test
    fun sha256_esDeterminista() {
        assertEquals(Hash.sha256("demo1234"), Hash.sha256("demo1234"))
    }

    @Test
    fun sha256_textosDistintos_danHashesDistintos() {
        assertNotEquals(Hash.sha256("demo1234"), Hash.sha256("demo1235"))
    }

    @Test
    fun sha256_tieneSiempre64CaracteresHexadecimales() {
        assertEquals(64, Hash.sha256("").length)
        assertEquals(64, Hash.sha256("Ñandú ñ 🙂").length)
    }
}
