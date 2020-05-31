package com.choubapp.running;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.ChartData;

class BarChartItem extends ChartItem  {
    private final Typeface mTf;

    BarChartItem(ChartData<?> cd, Context c) {
            super(cd);
            mTf = Typeface.createFromAsset(c.getAssets(), "OpenSans-Regular.ttf");
        }

        @Override
        public int getItemType() {
            return TYPE_BARCHART;
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(int position, View convertView, Context c) {

            ViewHolder holder;

            if (convertView == null) {

                holder = new ViewHolder();

                convertView = LayoutInflater.from(c).inflate(
                        R.layout.list_item_barchart, null);
                holder.chart = convertView.findViewById(R.id.chart);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            // apply styling
            holder.chart.getDescription().setEnabled(false);
            holder.chart.setDrawGridBackground(false);
            holder.chart.setDrawBarShadow(false);

            YAxis leftAxis = holder.chart.getAxisLeft();
            leftAxis.setTypeface(mTf);
            leftAxis.setLabelCount(5, false);
            leftAxis.setSpaceTop(20f);
            leftAxis.setAxisMinimum(0f);

            YAxis rightAxis = holder.chart.getAxisRight();
            rightAxis.setTypeface(mTf);
            rightAxis.setLabelCount(5, false);
            rightAxis.setSpaceTop(20f);
            rightAxis.setAxisMinimum(0f);

            mChartData.setValueTypeface(mTf);

            // set data
            holder.chart.setData((BarData) mChartData);
            holder.chart.setFitBars(true);
            holder.chart.animateY(700);

            return convertView;
        }

        private static class ViewHolder {
            BarChart chart;
        }
}

