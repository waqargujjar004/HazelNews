package com.hazelmobile.cores.utils

import android.Manifest

const val STORAGE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE
const val PERMISSIONS_REQ_CODE = 123

typealias IntCallback = (Int) -> Unit
typealias LongCallback = (Long) -> Unit
typealias BooleanCallback = (Boolean) -> Unit
typealias StringCallback = (String) -> Unit
typealias SimpleCallback = () -> Unit
typealias AnyCallback = (Any?) -> Unit
typealias GenericCallback <T> = (T?) -> Unit
typealias GenericPairCallback <S, T> = (S, T?) -> Unit

