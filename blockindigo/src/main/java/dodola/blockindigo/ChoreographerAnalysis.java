/*
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package dodola.blockindigo;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Choreographer;
import android.view.Choreographer.FrameCallback;

import com.github.moduth.blockcanary.CpuSampler;
import com.github.moduth.blockcanary.IBlockCanaryContext;
import com.github.moduth.blockcanary.LogWriter;
import com.github.moduth.blockcanary.OnBlockEventInterceptor;
import com.github.moduth.blockcanary.ThreadStackSampler;
import com.github.moduth.blockcanary.log.Block;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class ChoreographerAnalysis {
    private static boolean isStop = false;
    private long mFrameIntervalNanos;
    private Activity mActivity;
    private FrameMonitor monitor = null;
    public ThreadStackSampler threadStackSampler;
    public CpuSampler cpuSampler;
    private static IBlockCanaryContext sBlockCanaryContext;
    private OnBlockEventInterceptor mOnBlockEventInterceptor;

    @SuppressLint({ "NewApi" })
    private class FrameMonitor implements FrameCallback {
        private long doFrameCostTime;
        private boolean isTraceViewStarted;
        private long startTime;
        private long realTimeStart;
        private long realTimeEnd;
        private long threadTimeStart;
        private long threadTimeEnd;

        private FrameMonitor() {
            this.startTime = 0;
            this.doFrameCostTime = 0;
            this.isTraceViewStarted = false;
            realTimeStart = System.currentTimeMillis();
        }

        public void doFrame(long frameTimeNanos) {
            long doFrameStart = System.nanoTime();
            long costTime = TimeUnit.NANOSECONDS.toMillis(frameTimeNanos - this.startTime) - this.doFrameCostTime;
            this.startTime = frameTimeNanos;
            openTraceview(costTime);
            this.doFrameCostTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - doFrameStart);
            if (!ChoreographerAnalysis.isStop && ChoreographerAnalysis.this.mActivity != null) {
                ChoreographerAnalysis.this.mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        Choreographer.getInstance().postFrameCallback(ChoreographerAnalysis.this.monitor);
                    }
                });
            }
        }

        private void openTraceview(long costTime) {

            if (costTime > 160 && !this.isTraceViewStarted) {// 300ms
                this.isTraceViewStarted = true;
                realTimeEnd = System.currentTimeMillis();

                ArrayList<String> threadStackEntries =
                        threadStackSampler.getThreadStackEntries(realTimeStart, realTimeEnd);
                if (threadStackEntries.size() > 0) {
                    threadTimeStart = realTimeStart;
                    threadTimeEnd = SystemClock.currentThreadTimeMillis();
                    Block block =
                            Block.newInstance()
                                    .setMainThreadTimeCost(realTimeStart, realTimeEnd, threadTimeStart, threadTimeEnd)
                                    .setCpuBusyFlag(cpuSampler.isCpuBusy(realTimeStart, realTimeEnd))
                                    .setRecentCpuRate(cpuSampler.getCpuRateInfo())
                                    .setThreadStackEntries(threadStackEntries).flushString();
                    LogWriter.saveLooperLog(block.toString());

                    if (getContext().isNeedDisplay() && mOnBlockEventInterceptor != null) {
                        mOnBlockEventInterceptor.onBlockEvent(getContext().getContext(), block.timeStart);
                    }
                }
            } else if (costTime <= mFrameIntervalNanos && this.isTraceViewStarted) {
                this.isTraceViewStarted = false;
            }
            if (costTime <= mFrameIntervalNanos) {
                realTimeStart = System.currentTimeMillis();
            }
        }
    }

    private static final int MIN_INTERVAL_MILLIS = 300;

    private long sampleInterval(int blockThresholdMillis) {
        long sampleIntervalMillis = blockThresholdMillis / 2;
        if (sampleIntervalMillis < MIN_INTERVAL_MILLIS) {
            sampleIntervalMillis = MIN_INTERVAL_MILLIS;
        }
        return sampleIntervalMillis;
    }

    private ChoreographerAnalysis() {
        int blockThresholdMillis = getContext().getConfigBlockThreshold();
        long sampleIntervalMillis = sampleInterval(blockThresholdMillis);
        threadStackSampler = new ThreadStackSampler(Looper.getMainLooper().getThread(), sampleIntervalMillis);
        cpuSampler = new CpuSampler();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void start(Activity activity) {
        this.mActivity = activity;
        threadStackSampler.start();
        cpuSampler.start();
        float refreshRate = mActivity.getWindowManager().getDefaultDisplay().getRefreshRate();
        mFrameIntervalNanos = (long) (1000000000 / refreshRate);
        if (this.monitor == null) {
            this.monitor = new FrameMonitor();
        }
        isStop = false;
        if (this.mActivity != null) {
            this.mActivity.runOnUiThread(new Runnable() {
                public void run() {
                    Choreographer.getInstance().postFrameCallback(ChoreographerAnalysis.this.monitor);
                }
            });
        }
    }

    public void stop() {
        isStop = true;
        threadStackSampler.stop();
        cpuSampler.stop();
        this.monitor = null;
    }

    public static void setIBlockCanaryContext(IBlockCanaryContext blockCanaryContext) {
        sBlockCanaryContext = blockCanaryContext;
    }

    public void setOnBlockEventInterceptor(OnBlockEventInterceptor onBlockEventInterceptor) {
        mOnBlockEventInterceptor = onBlockEventInterceptor;
    }

    public static IBlockCanaryContext getContext() {
        return sBlockCanaryContext;
    }

    private static ChoreographerAnalysis sInstance;

    public static ChoreographerAnalysis get() {
        if (sInstance == null) {
            synchronized (ChoreographerAnalysis.class) {
                if (sInstance == null) {
                    sInstance = new ChoreographerAnalysis();
                }
            }
        }
        return sInstance;
    }

}