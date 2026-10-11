package com.example.aprendoiasantiago.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["nombreUsuario"], unique = true)]
)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombreUsuario: String,
    /** SHA-256 de la contraseña. Vacío = usuario sin acceso (solo aparece en el ranking). */
    val claveHash: String,
    val nombreVisible: String,
    val puntaje: Int = 0
)

@Entity(tableName = "puntos_turisticos")
data class PuntoTuristicoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val descripcion: String,
    val latitud: Double,
    val longitud: Double,
    val altitud: Double
)

@Entity(
    tableName = "capsulas",
    foreignKeys = [
        ForeignKey(
            entity = PuntoTuristicoEntity::class,
            parentColumns = ["id"],
            childColumns = ["puntoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["puntoId"]),
        Index(value = ["codigoQr"], unique = true)
    ]
)
data class CapsulaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val puntoId: Long,
    val titulo: String,
    val urlVideo: String,
    val codigoQr: String,
    val puntos: Int
)

/** Estado de avance de un usuario en una cápsula (pendiente = sin fila o completada = false). */
@Entity(
    tableName = "progreso",
    primaryKeys = ["usuarioId", "capsulaId"],
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CapsulaEntity::class,
            parentColumns = ["id"],
            childColumns = ["capsulaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["capsulaId"])]
)
data class ProgresoEntity(
    val usuarioId: Long,
    val capsulaId: Long,
    val completada: Boolean = false
)
