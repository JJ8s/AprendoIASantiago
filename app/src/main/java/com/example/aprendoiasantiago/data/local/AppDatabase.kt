package com.example.aprendoiasantiago.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UsuarioEntity::class,
        PuntoTuristicoEntity::class,
        CapsulaEntity::class,
        ProgresoEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun puntoDao(): PuntoDao
    abstract fun capsulaDao(): CapsulaDao
    abstract fun progresoDao(): ProgresoDao

    companion object {
        private const val NOMBRE = "aprendo_ia_santiago.db"

        fun crear(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, NOMBRE)
                .build()
    }
}
