package com.bnrc.busapp.presenter;

import android.widget.BaseExpandableListAdapter;

import com.bnrc.busapp.adapter.NearAdapter;
import com.bnrc.busapp.model.Child;
import com.bnrc.busapp.model.Group;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.util.List;

public interface RtPresenter {

    void getServerInfo(List<Group> groups, BaseExpandableListAdapter mAdapter);

    void getRtInfo(final Child child,  BaseExpandableListAdapter mAdapter) throws JSONException,
            UnsupportedEncodingException;

    void getRtInfo(final Child child, String url,  BaseExpandableListAdapter mAdapter);

}
