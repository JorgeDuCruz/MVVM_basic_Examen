package com.dam.mvvm_basic

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MyViewModel(): ViewModel() {

    // etiqueta para logcat
    private val TAG_LOG = "pruebas"

    // estados del juego
    // usamos LiveData para que la IU se actualice
    // patron de diseño observer
    val estadoActual = MutableStateFlow(Estados.INICIO)

    // este va a ser nuestra lista para la secuencia random
    // usamos mutable, ya que la queremos modificar
    var _numbers = MutableStateFlow(0)
    // tiempo que queda de cuenta atras
    val _tiempo = MutableStateFlow(5)
    var cuentaAtras: Job? = null
    val _activaCuentaAtras = MutableStateFlow(EstadosAuxiliares.INACTIVA)

    var cuentaAdelante: Job? = null

    val _activaCuentaAdelante = MutableStateFlow(EstadosAuxiliares.INACTIVA)

    val _tiempo2 = MutableStateFlow(0)

    // inicializamos variables cuando instanciamos
    init {
        // estado inicial
        Log.d(TAG_LOG, "Inicializamos ViewModel - Estado: ${estadoActual.value}")
    }

    /**
     * crear entero random
     */
    fun crearRandom() {
        // cambiamos estado, por lo tanto la IU se actualiza
        estadoActual.value = Estados.GENERANDO
        _numbers.value = (0..3).random()
        Log.d(TAG_LOG, "creamos random ${_numbers.value} - Estado: ${estadoActual.value}")
        actualizarNumero(_numbers.value)
    }

    fun actualizarNumero(numero: Int) {
        Log.d(TAG_LOG, "actualizamos numero en Datos - Estado: ${estadoActual.value}")
        Datos.numero = numero
        // cambiamos estado, por lo tanto la IU se actualiza
        estadoActual.value = Estados.ADIVINANDO
    }

    /**
     * comprobar si el boton pulsado es el correcto
     * @param ordinal: Int numero de boton pulsado
     * @return Boolean si coincide TRUE, si no FALSE
     */
    fun comprobar(ordinal: Int): Boolean {
        Log.d(TAG_LOG, "comprobamos - Estado: ${estadoActual.value}")
        return if (ordinal == Datos.numero) {
            Log.d(TAG_LOG, "es correcto")
            estadoActual.value = Estados.INICIO
            Log.d(TAG_LOG, "GANAMOS - Estado: ${estadoActual.value}")
            //lanzamos estados auxiliares en paralelo
            estadosAuxiliares("Ganador")
            cancelarCuentAtras()
            true
        } else {
            Log.d(TAG_LOG, "no es correcto")
            estadoActual.value = Estados.ADIVINANDO
            Log.d(TAG_LOG, "otro intento - Estado: ${estadoActual.value}")
            //lanzamos estados auxiliares en paralelo
            estadosAuxiliares("Fallo")
            false
        }
    }

    /**
     * Corutina que lanza estados auxiliares
     */
    fun estadosAuxiliares(msg: String = "") {
        viewModelScope.launch {
            // inicializamos estado auxiliar
            // los recorremos
            var estadoAux = EstadosAuxiliares.AUX1
            Log.d(TAG_LOG, "estado (corutina): ${estadoAux}")
            Log.d(TAG_LOG, "mensaje (corutina): ${msg}")
            delay(1500)
            estadoAux = EstadosAuxiliares.AUX2
            Log.d(TAG_LOG, "estado (corutina): ${estadoAux}")
            Log.d(TAG_LOG, "mensaje (corutina): ${msg}")
            delay(1500)
            estadoAux = EstadosAuxiliares.AUX3
            Log.d(TAG_LOG, "estado (corutina): ${estadoAux}")
            Log.d(TAG_LOG, "mensaje (corutina): ${msg}")
            delay(1500)
        }
    }

    /**
     * Corutina que inicia la cuenta atras
     */
    fun cuentaAtras(){
        _activaCuentaAtras.value= EstadosAuxiliares.ACTIVA
        _tiempo.value = 5
        cuentaAtras = viewModelScope.launch {
            while (_tiempo.value>0){
                delay(1000)
                _tiempo.value--
            }
            cancelarCuentAtras()
        }
    }

    /**
     * Función que ocurre cuando se tiene que terminar la cuenta atras
     */
    fun cancelarCuentAtras(){
        cuentaAtras?.cancel()
        estadoActual.value = Estados.INICIO
        _activaCuentaAtras.value = EstadosAuxiliares.INACTIVA
    }

    /**
     * Corutina que inicia la cuenta adelante
     */
    fun cuentaAdelante(){
        _activaCuentaAdelante.value= EstadosAuxiliares.ACTIVA
        _tiempo2.value = 0
        _activaCuentaAdelante.value = EstadosAuxiliares.EJECUTANDO
        cuentaAdelante = viewModelScope.launch {
            while (_tiempo2.value>10){
                delay(1000)
                _tiempo2.value++
            }
            cancelarCuentaAdelante()
        }
    }

    /**
     * Función que ocurre cuando se tiene que terminar la cuenta atras
     */
    fun cancelarCuentaAdelante(){
        cuentaAdelante?.cancel()
        estadoActual.value = Estados.INICIO
        _activaCuentaAtras.value = EstadosAuxiliares.INACTIVA
    }
}