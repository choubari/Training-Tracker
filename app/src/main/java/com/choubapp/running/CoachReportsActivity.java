package com.choubapp.running;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static java.security.AccessController.getContext;

public class CoachReportsActivity extends AppCompatActivity {
    ListView lv;
    RelativeLayout loading;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference Teams = db.collection("Equipe");
    CollectionReference Trainings = db.collection("Entrainement");
    CollectionReference Trackings = db.collection("tracking");
    HashMap<Timestamp,String> EndedTrainings = new HashMap<>();
    Map<Timestamp, String> map ;
    ArrayList<Long> speed = new ArrayList<>();
    ArrayList<Integer> steps = new ArrayList<>();
    long distance=0, time=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coach_reports);
        lv = findViewById(R.id.listView1);
        loading = findViewById(R.id.loading);
        //lv.setVisibility(View.INVISIBLE);
        loading.setVisibility(View.GONE);
        getTeamTrainingsData();
    }

    private void getTeamTrainingsData(){
        String Team ="ddwzDL";
        Trainings.whereEqualTo("TeamID",Team).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        String trainingName = document.getString("TrainingName");
                        if (trainingName!=null){
                            String mdate = document.get("Date").toString();
                            String mTimeDep = document.get("HeureDep").toString();
                            String mTimeArr = document.get("HeureArr").toString();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                            Date parsedDateDep=null;
                            Date parsedDateArr=null;
                            String DateDep = mdate +" "+mTimeDep;
                            String DateArr = mdate +" "+mTimeArr;
                            //System.out.println("Dates  "+DateDep+"  "+DateArr);

                            try {
                                parsedDateDep =(Date) dateFormat.parse(DateDep);
                                Timestamp timestampDep = new Timestamp(parsedDateDep.getTime());
                                parsedDateArr =(Date) dateFormat.parse(DateArr);
                                Timestamp timestampArr = new Timestamp(parsedDateArr.getTime());
                                //System.out.println("parsedDates  "+parsedDateDep+"  "+parsedDateArr);
                                //System.out.println("Timestamps  "+timestampDep+"  "+timestampArr);
                                Date datee= new Date();
                                Timestamp mytime = new Timestamp(datee.getTime());
                                if(mytime.after(timestampArr)){
                                    EndedTrainings.put(timestampArr,document.getId());
                                   // System.out.println("hashmap ended trainings"+EndedTrainings);
                                }

                            } catch(Exception e) {
                                e.printStackTrace();
                                System.out.println("Exception :" + e);
                            }
                        }
                    }getDataforCharts();
                }
            }
        });
    }
    private void getDataforCharts(){
        map = new TreeMap<Timestamp, String>(EndedTrainings);
        System.out.println("treemap ended trainings sorted"+map);
        System.out.println("map size: " + map.size());
        for (int j =0 ; j<map.size(); j++) {
            speed.add((long) 0);
            steps.add(0);
        }
        System.out.println("steps "+ steps + " speed "+ speed);
        final int[] counter = {0};
        for (int i=0 ; i<map.size(); i++){
            Timestamp key = (Timestamp) map.keySet().toArray()[i];
            String docID= map.get(key);
            int finalI = i;
            Trackings.document(docID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d("TAG", "Document exists!");
                            Trackings.document(docID).collection("Participants").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()){
                                        long dist=0, tm=0;
                                        for (DocumentSnapshot DOC : task.getResult()){
                                            counter[0]++;
                                            long sp = (long) ((long) DOC.get("Distance") / (long)DOC.get("TotalTime"));
                                            speed.set(finalI,speed.get(finalI)+sp);
                                            long stp = (long) DOC.get("Steps");
                                            //System.out.println("st sp"+ stp + "  "+sp);
                                            steps.set(finalI,steps.get(finalI)+(int)stp);
                                            dist+= (long) DOC.get("Distance");
                                            tm+=(long)DOC.get("TotalTime");
                                            System.out.println("participant " +finalI+" speed "+speed.get(finalI)+" steps "+steps.get(finalI)+" dist "+dist+ " time "+ tm);
                                        }
                                        distance += dist / counter[0];
                                        time += tm / counter[0];
                                        System.out.println("average distance "+distance + "avg time "+ time + "while counter = "+ counter[0]);
                                    }
                                    System.out.println("speed of training n° "+finalI + "is "+ speed.get(finalI) + "and avg steps  = "+ steps.get(finalI));
                                    speed.set(finalI, speed.get(finalI) / (counter[0]));
                                    steps.set(finalI, steps.get(finalI) / (counter[0]));
                                    System.out.println( "average i =  "+finalI+" "+speed + "  "+ steps);
                                    if (finalI == (map.size()-1)) generateCharts();
                                }
                            });
                        } else {
                            Log.d("TAG", "Document does not exist!");
                        }
                    } else {
                        Log.d("TAG", "Failed with: ", task.getException());
                    }
                }
            });
        }
    }

    private void generateCharts(){
        ArrayList<ChartItem> list = new ArrayList<>();
        // 30 items
      /*  for (int i = 0; i < 30; i++) {

            if(i % 3 == 0) {
                list.add(new LineChartItem(generateDataLine(), getApplicationContext()));
            } else if(i % 3 == 1) {
                list.add(new BarChartItem(generateDataBar(), getApplicationContext()));
            } else if(i % 3 == 2) {
                list.add(new PieChartItem(generateDataPie(), getApplicationContext()));
            }
        }
*/
        list.add(new LineChartItem(generateDataLine(), getApplicationContext()));
        list.add(new BarChartItem(generateDataBar(), getApplicationContext()));
        list.add(new PieChartItem(generateDataPie(), getApplicationContext()));
        ChartDataAdapter cda = new ChartDataAdapter(getApplicationContext(), list);
        lv.setAdapter(cda);
    }

    /** adapter that supports 3 different item types */
    private class ChartDataAdapter extends ArrayAdapter<ChartItem> {

        ChartDataAdapter(Context context, List<ChartItem> objects) {
            super(context, 0, objects);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            //noinspection ConstantConditions
            return getItem(position).getView(position, convertView, getContext());
        }

        @Override
        public int getItemViewType(int position) {
            // return the views type
            ChartItem ci = getItem(position);
            return ci != null ? ci.getItemType() : 0;
        }

        @Override
        public int getViewTypeCount() {
            return 3; // we have 3 different item-types
        }
    }

    /**
     * generates a random ChartData object with just one DataSet
     *
     * @return Bar data
     */
    private BarData generateDataBar() {

        ArrayList<BarEntry> entries = new ArrayList<>();

        for (int i = 0; i < steps.size(); i++) {
            entries.add(new BarEntry(i+1, (int) steps.get(i)));
        }

        BarDataSet d = new BarDataSet(entries, "Nombre des pas dans chaque entraînement");
        d.setColors(ColorTemplate.VORDIPLOM_COLORS);
        d.setHighLightAlpha(255);

        BarData cd = new BarData(d);
        cd.setBarWidth(0.9f);
        return cd;
    }

    /**
     * generates a random ChartData object with just one DataSet
     *
     * @return Line data
     */
    private LineData generateDataLine() {

        ArrayList<Entry> values1 = new ArrayList<>();

        for (int i = 0; i < speed.size(); i++) {
            values1.add(new Entry(i+1, (float) speed.get(i)));
        }

        LineDataSet d1 = new LineDataSet(values1, "Vitesses Moyennes de l'équipe (metre/seconde)");
        d1.setLineWidth(2.5f);
        d1.setCircleRadius(4.5f);
        d1.setHighLightColor(Color.rgb(244, 117, 117));
        d1.setColor(ColorTemplate.VORDIPLOM_COLORS[0]);
        d1.setCircleColor(ColorTemplate.VORDIPLOM_COLORS[0]);
        d1.setDrawValues(false);
/*
        ArrayList<Entry> values2 = new ArrayList<>();

        for (int i = 0; i < 12; i++) {
            values2.add(new Entry(i, values1.get(i).getY() - 30));
        }

        LineDataSet d2 = new LineDataSet(values2, "New DataSet " + cnt + ", (2)");
        d2.setLineWidth(2.5f);
        d2.setCircleRadius(4.5f);
        d2.setHighLightColor(Color.rgb(244, 117, 117));
        d2.setColor(ColorTemplate.VORDIPLOM_COLORS[0]);
        d2.setCircleColor(ColorTemplate.VORDIPLOM_COLORS[0]);
        d2.setDrawValues(false);
*/
        ArrayList<ILineDataSet> sets = new ArrayList<>();
        sets.add(d1);
        //  sets.add(d2);

        return new LineData(sets);
    }

    /**
     * generates a random ChartData object with just one DataSet
     *
     * @return Pie data
     */
    private PieData generateDataPie() {

        ArrayList<PieEntry> entries = new ArrayList<>();

        /*for (int i = 0; i < 2; i++) {
            entries.add(new PieEntry((float) ((Math.random() * 70) + 30), "Quarter " + (i+1)));
        }*/
        entries.add(new PieEntry((float) distance, "Distance(metre)" ));
        entries.add(new PieEntry((float) time/60, "Durée(min)" ));

        PieDataSet d = new PieDataSet(entries, " Distances et Durées Totales Parcourues par l'équipe");

        // space between slices
        d.setSliceSpace(2f);
        d.setColors(ColorTemplate.VORDIPLOM_COLORS);

        return new PieData(d);
    }


    public void BacktoDashboard(View v) {
        Intent intent = new Intent(CoachReportsActivity.this,CoachDashboardActivity.class);
        finish();
        startActivity(intent);
    }

}
