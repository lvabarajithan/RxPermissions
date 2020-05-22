package com.abarajithan.rxpermissions

import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject

/**
 * The shadow fragment for requesting and handling permission results.
 *
 * @author Abarajithan
 */
class ShadowFragment : Fragment() {

    companion object {
        private const val RC_PERMISSIONS = 100
    }

    private lateinit var permissions: ArrayList<PermissionResult>
    private lateinit var subject: PublishSubject<Array<PermissionResult>>

    fun requestPermissions(permissions: ArrayList<PermissionResult>) {
        this.permissions = permissions
        this.subject = PublishSubject.create()
        if (permissions.isEmpty() && !RxPermissions.ENABLE_EXCEPTIONS) {
            return
        }
        requestPermissions(Array(permissions.size) { permissions[it].permission }, RC_PERMISSIONS)
    }

    /**
     * Handle the requested permission results.
     * Emit the results and complete the subject.
     */
    override fun onRequestPermissionsResult(rc: Int, perms: Array<out String>, grants: IntArray) {
        if (rc == RC_PERMISSIONS) {
            for ((i, g) in grants.withIndex()) {
                permissions[i].granted = g == PERMISSION_GRANTED
                permissions[i].rationale = shouldShowRequestPermissionRationale(perms[i])
            }
            subject.onNext(Array(permissions.size) { permissions[it] })
            subject.onComplete()
        }
    }

    /**
     * @return the subject
     */
    fun permissionsResult(): Observable<Array<PermissionResult>> {
        return subject
    }

    /**
     * Checks if a permission is granted
     *
     * @throws IllegalStateException if fragment is not attached to the activity
     * @return true if permission is granted
     */
    fun isGranted(permission: String): Boolean {
        val activity = activity
            ?: throw IllegalStateException("This fragment is not attached to the activity")
        return ContextCompat.checkSelfPermission(activity, permission) == PERMISSION_GRANTED
    }

    /**
     * Checks if a permission is revoked
     *
     * @throws IllegalStateException if fragment is not attached to the activity
     * @return true if permission is revoked
     */
    @RequiresApi(Build.VERSION_CODES.M)
    fun isRevokedByPolicy(permission: String): Boolean {
        val activity = activity
            ?: throw IllegalStateException("This fragment is not attached to the activity")
        return activity.packageManager.isPermissionRevokedByPolicy(
            permission,
            activity.packageName
        )
    }

}