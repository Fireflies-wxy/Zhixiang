package com.bnrc.bnrcbus.view.activity;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.bnrc.bnrcbus.R;
import com.bnrc.bnrcbus.util.AnimationUtil;

public class BusCircleActivity extends AppCompatActivity {

    private TextView mSubway;
    private TextView mHotel;
    private TextView mRestaurant;
    private TextView mBank;
    private TextView mSupermarket;
    private TextView mOil;
    private TextView mNetbar;
    private TextView mKTV;

    private Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_circle);

        mContext = this;

        mSubway = (TextView) findViewById(R.id.tv_subway);
        mHotel = (TextView) findViewById(R.id.tv_hotel);
        mRestaurant = (TextView) findViewById(R.id.tv_restaurant);
        mBank = (TextView) findViewById(R.id.tv_ATM);
        mSupermarket = (TextView) findViewById(R.id.tv_supermarket);
        mOil = (TextView) findViewById(R.id.tv_oil);
        mNetbar = (TextView) findViewById(R.id.tv_netbar);
        mKTV = (TextView) findViewById(R.id.tv_KTV);
        setListener();
    }

    private void setListener() {
        mSubway.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                Intent intent = new Intent(mContext,
                        SearchSomethingActivity.class);

                // ���ñ�����͸��
                intent.putExtra("Keyword", "地铁");
                startActivity(intent);
                AnimationUtil.activityZoomAnimation(mContext);

            }
        });
        mOil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub

                Intent intent = new Intent(mContext,
                        SearchSomethingActivity.class);
                // ���ñ�����͸��
                intent.putExtra("Keyword", "加油站");
                startActivity(intent);
                AnimationUtil.activityZoomAnimation(mContext);

            }
        });

        mNetbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub

                Intent intent = new Intent(mContext,
                        SearchSomethingActivity.class);
                // ���ñ�����͸��
                intent.putExtra("Keyword", "网吧");
                startActivity(intent);
                AnimationUtil.activityZoomAnimation(mContext);

            }
        });

        mKTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub

                Intent intent = new Intent(mContext,
                        SearchSomethingActivity.class);
                intent.putExtra("Keyword", "KTV");
                startActivity(intent);
                AnimationUtil.activityZoomAnimation(mContext);

            }
        });
        mHotel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                // �Ƿ�ֻ���ѵ�¼�û����ܴ򿪷���ѡ��ҳ

                Intent intent = new Intent(mContext,
                        SearchSomethingActivity.class);
                // ���ñ�����͸��
                intent.putExtra("Keyword", "酒店");
                startActivity(intent);
                AnimationUtil.activityZoomAnimation(mContext);

            }
        });

        mRestaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub

                Intent intent = new Intent(mContext,
                        SearchSomethingActivity.class);
                intent.putExtra("Keyword", "小吃");
                startActivity(intent);
                AnimationUtil.activityZoomAnimation(mContext);

            }
        });

        mBank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                // �Ƿ�ֻ���ѵ�¼�û����ܴ򿪷���ѡ��ҳ

                Intent intent = new Intent(mContext,
                        SearchSomethingActivity.class);
                // ���ñ�����͸��
                intent.putExtra("Keyword", "银行");
                startActivity(intent);
                AnimationUtil.activityZoomAnimation(mContext);

            }
        });

        mSupermarket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                // �Ƿ�ֻ���ѵ�¼�û����ܴ򿪷���ѡ��ҳ

                Intent intent = new Intent(mContext,
                        SearchSomethingActivity.class);
                intent.putExtra("Keyword", "超市");
                startActivity(intent);
                AnimationUtil.activityZoomAnimation(mContext);

            }
        });

    }


}
