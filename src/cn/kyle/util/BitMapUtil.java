package cn.kyle.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class BitMapUtil {

	public static Drawable getDrawableByResid(Context context,int resid){
		return new BitmapDrawable(context.getResources(), BitmapFactory.decodeResource(context.getResources(), resid));
	}
	
	public static Drawable getDrawableByResid(Context context,int resid, int dst_w, int dst_h){
		return new BitmapDrawable(context.getResources(),imageScale(BitmapFactory.decodeResource(context.getResources(), resid),dst_w,dst_h));
	}
	
	public static Drawable getResizeDrawable(Context context,Drawable drawable, int dst_w, int dst_h){
		return new BitmapDrawable(context.getResources(), imageScale(drawabletoBitmap(drawable),dst_w,dst_h));
	}
	
	/********************* 图形缩放 **************************/
	public static Bitmap imageScale(Bitmap bitmap, int dst_w, int dst_h) {

		int src_w = bitmap.getWidth();
		int src_h = bitmap.getHeight();
		float scale_w = ((float) dst_w) / src_w;
		float scale_h = ((float) dst_h) / src_h;
		Matrix matrix = new Matrix();
		matrix.postScale(scale_w, scale_h);
		Bitmap dstbmp = Bitmap.createBitmap(bitmap, 0, 0, src_w, src_h, matrix,
				true);

		return dstbmp;
	}

	/********************* Drawable转Bitmap ************************/
	public static Bitmap drawabletoBitmap(Drawable drawable) {

		int width = drawable.getIntrinsicWidth();
		int height = drawable.getIntrinsicWidth();

		Bitmap bitmap = Bitmap.createBitmap(width, height, drawable
				.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, width, height);

		drawable.draw(canvas);

		return bitmap;
	}

	/*************************** 图片圆角处理 ********************************/
	public static Bitmap getRCB(Bitmap bitmap, float roundPX) // RCB means
																// Rounded
																// Corner Bitmap
	{
		Bitmap dstbmp = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(dstbmp);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPX, roundPX, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return dstbmp;
	}
}