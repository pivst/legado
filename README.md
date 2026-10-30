[![icon_android](https://github.com/gedoor/gedoor.github.io/blob/master/static/img/legado/icon_android.png)](https://play.google.com/store/apps/details?id=io.legado.play.release)
<a href="https://jb.gg/OpenSourceSupport" target="_blank">
<img width="24" height="24" src="https://resources.jetbrains.com/storage/products/company/brand/logos/jb_beam.svg?_gl=1*135yekd*_ga*OTY4Mjg4NDYzLjE2Mzk0NTE3MzQ.*_ga_9J976DJZ68*MTY2OTE2MzM5Ny4xMy4wLjE2NjkxNjMzOTcuNjAuMC4w&_ga=2.257292110.451256242.1669085120-968288463.1639451734" alt="idea"/>
</a>

<div align="center">
  
Legado 是一款免费的 Android 平台开源小说阅读器。
</div>

# 此 fork：按字数合并听书

基于 [LegadoTeam/legado](https://github.com/LegadoTeam/legado)，新增网络 TTS 的「合并多段朗读」。
在 **听书设置 → 合并多段朗读 → 每次朗读字数** 开启。系统 TTS、原逐段/整页模式保留；开启合并时，网络 TTS 优先使用合并模式。默认不开启，新设置默认选择 **智能章节**；保留固定 200、300、400、500、600、800、1000 字。已有用户的字数设置保留，可手动切换智能章节。

- 智能章节：1200 字以内整章合成；更长章节按约 1200 字均衡分块，通常在段尾或句末切分，单块至多约 1500 字。没有缓存时，当前起读块先切出约 400 字，其余继续大块预取；已有整块缓存时直接复用。
- 合并播放时按实际播放器队列时长与播放倍速补充预取，目标约 3 分钟、最多领先当前两块；下一章未完成排版时等待最多 30 秒再尝试预取前两块。音源失败有界重试，不以静音冒充缺失正文；后续预取失败不会立即暂停正在播放的音频。
- 从整章文本组块，可跨屏幕分页，保留段间换行。优先在目标的 75%–125% 范围内寻找段尾，其次句末、分句边界，最后才截断长段；400 字目标通常产生 300–500 字请求。章节尾部可更短，不跨章节拼接。
- 块内起读只提交未读部分；切块保存原文章内偏移，上一段/下一段仍按原始段落定位。
- 合并模式将每块音频完整下载后交给播放器，播放当前块时继续预取后续块；下一章完成排版后预取前两块。开启时优先于流式模式，避免播放器探测、定位时重发 TTS 合成 POST。关闭合并即可恢复原流式设置。
- 合并后，音源中的「段落间隔」作用于请求块之间；块内段落停顿由 TTS 根据换行、标点决定。
- 网络 TTS 不提供逐字时间戳时，文字高亮及页内进度仍为音频时长插值估算。

**Fish / ra.wangc.org 建议：** 使用智能章节；2026-10-30 转发接口已放宽至 2400 字、240 秒超时。实测 1200 字耗时 95.3 秒、音频 204.1 秒；1800 字耗时 165.8 秒、音频 348.9 秒；2400 字耗时 190.7 秒、音频 415.9 秒，均完整解码。保留约 1200 字作为智能目标，超大块仅用于边界验证。以下为旧接口的历史短文本结果：2026-10-22 使用现有服务默认声音（嘉岚3.0）、固定免费模型 `s2.1-pro-free` 顺序测试，每个长度两次并完整解码 MP3：300 字返回耗时 23.6–27.0 秒（音频 55.6–60.2 秒），400 字 34.3–37.5 秒（音频 77.3–80.3 秒），500 字 36.4–42.6 秒（音频 93.8–95.4 秒）。400 作为上下文和初始等待的折中；优先更快起播可选 300，连续长听可选 500。小样本存在波动，未证明某个阈值在所有声音、时段和文本上最优。当时的转发器每次限制 2000 个 UTF-16 单元、60 秒上游超时，并收齐 MP3 后返回；因此不建议直接用整章代替分块。这里的字数按 Android 字符串长度计算，标点、换行也计入，补充平面字符占两个单元，但不会被切成半个字符。

合并模式不需要修改已有 Fish 音源的 URL、Token 或服务模型。源码与 APK 不包含任何服务凭据。

开发预览可用 `./gradlew -I scripts/tts-preview.init.gradle :app:testAppDebugUnitTest :app:assembleAppDebug` 构建。此显式配置将预览包最低版本设为 Android 8（API 26），绕过上游 Rhino 在未压缩调试构建中的 MethodHandle/D8 限制；包名为 `com.legado.app.debug`，显示名为「阅读·合并听书」。正式构建配置未改变。

独立 Release 使用 `./gradlew -PttsFork=true :app:assembleAppRelease`：包名 `org.pivst.legado.tts`，显示名「阅读 pivst」，保留上游的 Android 6（API 23）最低版本、R8 代码优化和资源裁剪。由于新包名未注册上游 Firebase 项目，此构建不接入上游 Firebase 统计。未提供签名属性时生成 unsigned APK，可在本地使用自己的发布证书签名；私钥及密码不得提交到仓库。新包名与原版、之前的 Debug 包并存，不会自动继承它们的数据。省略 `ttsFork` 时保留上游的包名和 Firebase 配置。

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
