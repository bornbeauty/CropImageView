package com.jimbo.mycrop;

/**
 * Created by jimbo on 15-12-6.
 */
public class Point {
    public float x;
    public float y;

    public Point() {}

    public Point(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Point(android.graphics.Point p, float scale, float left, float top) {
        this(p.x * scale + left, p.y * scale + top);
    }

    @Override
    public String toString() {
        return "x:"+this.x+",y:"+this.y;
    }
}
