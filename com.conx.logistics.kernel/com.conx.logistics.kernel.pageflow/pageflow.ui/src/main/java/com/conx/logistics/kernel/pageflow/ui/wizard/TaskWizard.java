package com.conx.logistics.kernel.pageflow.ui.wizard;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.mvp.eventbus.EventBus;
import org.vaadin.mvp.presenter.IPresenter;
import org.vaadin.teemu.wizards.Wizard;
import org.vaadin.teemu.wizards.WizardStep;

import com.conx.logistics.common.utils.Validator;
import com.conx.logistics.kernel.pageflow.event.IPageFlowPageChangedEventHandler;
import com.conx.logistics.kernel.pageflow.event.IPageFlowPageChangedListener;
import com.conx.logistics.kernel.pageflow.event.PageFlowPageChangedEvent;
import com.conx.logistics.kernel.pageflow.services.BasePageFlowPage;
import com.conx.logistics.kernel.pageflow.services.IPageComponent;
import com.conx.logistics.kernel.pageflow.services.IPageFlowManager;
import com.conx.logistics.kernel.pageflow.services.IPageFlowPage;
import com.conx.logistics.kernel.pageflow.services.IPageFlowSession;
import com.conx.logistics.kernel.pageflow.services.ITaskWizard;
import com.conx.logistics.kernel.pageflow.ui.builder.VaadinPageFactoryImpl;
import com.conx.logistics.kernel.persistence.services.IEntityContainerProvider;
import com.conx.logistics.kernel.ui.common.entityprovider.jta.CustomCachingMutableLocalEntityProvider;
import com.conx.logistics.kernel.ui.factory.services.IEntityEditorFactory;
import com.conx.logistics.kernel.ui.service.contribution.IApplicationViewContribution;
import com.conx.logistics.kernel.ui.service.contribution.IMainApplication;
import com.conx.logistics.kernel.ui.service.contribution.IViewContribution;
import com.conx.logistics.mdm.domain.BaseEntity;
import com.conx.logistics.mdm.domain.application.Feature;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Component;
import com.vaadin.ui.Window.Notification;

public class TaskWizard extends Wizard implements ITaskWizard, IPageFlowPageChangedEventHandler, IEntityContainerProvider {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final long serialVersionUID = 8417208260717324494L;

	private IPageFlowSession session;
	private IPageFlowManager engine;

	private HashMap<IPageFlowPage, IPageComponent> pageComponentMap;
	private VaadinPageFactoryImpl pageFactory;
	private Feature onCompletionCompletionFeature;
	private IPresenter<?, ? extends EventBus> onCompletionCompletionViewPresenter;
	private final Set<IPageFlowPageChangedListener> pageFlowPageChangedListenerCache = Collections.synchronizedSet(new HashSet<IPageFlowPageChangedListener>());

	private boolean nextButtonBlocked = false;
	private boolean backButtonBlocked = false;

	@SuppressWarnings("unused")
	private boolean processPageFlowPageChangedEvents;

	private HashMap<String, Object> initParams;
	private UserTransaction userTransaction;

