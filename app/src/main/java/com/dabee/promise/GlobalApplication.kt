package com.dabee.promise

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class GlobalApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        KakaoSdk.init(this,"a885b205f3f68ff7b70039992bb3ba34")
    }
}