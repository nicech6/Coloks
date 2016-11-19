package com.cuihai.coloks;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

/**
 * author:  崔海
 * time:    2016/11/19 00:07
 * name:
 * overview:
 * usage:
 */
public class MyView extends View {
    private static final int CIRCLE_COLOR = Color.RED;//颜色
    private static final float CIRCLE_RADIUS = 200;//半径
    private static final int HANDLER_MSG = 0;
    private static final String BIAOPAN_LOGO = "icuihai";//表盘logo

    private int color;//表盘圆圈的颜色
    private float radius;//半径
    private Paint mBiaoPan;//面板画笔
    private TextPaint mLogoPaint;//logo文字画笔
    private Paint mSecondPaint;//秒针画笔
    private Paint mMinutesPaint;//分针画笔
    private Paint mHourPaint;//时针画笔
    private Paint mPointPaint;//中心画笔
    private Paint mDegreePaint;//刻度画笔
    private Paint mTimePaint;//数字时间画笔
    private Paint mHourTextPaint;
    private String replaceMsecond;//转化成String类型的时分秒，在写数字式时间用得到
    private String replaceMminutes;
    private String replaceMhour;
    private float mHour = 0;
    private float mMinutes = 0;
    private float mSecond = 0;

    private int heightResult;//控件宽高
    private int widthResult;

    public MyView(Context context) {
        this(context, null);
    }

