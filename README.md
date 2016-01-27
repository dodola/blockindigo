# blockindigo

##简介

之前在Blockcanary的issue里讨论过，使用Blockcanary监测细粒度的卡顿会产生很多日志。
另一方面Blockcanary和我们项目里所使用的日志系统有冲突。

所以直接把Blockcanary的日志采样机制和UI拿过来写了这个，是的你没有看错。

在此之前一直使用Debug的MethodTrace进行分析，好像会有效率问题，放弃使用了。

框架的原理使用的是黄油项...(Project Butter) 中的`Choreographer`来监测类似界面绘制时候的丢帧(Skipped Frame)情况。

原理具体可以参照：http://bugly.qq.com/blog/?p=166

##使用
```groovy

repositories {
    maven {
        url "http://dl.bintray.com/dodola/maven"
    }
}

compile 'com.dodola:blockindigo:1.0'
```

