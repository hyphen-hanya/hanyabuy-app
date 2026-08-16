# 韩亚Buy App - ProGuard 规则
# WebView 无原生 JS bridge，混淆影响小。保留以下选项以防万一。

# 保留 WebView 相关（一般无需）
-dontwarn android.webkit.**

# 如果不小心加了原生 bridge，需保留注解类；当前无，注释备档：
# -keepattributes JavascriptInterface
# -keepclassmembers class * { @android.webkit.JavascriptInterface <methods>; }
