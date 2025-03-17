package com.example.hazelnews.util // Change this if you have a different package

import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule

@GlideModule
class AppGlideModule : AppGlideModule()
{
    override fun isManifestParsingEnabled(): Boolean = false
}