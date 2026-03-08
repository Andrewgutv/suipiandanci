# ✅ 原生插件目录结构已修复

## **问题原因**
HBuilderX 原生插件需要特定的目录结构，之前的文件组织不正确。

---

## **已修复的目录结构**

```
nativeplugins/
└── WordLockScreen/
    ├── package.json                      # 插件元数据
    └── android/
        ├── AndroidManifest.xml          # Android 配置
        ├── android-plugins.json         # 插件注册（新增）
        └── src/
            └── com/
                └── wordlocks/
                    ├── LockScreenWordService.java
                    ├── WordLockModule.java
                    └── WordLockReceiver.java
```

---

## **现在需要做的操作**

### **1. 清理 HBuilderX 缓存**

```
HBuilderX 菜单 → 工具 → 清理缓存
→ 清理项目缓存
→ 确认
```

### **2. 重启 HBuilderX**

```
完全关闭 HBuilderX
→ 重新打开
→ 打开 fragment-words 项目
```

### **3. 验证插件识别**

打开 `manifest.json`：

```
┌─────────────────────────────────────┐
│  App常用其它配置                     │
├─────────────────────────────────────┤
│                                      │
│  原生插件配置                        │
│  ☑️ WordLockScreen                  │  ← 应该能看到这个插件
│                                      │
│  如果看不到：                        │
│  1. 点击"刷新"按钮                   │
│  2. 重启 HBuilderX                   │
│                                      │
└─────────────────────────────────────┘
```

### **4. 重新云打包**

```
发行 → 原生App-云打包
→ Android → 勾选
→ Android配置
→ ☑️ 使用原生插件（应该显示 WordLockScreen）
→ 打包
```

---

## **验证清单**

在打包前确认：

```
✅ 插件目录结构正确（如上所示）
✅ package.json 中 id 为 "WordLockScreen"
✅ manifest.json 中 nativePlugins 包含 "WordLockScreen"
✅ android-plugins.json 文件存在
✅ Java 文件在 src/com/wordlocks/ 目录下
✅ HBuilderX 已重启
✅ 项目缓存已清理
```

---

## **如果还是提示插件不存在**

### **方案 A：手动刷新插件**

1. 在 HBuilderX 项目管理器中
2. 右键点击 `nativeplugins` 文件夹
3. 选择"刷新"或"重新加载"

### **方案 B：重新导入项目**

1. 关闭当前项目
2. 文件 → 导入 → 从本地目录导入
3. 选择 `D:\workspace\app\fragment-words`

### **方案 C：检查文件编码**

确保所有文本文件（.json, .xml）都是 UTF-8 编码：
```
manifest.json → UTF-8
package.json → UTF-8
AndroidManifest.xml → UTF-8
```

---

## **目录结构对比**

### ❌ 之前错误的结构
```
WordLockScreen/
├── package.json
└── android/
    ├── AndroidManifest.xml
    ├── LockScreenWordService.java     ← 位置错误
    ├── WordLockModule.java            ← 位置错误
    └── WordLockReceiver.java          ← 位置错误
```

### ✅ 现在正确的结构
```
WordLockScreen/
├── package.json
└── android/
    ├── AndroidManifest.xml
    ├── android-plugins.json          ← 新增
    └── src/
        └── com/
            └── wordlocks/
                ├── LockScreenWordService.java  ✅
                ├── WordLockModule.java         ✅
                └── WordLockReceiver.java       ✅
```

---

## **下一步**

1. **重启 HBuilderX**
2. **清理项目缓存**
3. **验证插件被识别**
4. **重新云打包**

如果还遇到问题，请告诉我具体的错误信息。

---

**更新时间**: 2026-03-02
**状态**: 已修复目录结构
