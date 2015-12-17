>算是自己造的第一个轮子吧 以前总是拿过别人开源的东西来用 即使做出什么东西来也没有什么成就感 能自己造轮子感觉很棒

开篇废话:距离自己上一篇的博客已经过了好几天了 一来是因为自己忙了些其他事情 另外 也许是因为过了新鲜感了吧 当刚建博客的时候总是时不时来看看 欣赏以下自己的劳动成果 几天过后 没有了新鲜劲 也就那么回事了 人嘛 总是有惰性的

# 一 概述

功能需求：
类似于全能扫描王的功能：裁剪一个四边形然后利用算法矫正成一个矩形的图片。这个裁剪模块是这样的，外部传入一个Bitmap和四个点(利用算法识别出来的原始点)，显示四个点并且让用户调整到合适的位置，最后获取到用户调整后的四个点。
这就是基本的需求，比较简单。

# 二 思路

继承ImageView，并且重写`onDraw()`，`onLayout()`和`onTouchEvent()`。
1. 用四个圆圈提供用户的交互接口，
2. 在`onLayout()`中获取到view的`width`和`height`，获取`bitmap`的`width`和`height`，在这里计算出图片在view中的缩放比例。(为了好操作和美观，让图片在view中铺满居中显示)。
3. 在`onDraw()`方法中绘制操作符号。
4. 在`onTouchEvent()`方法中监听用户的触摸事件。在`ACTION_DOWN`事件的时候记录下事件发生的位置，然后在`ACTION_MOVE`每次触发的时候记录事件位置，并且和上一个的事件位置比较，得出相应的事件并且做出相应的回应。在`ACTION_UP`中结束事件。

#　三　效果展示

![](http://7xjtan.com1.z0.glb.clouddn.com/cropImageView_Show.png)

# 四 使用方法

1. setImageView() 设置bitmap
2. setPoints() 给出初始的四个点
3. getPoints() 获取调整后的四个点
4. isRightStatus() 判断当前位置是能够构成一个凸四边形

# 五 项目地址

[bornbeauty-CropImageView:不规则四边形裁剪](https://github.com/bornbeauty/CropImageView)

