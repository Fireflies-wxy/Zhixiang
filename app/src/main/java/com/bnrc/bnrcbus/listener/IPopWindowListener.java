package com.bnrc.bnrcbus.listener;


import com.bnrc.bnrcbus.model.Child;
import com.bnrc.bnrcbus.ui.LoadingDialog;

public interface IPopWindowListener {
	void onPopClick(Child child);

	void onLoginClick();

	LoadingDialog showLoading();

	void dismissLoading();
}
