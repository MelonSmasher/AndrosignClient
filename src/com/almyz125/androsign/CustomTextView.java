package com.almyz125.androsign;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.View;

/* CustomTextView , (C) 2010 , Radu Motisan radu.motisan@gmail.com
 * 
 * Purpose: create a custom textview control, that supports rotation and other customizations 
 */
class CustomTextView extends View {
	private int m_nColor;
	private Typeface m_tTypeface;
	private int m_nSize;
	private int m_nRotationAngle, m_nRotationW, m_nRotationH;
	private String m_szText;
	private Paint paint = new Paint();

	public CustomTextView(Context context) {
		super(context);
		// set default parameters
		m_nColor = Color.WHITE;
		m_nSize = 14;
		m_nRotationAngle = 0;
		m_nRotationW = 0;
		m_nRotationH = 0;
		m_tTypeface = Typeface.create("arial", Typeface.NORMAL);
	}

	public void SetColor(int newcolor) {
		m_nColor = newcolor;
		this.invalidate();
	}

	public void SetTextSize(int newsize) {
		m_nSize = newsize;
		this.invalidate();
	}

	// style: normal-0,bold-1,italic-2,bold-italic-3,
	public void SetFont(String newfontface, int style) {
		m_tTypeface = Typeface.create(newfontface, style);
		this.invalidate();
	}

	public void SetRotation(int newangle, int neww, int newh) {
		m_nRotationAngle = newangle;
		m_nRotationW = neww;
		m_nRotationH = newh;
		this.invalidate();
	}

	public void SetText(String newtext) {
		m_szText = newtext;
		this.invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		paint.setTypeface(m_tTypeface);
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(m_nColor);
		// paint.setShadowLayer(1, 0, 1, Color.parseColor("#000000"));
		paint.setTextSize(m_nSize);
		canvas.rotate(m_nRotationAngle, m_nRotationW, m_nRotationH);
		canvas.drawText(m_szText, 0, 0, paint);
		super.onDraw(canvas);
	}
}