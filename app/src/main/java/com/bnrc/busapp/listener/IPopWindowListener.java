package com.bnrc.busapp.listener;


import com.bnrc.busapp.model.Child;
import com.bnrc.busapp.ui.LoadingDialog;

public interface IPopWindowListener {
	void onPopClick(Child child);

	void onLoginClick();

	LoadingDialog showLoading();

	void dismissLoading();
}
