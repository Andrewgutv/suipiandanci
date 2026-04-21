# Figma 设计导出指南

## 📋 导出清单

### 1️⃣ 导出图标
- [ ] 应用图标 (launcher icon)
- [ ] 底部导航图标（首页、生词本、设置）
- [ ] 功能图标（词库、刷新等）
- [ ] 按钮图标

### 2️⃣ 导出图片资源
- [ ] 背景图
- [ ] 插图
- [ ] 装饰元素

### 3️⃣ 导出颜色
- [ ] 主色调 (Primary Color)
- [ ] 强调色 (Accent Color)
- [ ] 背景色 (Background)
- [ ] 文字颜色 (Text)

### 4️⃣ 导出文字样式
- [ ] 标题字号
- [ ] 正文字号
- [ ] 按钮字号

---

## 🚀 快速导出步骤

### A. 导出图片/图标

1. **打开Figma文件**
   - 在浏览器中打开你的Figma链接
   - 登录Figma账号

2. **选中要导出的元素**
   - 点击左侧图层面板
   - 选中画板或图层

3. **设置导出参数**
   ```
   右侧面板 → Export
   格式：PNG (透明背景) 或 SVG (矢量图标)
   倍率：1x, 2x, 3x
   ```

4. **点击导出按钮**
   - 点击 "Export [Frame name]"
   - 保存到文件夹

### B. 导出颜色代码

1. **选中元素**
2. **查看右侧Fill属性**
3. **复制颜色代码**
   - 例如：#2196F3
   - 或者 RGB: (33, 150, 243)

### C. 导出尺寸标注

1. **选中元素**
2. **查看右侧属性**
3. **记录尺寸**
   - Width（宽度）
   - Height（高度）
   - Margin/Padding（间距）

---

## 📁 Android文件夹结构

导出后按此结构放置：

```
app/src/main/res/
├── drawable/
│   ├── ic_home.png
│   ├── ic_notebook.png
│   ├── ic_settings.png
│   └── ic_launcher.png
├── drawable-hdpi/    # 1.5x
├── drawable-xhdpi/   # 2x
├── drawable-xxhdpi/  # 3x
├── values/
│   └── colors.xml    # 颜色
└── layout/
    └── activity_*.xml
```

---

## 💻 示例：导出后如何使用

### 1. 颜色 → colors.xml
```xml
<color name="primary">#2196F3</color>
<color name="accent">#FF4081</color>
<color name="background">#FFFFFF</color>
<color name="text_primary">#212121</color>
```

### 2. 图标 → drawable
```xml
<ImageView
    android:src="@drawable/ic_home"
    android:layout_width="24dp"
    android:layout_height="24dp" />
```

### 3. 尺寸转换
Figma: width: 375px → Android: android:layout_width="375dp"
Figma: font-size: 16px → Android: android:textSize="16sp"

---

## 🎯 替代方案：提供截图

如果你无法在Figma中导出，可以：

1. **截图设计稿**
2. **发给我**
3. **我帮你：**
   - 分析设计结构
   - 生成对应的Android XML代码
   - 创建颜色资源
   - 提供布局建议

---

## 🔗 使用Figma插件（推荐）

### 安装导出插件
1. Figma → Resources → Plugins
2. 搜索并安装：
   - **"Iconic"** - 图标导出
   - **"Android Res Export"** - Android资源导出
   - **"Export"** - 批量导出

### 使用插件导出
1. 选中多个图层
2. 运行插件
3. 一键导出到对应文件夹

---

## ⚡ 快速检查

导出前检查：
- [ ] 图标是否有透明背景
- [ ] 是否导出了多种分辨率
- [ ] 颜色是否记录为HEX格式
- [ ] 文件命名是否清晰（英文）

---

需要我帮你分析设计稿吗？请提供截图！
