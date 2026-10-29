package com.example.aprendoiasantiago.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UsuarioEntity::class,
        PuntoTuristicoEntity::class,
        CapsulaEntity::class,
        ProgresoEntity::class,
        RutaEntity::class,
        RutaPuntoEntity::class,
        NotificacionEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun puntoDao(): PuntoDao
    abstract fun capsulaDao(): CapsulaDao
    abstract fun progresoDao(): ProgresoDao
    abstract fun rutaDao(): RutaDao
    abstract fun notificacionDao(): NotificacionDao

    companion object {
        private const val NOMBRE = "aprendo_ia_santiago.db"

        /**
         * v1 → v2: la tabla `progreso` gana fecha, ubicación (lat/lon/alt) y evidencias visuales.
         * Solo agrega columnas vacías, así que los puntos y avances ya guardados se conservan.
         */
        private val MIGRACION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE progreso ADD COLUMN fechaMillis INTEGER")
                db.execSQL("ALTER TABLE progreso ADD COLUMN latitud REAL")
                db.execSQL("ALTER TABLE progreso ADD COLUMN longitud REAL")
                db.execSQL("ALTER TABLE progreso ADD COLUMN altitud REAL")
                db.execSQL("ALTER TABLE progreso ADD COLUMN selfieUri TEXT")
                db.execSQL("ALTER TABLE progreso ADD COLUMN fotoEntornoUri TEXT")
            }
        }

        /** v2 → v3: tablas nuevas para las rutas. No toca ninguna tabla existente. */
        private val MIGRACION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `rutas` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`nombre` TEXT NOT NULL, `descripcion` TEXT NOT NULL)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `ruta_puntos` (" +
                        "`rutaId` INTEGER NOT NULL, `puntoId` INTEGER NOT NULL, `orden` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`rutaId`, `puntoId`))"
                )
            }
        }

        /**
         * v3 → v4: perfil y configuración. `usuarios` gana datos de contacto, avatar, preferencias de
         * notificaciones e interruptores de permisos (todo con valor por defecto, no se pierde nada),
         * y nace la tabla `notificaciones`.
         */
        private val MIGRACION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE usuarios ADD COLUMN correo TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE usuarios ADD COLUMN telefono TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE usuarios ADD COLUMN avatarIndice INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE usuarios ADD COLUMN avatarUri TEXT")
                db.execSQL("ALTER TABLE usuarios ADD COLUMN notifRecordatorios INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE usuarios ADD COLUMN notifNuevosPuntos INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE usuarios ADD COLUMN notifLogros INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE usuarios ADD COLUMN permitirUbicacion INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE usuarios ADD COLUMN permitirCamara INTEGER NOT NULL DEFAULT 1")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `notificaciones` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`usuarioId` INTEGER NOT NULL, `titulo` TEXT NOT NULL, `mensaje` TEXT NOT NULL, " +
                        "`tipo` TEXT NOT NULL, `fechaMillis` INTEGER NOT NULL, `leida` INTEGER NOT NULL, " +
                        "FOREIGN KEY(`usuarioId`) REFERENCES `usuarios`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_notificaciones_usuarioId` ON `notificaciones` (`usuarioId`)"
                )
            }
        }

        fun crear(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, NOMBRE)
                .addMigrations(MIGRACION_1_2, MIGRACION_2_3, MIGRACION_3_4)
                .build()
    }
}
