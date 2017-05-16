package com.example.star.customTest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.example.star.utils.ScreenUtil;

import java.util.Arrays;

import static android.content.ContentValues.TAG;
import static android.graphics.Color.WHITE;

/**
 * Created by Star on 2017/5/5.
 * 仿苹果健康图表。支持两种图表，柱状图，折线图
 */

public class HealthChart extends View {


    //设置必要的Data
    public synchronized void setData(HealthChartData mData) {
        //数据空
        if(null == mData||null == mData.days||null == mData.steps)
            throw new IllegalArgumentException("data not found!");
        //数据和X下标长度不同
        if(mData.days.length!=mData.steps.length&&mData.steps.length<=0)
            throw new IllegalArgumentException("data not match!");
        this.mData = mData;
    }


    //获取图表类型
    private int getChartType() {
        return mChartType;
    }

    //设置列表类型，如果数据非法，默认是柱形图。
    public void setChartType(int mChartType) {
        if(mChartType == ChartType.HISTOGRAM||mChartType == ChartType.LINECHART)
        this.mChartType = mChartType;
    }

    //默认图表类型：柱形图
    private int mChartType = ChartType.HISTOGRAM;
    //数据载体
    private HealthChartData mData;

    /*
    画笔初始化
     */
    //背景画笔
    private Paint mBackgroundPaint;
    private Paint mBigTextPaint;
    private Paint mSmallTextPaint;
    private Paint mLinePaint;
    private Paint mSeBigTextPaint;
    private Paint mDataLinePaint;
    private Paint mChartLineDataPaint;
    private Paint mChartLineCirDataPaint;


    //是否开启柱状图加载动画效果
    public void setAnimation(boolean animation) {
        this.animation = animation;
    }

    //获取是否使用动画状态
    private boolean isAnimation() {
        return animation;
    }

    //动画控制变量
    private boolean animation ;
    //停止动画变量
    private boolean stopAnimation ;


    /*
    元素比例初始化区域
     */
    //图表的高度大小矩阵
    private RectF rf;
    //图表相对屏幕宽度比例
    private static final float mChartWidthP = 7 / 8F;
    //图表相对屏幕高度比例
    private static final float mChartHeightP = 3 / 8F;
    //"步数"词的相对位置
    private static final float stepTextStartX = 1 / 20F, stepTextStartY = 1 / 20F;
    //步数数量开始位置x
    private static final float stepCountTextStartX = 3 / 4F, stepCountTextStartY = 1 / 20F;
    //平均值文字的开始位置x
    private static final float avgTextStartX = 1 / 20F, avgTextStartY = 4 / 25F;
    //时间文字的开始位置x
    private static final float timeTextStartX = 7 / 10F, timeTextStartY = 4 / 25F;
    //顶部分割线位置
    private static final float topLineStartX = 1 / 20F, topLineStartY = 1 / 4F, topLineEndY = 1 / 4F, topLineEndX = 19 / 20F;
    //底部分割线位置
    private static final float bottomLineStartX = 1 / 20F, bottomLineStartY = 4 / 5F, bottomLineEndY = 4 / 5F, bottomLineEndX = 19 / 20F;
    //图表最大值文字位置
    private static final float maxStepStartX = 17 / 20F, maxStepStartY = 1 / 4F;
    //字体大小
    private static final float bigTextSize = 1 / 10F, smallTextSize = 1 / 20F;
    //当前步数字体大小
    private static final float CurStepTextSize = 2 / 25F;
    //底部X轴和数据的间隔
    private static final float bottomDataLineY = 1 / 50F;

    //X坐标系可绘制区域，
    private static float paintAreaStartX = 3 / 50F, paintAreaStartY = 22 / 25F, paintAreaEndX = 4 / 5F, paintAreaEndY = 22 / 25F;
    //X每段间隔偏移量
    private static float offSetX;
    //图形所有坐标
    private static PointF[] chartIndex;

    //画折线图的路径对象
    private Path mPath;// 路径对象

    private int lineRadio = 5;



    private Bitmap  mRefBitmap;// 位图



    /*
    颜色初始化
     */
    //背景上方的浅红色
    private static final int topBackGroundColor = 0xffff7f53;
    //背景下方的深红色
    private static final int bottomBackGroundColor = 0xffff3b38;
    //大文字颜色
    private static final int bigTextColor = WHITE;
    private static final int smallTextColor = 0xffffc0a9;
    private static final int lineColor = 0xffffb88e;
    private static final int dataLineColor=0xfffc7259;



