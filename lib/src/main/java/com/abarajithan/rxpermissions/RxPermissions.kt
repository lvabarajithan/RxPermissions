package com.abarajithan.rxpermissions

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.M
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import io.reactivex.rxjava3.core.Observable

/**
 * Class that uses a ShadowFragment for handling
 * permissions in reactive way
 *
 * @author Abarajithan
 */
class RxPermissions {

    companion object {
        private val TAG = RxPermissions::class.java.simpleName

        /**
         * By default Android will throw {@link IllegalArgumentException},
         * if permission is null or empty.
         * Use this flag to control this exception handling
         */
        public var ENABLE_EXCEPTIONS: Boolean = false

        fun test(fragment: ShadowFragment) = RxPermissions(fragment)
    }

    /**
     * A shadow fragment
     */
    private var fragment: ShadowFragment

    constructor(fragmentActivity: FragmentActivity) {
        this.fragment = getShadowFragment(fragmentActivity.supportFragmentManager)
    }

    constructor(fragment: Fragment) {
        this.fragment = getShadowFragment(fragment.childFragmentManager)
    }

    private constructor(fragment: ShadowFragment) {
        this.fragment = fragment
    }

    /**
     * Attach a ShadowFragment and return it
     *
     * @return the ShadowFragment
     */
    private fun getShadowFragment(fragmentManager: FragmentManager): ShadowFragment {
        var shadowFragment = fragmentManager.findFragmentByTag(TAG) as ShadowFragment?
        if (shadowFragment == null) {
            shadowFragment = ShadowFragment()
            fragmentManager.beginTransaction()
                .add(shadowFragment, TAG)
                .commitNow()
        }
        return shadowFragment
    }

    /**
     * Ask for permissions with return values as {@link PermissionResult}.
     * All the granted permission results will be emitted as soons as requested, then
     * the corresponding results are emitted.
     *
     * In Android versions < M, all the results will contain the {@link PermissionResult#granted}
     * as {@code true}
     *
     * @param permissions the list of permissions
     * @return the permission results
     */
    fun request(vararg permissions: String): Observable<Array<out PermissionResult>> {
        if (!isMarshmallow()) {
            return Observable.just(
                Array(permissions.size) { index ->
                    PermissionResult(
                        index, permissions[index],
                        granted = true,
                        rationale = false
                    )
                }
            )
        }
        val granted = ArrayList<PermissionResult>()
        val unGranted = ArrayList<PermissionResult>()

        for ((index, permission) in permissions.withIndex()) {
            if (isGranted(permission)) {
                granted += PermissionResult(index, permission, granted = true, rationale = false)
                continue
            }
            if (isRevokedByPolicy(permission)) {
                granted += PermissionResult(index, permission, granted = false, rationale = false)
                continue
            }
            unGranted += PermissionResult(index, permission, granted = false, rationale = true)
        }
        fragment.requestPermissions(unGranted)
        return Observable.just(Array(granted.size) { granted[it] })
            .concatWith(fragment.permissionsResult())
            .flatMap { arr -> Observable.fromArray(arr.sortedArrayWith(Comparator { p1, p2 -> p1.index - p2.index })) }
    }

    /**
     * Ask for permissions with return values as {@link PermissionResult}
     * This method skips the granted permissions and emits
     * only un-granted {@link PermissionResult}
     *
     * @param permissions the list of permissions
     * @return array of {@link PermissionResult}s with un-granted permissions only
     */
    fun requestSkipGranted(vararg permissions: String): Observable<Array<PermissionResult>> {
        return request(*permissions).concatMap { arr ->
            val list = arr.filter { !it.granted }
            return@concatMap Observable.just(Array(list.size) { i -> list[i] })
        }
    }

    /**
     * Ask for permissions with return values as array of {@link Boolean}.
     *
     * @param permissions the list of permissions
     * @return array of value containing {@code true} for all granted permissions, and
     *         {@code false} for all un-granted permissions
     */
    fun requestSimple(vararg permissions: String): Observable<Array<Boolean>> {
        if (!isMarshmallow()) {
            return Observable.just(Array(permissions.size) { true })
        }
        return request(*permissions).concatMap { arr ->
            val list = arr.map { it.granted }
            return@concatMap Observable.just(Array(list.size) { i -> list[i] })
        }
    }

    /**
     * Checks if a permission is granted
     *
     * @throws IllegalStateException if fragment is not attached to the activity
     * @return true if permission is granted
     */
    private fun isGranted(permission: String) = fragment.isGranted(permission)

    /**
     * Checks if a permission is revoked
     *
     * @throws IllegalStateException if fragment is not attached to the activity
     * @return true if permission is revoked
     */
    @RequiresApi(M)
    private fun isRevokedByPolicy(permission: String) = fragment.isRevokedByPolicy(permission)

    /**
     * @return true if device is >= {@ Build.VERSION_CODES.M}
     */
    private fun isMarshmallow() = SDK_INT >= M
}