package lt.markmerkk.locaping

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import lt.markmerkk.locaping.databinding.ActivitySplashBinding
import timber.log.Timber

class SplashActivity: AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        launchStep1()
    }

    private fun launchStep1() {
        val requiredPermissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.FOREGROUND_SERVICE,
            // Manifest.permission.POST_NOTIFICATIONS,
        )
        val hasPermissions = hasPermissions(requiredPermissions)
        Timber.tag(Tags.SPLASH).d("launchStep1(hasPermissions: %s)", hasPermissions)
        if (!hasPermissions) {
            launchPermissionGrant(requiredPermissions)
            return
        }
        launchStep2()
    }

    private fun launchStep2() {
        val pkg = packageName
        val pm = getSystemService(PowerManager::class.java)
        val ignoringBatteryOptimizations = pm.isIgnoringBatteryOptimizations(pkg)
        Timber.tag(Tags.SPLASH)
            .d("launchStep2(isIgnoringBatteryOptimizations: %s)", ignoringBatteryOptimizations)
        if (!ignoringBatteryOptimizations) {
            launchBackgroundOptimizationDisable(packageName = packageName)
            return
        }
        launchStep3()
    }

    private fun launchStep3() {
        Timber.tag(Tags.SPLASH).d("launchStep3()")
        launchMain()
    }

    private fun hasPermissions(requiredPermissions: List<String>): Boolean {
        return requiredPermissions
            .filterNot { permission -> hasPermission(permission) }
            .isEmpty()
    }

    private fun launchPermissionGrant(requiredPermissions: List<String>) {
        Timber.tag(Tags.SPLASH)
            .d("launchPermissionGrant(requiredPermissions: %s)", requiredPermissions)
        ActivityCompat.requestPermissions(
            this,
            requiredPermissions.toTypedArray(),
            AppConstants.REQUEST_TAG_PERMISSION
        )
    }

    private fun hasPermission(permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            permission,
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            AppConstants.REQUEST_TAG_BATTERY_IGNORE -> {
                launchStep2()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            AppConstants.REQUEST_TAG_PERMISSION -> {
                Timber.tag(Tags.SPLASH).d("onRequestPermissionsResult()")
                launchStep1()
            }
        }
    }

    private fun launchBackgroundOptimizationDisable(packageName: String) {
        Timber.tag(Tags.SPLASH).d("launchBackgroundOptimizationDisable()")
        val i: Intent = Intent(ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            .setData(Uri.parse("package:$packageName"))
        startActivityForResult(i, AppConstants.REQUEST_TAG_BATTERY_IGNORE)
    }

    private fun launchMain() {
        Timber.tag(Tags.SPLASH).d("launchMain()")
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}