    public HealthChart(Context context) {
        super(context);
    }

    public HealthChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        //获取屏幕大小
        int screenX = ScreenUtil.getScreenWidth(context);
        int screenY = ScreenUtil.getScreenHeight(context);
        //设置矩阵，根据比例，动态计算出图表的宽度和高度。
        rf = new RectF();
        rf.top = 0;
        rf.left = 0;
        rf.right = screenX * mChartWidthP - getPaddingLeft() - getPaddingRight();
        rf.bottom = screenY * mChartHeightP-getPaddingTop()-getPaddingBottom();
        //图表X坐标轴和数据的间隔。初始化
        offSetX = 0;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int resultWidth = 0;
        // 获取宽度测量规格中的mode
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        // 获取宽度测量规格中的size
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);

        //父控件给出建议宽度，直接使用
        if (modeWidth == MeasureSpec.EXACTLY) {
            resultWidth = sizeWidth;
        } else {
            //否则直接使用计算的宽度
            resultWidth = (int) rf.right;
            if (modeWidth == MeasureSpec.AT_MOST) {
                //给出最大宽度，则取父控件最大宽度与自己计算的宽度的最小值
                resultWidth = Math.min(resultWidth, sizeWidth);
            }
        }
        //高度同理
        int resultHeight = 0;
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);

        if (modeHeight == MeasureSpec.EXACTLY) {
            resultHeight = sizeHeight;
        } else {
            resultHeight = (int) rf.bottom;
            if (modeHeight == MeasureSpec.AT_MOST) {
                resultHeight = Math.min(resultHeight, sizeHeight);
            }
        }

        Log.e(TAG, "onMeasure: " + resultWidth + "~~" + resultHeight);
        // 设置测量尺寸
        setMeasuredDimension(resultWidth, resultHeight);


    }




    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        //画圆角背景，大小是构造函数中计算出的。


        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        mBackgroundPaint.setShader(new LinearGradient(rf.left, rf.top, rf.top, rf.bottom, topBackGroundColor, bottomBackGroundColor, Shader.TileMode.REPEAT));
        canvas.drawRoundRect(rf, 10, 10, mBackgroundPaint);


        //画图表上方的文字
        drawText(canvas);
        //画图表Y轴的上下分界线
        drawLine(canvas);
        //画X轴的下标
        drawXData(canvas);

        calChartData();

        if(!isAnimation()) {
            //第二次绘制数据了，下面不需要再重绘
            stopAnimation = true;
            //mDataLinePaint.getTextBounds(str, 0, str.length(), rect);
            if(getChartType()==ChartType.HISTOGRAM) {
                drawHisData(canvas);
            }
            else
            {
                drawLineChart(canvas);
            }
        }
        else {
            //
            animation = false;
            //如果stopAnimation == true 就不需要重绘了。
            if(!stopAnimation)
                invalidate();

        }
            //判断类型，以不同形式显示数据





    }

    private void calChartData() {



        //这里先计算一下所有起点和终点的坐标（柱状图画直线，需要起始点和终点，折线图只需要终点）所以申请2倍数据的空间
        //复数index存储起点，单数index存储终点
        chartIndex = new PointF[mData.days.length*2];

        //这里以柱状图的画笔为标尺
        mDataLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        //描边
        mDataLinePaint.setStyle(Paint.Style.STROKE);
        //画笔宽度
        mDataLinePaint.setStrokeWidth(20);
        mDataLinePaint.setColor(dataLineColor);

        float dataLineX,baseDataLineY,baseDataLineEndY;
        Rect rect = new Rect();
        //这里不能修改offSetX中的引用值。可以赋值给临时引用变量。修改后，会造成所有引用offSetX的x坐标偏移。
        PointF p,p1;
        for (int x = 0, index = 0,index1 = 0; index < mData.days.length*2&&index1<mData.days.length;index+=2,x += offSetX,index1++) {
            mDataLinePaint.getTextBounds(mData.days[index1], 0, mData.days[index1].length(), rect);
            dataLineX = rf.right * paintAreaStartX + x + Math.abs(rect.width());
            baseDataLineY = (bottomLineStartY - bottomDataLineY) * rf.bottom;
            baseDataLineEndY = baseDataLineY - ((bottomLineStartY - bottomDataLineY - maxStepStartY) * rf.bottom - Math.abs(mSmallTextPaint.getFontMetrics().top)) * (Float.parseFloat(mData.steps[index1]) / Integer.parseInt(mData.maxStep));
            p = new PointF(dataLineX,baseDataLineY);
            p1 = new PointF(dataLineX,baseDataLineEndY);
            chartIndex[index] = p;
            chartIndex[index+1] = p1;
        }
    }

    private void drawLineChart(Canvas canvas) {
        //折线图的线段画笔
        mChartLineDataPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        //描边
        mChartLineDataPaint.setStyle(Paint.Style.STROKE);
        //画笔宽度
        mChartLineDataPaint.setStrokeWidth(lineRadio);
        mChartLineDataPaint.setColor(dataLineColor);

        //折线图圆点画笔
        mChartLineCirDataPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mChartLineCirDataPaint.setStyle(Paint.Style.FILL);
        mChartLineCirDataPaint.setStrokeWidth(lineRadio);
        mChartLineCirDataPaint.setColor(dataLineColor);

        mPath = new Path();
        for (int i = 1;i<mData.days.length*2;i+=2) {
            try {
                Thread.sleep(50);
                if (i == 1) {
                    mPath.moveTo(chartIndex[i].x, chartIndex[i].y);
                } else {
                    mPath.lineTo(chartIndex[i].x, chartIndex[i].y);
                }
                canvas.drawCircle(chartIndex[i].x, chartIndex[i].y, lineRadio*1.5F, mChartLineCirDataPaint);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            canvas.drawPath(mPath, mChartLineDataPaint);
        }
        Paint Aha = new Paint();
        Aha.setStyle(Paint.Style.FILL);
        Aha.setColor(Color.WHITE);
        Aha.setAlpha(6);
        mPath.lineTo(chartIndex[mData.days.length*2-2].x,chartIndex[mData.days.length*2-2].y);
        mPath.lineTo(chartIndex[0].x,chartIndex[0].y);
        mPath.close();
        canvas.drawPath(mPath,Aha);




    }
    //画X下标
    private void drawXData(Canvas canvas) {

        offSetX = getOffSetX();
        for (int x = 0, index = 0; index < mData.days.length; index++, x += offSetX) {
            canvas.drawText(mData.days[index], rf.right * paintAreaStartX + x, rf.bottom * paintAreaStartY + Math.abs(mSmallTextPaint.getFontMetrics().top), mSmallTextPaint);
        }
    }

    //计算每段数据的间隔
    private float getOffSetX()
    {
        //length个数据，分length-1段  ~T_T~
        return (paintAreaEndX - paintAreaStartX) / (mData.days.length-1) * rf.right;
    }

    private void drawHisData(Canvas canvas) {


        //如果有动画，加载一次空数据。通过两次绘制，实现动画

            for (int index = 0; index < mData.days.length*2;index+=2) {
                try {
                    Thread.sleep(50);
                    canvas.drawLine(chartIndex[index].x, chartIndex[index].y, chartIndex[index+1].x, chartIndex[index+1].y, mDataLinePaint);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }




    }


    //画初始分割线。上下两条
    private void drawLine(Canvas canvas) {
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(3);
        mLinePaint.setColor(lineColor);
        canvas.drawLine(rf.right * topLineStartX, rf.bottom * topLineStartY, rf.right * topLineEndX, rf.bottom * topLineEndY, mLinePaint);
        canvas.drawLine(rf.right * bottomLineStartX, rf.bottom * bottomLineStartY, rf.right * bottomLineEndX, rf.bottom * bottomLineEndY, mLinePaint);
    }

    //画字，步数，最大步数，平均值，时间等信息，坐标计算，通过下偏移*字体偏移比例，
    // 注意：字体的初始坐标是BaseLine，需要+ABS(top)个距离。转换成左上角坐标
    private void drawText(Canvas canvas) {


        mBigTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mBigTextPaint.setStyle(Paint.Style.STROKE);
        mBigTextPaint.setStrokeCap(Paint.Cap.ROUND);
        mBigTextPaint.setColor(bigTextColor);
        mBigTextPaint.setTextSize(rf.bottom * bigTextSize);

        mSmallTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mSmallTextPaint.setStyle(Paint.Style.STROKE);
        mSmallTextPaint.setStrokeCap(Paint.Cap.ROUND);
        mSmallTextPaint.setColor(smallTextColor);
        mSmallTextPaint.setTextSize(rf.bottom * smallTextSize);

        mSeBigTextPaint = new Paint(mBigTextPaint);
        mSeBigTextPaint.setTextSize(rf.bottom * CurStepTextSize);


        canvas.drawText(mData.chartTitle, rf.right * stepTextStartX, rf.bottom * stepTextStartY + Math.abs(mBigTextPaint.getFontMetrics().top), mBigTextPaint);
        canvas.drawText(mData.curStep, rf.right * stepCountTextStartX, rf.bottom * stepCountTextStartY + Math.abs(mSeBigTextPaint.getFontMetrics().top), mSeBigTextPaint);
        canvas.drawText(mData.avgStep, rf.right * avgTextStartX, rf.bottom * avgTextStartY + Math.abs(mSmallTextPaint.getFontMetrics().top), mSmallTextPaint);
        canvas.drawText(mData.date, rf.right * timeTextStartX, rf.bottom * timeTextStartY + Math.abs(mSmallTextPaint.getFontMetrics().top), mSmallTextPaint);
        canvas.drawText(mData.maxStep, rf.right * maxStepStartX, rf.bottom * maxStepStartY + Math.abs(mSmallTextPaint.getFontMetrics().top), mSmallTextPaint);




    }
    //封装内部数据类。提供数据模板

    public static class HealthChartData {
        @Override
        public String toString() {
            return "HealthChartData{" +
                    "chartTitle='" + chartTitle + '\'' +
                    ", maxStep='" + maxStep + '\'' +
                    ", date='" + date + '\'' +
                    ", days=" + Arrays.toString(days) +
                    ", avgStep='" + avgStep + '\'' +
                    '}';
        }
        //获取样式数据

        public HealthChartData sampleData() {
            return chartTitle("步数")
                    .date("今天 下午2：22")
                    .days(new String[]{"5.1", "2", "3", "4", "5", "6", "7"})
                    .avgStep("日平均值：")
                    .curStep("1999步")
                    .steps(new String[]{"9999", "2000", "5000", "7000", "3000", "9500", "9999"})
                    .inIt();
        }

        //步数字样
        private String chartTitle;
        //今天多少步
        private String curStep;
        //最大步数，Y轴上坐标
        private String maxStep;
        //当前时间
        private String date;
        //天数
        private String[] days;
        //根据数据算出平均步数
        private String avgStep;
        //步数数据数组
        private String[] steps;

        public HealthChartData steps(String[] steps) {
            this.steps = steps;
            return this;
        }

        public HealthChartData chartTitle(String chartTitle) {
            this.chartTitle = chartTitle;
            return this;
        }

        public HealthChartData curStep(String curStep) {
            this.curStep = curStep;
            return this;
        }

        public HealthChartData maxStep(String maxStep) {
            this.maxStep = maxStep;
            return this;
        }

        public HealthChartData date(String date) {
            this.date = date;
            return this;
        }

        public HealthChartData days(String[] days) {
            this.days = days;
            return this;
        }

        public HealthChartData avgStep(String avgStep) {
            this.avgStep = avgStep;
            return this;
        }

        public HealthChartData getMaxCeil(int max) {
            String sMax = max + "";

            int sLength = sMax.length();
            maxStep = (int) (Math.ceil(max / (Math.pow(10, (sLength - 1)))) * Math.pow(10, (sLength - 1))) + "";
            return this;
        }

        //根据用户插入的数据 算出内部一些数据
        public HealthChartData inIt() {
            int length = this.steps.length;
            int[] dataInt = new int[length];
            int max = 0;
            long num = 0;
            for (int i = 0; i < length; i++) {

                dataInt[i] = Integer.parseInt(this.steps[i]);
                num += dataInt[i];
                if (dataInt[i] > max)
                    max = dataInt[i];
            }
            avgStep += (num / length) + "";
            getMaxCeil(max);
            return this;
        }
    }

    //定义图表类型接口。暂时支持两种，柱状图和折线图。
    public interface ChartType{
        int HISTOGRAM = 0;
        int LINECHART = 1;

    }
}
