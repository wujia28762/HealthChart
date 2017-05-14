# 控件信息
 HealthChart（仿苹果健康图表）
# 说明
>支持`柱状图`和`折线图`和`动画加载`效果;<br>
>控件注释写的很详细，发现的坑也已经注释了，需要的同学可以自行修改。欢迎大家提出改进意见。另外推荐一下最下方的参考博客，写的非常全面和详细。

# 比例图
>![](https://raw.githubusercontent.com/wujia28762/HealthChart/master/line.png)

# 效果图
## 折线图
>![](https://raw.githubusercontent.com/wujia28762/HealthChart/master/Line5.png)
## 柱状图
>![](https://raw.githubusercontent.com/wujia28762/HealthChart/master/Line6.png)

# 公开方法
>setAnimation(boolean);//设置是否动画显示<br>
>setChartType(HealthChart.ChartType);//设置图表类型，柱状图OR折线图<br>
>setData(HealthChart.HealthChartData);//设置数据

# 使用代码

## xml代码
```xml
 <com.example.star.customTest.HealthChart
        android:id="@+id/main_cv"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
  />
```


## View
```java 
HealthChart hc = findView(R.id.main_cv);
HealthChart.HealthChartData builder= new HealthChart.HealthChartData();
hc.setAnimation(true);
hc.setChartType(HealthChart.ChartType.LINECHART);
hc.setData(builder.sampleData());
```

# 参考博客
http://blog.csdn.net/column/details/androidcustomview.html
