/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 11/11/19 7:26 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package net.geeksempire.loadingspin.sprite;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by ybq.
 */
public class RingSprite extends ShapeSprite {


    @Override
    public void drawShape(Canvas canvas, Paint paint) {
        if (getDrawBounds() != null) {
            paint.setStyle(Paint.Style.STROKE);
            int radius = Math.min(getDrawBounds().width(), getDrawBounds().height()) / 2;
            paint.setStrokeWidth(radius / 12);
            canvas.drawCircle(getDrawBounds().centerX(),
                    getDrawBounds().centerY(),
                    radius, paint);
        }
    }

    @Override
    public ValueAnimator onCreateAnimation() {
        return null;
    }
}
