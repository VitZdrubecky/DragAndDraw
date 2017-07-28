package cz.zdrubecky.draganddraw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;


public class BoxDrawingView extends View {
    private static final String TAG = "BoxDrawingView";

    private Box mCurrentBox;
    private List<Box> mBoxen = new ArrayList<>();

    // The Paint classes represent how the operations on Canvas are made
    private Paint mBoxPaint;
    private Paint mBackgroundPaint;

    private Integer mPrimaryPointerId;
    private Integer mSecondaryPointerId;
    private int mCurrentRotationDegree;

    // When creating programatically
    public BoxDrawingView(Context context) {
        this(context, null);
    }

    // When inflating from XML
    public BoxDrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Paint the boxes semitransparent red
        mBoxPaint = new Paint();
        mBoxPaint.setColor(0x22ff0000);

        // Paint the background off-white
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(0xfff8efe0);

        mCurrentRotationDegree = 0;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        handleTouch(event);

        return true;
    }

    void handleTouch(MotionEvent m)
    {
        int pointerCount = m.getPointerCount();

        for (int i = 0; i < pointerCount; i++)
        {
            // A pair of floats
            PointF current = new PointF(m.getX(i), m.getY(i));

            int id = m.getPointerId(i);
            int action = m.getActionMasked();
            // This returns an index related to the current action, but only for pointer up and down, note move!
            int actionIndex = m.getActionIndex();
            int actionPointerId = m.getPointerId(actionIndex);
            String actionString;


            switch (action)
            {
                case MotionEvent.ACTION_DOWN:
                    actionString = "DOWN";

                    if (mPrimaryPointerId == null) {
                        mPrimaryPointerId = actionPointerId;
                    }

                    // Reset drawing state
                    mCurrentBox = new Box(current);
                    mBoxen.add(mCurrentBox);

                    break;
                case MotionEvent.ACTION_UP:
                    actionString = "UP";

                    if (mPrimaryPointerId != null) {
                        mPrimaryPointerId = null;
                        mCurrentBox = null;
                    } else if (mSecondaryPointerId != null) {
                        mSecondaryPointerId = null;
                        mCurrentRotationDegree = 0;
                    }

                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    actionString = "PNTR DOWN";

                    if (mSecondaryPointerId == null) {
                        mSecondaryPointerId = actionPointerId;
                    }

                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    actionString = "PNTR UP";

                    if (mSecondaryPointerId != null) {
                        mSecondaryPointerId = null;
                        mCurrentRotationDegree = 0;
                    }

                    break;
                case MotionEvent.ACTION_MOVE:
                    actionString = "MOVE";

                    // If a box is being drawn and this pointer references the primary finger, set the new coordinates regardless of them being changed
                    // But if the movement is made using the second finger, rotate the box instead
                    if (mCurrentBox != null) {
                        if (id == mPrimaryPointerId) {
                            mCurrentBox.setCurrent(current);

                            // Force the View to re-draw via the onDraw method, called on itself and the children as well
                            invalidate();
                        } else if (id == mSecondaryPointerId) {
                            mCurrentRotationDegree++;

                            Log.d(TAG, "Incrementing the current rotation degree to " + Integer.toString(mCurrentRotationDegree));

                            mCurrentBox.setRotationDegree(mCurrentRotationDegree);
                        }

                    }

                    break;
                case MotionEvent.ACTION_CANCEL:
                    actionString = "CANCEL";

                    if (mPrimaryPointerId != null) {
                        mPrimaryPointerId = null;
                    } else if (mSecondaryPointerId != null) {
                        mSecondaryPointerId = null;
                        mCurrentRotationDegree = 0;
                    }

                    mCurrentBox = null;

                    break;
                default:
                    actionString = "";
            }

            String touchStatus = "Action: " + actionString + " Index: " + actionIndex + " ID: " + id + " X: " + current.x + " Y: " + current.y;

            if (id == 0)
                Log.d(TAG, "First pointer: " + touchStatus);
            else
                Log.d(TAG, "Subsequent pointer: " + touchStatus);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // First, fill the background
        canvas.drawPaint(mBackgroundPaint);

        for (Box box : mBoxen) {
            float left = Math.min(box.getOrigin().x, box.getCurrent().x);
            float right = Math.max(box.getOrigin().x, box.getCurrent().x);

            // The coordinates origin is in the top left corner
            float top = Math.min(box.getOrigin().y, box.getCurrent().y);
            float bottom = Math.max(box.getOrigin().y, box.getCurrent().y);

            // Save the current matrix to a private stack to which it's than restored back
            canvas.save();
            // Rotate by the given degree
            canvas.rotate(box.getRotationDegree());
            Log.d(TAG, "Box's rotation degree: " + Integer.toString(box.getRotationDegree()));
            canvas.drawRect(left, top, right, bottom, mBoxPaint);
            // Let go of the canvas
            canvas.restore();
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        // The parent view's state has to be also saved
        bundle.putParcelable("superState", super.onSaveInstanceState());

        if (mBoxen.size() > 0) {
            // Transform the list into an array
            Box[] boxes = new Box[mBoxen.size()];
            // The argument represents the runtime type, so it's viewed as a Box instead of an Object
            boxes = mBoxen.toArray(boxes);
            // Now I can pass the array of Boxes as Parcelables
            bundle.putParcelableArray("boxes", boxes);
        }

        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            Parcelable[] boxes = bundle.getParcelableArray("boxes");

            if (boxes != null) {
                // Add the boxes one by one, casting each one of them
                for (Parcelable parcel : boxes) {
                    mBoxen.add((Box) parcel);
                }
            }

            state = bundle.getParcelable("superState");
        }

        super.onRestoreInstanceState(state);
    }
}
