package com.a1573595.musicplayer.common.delegate

import timber.log.Timber
import java.lang.ref.WeakReference
import kotlin.reflect.KProperty

class WeakReferenceDelegate<T : Any>(initializer: () -> T?) {
    var weakReference = WeakReference<T?>(initializer())

    constructor() : this({ null })

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        Timber.d("WeakReferenceDelegate Delegate getValue")
        return weakReference.get()
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        Timber.d("WeakReferenceDelegate Delegate setValue")
        weakReference = WeakReference(value)
    }
}
