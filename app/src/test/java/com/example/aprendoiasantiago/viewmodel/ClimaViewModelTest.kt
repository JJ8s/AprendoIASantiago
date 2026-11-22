package com.example.aprendoiasantiago.ui.viewmodels

import com.example.aprendoiasantiago.data.remote.Respuesta
import com.example.aprendoiasantiago.data.remote.TipoFallo
import com.example.aprendoiasantiago.data.repository.ClimaRepository
import com.example.aprendoiasantiago.domain.ClimaPunto
import com.example.aprendoiasantiago.domain.CondicionClima
import com.example.aprendoiasantiago.util.pruebaConMain
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk

class ClimaViewModelTest : StringSpec({

    val clima = ClimaPunto(18.0, 17.0, 10.0, CondicionClima.DESPEJADO)

    "al partir está cargando" {
        pruebaConMain {
            ClimaViewModel(mockk()).estado.value shouldBe EstadoClima.Cargando
        }
    }

    "con respuesta correcta queda disponible" {
        pruebaConMain {
            val repo = mockk<ClimaRepository>()
            coEvery { repo.obtener(-33.4, -70.6) } returns Respuesta.Ok(clima)
            val vm = ClimaViewModel(repo)

            vm.cargar(-33.4, -70.6)

            vm.estado.value shouldBe EstadoClima.Disponible(clima)
        }
    }

    "sin conexión queda no disponible marcando el motivo" {
        pruebaConMain {
            val repo = mockk<ClimaRepository>()
            coEvery { repo.obtener(any(), any()) } returns Respuesta.Fallo(TipoFallo.SIN_CONEXION)
            val vm = ClimaViewModel(repo)

            vm.cargar(0.0, 0.0)

            vm.estado.value shouldBe EstadoClima.NoDisponible(sinConexion = true)
        }
    }

    "un error del servicio queda no disponible sin marcar falta de conexión" {
        pruebaConMain {
            val repo = mockk<ClimaRepository>()
            coEvery { repo.obtener(any(), any()) } returns Respuesta.Fallo(TipoFallo.SERVIDOR, "HTTP 500")
            val vm = ClimaViewModel(repo)

            vm.cargar(0.0, 0.0)

            vm.estado.value shouldBe EstadoClima.NoDisponible(sinConexion = false)
        }
    }
})
