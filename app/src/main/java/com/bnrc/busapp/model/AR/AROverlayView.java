package com.bnrc.busapp.model.AR;

import android.content.Context;
import android.graphics.Canvas;
import android.opengl.Matrix;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.utils.DistanceUtil;
import com.bnrc.busapp.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ntdat on 1/13/17.
 */

public class AROverlayView extends View {

    Context context;
    private float[] rotatedProjectionMatrix = new float[16];
    private BDLocation currentLocation;
    private List<ARPoint> arPoints = null;
    private FrameLayout mARContainer;
    private TextView poi_name,poi_distance;
    private int maxWidth;
    private int movingSpeed = 20;
    private ARPoint movingCar;
    private MyAsyncTask mTask;


    public AROverlayView(Context context, FrameLayout mARContainer) {
        super(context);

        this.context = context;
        this.mARContainer = mARContainer;

        //Demo points

        arPoints = new ArrayList<ARPoint>();
//        arPoints = new ArrayList<ARPoint>() {{
//            add(new ARPoint("北邮科技酒店","467m", 39.9704,116.3618, 0));
//            add(new ARPoint("枫蓝国际","975m", 39.9594, 116.3639, 0));
//        }};
    }

    public void updateRotatedProjectionMatrix(float[] rotatedProjectionMatrix) {
        this.rotatedProjectionMatrix = rotatedProjectionMatrix;
        this.invalidate();
    }

    public void updateCurrentLocation(BDLocation currentLocation){
        this.currentLocation = currentLocation;
        this.invalidate();
    }

    public void updatePoiResult(PoiResult poiResult){
        for(PoiInfo poi:poiResult.getAllPoi()){
            if(!arPoints.contains(poi))
                arPoints.add(new ARPoint(poi.name,(int)DistanceUtil.getDistance(
                        new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()),
                        new LatLng(poi.location.latitude,poi.location.longitude)
                                )+"m",poi.location.latitude,poi.location.longitude,0));
        }
    }

    public void updateMovingPoint(int inclat){
        movingCar.setLongitude(116.3618+0.0003*inclat);
        Log.i("async", "updated moving point: "+inclat);
    }

    public void startMove(){
        Log.i("async", "strat move");
        mTask = new MyAsyncTask();
        mTask.execute(0,200);
    }

    public void cancleTask(){

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (currentLocation == null) {
            return;
        }

        if (arPoints.size()>0){
            for (int i = 0; i < arPoints.size(); i ++) {
                float[] currentLocationInECEF = LocationHelper.WSG84toECEF(currentLocation);
                float[] pointInECEF = LocationHelper.WSG84toECEF(arPoints.get(i).getLocation());
                float[] pointInENU = LocationHelper.ECEFtoENU(currentLocation, currentLocationInECEF, pointInECEF);

                float[] cameraCoordinateVector = new float[4];
                Matrix.multiplyMV(cameraCoordinateVector, 0, rotatedProjectionMatrix, 0, pointInENU, 0);

                // cameraCoordinateVector[2] is z, that always less than 0 to display on right position
                // if z > 0, the point will display on the opposite
                if (cameraCoordinateVector[2] < 0) {
                    float x  = (0.5f + cameraCoordinateVector[0]/cameraCoordinateVector[3]) * canvas.getWidth();
                    float y = (0.5f - cameraCoordinateVector[1]/cameraCoordinateVector[3]) * canvas.getHeight();


                    if(arPoints.get(i).getPoiTag()==null){
                        View poiTag = LayoutInflater.from(context).inflate(R.layout.poitag_layout, null, false);
                        poi_name = poiTag.findViewById(R.id.poi_name);
                        poi_distance = poiTag.findViewById(R.id.poi_distance);
                        poi_name.setText(arPoints.get(i).getName());
                        poi_distance.setText(arPoints.get(i).getDistance());

                        poi_name.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                        poi_distance.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

                        Log.i("poiTag", "poi_name: "+poi_name.getMeasuredWidth());
                        Log.i("poiTag", "poi_distance: "+poi_distance.getMeasuredWidth());

                        maxWidth = poi_name.getMeasuredWidth()>=poi_distance.getMeasuredWidth()?poi_name.getMeasuredWidth():poi_distance.getMeasuredWidth();
                        Log.i("poiTag", "maxWidth: "+maxWidth);

                        poi_name.setWidth(maxWidth);
                        poi_distance.setWidth(maxWidth);

                        arPoints.get(i).setPoiTag(poiTag);
                        mARContainer.addView(poiTag);
                    }
                    arPoints.get(i).getPoiTag().setX(x);
                    arPoints.get(i).getPoiTag().setY(y);
                }
            }
        }

    }

    class MyAsyncTask extends AsyncTask<Integer,Integer,Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mARContainer.removeAllViews();
            arPoints.clear();
            movingCar = new ARPoint("387路公交","467m", 39.9584,116.3618, 0);
            arPoints.add(movingCar);
            Log.i("async", "Begin asyncTask------------------------------------");
        }

        @Override
        protected Integer doInBackground(Integer... integers) {
            int start = integers[0];
            int end = integers[1];

            int result = 0;
            for(int i = start; i <=end; i++){
                SystemClock.sleep(50);
                result = i;
                publishProgress(result);
            }

            return result;
        }

        @Override
        protected void onProgressUpdate(Integer[] values) {
            super.onProgressUpdate(values);
            updateMovingPoint(values[0]);
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
        }

    }



}