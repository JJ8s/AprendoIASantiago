package com.example.aprendoiasantiago.data.local

import androidx.room.ColumnInfo
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
    val puntaje: Int = 0,
    // --- Perfil y configuración (BD v4) ---
    @ColumnInfo(defaultValue = "''") val correo: String = "",
    @ColumnInfo(defaultValue = "''") val telefono: String = "",
    /** Avatar prediseñado elegido (índice en la lista de íconos de la app). */
    @ColumnInfo(defaultValue = "0") val avatarIndice: Int = 0,
    /** Foto de avatar propia (`file://...` en el almacenamiento privado). Si existe, manda sobre [avatarIndice]. */
    val avatarUri: String? = null,
    @ColumnInfo(defaultValue = "1") val notifRecordatorios: Boolean = true,
    @ColumnInfo(defaultValue = "1") val notifNuevosPuntos: Boolean = true,
    @ColumnInfo(defaultValue = "1") val notifLogros: Boolean = true,
    /** Interruptores propios de la app (Android no deja revocar permisos por código; ver pantalla Permisos). */
    @ColumnInfo(defaultValue = "1") val permitirUbicacion: Boolean = true,
    @ColumnInfo(defaultValue = "1") val permitirCamara: Boolean = true
)

/** Bandeja de notificaciones de un jugador (Pantalla 14). Se borra junto con el usuario. */
@Entity(
    tableName = "notificaciones",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["usuarioId"])]
)
data class NotificacionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuarioId: Long,
    val titulo: String,
    val mensaje: String,
    /** Nombre de [com.example.aprendoiasantiago.domain.TipoNotificacion]. */
    val tipo: String,
    val fechaMillis: Long,
    val leida: Boolean = false
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

/**
 * Estado de avance de un usuario en una cápsula (pendiente = sin fila).
 * Además guarda cuándo y dónde se completó, y las evidencias visuales (rutas de las fotos).
 */
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
    val completada: Boolean = false,
    val fechaMillis: Long? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val altitud: Double? = null,
    val selfieUri: String? = null,
    val fotoEntornoUri: String? = null
)

/** Recorrido temático: una lista ordenada de puntos turísticos (las paradas están en [RutaPuntoEntity]). */
@Entity(tableName = "rutas")
data class RutaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val descripcion: String
)

/** Una parada de una ruta: qué punto es y en qué posición (orden 1, 2, 3…) se visita. */
@Entity(tableName = "ruta_puntos", primaryKeys = ["rutaId", "puntoId"])
data class RutaPuntoEntity(
    val rutaId: Long,
    val puntoId: Long,
    val orden: Int
)
