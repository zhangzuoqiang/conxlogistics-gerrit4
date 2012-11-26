package com.conx.logistics.kernel.ui.service;

import com.conx.logistics.kernel.ui.service.contribution.IActionContribution;
import com.conx.logistics.kernel.ui.service.contribution.IApplicationViewContribution;
import com.conx.logistics.kernel.ui.service.contribution.IViewContribution;
import com.vaadin.Application;

public interface IUIContributionManager {
	public final String UISERVICE_PROPERTY_CODE = "code";
	
	public IViewContribution getViewContributionByCode(Application application,String code);

	public IApplicationViewContribution getApplicationContributionByCode(Application application,String code);
	
	public IApplicationViewContribution[] getCurrentApplicationContributions();
	
	public IActionContribution getActionContributionByCode(Application application, String code);
}
