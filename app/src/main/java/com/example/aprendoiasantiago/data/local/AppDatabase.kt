package com.example.aprendoiasantiago.data.local

import com.example.aprendoiasantiago.data.model.UsuarioEntity
import com.example.aprendoiasantiago.data.model.PuntoTuristicoEntity
import com.example.aprendoiasantiago.data.model.CapsulaEntity
import com.example.aprendoiasantiago.data.model.ProgresoEntity
import com.example.aprendoiasantiago.data.model.RutaEntity
import com.example.aprendoiasantiago.data.model.RutaPuntoEntity

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
        RutaPuntoEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun puntoDao(): PuntoDao
    abstract fun capsulaDao(): CapsulaDao
    abstract fun progresoDao(): ProgresoDao
    abstract fun rutaDao(): RutaDao

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

        fun crear(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, NOMBRE)
                .addMigrations(MIGRACION_1_2, MIGRACION_2_3)
                .build()
    }
}
