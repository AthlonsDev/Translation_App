package com.example.translation_app

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.view.MenuItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.translation_app.Models.ModelActivity
import com.example.translation_app.databinding.ActivityMainBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import java.util.Arrays
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase.wrap(Locale.getDefault()))
    }

    fun Context.wrap(desiredLocale: Locale): Context {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M)
            return getUpdatedContextApi23(desiredLocale)

        return if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N)
            getUpdatedContextApi24(desiredLocale)
        else
            getUpdatedContextApi25(desiredLocale)
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun Context.getUpdatedContextApi23(locale: Locale): Context {
        val configuration = resources.configuration
        configuration.locale = locale
        return createConfigurationContext(configuration)
    }

    private fun Context.getUpdatedContextApi24(locale: Locale): Context {
        val configuration = resources.configuration
        configuration.setLocale(locale)
        return createConfigurationContext(configuration)
    }

    @TargetApi(Build.VERSION_CODES.N_MR1)
    private fun Context.getUpdatedContextApi25(locale: Locale): Context {
        val localeList = LocaleList(locale)
        val configuration = resources.configuration
        configuration.setLocales(localeList)
        return createConfigurationContext(configuration)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        manageLanguageModels()

        setSupportActionBar(binding.myToolbar)

        val navView: BottomNavigationView = binding.navView
//        val toolbar: androidx.appcompat.widget.Toolbar = binding.toolbar

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setSupportActionBar(findViewById(R.id.my_toolbar))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val adSize = com.google.android.gms.ads.AdSize.BANNER
        val adView = binding.adView
//        adView.adUnitId = "ca-app-pub-3940256099942544/6300978111"
//        adView.setAdSize(adSize)

        val testDeviceIds = Arrays.asList("33BE2250B43518CCDA7DE426D04EE231")
        val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
        MobileAds.setRequestConfiguration(configuration)

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

    }

    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.toolbar, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
//

        R.id.models -> {
            val intent = Intent(this, ModelActivity::class.java)
            startActivity(intent)
            true
        }
        R.id.camera -> {
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
            true
        }
        R.id.gallery -> {
            val intent = Intent(this, GalleryActivity::class.java)
            startActivity(intent)
            true
        }

        else -> {
            // The user's action isn't recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }


    fun manageLanguageModels() {

        //Models
        val frenchModel = TranslateRemoteModel.Builder(TranslateLanguage.FRENCH).build()
        val germanModel = TranslateRemoteModel.Builder(TranslateLanguage.GERMAN).build()
        val spanishModel = TranslateRemoteModel.Builder(TranslateLanguage.SPANISH).build()
        val chineseModel = TranslateRemoteModel.Builder(TranslateLanguage.CHINESE).build()
        val hindiModel = TranslateRemoteModel.Builder(TranslateLanguage.HINDI).build()
        val arabicModel = TranslateRemoteModel.Builder(TranslateLanguage.ARABIC).build()
        val russianModel = TranslateRemoteModel.Builder(TranslateLanguage.RUSSIAN).build()
        val japaneseModel = TranslateRemoteModel.Builder(TranslateLanguage.JAPANESE).build()
        val koreanModel = TranslateRemoteModel.Builder(TranslateLanguage.KOREAN).build()
        val italianModel = TranslateRemoteModel.Builder(TranslateLanguage.ITALIAN).build()

        val modelManager = RemoteModelManager.getInstance()

// Get translation models stored on the device.
        modelManager.getDownloadedModels(TranslateRemoteModel::class.java)
            .addOnSuccessListener { models ->
                // ...
            }
            .addOnFailureListener {
                // Error.
            }

//  Download the French model.
        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()
        modelManager.download(frenchModel, conditions)
            .addOnSuccessListener {
                // Model downloaded.
//                Toast.makeText(this, "French Model downloaded", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                // Error.
            }

//  Download the German model.
        modelManager.download(germanModel, conditions)
            .addOnSuccessListener {
                // Model downloaded.
//                Toast.makeText(this, "French Model downloaded", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                // Error.
            }

//  Download the Italian model
        modelManager.download(italianModel, conditions)
            .addOnSuccessListener {
                // Model downloaded.
//                Toast.makeText(this, "Ita Model downloaded", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                // Error.
            }

//  Download the Japanese model
        modelManager.download(japaneseModel, conditions)
            .addOnSuccessListener {
                // Model downloaded.
//                Toast.makeText(this, "Jap Model downloaded", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                // Error.
            }

//  Download the Korean model
        modelManager.download(koreanModel, conditions)
            .addOnSuccessListener {
                // Model downloaded.
//                Toast.makeText(this, "Jap Model downloaded", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                // Error.
            }

    }


//    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
//        R.id.menu_camera -> {
//            // User chose the "Settings" item, show the app settings UI...
//            true
//        }
//
//        else -> {
//            // If we got here, the user's action was not recognized.
//            // Invoke the superclass to handle it.
//            super.onOptionsItemSelected(item)
//        }
//    }
}