    public MyView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MyView, defStyleAttr, 0);
        //获取到xml属性
        color = typedArray.getColor(R.styleable.MyView_MyView_color, CIRCLE_COLOR);//表圈颜色
        radius = typedArray.getFloat(R.styleable.MyView_MyView_radius, CIRCLE_RADIUS);//半径
        typedArray.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        BiaoPan(canvas);//画表盘
        Logo(canvas);//画logo
        Time(canvas);//画数字时间
        Degree(canvas);//画刻度
        HourText(canvas);

        mHandler.sendEmptyMessage(HANDLER_MSG);
        /**
         * 在画指针的时候要使用save()方法河restore()方法保存当前的状态，因为如果不这样做的话，
         * 比如当秒针运动的时候分针也会运动，因为他们在同一块画布上，所以这里要使用这两种方法保证互不干扰，
         * */
        canvas.save();
        hour(canvas);//画时针
        canvas.restore();

        canvas.save();
        minutes(canvas);//画分针
        canvas.restore();

        canvas.save();
        second(canvas);//画秒针
        canvas.restore();

        Point(canvas);//画中间小点

    }

    /**
     * 画小时的数字
     */
    private void HourText(Canvas canvas) {
        mHourTextPaint = new Paint();
        mHourTextPaint.setStrokeWidth(2);
        mHourTextPaint.setAntiAlias(true);
        mHourTextPaint.setTextSize(16);
        mHourTextPaint.setColor(Color.DKGRAY);
        mHourTextPaint.setTextAlign(Paint.Align.CENTER);//中心
        float textSize = (mHourTextPaint.getFontMetrics().bottom - mHourTextPaint.getFontMetrics().top);//文字高度
        float distance = radius - 30;
        float a, b;//坐标
        for (int i = 0; i < 12; i++) {
            a = (float) (distance * Math.sin(i * 30 * Math.PI / 180));
            b = (float) (distance * Math.cos(i * 30 * Math.PI / 180));
            if (i == 0) {
                canvas.drawText("12", a, -b + textSize / 3, mHourTextPaint);
            } else {
                canvas.drawText(String.valueOf(i), a, -b + textSize / 3, mHourTextPaint);
            }
        }

    }

    /**
     * 画数字时间
     */
    private void Time(Canvas canvas) {
        //画数字式时间
        mTimePaint = new Paint();
        mTimePaint.setAntiAlias(true);
        mTimePaint.setColor(Color.RED);
        mTimePaint.setStyle(Paint.Style.FILL);
        mTimePaint.setTextSize(25);
        /**根据实际情况我们需要判断下时分秒的数字是不是小于10，如果是前面补一位0*/
        if (mSecond < 10) {
            replaceMsecond = "0" + String.valueOf(mSecond).replace(".0", "");
        } else {
            replaceMsecond = String.valueOf(mSecond).replace(".0", "");
        }
        if (mMinutes < 10) {
            replaceMminutes = "0" + String.valueOf(mMinutes).replace(".0", "");
        } else {
            replaceMminutes = String.valueOf(mMinutes).replace(".0", "");
        }
        if (mHour < 10) {
            replaceMhour = "0" + String.valueOf(mHour).replace(".0", "");
        } else {
            replaceMhour = String.valueOf(mHour).replace(".0", "");
        }
        float v = mTimePaint.measureText(replaceMhour + replaceMminutes + replaceMsecond + ":" + ":");//宽度
        canvas.drawText(replaceMhour + ":" + replaceMminutes + ":" + replaceMsecond, -v / 2, radius / 3 * 2, mTimePaint);
    }

    /**
     * 画表盘
     */
    private void BiaoPan(Canvas canvas) {
        mBiaoPan = new Paint();
        mBiaoPan.setAntiAlias(true);//设置平滑
        canvas.translate(widthResult/2, heightResult/2);//绘制点移动到中心
        mBiaoPan.setStyle(Paint.Style.STROKE);
        mBiaoPan.setColor(color);
        mBiaoPan.setStrokeWidth(2);
        canvas.drawCircle(0, 0, radius, mBiaoPan);
    }

    /**
     * 画logo
     */
    private void Logo(Canvas canvas) {
        //画 logo
        mLogoPaint = new TextPaint();
        mLogoPaint.setColor(Color.BLACK);
        mLogoPaint.setAntiAlias(true);
        mLogoPaint.setStyle(Paint.Style.STROKE);
        mLogoPaint.setStrokeWidth(2);
        mLogoPaint.setTextSize(20);
        float width = mLogoPaint.measureText(BIAOPAN_LOGO);//字符串宽度
        canvas.drawText(BIAOPAN_LOGO, -width / 2, -radius / 5 * 2, mLogoPaint);
    }

    /**
     * 画刻度
     */
    private void Degree(Canvas canvas) {
        //画刻度
        mDegreePaint = new Paint();
        mDegreePaint.setAntiAlias(true);
        mDegreePaint.setStrokeWidth(2);
        int counts = 60;
        for (int i = 0; i < counts; i++) {
            if (i % 5 == 0) {
                String hour = String.valueOf(i / 5);//小时刻度
                canvas.drawLine(0, radius, 0, radius - 20, mDegreePaint);
             /* if (i%5==0&&i!=0){
                  canvas.drawText(hour,-5,-120, mDegreePaint);
              }else if (i==0){
                  canvas.drawText("12",-10,-120, mDegreePaint);
              }*/
            } else {
                canvas.drawLine(0, radius - 10, 0, radius, mDegreePaint);
            }
            canvas.rotate(360 / counts, 0f, 0f);
        }
    }

    /**
     * 画中间小点
     */
    private void Point(Canvas canvas) {
        //画中心小圆
        mPointPaint = new Paint();
        //paint.setStrokeWidth(2);
        mPointPaint.setStyle(Paint.Style.FILL);
        mPointPaint.setColor(Color.GREEN);
        canvas.drawCircle(0, 0, 5, mPointPaint);
    }

    /***
     * 画时针
     */
    private void hour(Canvas canvas) {
        mHourPaint = new Paint();
        mHourPaint.setStrokeWidth(4);
        mHourPaint.setAntiAlias(true);
        mHourPaint.setStyle(Paint.Style.FILL);
        mHourPaint.setColor(Color.RED);
        //mHandler.sendEmptyMessage(2);
        //mHandler.sendEmptyMessageAtTime(2,1000*60*60);
        canvas.rotate(mHour * 30 + mMinutes / 60 * 30, 0, 0);//其中mMinutes/60*30为时钟偏移的角度
        canvas.drawLine(0, 10, 0, -(radius - 90), mHourPaint);


    }

    /***
     * 画分针
     */
    private void minutes(Canvas canvas) {
        mMinutesPaint = new Paint();
        mMinutesPaint.setStrokeWidth(3);
        mMinutesPaint.setAntiAlias(true);
        mMinutesPaint.setColor(Color.BLUE);
        mMinutesPaint.setStyle(Paint.Style.FILL);
        //mHandler.sendEmptyMessageAtTime(1,60000);
        //mHandler.sendEmptyMessage(1);
        canvas.rotate(mMinutes * 6, 0, 0);
        canvas.drawLine(0, 20, 0, -(radius - 70), mMinutesPaint);


    }

    /***
     * 画秒针
     */
    private void second(Canvas canvas) {
        //画秒针
        mSecondPaint = new Paint();
        mSecondPaint.setStrokeWidth(2);
        mSecondPaint.setAntiAlias(true);//设置抗锯齿
        mSecondPaint.setColor(Color.GREEN);
        mSecondPaint.setStyle(Paint.Style.FILL);
        canvas.rotate(mSecond * 6, 0, 0);
        canvas.drawLine(0, 30, 0, -(radius - 40), mSecondPaint);

    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HANDLER_MSG:
                    invalidate();
                    long time = System.currentTimeMillis();
                    mSecond = time / 1000 % 60;
                    mMinutes = (time / 1000 / 60 % 60);
                    mHour = ((time / 1000 / 60 / 60 + 8) % 12);
                    break;
            }
        }
    };

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    }

    //设置控件的宽
    private int measureWidth(int widthMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY) {
            widthResult = widthSize;//当我们在xml文件中对宽做了具体的数值时，即EXACTLY模式下，宽度直接取值我们所规定的大小
        } else {
            widthResult = 400;//默认的宽度
            if (widthMode == MeasureSpec.AT_MOST) {
                widthResult = Math.min(widthResult, widthSize);
                /** 如果指定AT_MOST模式下，，则需要取我们设定的值和测得的值的的最小值，比如这里我们可以设置widthResult的值为2000，
                 如果不写这句的话，我们在屏幕中肯定的就看不到我们写的控件了，*/
            }
        }
        return widthResult;
    }

    private int measureHeight(int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (heightMode == MeasureSpec.EXACTLY) {
            heightResult = heightSize;
        } else {
            heightResult = 600;
            if (heightMode == MeasureSpec.AT_MOST) {
                heightResult = Math.min(heightResult, heightSize);
            }
        }
        return heightResult;
    }
}