package cz.zdrubecky.draganddraw;

import android.graphics.PointF;
import android.os.Parcel;
import android.os.Parcelable;

public class Box implements Parcelable {
    private PointF mOrigin;
    private PointF mCurrent;
    private int mRotationDegree;

    public Box(PointF origin) {
        mOrigin = origin;
        mCurrent = origin;
        mRotationDegree = 0;
    }

    public PointF getCurrent() {
        return mCurrent;
    }

    public void setCurrent(PointF current) {
        mCurrent = current;
    }

    public PointF getOrigin() {
        return mOrigin;
    }

    public int getRotationDegree() {
        return mRotationDegree;
    }

    public void setRotationDegree(int rotationDegree) {
        mRotationDegree = rotationDegree;
    }

    public Box(Parcel in) {
        float[] coordinates = new float[] {};
        in.readFloatArray(coordinates);

        // Keep the order
        mOrigin.x = coordinates[0];
        mOrigin.y = coordinates[1];
        mCurrent.x = coordinates[2];
        mCurrent.y = coordinates[3];
    }

    public void writeToParcel(Parcel destination, int flags) {
        destination.writeFloatArray(new float[] {mOrigin.x, mOrigin.y, mCurrent.x, mCurrent.y});
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // This takes care of the parcelling itself
    public static final Parcelable.Creator CREATOR = new Creator() {
        @Override
        public Object createFromParcel(Parcel source) {
            return new Box(source);
        }

        @Override
        public Box[] newArray(int size) {
            return new Box[size];
        }
    };
}
