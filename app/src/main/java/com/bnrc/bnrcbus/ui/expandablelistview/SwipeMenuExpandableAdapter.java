package com.bnrc.bnrcbus.ui.expandablelistview;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;

/**
 * Created by frankrong on 15/11/24.
 */
public abstract class SwipeMenuExpandableAdapter extends BaseExpandableListAdapter implements
        SwipeMenuView.OnSwipeItemClickListener  {
    private static final String TAG = SwipeMenuExpandableAdapter.class.getName();
    private BaseExpandableListAdapter mAdapter;
    private Context mContext;
    private SwipeMenuExpandableListView.OnMenuItemClickListener onMenuItemClickListener;

    public SwipeMenuExpandableAdapter(Context context, ExpandableListAdapter adapter) {
        this.mContext = context;
        this.mAdapter = (BaseExpandableListAdapter)adapter;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        mAdapter.registerDataSetObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        mAdapter.unregisterDataSetObserver(observer);
    }

    @Override
    public int getGroupCount() {
        return mAdapter.getGroupCount();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mAdapter.getChildrenCount(groupPosition);
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mAdapter.getGroup(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mAdapter.getChild(groupPosition, childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return mAdapter.getGroupId(groupPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return mAdapter.getChildId(groupPosition, childPosition);
    }

    @Override
    public int getChildType(int groupPosition, int childPosition) {
        return mAdapter.getChildType(groupPosition, childPosition);
    }

    @Override
    public int getChildTypeCount() {
        return mAdapter.getChildTypeCount();
    }

    @Override
    public int getGroupType(int groupPosition) {
        return mAdapter.getGroupType(groupPosition);
    }

    @Override
    public int getGroupTypeCount() {
        return mAdapter.getGroupTypeCount();
    }

    @Override
    public boolean hasStableIds() {
        return mAdapter.hasStableIds();
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View view, ViewGroup parent) {
        SwipeMenuLayout layout = null;
        if (view == null) {
            View contentView = mAdapter.getGroupView(groupPosition, isExpanded, view, parent);
            layout = addMenuItem(mAdapter.getGroupType(groupPosition), contentView, parent);
        } else {
            layout = (SwipeMenuLayout) view;
            layout.closeMenu();
            mAdapter.getGroupView(groupPosition, isExpanded, layout.getContentView(), parent);
        }

        layout.setGroupPosition(groupPosition);
        layout.setPosition(-1);

        layout.setTag(layout.getContentView().getId());
        layout.setChanged(true);
        return layout;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View view, ViewGroup parent) {
        SwipeMenuLayout layout = null;
        if (view == null) {
            View contentView = mAdapter.getChildView(groupPosition, childPosition, isLastChild, view, parent);
            layout = addMenuItem(mAdapter.getChildType(groupPosition, childPosition), contentView, parent);
        } else {
            layout = (SwipeMenuLayout) view;
            layout.closeMenu();
            mAdapter.getChildView(groupPosition, childPosition, isLastChild, layout.getContentView(), parent);
        }

        layout.setGroupPosition(groupPosition);
        layout.setPosition(childPosition);

        layout.setTag(layout.getContentView().getId());
        layout.setChanged(true);
        return layout;
    }

    private SwipeMenuLayout addMenuItem(int type, View convertView, View parent){
        SwipeMenu menu = new SwipeMenu(mContext);
        menu.setViewType(type);
        createMenu(menu);
        SwipeMenuView menuView = new SwipeMenuView(menu);
        menuView.setOnSwipeItemClickListener(this);
        SwipeMenuExpandableListView listView = (SwipeMenuExpandableListView) parent;
        return new SwipeMenuLayout(convertView, menuView,
                listView.getCloseInterpolator(),
                listView.getOpenInterpolator());
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return mAdapter.isChildSelectable(groupPosition, childPosition);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return mAdapter.areAllItemsEnabled();
    }

    @Override
    public boolean isEmpty() {
        return mAdapter.isEmpty();
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        mAdapter.onGroupExpanded(groupPosition);
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
        mAdapter.onGroupCollapsed(groupPosition);
    }

    @Override
    public long getCombinedChildId(long groupId, long childId) {
        return mAdapter.getCombinedChildId(groupId, childId);
    }

    @Override
    public long getCombinedGroupId(long groupId) {
        return mAdapter.getCombinedGroupId(groupId);
    }

    @Override
    public void onItemClick(SwipeMenuView view, SwipeMenu menu, int index) {

    }

    public abstract void createMenu(SwipeMenu menu);

}
