package com.example.ubuexoworks

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkInfo
import android.os.Build
import androidx.lifecycle.LiveData

@Suppress("DEPRECATION")
@SuppressLint("MissingPermission")
class ConexionDeRed(private val context: Context) : LiveData<Boolean>() {
    private val administradorConectividad: ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private lateinit var networkConnectionCallback: ConnectivityManager.NetworkCallback
    override fun onActive() {
        super.onActive()
        actualizarConexionRed()
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
                administradorConectividad.registerDefaultNetworkCallback(devolucionLlamadaAdministrador())
            }
            else -> {
                context.registerReceiver(
                    receptorDeRed,
                    IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
                )
            }
        }
    }

    private fun devolucionLlamadaAdministrador(): ConnectivityManager.NetworkCallback {
        networkConnectionCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onLost(network: Network) {
                super.onLost(network)
                postValue(false)
            }
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                postValue(true)
            }
        }
        return networkConnectionCallback
    }
    private fun actualizarConexionRed() {
        val activeNetworkConnection: NetworkInfo? = administradorConectividad.activeNetworkInfo
        postValue(activeNetworkConnection?.isConnected == true)
    }
    private val receptorDeRed= object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            actualizarConexionRed()
        }
    }
}