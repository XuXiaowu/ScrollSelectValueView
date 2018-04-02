package mirahome.customapplication.base;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by xuxiaowu on 2016/12/22.
 */
public abstract class AbsViewGroup extends ViewGroup {
    public static final int LINE_VIEW_HEIGHT = 1;

    protected int mViewWidth;
    protected int mViewHeight;
    protected Context mContext;

    public AbsViewGroup(Context context) {
        this(context, null);
    }

    public AbsViewGroup(Context context, AttributeSet attr) {
        super(context, attr);
        mContext = context;
        initView(context);
        initSize(context);
        initPadding(context);
        initRect(context);
    }

    public abstract void initView(Context context);

    public abstract void initSize(Context context);

    public abstract void initPadding(Context context);

    public abstract void initRect(Context context);

    public final void reMeasure() {
        mViewHeight = 0;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            if (view instanceof AbsViewGroup) {
                ((AbsViewGroup) view).reMeasure();
            }
        }
        requestLayout();
    }


    protected void recycleBitmapByBg(ImageView iv) {
        Drawable drawable = iv.getBackground();
        if (drawable != null) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap recycleBitmap = bitmapDrawable.getBitmap();
            if (recycleBitmap != null && !recycleBitmap.isRecycled()) {
                recycleBitmap.recycle();
                recycleBitmap = null;
            }
            System.gc();
        }
    }

    protected void recycleBitmapBySrc(ImageView iv) {
        Drawable drawable = iv.getDrawable();
        if (drawable != null) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap recycleBitmap = bitmapDrawable.getBitmap();
            if (recycleBitmap != null && !recycleBitmap.isRecycled()) {
                recycleBitmap.recycle();
                recycleBitmap = null;
            }
            System.gc();
        }
    }

    public void setImageBitmap(int resId, ImageView iv) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resId);
        iv.setImageBitmap(bitmap);
    }

    public final void setImageBitmapBackground(int resId, ImageView iv) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resId);
        iv.setScaleType(ImageView.ScaleType.FIT_XY);
        iv.setImageBitmap(bitmap);
    }

    public final Drawable setImageBackGroundDrawable(int resId, ImageView iv) {
        Drawable drawable = getResources().getDrawable(resId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            iv.setBackgroundDrawable(drawable);
        } else {
            iv.setBackground(drawable);
        }
        return drawable;
    }

    public final Drawable setTextBackGroundDrawable(int resId, TextView tv) {
        Drawable drawable = getResources().getDrawable(resId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            tv.setBackgroundDrawable(drawable);
        } else {
            tv.setBackground(drawable);
        }
        return drawable;
    }

    public final Drawable setEditBackGroundDrawable(int resId, EditText et) {
        Drawable drawable = getResources().getDrawable(resId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            et.setBackgroundDrawable(drawable);
        } else {
            et.setBackground(drawable);
        }
        return drawable;
    }


}

