package com.akapps.check_verification_system.classes.Glide;

import android.content.Context;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.google.firebase.storage.StorageReference;
import java.io.InputStream;

/**
 * Glide Class that is required for using with Firebase Storage.
 * FirebaseImageLoader class could not be found so I added it from
 * FirebaseUI Library so it could work.
 */

@GlideModule
public class MyAppGlideModule extends AppGlideModule {

    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {
        registry.append(StorageReference.class, InputStream.class,
                new FirebaseImageLoader.Factory());
    }
}