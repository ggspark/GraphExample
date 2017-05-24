package co.myproject.graphexample;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements
        OnChartValueSelectedListener {
    private LineChart mChart;
    private Thread thread;
    private float lastEntry = 9450;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final long currentTime = new Date().getTime() - 60000;

        final String[] amountArr = new String[]{"1000", "2000", "3000", "4000", "5000", "6000", "7000", "8000", "9000", "10000"};
        final TextView amount = (TextView) findViewById(R.id.amount);
        NumberPicker numberPicker = (NumberPicker) findViewById(R.id.amount_picker);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(amountArr.length);
        numberPicker.setDisplayedValues(amountArr);
        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                amount.setText("Amount: ₹ " + amountArr[newVal - 1]);
            }
        });
        mChart = (LineChart) findViewById(R.id.chart1);
        mChart.setOnChartValueSelectedListener(this);

        // enable description text
        mChart.getDescription().setEnabled(false);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleXEnabled(true);
        mChart.setScaleYEnabled(false);
        mChart.setAutoScaleMinMaxEnabled(true);
        mChart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(false);

        // set an alternative background color
        mChart.setBackgroundColor(getResources().getColor(R.color.background));

        LineData data = new LineData();
        data.setValueTextColor(Color.LTGRAY);
        // add empty data
        mChart.setData(data);

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();
        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(getResources().getColor(R.color.colorLine));

        XAxis xAxis = mChart.getXAxis();
        xAxis.setTextColor(getResources().getColor(R.color.secondary_text));
        xAxis.setDrawGridLines(false);
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setEnabled(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IAxisValueFormatter() {

            private SimpleDateFormat mFormat = new SimpleDateFormat("HH:mm:ss");

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                long millis = TimeUnit.SECONDS.toMillis((long) value);
                return mFormat.format(new Date(currentTime + millis));
            }
        });

        YAxis axisRight = mChart.getAxisRight();
        axisRight.setTextColor(getResources().getColor(R.color.secondary_text));
        axisRight.setSpaceTop(30f);
        axisRight.setSpaceBottom(30f);
//        axisRight.setAxisMaximum(9500f);
//        axisRight.setAxisMinimum(9400f);
        axisRight.setDrawGridLines(false);
        YAxis axisLeft = mChart.getAxisLeft();
        axisLeft.setEnabled(false);

        for (int i = 0; i < 60; i++) {
            addEntry();
        }
        feedMultiple();

    }


    private void addEntry() {

        LineData data = mChart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            lastEntry = (float) (lastEntry + ((Math.random() * 10) - 5));
            data.addEntry(new Entry(set.getEntryCount(), lastEntry), 0);
            data.notifyDataChanged();

            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();

            // limit the number of visible entries
            mChart.setVisibleXRangeMaximum(60);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            mChart.moveViewToX(data.getEntryCount());

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }

    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "NIFTY 50");
        set.setAxisDependency(YAxis.AxisDependency.RIGHT);
        set.setColor(getResources().getColor(R.color.colorLine));
        set.setLineWidth(.5f);
        set.setDrawCircles(false);
        set.setDrawCircleHole(false);
        set.setFillAlpha(100);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.fade_drawable);
        set.setFillDrawable(drawable);
        set.setDrawFilled(true);
        return set;
    }


    private void feedMultiple() {

        if (thread != null)
            thread.interrupt();

        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                addEntry();
            }
        };

        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                for (int i = 0; i < 1000; i++) {

                    // Don't generate garbage runnables inside the loop.
                    runOnUiThread(runnable);

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("Entry selected", e.toString());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (thread != null) {
            thread.interrupt();
        }
    }

}
