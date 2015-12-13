1.在CropActivity中的onActivityResult方法中获取图片的uri
然后你自己编码一下就好了 存的位置是 PATH = "image.jpg";//内存卡根目录下的image.jpg


2.调用CropActivity实现裁剪功能
Intent intent = new Intent(this, CropActivity,class);
Bundle bundle = new Bundle();
//表示四个顶点
int[] p = new int[]{points[0].x, points[0].y,
                        points[1].x, points[1].y,
                        points[2].x, points[2].y,
                        points[3].x, points[3].y,
                };
bundle.putIntArray(CROP_IMAGE_POINTS, p);
intent.putExtras(bundle);
startActivity(intent);

3.通过CropImageView获取用户调成之后的四个顶点
  通过getPoints()方法获取 返回值是一个Point数组
  在调用这个方法之前应该首先调用isRightStatus()方法 查看当前状态是否是可裁剪的

