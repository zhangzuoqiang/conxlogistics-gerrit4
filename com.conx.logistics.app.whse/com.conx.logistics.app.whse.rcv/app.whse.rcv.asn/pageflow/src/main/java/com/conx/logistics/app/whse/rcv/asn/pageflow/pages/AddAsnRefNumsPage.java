package com.conx.logistics.app.whse.rcv.asn.pageflow.pages;

import java.util.Map;

import com.conx.logistics.kernel.pageflow.services.BasePageFlowPage;
import com.conx.logistics.kernel.pageflow.services.IModelDrivenPageFlowPage;

public class AddAsnRefNumsPage extends BasePageFlowPage {
	@Override
	public Map<Class<?>, String> getParamKeyMap() {
		return null;
	}

	@Override
	public Map<Class<?>, String> getResultKeyMap() {
		return null;
	}

	@Override
	public String getTaskName() {
		return "AddAsnRefNumsPage";
	}

	@Override
	public Class<?> getType() {
		return IModelDrivenPageFlowPage.class;
	}
}