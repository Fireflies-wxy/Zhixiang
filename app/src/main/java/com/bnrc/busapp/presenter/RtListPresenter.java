package com.bnrc.busapp.presenter;

import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;

import com.bnrc.busapp.model.Child;
import com.bnrc.busapp.model.Group;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.util.List;

public interface RtListPresenter {

    void getServerInfo(List<Group> groups, BaseAdapter mAdapter);

    void getRtInfo(final Child child, BaseAdapter mAdapter) throws JSONException,
            UnsupportedEncodingException;

    void getRtInfo(final Child child, String url, BaseAdapter mAdapter);

}