	public TaskWizard(IPageFlowSession session) {
		this.session = session;
		this.engine = session.getPageFlowEngine();
		this.userTransaction = this.engine.getUserTransaction();
		this.pageComponentMap = new HashMap<IPageFlowPage, IPageComponent>();

		HashMap<String, Object> config = new HashMap<String, Object>();
		config.put(IEntityEditorFactory.CONTAINER_PROVIDER, this);
		this.pageFactory = new VaadinPageFactoryImpl(config);

		getNextButton().setImmediate(true);
		getBackButton().setImmediate(true);

		// Init steps
		addAllPages();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object createPersistenceContainer(Class<?> entityClass) {
		if (this.userTransaction != null) {
			CustomCachingMutableLocalEntityProvider provider = new CustomCachingMutableLocalEntityProvider(entityClass, this.session.getConXEntityManagerfactory(), this.userTransaction);
			JPAContainer<?> container = JPAContainerFactory.make(entityClass, (EntityManager) null);
			container.setEntityProvider(provider);
			return container;
		} else {
			return JPAContainerFactory.makeReadOnly(entityClass, this.session.getConXEntityManagerfactory().createEntityManager());
		}
	}

	private void addAllPages() {
		if (initParams == null) {
			initParams = new HashMap<String, Object>();
			initParams.put(IPageComponent.CONX_ENTITY_MANAGER_FACTORY, session.getConXEntityManagerfactory());
			initParams.put(IPageComponent.JTA_GLOBAL_TRANSACTION_MANAGER, session.getJTAGlobalTransactionManager());
			initParams.put(IPageComponent.TASK_WIZARD, this);
			initParams.put(IPageComponent.PAGE_FLOW_PAGE_CHANGE_EVENT_HANDLER, this);
			initParams.put(IPageComponent.ENTITY_CONTAINER_PROVIDER, this);
			initParams.put(IPageComponent.ENTITY_TYPE_DAO_SERVICE, this.engine.getEntityTypeDAOService());
		}
		if (session.getPages() != null) {
			for (IPageFlowPage page : session.getPages()) {
				IPageComponent pageComponent = this.pageFactory.createPage((IPageFlowPage) page, initParams);
				this.pageComponentMap.put(page, pageComponent);
				addStep(pageComponent);
			}
		}
	}

	public IPageFlowSession getSession() {
		return session;
	}

	public void setSession(IPageFlowSession session) {
		this.session = session;
	}

	@Override
	public Component getComponent() {
		return this;
	}

	public WizardStep getCurrentStep() {
		return currentStep;
	}

	public void setCurrentStep(WizardStep step) {
		currentStep = step;
	}

	@Override
	public void addStep(WizardStep step) {
		if (getSteps().size() == 0)// First page
			((IPageComponent) step).setParameterData(getProperties());
		super.addStep(step);
	}

	@Override
	public Map<String, Object> getProperties() {
		// Map<String,Object> props = new HashMap<String, Object>();
		// props.put("session",session);
		// props.putAll(session.getProcessVars());
		return session.getProcessVars();
	}

	@Override
	public Feature getOnCompletionFeature() {
		return session.getOnCompletionFeature();
	}

	@Override
	public void onNext(BasePageFlowPage currentPage, Map<String, Object> taskOutParams) {
		try {
			engine.executeTaskWizard(this, taskOutParams);
			// getProperties().
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onPrevious(BasePageFlowPage currentPage, Map<String, Object> state) {
		// TODO Implement previous
	}

	@Override
	public void next() {
		getNextButton().setEnabled(false);
		getBackButton().setEnabled(false);
		getCancelButton().setEnabled(false);
		try {
			completeCurrentTaskAndAdvanceToNext();
		} catch (Exception e) {
			e.printStackTrace();
			getWindow().showNotification("There was an unexpected error on the next page", "</br>Try to continue again. If the problem persists, contact your Administrator",
					Notification.TYPE_ERROR_MESSAGE);
			return;
		}
		super.next();
		getNextButton().setEnabled(!nextButtonBlocked);
		getBackButton().setEnabled(!backButtonBlocked);
		getCancelButton().setEnabled(false);
	}

	@Override
	public void back() {
		super.back();
	}

	@SuppressWarnings({ "unused", "null" })
	@Override
	public void finish() {
		// FIXME
		try {
			completeCurrentTaskAndAdvanceToNext();
		} catch (Exception e) {
			e.printStackTrace();
			getWindow().showNotification("There was an unexpected error on the next page", "</br>Try to continue again. If the problem persists, contact your Administrator",
					Notification.TYPE_ERROR_MESSAGE);
			return;
		}

		IMainApplication mainApp = null;
		if (Validator.isNotNull(mainApp)) {
			com.conx.logistics.mdm.domain.application.Application parentApp = onCompletionCompletionFeature.getParentApplication();

			IApplicationViewContribution avc = mainApp.getApplicationContributionByCode(parentApp.getCode());

			String viewCode = onCompletionCompletionFeature.getCode();
			IViewContribution vc = mainApp.getViewContributionByCode(viewCode);
			if (avc != null) {
				Method featureViewerMethod = null;
				Class<?> featureViewerMethodHandler = null;
				try {
					/*
					 * IPresenterFactory pf =
					 * this.engine.getMainApp().getPresenterFactory();
					 * IPresenter<?, ? extends EventBus> viewPresenter =
					 * pf.createPresenter
					 * (avc.getPresenterClass((Application)mainApp)); EventBus
					 * buss = (EventBus)viewPresenter.getEventBus();
					 */

					Method openFeatureViewMethod = onCompletionCompletionViewPresenter.getClass().getMethod("onOpenFeatureView", Feature.class);
					Object[] args = new Object[1];
					args[0] = onCompletionCompletionFeature;
					openFeatureViewMethod.invoke(onCompletionCompletionViewPresenter, args);
					/*
					 * for (Method event : events) {
					 * //logger.info("Event method: {} - handlers: ",
					 * event.getName());
					 * org.vaadin.mvp.eventbus.annotation.Event ea =
					 * event.getAnnotation
					 * (org.vaadin.mvp.eventbus.annotation.Event.class); if (ea
					 * != null) { for (Class<?> handler : ea.handlers()) {
					 * featureViewerMethodHandler = handler; } } }
					 */
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		super.finish();
	}

	private void completeCurrentTaskAndAdvanceToNext() throws Exception {
		IPageComponent currentPage, nextPage;
		Map<String, Object> params = null;
		currentPage = (IPageComponent) currentStep;
		if (currentPage.isExecuted()) {
			// engine.updateProcessInstanceVariables(this,
			// currentPage.getResultData());
		} else {
			// Complete current task and get input variables for the next task
			try {
				// params = currentPage.getResultData(); // Completes current
				// task with
				params = engine.executeTaskWizard(this, currentPage.getResultData()).getProperties();
			} catch (Exception e) {
				getWindow().showNotification("Could not complete this task", "", Notification.TYPE_ERROR_MESSAGE);
				// TODO Exception Handing
				e.printStackTrace();
				return;
			}
			// Start the next task (if it exists) with input variables from
			// previous task
			int index = steps.indexOf(currentStep);
			if ((index + 1) < steps.size()) {
				nextPage = (IPageComponent) steps.get(index + 1);
				nextPage.setParameterData(params);
			}
		}
	}

	public boolean currentStepIsLastStep() {
		return isLastStep(currentStep);
	}

	@Override
	public void registerForPageFlowPageChanged(IPageFlowPageChangedListener listener) {
		pageFlowPageChangedListenerCache.add(listener);
	}

	@Override
	public void unregisterForPageFlowPageChanged(IPageFlowPageChangedListener listener) {
		pageFlowPageChangedListenerCache.remove(listener);
	}

	@Override
	public void enablePageFlowPageChangedEventHandling(boolean enable) {
		this.processPageFlowPageChangedEvents = enable;
	}

	@Override
	protected boolean isLastStep(WizardStep step) {
		return this.session.isOnLastPage();
	};

	@Override
	public void fireOnPageFlowChanged(PageFlowPageChangedEvent event) {
		for (IPageFlowPageChangedListener listener : pageFlowPageChangedListenerCache) {
			listener.onPageChanged(event);
		}
	}

	@Override
	public void setNextEnabled(boolean isEnabled) {
		getNextButton().setEnabled(isEnabled);
		nextButtonBlocked = !isEnabled;
	}

	@Override
	public boolean isNextEnabled() {
		return !nextButtonBlocked;
	}

	@Override
	public void setBackEnabled(boolean isEnabled) {
		getBackButton().setEnabled(isEnabled);
		backButtonBlocked = !isEnabled;
	}

	@Override
	public boolean isBackEnabled() {
		return !backButtonBlocked;
	}

	public void onPagesChanged() {
		List<WizardStep> stepsCopy = new ArrayList<WizardStep>();
		stepsCopy.addAll(steps);
		// Remove steps
		// for (WizardStep step_ : stepsCopy)
		// {
		// removeStep(step_);
		// }

		// Add pages from path
		if (session.getPages() != null) {
			IPageComponent pageComponent = null;
			for (IPageFlowPage page : session.getPages()) {
				pageComponent = pageComponentMap.get(page);
				boolean stepAlreadyExists = pageComponent != null;
				if (!stepAlreadyExists) {
					pageComponent = this.pageFactory.createPage((IPageFlowPage) page, initParams);
					this.pageComponentMap.put(page, pageComponent);
					addStep(pageComponent);
				}
			}
		}
	}

	@Override
	public Object createBeanContainer(Class<?> entityClass) {
		if (BaseEntity.class.isAssignableFrom(entityClass)) {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			BeanItemContainer container = new BeanItemContainer(entityClass);
			return container;
		}
		return null;
	}

	@Override
	public EntityManagerFactory getEmf() {
		return this.session.getConXEntityManagerfactory();
	}
}