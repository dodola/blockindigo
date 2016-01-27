/*
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package dodola.block.demo;

import android.app.Application;

import com.github.moduth.blockcanary.BlockCanaryContext;

import dodola.blockindigo.BlockIndigo;

/**
 * Created by sunpengfei on 16/1/17.
 */
public class WatcherApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        BlockIndigo.install(this, new BlockCanaryContext());
    }
}
