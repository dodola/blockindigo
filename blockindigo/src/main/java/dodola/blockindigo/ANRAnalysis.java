///*
// * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
// */
//package dodola.blockindigo;
//
//import android.os.Handler;
//import android.os.Looper;
//
///**
// * Created by sunpengfei on 16/1/17.
// */
//public class ANRAnalysis extends Thread {
//    private volatile int prevTimeTick;
//    private volatile int nextTimeTick;
//    /**
//     * 主线程Handler,
//     */
//    private Handler mainUIHandler;
//    /**
//     *
//     */
//    private Runnable prevTimeTickRunnable = new Runnable() {
//        @Override
//        public void run() {
//            prevTimeTick = (prevTimeTick + 1) % 100;
//        }
//    };
//    /**
//     *
//     */
//    private Runnable nextTimeTickRunnable = new Runnable() {
//        @Override
//        public void run() {
//            nextTimeTick = (nextTimeTick + 1) % 100;// %100为了防止整数溢出
//        }
//    };
//
//    /**
//     * 判定anr开始的时限,在主线程卡住5000毫秒的时候,开始记录method_trace
//     */
//    private final int DEFAULT_ANR_TIME = 3000;
//
//    public ANRAnalysis() {
//        setName("DODO_ANR_WATCHER");
//        mainUIHandler = new Handler(Looper.getMainLooper());
//    }
//
//    public void run() {
//        boolean mayAnr = false;
//        while (true) {
//            int lastPrevTick = this.prevTimeTick;
//            int lastNextTick = this.nextTimeTick;
//            this.mainUIHandler.post(this.prevTimeTickRunnable);
//            this.mainUIHandler.post(this.nextTimeTickRunnable);
//            try {
//                Thread.sleep(50);
//                if (this.prevTimeTick == lastPrevTick) {
//                    mayAnr = true;
//                }
//                if (mayAnr) {
//                    mayAnr = false;
//                    try {
//                        Thread.sleep(DEFAULT_ANR_TIME);
//                        if (this.nextTimeTick == lastNextTick) {
//                            return;
//                        }
//                    } catch (InterruptedException e2) {
//                        return;
//                    }
//                }
//            } catch (InterruptedException e3) {
//                return;
//            }
//        }
//    }
//}
