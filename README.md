[![icon_android](https://github.com/gedoor/gedoor.github.io/blob/master/static/img/legado/icon_android.png)](https://play.google.com/store/apps/details?id=io.legado.play.release)
<a href="https://jb.gg/OpenSourceSupport" target="_blank">
<img width="24" height="24" src="https://resources.jetbrains.com/storage/products/company/brand/logos/jb_beam.svg?_gl=1*135yekd*_ga*OTY4Mjg4NDYzLjE2Mzk0NTE3MzQ.*_ga_9J976DJZ68*MTY2OTE2MzM5Ny4xMy4wLjE2NjkxNjMzOTcuNjAuMC4w&_ga=2.257292110.451256242.1669085120-968288463.1639451734" alt="idea"/>
</a>

<div align="center">
  
Legado 是一款免费的 Android 平台开源小说阅读器。
</div>

# 此 fork：按字数合并听书

基于 [LegadoTeam/legado](https://github.com/LegadoTeam/legado)，新增网络 TTS 的「合并多段朗读」。
在 **听书设置 → 合并多段朗读 → 每次朗读字数** 开启。系统 TTS、原逐段/整页模式保留；开启合并时，网络 TTS 优先使用合并模式。默认不开启，默认目标为 **400 字**，可选 200、300、400、500、600、800、1000。

- 从整章文本组块，可跨屏幕分页，保留段间换行。优先在目标的 75%–125% 范围内寻找段尾，其次句末、分句边界，最后才截断长段；400 字目标通常产生 300–500 字请求。章节尾部可更短，不跨章节拼接。
- 块内起读只提交未读部分；切块保存原文章内偏移，上一段/下一段仍按原始段落定位。
- 合并模式将每块音频完整下载后交给播放器，播放当前块时继续预取后续块；下一章完成排版后预取前两块。开启时优先于流式模式，避免播放器探测、定位时重发 TTS 合成 POST。关闭合并即可恢复原流式设置。
- 合并后，音源中的「段落间隔」作用于请求块之间；块内段落停顿由 TTS 根据换行、标点决定。
- 网络 TTS 不提供逐字时间戳时，文字高亮及页内进度仍为音频时长插值估算。

**Fish / ra.wangc.org 建议：** 先试 400 字；如果首段等待明显，试 300；如果短段很多且合成速度足够，再试 500。2026-10-22 使用现有服务默认声音（嘉岚3.0）、固定免费模型 `s2.1-pro-free` 顺序测试，每个长度两次并完整解码 MP3：300 字返回耗时 23.6–27.0 秒（音频 55.6–60.2 秒），400 字 34.3–37.5 秒（音频 77.3–80.3 秒），500 字 36.4–42.6 秒（音频 93.8–95.4 秒）。400 作为上下文和初始等待的折中；优先更快起播可选 300，连续长听可选 500。小样本存在波动，未证明某个阈值在所有声音、时段和文本上最优。当前转发器源码每次限制 2000 个 UTF-16 单元、60 秒上游超时，并收齐 MP3 后返回；因此不建议直接用整章代替分块。这里的字数按 Android 字符串长度计算，标点、换行也计入，补充平面字符占两个单元，但不会被切成半个字符。

合并模式不需要修改已有 Fish 音源的 URL、Token 或服务模型。源码与 APK 不包含任何服务凭据。

开发预览可用 `./gradlew -I scripts/tts-preview.init.gradle :app:testAppDebugUnitTest :app:assembleAppDebug` 构建。此显式配置将预览包最低版本设为 Android 8（API 26），绕过上游 Rhino 在未压缩调试构建中的 MethodHandle/D8 限制；包名为 `com.legado.app.debug`，显示名为「阅读·合并听书」。正式构建配置未改变。

# Sponsor

> 感谢 [PiPio 中转站](https://pipio.io/offer/miaogongzi) 对项目的赞助支持。

# Grateful-感谢 [![](https://img.shields.io/badge/-Grateful-F5F5F5.svg)](#Grateful-感谢-)
> * org.jsoup:jsoup
> * cn.wanghaomiao:JsoupXpath
> * com.jayway.jsonpath:json-path
> * com.github.gedoor:rhino-android
> * com.squareup.okhttp3:okhttp
> * com.github.bumptech.glide:glide
> * org.nanohttpd:nanohttpd
> * org.nanohttpd:nanohttpd-websocket
> * cn.bingoogolapple:bga-qrcode-zxing
> * com.jaredrummler:colorpicker
> * org.apache.commons:commons-text
> * io.noties.markwon:core
> * io.noties.markwon:image-glide
> * com.hankcs:hanlp
> * com.positiondev.epublib:epublib-core
> * com.github.Moriafly:LyricViewX
> * io.github.rosemoe:editor
