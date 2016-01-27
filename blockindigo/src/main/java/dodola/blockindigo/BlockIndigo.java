/*
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package dodola.blockindigo;

import android.app.Activity;
import android.content.Context;
import android.preference.PreferenceManager;

import com.github.moduth.blockcanary.BlockCanaryContext;
import com.github.moduth.blockcanary.BlockCanaryCore;
import com.github.moduth.blockcanary.OnBlockEventInterceptor;
import com.github.moduth.blockcanary.UploadMonitorLog;

import java.lang.reflect.Constructor;

public class BlockIndigo {

    private static BlockIndigo sInstance;
    private ChoreographerAnalysis choregrapherAnalysis;

    private BlockIndigo() {
        BlockCanaryCore.setIBlockCanaryContext(BlockCanaryContext.get());

        ChoreographerAnalysis.setIBlockCanaryContext(BlockCanaryContext.get());

        choregrapherAnalysis = ChoreographerAnalysis.get();
        initNotification();
    }

    /**
     * Install {@link BlockIndigo}
     *
     * @param context application context
     * @param blockCanaryContext implementation for {@link BlockCanaryContext}
     * @return {@link BlockIndigo}
     */
    public static BlockIndigo install(Context context, BlockCanaryContext blockCanaryContext) {
        BlockCanaryContext.init(context, blockCanaryContext);
        return get();
    }

    /**
     * Get {@link BlockIndigo} singleton.
     *
     * @return {@link BlockIndigo} instance
     */
    public static BlockIndigo get() {
        if (sInstance == null) {
            synchronized (BlockIndigo.class) {
                if (sInstance == null) {
                    sInstance = new BlockIndigo();
                }
            }
        }
        return sInstance;
    }

    /**
     * Start main-thread monitoring.
     */
    public void start() {
    }

    /**
     * Stop monitoring.
     */
    public void stop() {
        choregrapherAnalysis.stop();
    }

    public void start(Activity activity) {
        choregrapherAnalysis.start(activity);
    }

    /**
     * Zip and upload log files.
     */
    public void upload() {
        UploadMonitorLog.forceZipLogAndUpload();
    }

    /**
     * 记录开启监控的时间到preference，可以在release包收到push通知后调用。
     */
    public void recordStartTime() {
        PreferenceManager.getDefaultSharedPreferences(BlockCanaryContext.get().getContext()).edit()
                .putLong("BlockCanary_StartTime", System.currentTimeMillis()).commit();
    }

    /**
     * 是否监控时间结束，根据上次开启的时间(recordStartTime)和getConfigDuration计算出来。
     *
     * @return true则结束
     */
    public boolean isMonitorDurationEnd() {
        long startTime =
                PreferenceManager.getDefaultSharedPreferences(BlockCanaryContext.get().getContext()).getLong(
                        "BlockCanary_StartTime", 0);
        return startTime != 0
                && System.currentTimeMillis() - startTime > BlockCanaryContext.get().getConfigDuration() * 3600 * 1000;
    }

    private void initNotification() {
        if (!BlockCanaryContext.get().isNeedDisplay()) {
            return;
        }

        try {
            Class notifier = Class.forName("com.github.moduth.blockcanary.ui.Notifier");
            if (notifier == null) {
                return;
            }
            Constructor<? extends OnBlockEventInterceptor> constructor = notifier.getConstructor();
            choregrapherAnalysis.setOnBlockEventInterceptor(constructor.newInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
