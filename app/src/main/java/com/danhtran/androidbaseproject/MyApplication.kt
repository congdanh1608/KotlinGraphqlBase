package com.danhtran.androidbaseproject

import android.os.Bundle
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.multidex.MultiDexApplication
import com.danhtran.androidbaseproject.di.component.AppComponent
import com.danhtran.androidbaseproject.di.component.DaggerAppComponent
import com.danhtran.androidbaseproject.di.module.AppModule
import com.livefront.bridge.Bridge
import com.livefront.bridge.SavedStateHandler
import com.orhanobut.hawk.Hawk
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import uk.co.chrisjenx.calligraphy.CalligraphyConfig
import java.io.IOException
import java.net.SocketException


/**
 * Created by danhtran on 2/25/2018.
 */

class MyApplication : MultiDexApplication() {
    lateinit var appComponent: AppComponent
        private set
//    var token: String? = null
    var token: String? = "eyJhbGciOiJSUzI1NiIsImtpZCI6Ijk3NmU2NmEyNTRiZmY4ZDk0ZTcwZmQ2Yzc5YmZmMGFmIiwidHlwIjoiSldUIn0.eyJuYmYiOjE1ODc4ODkzODQsImV4cCI6MTU5MDQ4MTM4NCwiaXNzIjoiaHR0cHM6Ly9wYXluaW5qYS1zdGFnaW5nLWlkZW50aXR5LmF6dXJld2Vic2l0ZXMubmV0IiwiYXVkIjpbImh0dHBzOi8vcGF5bmluamEtc3RhZ2luZy1pZGVudGl0eS5henVyZXdlYnNpdGVzLm5ldC9yZXNvdXJjZXMiLCJwYXluaW5qYUFQSSJdLCJjbGllbnRfaWQiOiJyby5jbGllbnQiLCJzdWIiOiIxYTI5NDFiMy1hMGJmLTQ3YTctYTVlMS1mYWJkNzgwODc0MTQiLCJhdXRoX3RpbWUiOjE1ODc4ODkzODQsImlkcCI6ImxvY2FsIiwibmFtZSI6ImNvbmdkYW5oMTYwOEBnbWFpbC5jb20iLCJlbWFpbCI6ImNvbmdkYW5oMTYwOEBnbWFpbC5jb20iLCJEaXNwbGF5TmFtZSI6IkRhbmggQ29uZyBUcmFuIiwic2NvcGUiOlsicGF5bmluamEuYXBpLmZ1bGxfYWNjZXNzIl0sImFtciI6WyJwd2QiXX0.Rs2yNZE3hjaraDPucRTK8U2iUoaXdDjWCnVkLAa6rxytYEaZAZb6RE1VcFZ4eFohczlyGslTnuPk0bmEeyKsBKepouVE6eSzmRftk3dzTcfF655ZNLDh67n1bAkj2G7dZuVj2QDUkW-wPlKfxyOk4GUfo6Gs-BovuUIJ4BwWEYpZD7SXTAC2vX1eFmEBqF9KKfRjKCpZ0gazeAeuXMdiCdZmvH6VwtogfwcHzSUyHzrhkCeh5pi0k-rkcyGMJq-ruG5uBaxTndGBSHYw097nnOkBp45mMwQhFQmluUltS2E13TQkj66b8-uFQHChWpTtBLPcMIO8vkzw8q3T2uhyNA"

    init {
        myApplication = this
    }

    override fun onCreate() {
        super.onCreate()
        initSDK()
        initData()
    }


    //region init SDK
    private fun initSDK() {
        initBridge()
        initHawk()
        initFont()
        initLogger()
        initRxJavaErrorHandler()
    }

    //init fonts for app
    private fun initFont() {
        CalligraphyConfig.initDefault(
                CalligraphyConfig.Builder()
                        .setDefaultFontPath("fonts/Helvetica.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        )
    }

    private fun initLogger() {
        Logger.addLogAdapter(object : AndroidLogAdapter() {
            override fun isLoggable(priority: Int, tag: String?): Boolean {
                return BuildConfig.DEBUG
            }
        })
    }

    private fun initHawk() {
        Hawk.init(this).build()
    }

    private fun initBridge() {
        Bridge.initialize(applicationContext, object : SavedStateHandler {
            override fun saveInstanceState(@NonNull target: Any, @NonNull state: Bundle) {
                //register other state saving libraries here
            }

            override fun restoreInstanceState(@NonNull target: Any, @Nullable state: Bundle?) {
                //register other state saving libraries here
            }
        })
    }

    private fun initRxJavaErrorHandler() {
        RxJavaPlugins.setErrorHandler { throwable: Throwable ->
            if (throwable is UndeliverableException) {
                throwable.cause?.let {
                    Thread.currentThread().uncaughtExceptionHandler?.uncaughtException(Thread.currentThread(), it)
                    return@setErrorHandler
                }
            }
            if (throwable is IOException || throwable is SocketException) {
                // fine, irrelevant network problem or API that throws on cancellation
                return@setErrorHandler
            }
            if (throwable is InterruptedException) {
                // fine, some blocking code was interrupted by a dispose call
                return@setErrorHandler
            }
            if (throwable is NullPointerException || throwable is IllegalArgumentException) {
                // that's likely a bug in the application
                Thread.currentThread().uncaughtExceptionHandler?.uncaughtException(Thread.currentThread(), throwable)
                return@setErrorHandler
            }
            if (throwable is IllegalStateException) {
                // that's a bug in RxJava or in a custom operator
                Thread.currentThread().uncaughtExceptionHandler?.uncaughtException(Thread.currentThread(), throwable)
                return@setErrorHandler
            }
            Logger.w("Undeliverable exception received, not sure what to do ${throwable.message}")
        }
    }
    //endregion init SDK


    //region init Data
    private fun initData() {
        appComponent = DaggerAppComponent.builder()
                .appModule(AppModule(this))
                .build()
        appComponent.inject(this)

        //language
        //        String s = SharedPrefsHelper.getInstance().readString(SharePref.LANGUAGE.toString());
        //        if (s != null) lang = s;
        //        else lang = Locale.getDefault().getLanguage();

        //load token
        //        session = SharedPrefsHelper.getInstance().readObject(Session.class, SharePref.SESSION_LOGIN.toString());

        //secret key
        //        Utils.generalSHAKey(this);
    }
    //endregion init Data


    companion object {
        private lateinit var myApplication: MyApplication

        fun instance(): MyApplication {
            return myApplication
        }
    }
}
