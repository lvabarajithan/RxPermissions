package com.abarajithan.rxpermissions

/**
 * Permission result model
 *
 * @author Abarajithan
 */
class PermissionResult(
    var index: Int,
    var permission: String,
    var granted: Boolean = false,
    var rationale: Boolean = true
)