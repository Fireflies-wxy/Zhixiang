package com.bnrc.busapp.model.AR;

import android.content.Context;
import android.graphics.Canvas;
import android.opengl.Matrix;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
    private int maxWidth;
    private int movingSpeed = 20;
    private ARPoint movingCar;
    private float[][] pointxXY;
//    private MyAsyncTask mTask;


    public AROverlayView(Context context, FrameLayout mARContainer) {
        super(context);
        this.context = context;
        this.mARContainer = mARContainer;

        arPoints = new ArrayList<ARPoint>();

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
        Log.i("testpoi", "updatePoiResult: 1");
        arPoints.clear();
        for(PoiInfo poi:poiResult.getAllPoi()){
            if(!arPoints.contains(poi))
                arPoints.add(new ARPoint(poi.name,(int)DistanceUtil.getDistance(
                        new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()),
                        new LatLng(poi.location.latitude,poi.location.longitude)
                                )+"m",poi.location.latitude,poi.location.longitude,0,1));
        }
    }

    public void updatePoiResult(ARPoint arPoint){
        Log.i("testpoi", "updatePoiResult: 2");
        arPoints.clear();
        if(!arPoints.contains(arPoint))
            arPoints.add(arPoint);
    }

    public void updatePoiResult(ArrayList<ARPoint> points){
        arPoints.clear();
        for(int i = 0;i<points.size();i++){
            if(!arPoints.contains(points.get(i))){
                arPoints.add(points.get(i));
            }
        }
    }

//    public void updateMovingPoint(int inclat){
//        movingCar.setLongitude(116.3618+0.0003*inclat);
//        Log.i("async", "updated moving point: "+inclat);
//    }
//
//    public void startMove(){
//        Log.i("async", "strat move");
//        mTask = new MyAsyncTask();
//        mTask.execute(0,200);
//    }

    public void cancleTask(){

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        pointxXY = new float[arPoints.size()][2];

        if (currentLocation == null) {
            return;
        }

        if (arPoints.size()>0){
            for (int i = 0; i < arPoints.size(); i ++) {

                final FrameLayout mPoiTag;
                final TextView poi_name, poi_description;
                final ImageView poi_img;
                final boolean flag = true;

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

                    pointxXY[i][0] = x;
                    pointxXY[i][1] = y;

                    final ARPoint arPoint = arPoints.get(i);
                    if(arPoint.getPoiTag()==null){
                        View poiTag = LayoutInflater.from(context).inflate(R.layout.poitag_layout, null, true);
                        poiTag.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT));
                        poi_name = poiTag.findViewById(R.id.poi_name);
                        poi_description = poiTag.findViewById(R.id.poi_description);
                        poi_name.setText(arPoints.get(i).getName());
                        poi_description.setText(arPoints.get(i).getDescription());
                        poi_img = poiTag.findViewById(R.id.img_poi);
                        mPoiTag = poiTag.findViewById(R.id.poi_tag);
                        switch (arPoint.getType()){
                            case 1:
                                poi_img.setImageDrawable(getResources().getDrawable(R.drawable.icon_ar_poi_selector));
                                break;
                            case 2:
                                poi_img.setImageDrawable(getResources().getDrawable(R.drawable.icon_ar_bus_selector));
                                break;
                            case 3:
                                poi_img.setImageDrawable(getResources().getDrawable(R.drawable.icon_ar_stop_selector));
                                break;
                            default:
                                poi_img.setImageDrawable(getResources().getDrawable(R.drawable.icon_ar_poi_selector));
                                break;
                        }

                        mPoiTag.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Log.i("testClick", "onClick: "+arPoint.getName());
                                poi_name.setEnabled(!poi_name.isEnabled());
                                poi_description.setEnabled(!poi_description.isEnabled());
                                poi_img.setEnabled(!poi_img.isEnabled());
                            }
                        });


                        poi_name.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                        poi_description.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

                        Log.i("poiTag", "poi_name: "+poi_name.getMeasuredWidth());
                        Log.i("poiTag", "poi_description: "+ poi_description.getMeasuredWidth());

                        maxWidth = poi_name.getMeasuredWidth()>= poi_description.getMeasuredWidth()?poi_name.getMeasuredWidth(): poi_description.getMeasuredWidth();
                        Log.i("poiTag", "maxWidth: "+maxWidth);

                        poi_name.setWidth(maxWidth);
                        poi_description.setWidth(maxWidth);

                        arPoints.get(i).setPoiTag(poiTag);
                        mARContainer.addView(poiTag);
                    }
                    arPoints.get(i).getPoiTag().setX(x);
                    arPoints.get(i).getPoiTag().setY(y);
                }
            }
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        float x = event.getX();
        float y = event.getY();

        for(int i=0;i<pointxXY.length;i++){
            float xPoint = pointxXY[i][0];
            float yPoint = pointxXY[i][1];
            if(x>xPoint-100 && x< xPoint+100){
                if(y>yPoint-100&&y<yPoint+100){
                    arPoints.get(i).getPoiTag().callOnClick();
                }
            }
        }

        return false;
    }

    //    class MyAsyncTask extends AsyncTask<Integer,Integer,Integer> {
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            mARContainer.removeAllViews();
//            arPoints.clear();
//            movingCar = new ARPoint("387路公交","467m", 39.9584,116.3618, 0);
//            arPoints.add(movingCar);
//            Log.i("async", "Begin asyncTask------------------------------------");
//        }
//
//        @Override
//        protected Integer doInBackground(Integer... integers) {
//            int start = integers[0];
//            int end = integers[1];
//
//            int result = 0;
//            for(int i = start; i <=end; i++){
//                SystemClock.sleep(50);
//                result = i;
//                publishProgress(result);
//            }
//
//            return result;
//        }
//
//        @Override
//        protected void onProgressUpdate(Integer[] values) {
//            super.onProgressUpdate(values);
//            updateMovingPoint(values[0]);
//        }
//
//        @Override
//        protected void onPostExecute(Integer result) {
//            super.onPostExecute(result);
//        }
//
//    }



}