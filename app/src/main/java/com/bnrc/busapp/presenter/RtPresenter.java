package com.bnrc.busapp.presenter;

import com.bnrc.busapp.adapter.NearAdapter;
import com.bnrc.busapp.model.Child;
import com.bnrc.busapp.model.Group;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.util.List;

public interface RtPresenter {

    void getServerInfo(List<Group> groups, NearAdapter mNearAdapter);

    void getRtInfo(final Child child, NearAdapter mNearAdapter) throws JSONException,
            UnsupportedEncodingException;

    void getRtInfo(final Child child, String url, NearAdapter mNearAdapter);

}
