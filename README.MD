##### 一 概述

功能需求：
类似于全能扫描王的功能：裁剪一个四边形然后利用算法矫正成一个矩形的图片。这个裁剪模块是这样的，外部传入一个Bitmap和四个点(利用算法识别出来的原始点)，显示四个点并且让用户调整到合适的位置，最后获取到用户调整后的四个点。
这就是基本的需求，比较简单。

##### 二 思路

继承ImageView，并且重写`onDraw()`，`onLayout()`和`onTouchEvent()`。
1. 用四个圆圈提供用户的交互接口，
2. 在`onLayout()`中获取到view的`width`和`height`，获取`bitmap`的`width`和`height`，在这里计算出图片在view中的缩放比例。(为了好操作和美观，让图片在view中铺满居中显示)。
3. 在`onDraw()`方法中绘制操作符号。
4. 在`onTouchEvent()`方法中监听用户的触摸事件。在`ACTION_DOWN`事件的时候记录下事件发生的位置，然后在`ACTION_MOVE`每次触发的时候记录事件位置，并且和上一个的事件位置比较，得出相应的事件并且做出相应的回应。在`ACTION_UP`中结束事件。

#####　三　效果展示

![](http://7xjtan.com1.z0.glb.clouddn.com/cropImageView_Show.png)

##### 四 使用方法

1. setImageView() 设置bitmap
2. setPoints() 给出初始的四个点
3. getPoints() 获取调整后的四个点
4. isRightStatus() 判断当前位置是能够构成一个凸四边形

##### 五 项目地址

[bornbeauty-CropImageView:不规则四边形裁剪](https://github.com/bornbeauty/CropImageView)


# License

> Copyright 2013, Edmodo, Inc.

>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with the License. You may obtain a copy of the License in the LICENSE file, or at:

>http://www.apache.org/licenses/LICENSE-2.0

>Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.