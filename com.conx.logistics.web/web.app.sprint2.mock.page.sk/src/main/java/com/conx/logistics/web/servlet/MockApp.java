package com.conx.logistics.web.servlet;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.vaadin.mvp.eventbus.EventBus;
import org.vaadin.mvp.eventbus.EventBusManager;
import org.vaadin.mvp.presenter.IPresenter;
import org.vaadin.mvp.uibinder.UiBinderException;

import com.conx.logistics.app.whse.dao.services.ILocationDAOService;
import com.conx.logistics.app.whse.rcv.rcv.dao.services.IArrivalDAOService;
import com.conx.logistics.app.whse.rcv.rcv.dao.services.IReceiveDAOService;
import com.conx.logistics.app.whse.rcv.rcv.domain.Arrival;
import com.conx.logistics.app.whse.rcv.rcv.domain.Receive;
import com.conx.logistics.common.utils.Randomizer;
import com.conx.logistics.common.utils.Validator;
import com.conx.logistics.kernel.documentlibrary.remote.services.IRemoteDocumentRepository;
import com.conx.logistics.kernel.metamodel.dao.services.IEntityTypeDAOService;
import com.conx.logistics.kernel.pageflow.event.IPageFlowPageChangedEventHandler;
import com.conx.logistics.kernel.pageflow.event.IPageFlowPageChangedListener;
import com.conx.logistics.kernel.pageflow.event.PageFlowPageChangedEvent;
import com.conx.logistics.kernel.pageflow.services.IModelDrivenPageFlowPage;
import com.conx.logistics.kernel.pageflow.services.IPageComponent;
import com.conx.logistics.kernel.pageflow.services.IPageFlowManager;
import com.conx.logistics.kernel.pageflow.services.IPageFlowPage;
import com.conx.logistics.kernel.pageflow.ui.builder.VaadinPageFactoryImpl;
import com.conx.logistics.kernel.pageflow.ui.ext.form.container.VaadinBeanItemContainer;
import com.conx.logistics.kernel.pageflow.ui.ext.form.container.VaadinJPAContainer;
import com.conx.logistics.kernel.persistence.services.IEntityContainerProvider;
import com.conx.logistics.kernel.portal.remote.services.IPortalOrganizationService;
import com.conx.logistics.kernel.portal.remote.services.IPortalRoleService;
import com.conx.logistics.kernel.portal.remote.services.IPortalUserService;
import com.conx.logistics.kernel.system.dao.services.application.IApplicationDAOService;
import com.conx.logistics.kernel.ui.common.data.container.EntityTypeContainerFactory;
import com.conx.logistics.kernel.ui.common.entityprovider.jta.CustomCachingMutableLocalEntityProvider;
import com.conx.logistics.kernel.ui.common.entityprovider.jta.CustomNonCachingMutableLocalEntityProvider;
import com.conx.logistics.kernel.ui.common.ui.menu.app.AppMenuEntry;
import com.conx.logistics.kernel.ui.factory.services.IEntityEditorFactory;
import com.conx.logistics.kernel.ui.factory.services.data.IDAOProvider;
import com.conx.logistics.kernel.ui.service.IUIContributionManager;
import com.conx.logistics.kernel.ui.service.contribution.IActionContribution;
import com.conx.logistics.kernel.ui.service.contribution.IApplicationViewContribution;
import com.conx.logistics.kernel.ui.service.contribution.IViewContribution;
import com.conx.logistics.mdm.dao.services.IEntityMetadataDAOService;
import com.conx.logistics.mdm.dao.services.documentlibrary.IFolderDAOService;
import com.conx.logistics.mdm.domain.BaseEntity;
import com.conx.logistics.mdm.domain.user.User;
import com.sun.syndication.io.impl.Base64;
import com.vaadin.Application;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.util.EntityManagerPerRequestHelper;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class MockApp extends Application implements IPageFlowPageChangedEventHandler, IEntityContainerProvider {
	private static final long serialVersionUID = 7554815261443700716L;
	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	/** Per application (session) event bus manager */
	private EventBusManager ebm = new EventBusManager();
	/** Per application presenter factory */

	/** Main presenter */
	private IUIContributionManager uiContributionManager;

	private final Map<String, Map<String, IPageFlowPage>> pageCache = Collections.synchronizedMap(new HashMap<String, Map<String, IPageFlowPage>>());
	
	private IApplicationDAOService applicationDAOService;

	@SuppressWarnings("unused")
	private IFolderDAOService folderDAOService;

	private IRemoteDocumentRepository remoteDocumentRepository;

	private boolean appServiceInititialized = false;

	private PlatformTransactionManager kernelSystemTransManager;

	private UserTransaction userTransaction;

	private EntityManagerFactory kernelSystemEntityManagerFactory;

	private IPageFlowManager pageFlowEngine;

	private IEntityEditorFactory entityEditorFactory;
	
	private IDAOProvider daoProvider;

	/** UI contributions management */
	private final Map<String, IApplicationViewContribution> appContributions = Collections.synchronizedMap(new HashMap<String, IApplicationViewContribution>());
	private final Map<String, IViewContribution> viewContributions = Collections.synchronizedMap(new HashMap<String, IViewContribution>());
	private final Map<String, IActionContribution> actionContributions = Collections.synchronizedMap(new HashMap<String, IActionContribution>());

	private volatile boolean initialized = false;

	private EntityTypeContainerFactory entityTypeContainerFactory;

	private EntityManagerPerRequestHelper entityManagerPerRequestHelper;

	private IEntityMetadataDAOService entityMetaDataDAOService;
	
	private IEntityTypeDAOService entityTypeDAOService;

	private IPortalUserService portalUserService;

	private IPortalRoleService portalRoleService;

	private IPortalOrganizationService portalOrganizationService;

	private User currentUser = null;

	public void registerModelDrivenPageFlowPage(IModelDrivenPageFlowPage page, Map<String, Object> properties) {
		String processId = (String) properties.get(IPageFlowPage.PROCESS_ID);
		String taskName = (String) properties.get(IPageFlowPage.TASK_NAME);
		logger.debug("registerModelDrivenPageFlowPage(" + processId + "," + taskName + ")");
		Map<String, IPageFlowPage> map = this.pageCache.get(processId);
		if (map == null) {
			map = new HashMap<String, IPageFlowPage>();
			pageCache.put(processId, map);
		}
		map.put(taskName, page);
	}

	public void unregisterModelDrivenPageFlowPage(IModelDrivenPageFlowPage page, Map<String, Object> properties) {
		String processId = (String) properties.get(IPageFlowPage.PROCESS_ID);
		logger.debug("unregisterModelDrivenPageFlowPage(" + processId + ")");
		Map<String, IPageFlowPage> map = this.pageCache.get(processId);
		String taskName = (String) properties.get(IPageFlowPage.TASK_NAME);
		if (map != null) {
			map.remove(taskName);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void init() {
		try {
			setTheme("conx");
			// Create container manager/helper
			this.entityManagerPerRequestHelper = new EntityManagerPerRequestHelper();
			if (this.pageFlowEngine != null) {
				Map<String, IPageFlowPage> map = this.pageCache.get("whse.rcv.arrivalproc.ProcessCarrierArrivalV1.0");
				IPageFlowPage page = map.get("ProcessArrivalReceipts");

				VerticalLayout layout = new VerticalLayout();
				layout.setSizeFull();

				if (page != null) {
					HashMap<String, Object> config = new HashMap<String, Object>();
					config.put(IEntityEditorFactory.CONTAINER_PROVIDER, this);
					VaadinPageFactoryImpl factory = new VaadinPageFactoryImpl(config);

					HashMap<String, Object> initParams = new HashMap<String, Object>();
					initParams.put(IPageComponent.CONX_ENTITY_MANAGER_FACTORY, this.kernelSystemEntityManagerFactory);
					initParams.put(IPageComponent.JTA_GLOBAL_TRANSACTION_MANAGER, this.kernelSystemTransManager);
					initParams.put(IPageComponent.PAGE_FLOW_PAGE_CHANGE_EVENT_HANDLER, this);
					initParams.put(IPageComponent.ENTITY_CONTAINER_PROVIDER, this);
					initParams.put(IEntityEditorFactory.FACTORY_PARAM_IENTITY_METADATA_SERVICE, entityMetaDataDAOService);
					initParams.put(IPageComponent.ENTITY_TYPE_DAO_SERVICE, entityTypeDAOService);
					initParams.put(IPageComponent.DAO_PROVIDER, daoProvider);
					
					IPageComponent pageComponent = factory.createPage(page, initParams);
					
					Receive rcv = new Receive();
					rcv.setCode("RDFLT" + Randomizer.getInstance().randomize("012345"));
					
					Arrival arrival = new Arrival();
					
					DefaultTransactionDefinition def = new DefaultTransactionDefinition();
					def.setName("whse.app.page.sk");
					def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
					TransactionStatus status = this.kernelSystemTransManager.getTransaction(def);
					try {
						rcv = this.daoProvider.provideByDAOClass(IReceiveDAOService.class).add(rcv);
						arrival = this.daoProvider.provideByDAOClass(IArrivalDAOService.class).add(arrival, rcv);
						this.daoProvider.provideByDAOClass(ILocationDAOService.class).provide(1, 1, "A");
						this.kernelSystemTransManager.commit(status);
					} catch (Exception e) {
						StringWriter sw = new StringWriter();
						e.printStackTrace(new PrintWriter(sw));
						String stacktrace = sw.toString();
						logger.error(stacktrace);
						this.kernelSystemTransManager.rollback(status);
						throw e;
					}
					
					HashMap<String, Object> params = new HashMap<String, Object>();
					params.put("arrivalIn", arrival);
					pageComponent.setParameterData(params);
					
					Component layoutComponent = pageComponent.getContent();
					layout.addComponent(layoutComponent);
					layout.setExpandRatio(layoutComponent, 1.0f);
				}

				Window mainWindow = new Window();
				mainWindow.setSizeFull();
				mainWindow.setLayout(layout);

				this.setMainWindow(mainWindow);
			}
			initialized = true;
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String stacktrace = sw.toString();
			logger.error(stacktrace);
		}

	}

	@SuppressWarnings("rawtypes")
	public void bindEntityMetaDataDAOService(IEntityMetadataDAOService entityMetaDataDAOService, Map properties) {
		logger.debug("bindEntityMetaDataDAOService()");
		this.entityMetaDataDAOService = entityMetaDataDAOService;
	}

	@SuppressWarnings("rawtypes")
	public void unbindEntityMetaDataDAOService(IEntityMetadataDAOService entityMetaDataDAOService, Map properties) {
		logger.debug("unbindEntityMetaDataDAOService()");
		this.entityMetaDataDAOService = null;
	}
	
	@SuppressWarnings("rawtypes")
	public void bindEntityTypeDAOService(IEntityTypeDAOService entityTypeDAOService, Map properties) {
		logger.debug("bindEntityTypeDAOService()");
		this.entityTypeDAOService = entityTypeDAOService;
	}

	@SuppressWarnings("rawtypes")
	public void unbindEntityTypeDAOService(IEntityTypeDAOService entityTypeDAOService, Map properties) {
		logger.debug("unbindEntityTypeDAOService()");
		this.entityTypeDAOService = null;
	}

	/**
	 * 
	 * HttpServletRequestListener
	 * 
	 */
	@SuppressWarnings("rawtypes")
	public void onRequestStart(HttpServletRequest request, HttpServletResponse response) {
		// authenticate(request);
		Map pns = request.getParameterMap();// email,pwd
		String email = null;

		if (pns.containsKey("email")) {
			String[] emailArray = (String[]) pns.get("email");
			email = emailArray[0];
		}

		if (pns.containsKey("pwd")) {
			String[] emailArray = (String[]) pns.get("pwd");
			email = emailArray[0];
		}

		String screenName = null;

		if (Validator.isNull(currentUser)) {
			if (Validator.isNotNull(email)) // Normal login
			{
				try {
					currentUser = portalUserService.provideUserByEmailAddress(email);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else// Test/Dev login
			{
				email = "test@liferay.com";
				screenName = "ConX Test Admin";
				currentUser = new User();
				currentUser.setEmailAddress(email);
				currentUser.setScreenName(screenName);
			}
		}

		// Start request helper
		if (this.entityManagerPerRequestHelper != null)// Init called already
			this.entityManagerPerRequestHelper.requestStart();
	}

	@SuppressWarnings("unused")
	private boolean authenticate(HttpServletRequest req) {
		String authhead = req.getHeader("Authorization");

		if (authhead != null) {
			// *****Decode the authorisation String*****
			String usernpass = Base64.decode(authhead.substring(6));
			// *****Split the username from the password*****
			String user = usernpass.substring(0, usernpass.indexOf(":"));
			String password = usernpass.substring(usernpass.indexOf(":") + 1);

			if (user.equals("user") && password.equals("pass"))
				return true;
		}

		return false;
	}

	public void onRequestEnd(HttpServletRequest request, HttpServletResponse response) {
		if (this.entityManagerPerRequestHelper != null)// Init called already
			this.entityManagerPerRequestHelper.requestEnd();
	}

	@SuppressWarnings("rawtypes")
	public void bindActionContribution(IActionContribution actionContribution, Map properties) {
		String code = (String) properties.get(IUIContributionManager.UISERVICE_PROPERTY_CODE);
		if (Validator.isNotNull(code)) {
			logger.info("bindActionContribution(" + code + ")");
			actionContributions.put(code, actionContribution);
			if (initialized) {
				/*
				 * tabSheet.addTab(viewContribution.getView(this),
				 * viewContribution .getName(), new
				 * ThemeResource(viewContribution.getIcon()));
				 */
			}
		} else {
			logger.error("bindViewContribution has no code associated with it. Registration failed.");
		}
	}
	
	@SuppressWarnings("rawtypes")
	public void unbindActionContribution(IActionContribution actionContribution, Map properties) {
		String code = (String) properties.get(IUIContributionManager.UISERVICE_PROPERTY_CODE);
		if (Validator.isNotNull(code)) {
			logger.info("unbindActionContribution(" + code + ")");
			actionContributions.remove(code);
			if (initialized) {
				/*
				 * tabSheet.addTab(viewContribution.getView(this),
				 * viewContribution .getName(), new
				 * ThemeResource(viewContribution.getIcon()));
				 */
			}
		} else {
			logger.error("unbindActionContribution has no code associated with it. Deregistration failed.");
		}
	}

	@SuppressWarnings("rawtypes")
	public void bindViewContribution(IViewContribution viewContribution, Map properties) {
		String code = (String) properties.get(IUIContributionManager.UISERVICE_PROPERTY_CODE);
		if (Validator.isNotNull(code)) {
			logger.info("bindViewContribution(" + code + ")");
			viewContributions.put(code, viewContribution);
			if (initialized) {
				/*
				 * tabSheet.addTab(viewContribution.getView(this),
				 * viewContribution .getName(), new
				 * ThemeResource(viewContribution.getIcon()));
				 */
			}
		} else {
			logger.error("bindViewContribution has no code associated with it. Registration failed.");
		}
	}

	@SuppressWarnings("rawtypes")
	public void unbindViewContribution(IViewContribution viewContribution, Map properties) {
		String code = (String) properties.get(IUIContributionManager.UISERVICE_PROPERTY_CODE);
		if (Validator.isNotNull(code)) {
			logger.info("unbindViewContribution(" + code + ")");
			viewContributions.remove(code);
			if (initialized) {
				/*
				 * tabSheet.addTab(viewContribution.getView(this),
				 * viewContribution .getName(), new
				 * ThemeResource(viewContribution.getIcon()));
				 */
			}
		} else {
			logger.error("unbindViewContribution has no code associated with it. Deregistration failed.");
		}
	}

	@SuppressWarnings("unused")
	private AppMenuEntry createAppMenuEntry(IApplicationViewContribution avc) throws UiBinderException {
		return new AppMenuEntry(avc.getCode(), avc.getName(), avc.getIcon(), avc.getApplicationComponent(this));
	}

	public AppMenuEntry[] createAppMenuEntries() throws UiBinderException {
		if (appContributions.isEmpty())
			return new AppMenuEntry[] {};
		else {
			ArrayList<AppMenuEntry> entries = new ArrayList<AppMenuEntry>();
			for (IApplicationViewContribution ac : appContributions.values()) {
				entries.add(new AppMenuEntry(ac.getCode(), ac.getName(), ac.getIcon(), ac.getApplicationComponent(this)));
			}
			return entries.toArray(new AppMenuEntry[] {});
		}
	}

	@SuppressWarnings("rawtypes")
	public void bindApplicationDAOService(IApplicationDAOService applicationDAOService, Map properties) {
		logger.debug("bindApplicationDAOService()");
		this.applicationDAOService = applicationDAOService;
		if (!appServiceInititialized)
			initAppService();
	}

	private void initAppService() {
		if (Validator.isNotNull(this.kernelSystemTransManager) && Validator.isNotNull(this.applicationDAOService)) {
			DefaultTransactionDefinition def = new DefaultTransactionDefinition();
			// explicitly setting the transaction name is something that can
			// only be done programmatically
			def.setName("sendFlightScheduleUpdate");
			def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
			TransactionStatus status = this.kernelSystemTransManager.getTransaction(def);
			try {
				this.applicationDAOService.provideControlPanelApplication();
				this.kernelSystemTransManager.commit(status);
			} catch (Exception e) {
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				String stacktrace = sw.toString();
				logger.error(stacktrace);

				this.kernelSystemTransManager.rollback(status);
			}
			appServiceInititialized = true;
		}
	}

	@SuppressWarnings("rawtypes")
	public void unbindApplicationDAOService(IApplicationDAOService applicationDAOService, Map properties) {
		logger.debug("unbindApplicationDAOService()");
		this.applicationDAOService = null;
		appServiceInititialized = false;
	}

	@SuppressWarnings("rawtypes")
	public void unbindPageFlowEngine(IPageFlowManager pageflowEngine, Map properties) {
		logger.debug("unbindPageFlowEngine()");
		this.pageFlowEngine = null;
	}

	@SuppressWarnings("rawtypes")
	public void bindPageFlowEngine(IPageFlowManager pageflowEngine, Map properties) {
		logger.debug("bindPageFlowEngine()");
		this.pageFlowEngine = pageflowEngine;
	}

	@SuppressWarnings("rawtypes")
	public void bindKernelSystemTransManager(PlatformTransactionManager kernelSystemTransManager, Map properties) {
		logger.debug("bindKernelSystemTransManager()");
		this.kernelSystemTransManager = kernelSystemTransManager;
		if (!appServiceInititialized)
			initAppService();
	}

	@SuppressWarnings("rawtypes")
	public void unbindKernelSystemTransManager(PlatformTransactionManager kernelSystemTransManager, Map properties) {
		logger.debug("unbindKernelSystemTransManager()");
		this.kernelSystemTransManager = null;
		appServiceInititialized = false;
	}

	@SuppressWarnings("rawtypes")
	public void bindUserTransaction(UserTransaction userTransaction, Map properties) {
		logger.debug("bindUserTransaction()");
		this.userTransaction = userTransaction;
	}

	@SuppressWarnings("rawtypes")
	public void unbindUserTransaction(UserTransaction userTransaction, Map properties) {
		logger.debug("unbindUserTransaction()");
		this.userTransaction = null;
	}

	@SuppressWarnings("rawtypes")
	public void bindKernelSystemEntityManagerFactory(EntityManagerFactory kernelSystemEntityManagerFactory, Map properties) {
		logger.debug("bindKernelSystemEntityManagerFactory()");
		this.entityTypeContainerFactory = new EntityTypeContainerFactory(kernelSystemEntityManagerFactory.createEntityManager());
		this.kernelSystemEntityManagerFactory = kernelSystemEntityManagerFactory;
	}

	@SuppressWarnings("rawtypes")
	public void unbindKernelSystemEntityManagerFactory(PlatformTransactionManager kernelSystemTransManager, Map properties) {
		logger.debug("unbindKernelSystemEntityManagerFactory()");
		this.entityTypeContainerFactory = null;
		this.kernelSystemEntityManagerFactory = null;
	}

	@SuppressWarnings("rawtypes")
	public void bindEntityEditorFactory(IEntityEditorFactory entityEditorFactory, Map properties) {
		logger.debug("bindEntityEditorFactory()");
		this.entityEditorFactory = entityEditorFactory;
	}

	@SuppressWarnings("rawtypes")
	public void unbindEntityEditorFactory(IEntityEditorFactory entityEditorFactory, Map properties) {
		logger.debug("unbindEntityEditorFactory()");
		this.entityEditorFactory = null;
	}

	@SuppressWarnings({ "rawtypes" })
	public void bindRemoteDocumentRepository(IRemoteDocumentRepository remoteDocumentRepository, Map properties) {
		logger.debug("bindRemoteDocumentRepository()");
		this.remoteDocumentRepository = remoteDocumentRepository;
	}

	@SuppressWarnings({ "rawtypes" })
	public void unbindRemoteDocumentRepository(IRemoteDocumentRepository remoteDocumentRepository, Map properties) {
		logger.debug("unbindRemoteDocumentRepository()");
		this.remoteDocumentRepository = null;
	}

	@SuppressWarnings({ "rawtypes" })
	public void bindFolderDAOService(IFolderDAOService folderDAOService, Map properties) {
		logger.debug("bindFolderDAOService()");
		this.folderDAOService = folderDAOService;
	}

	@SuppressWarnings({ "rawtypes" })
	public void unbindFolderDAOService(IFolderDAOService folderDAOService, Map properties) {
		logger.debug("unbindFolderDAOService()");
		this.folderDAOService = null;
	}

	public IUIContributionManager getUiContributionManager() {
		return uiContributionManager;
	}

	public EntityManagerFactory getKernelSystemEntityManagerFactory() {
		return kernelSystemEntityManagerFactory;
	}

	public EntityTypeContainerFactory getEntityTypeContainerFactory() {
		return entityTypeContainerFactory;
	}

	public IRemoteDocumentRepository getRemoteDocumentRepository() {
		return remoteDocumentRepository;
	}

	public EventBusManager getEeventBusManager() {
		return ebm;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public EventBus createEventBuss(Class eventBusClass, IPresenter presenter) {
		return ebm.register(eventBusClass, presenter);
	}

	public IViewContribution getViewContributionByCode(String code) {
		return viewContributions.get(code);
	}

	public IActionContribution getActionContributionByCode(String code) {
		return actionContributions.get(code);
	}

	public IApplicationViewContribution getApplicationContributionByCode(String code) {
		return appContributions.get(code);
	}

	public IEntityEditorFactory getEntityEditorFactory() {
		return entityEditorFactory;
	}

	public void setEntityEditorFactory(IEntityEditorFactory entityEditorFactory) {
		this.entityEditorFactory = entityEditorFactory;
	}

	public EntityManagerPerRequestHelper getEntityManagerPerRequestHelper() {
		return entityManagerPerRequestHelper;
	}

	public void setEntityManagerPerRequestHelper(EntityManagerPerRequestHelper entityManagerPerRequestHelper) {
		this.entityManagerPerRequestHelper = entityManagerPerRequestHelper;
	}

	public IPortalUserService getPortalUserService() {
		return portalUserService;
	}

	public void setPortalUserService(IPortalUserService portalUserService) {
		this.portalUserService = portalUserService;
	}

	public IPortalOrganizationService getPortalOrganizationService() {
		return portalOrganizationService;
	}

	public void setPortalOrganizationService(IPortalOrganizationService portalOrganizationService) {
		this.portalOrganizationService = portalOrganizationService;
	}

	public IPortalRoleService getPortalRoleService() {
		return portalRoleService;
	}

	public void setPortalRoleService(IPortalRoleService portalRoleService) {
		this.portalRoleService = portalRoleService;
	}

	public User getCurrentUser() {
		return currentUser;
	}

	public void setCurrentUser(User currentUser) {
		this.currentUser = currentUser;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object createPersistenceContainer(Class<?> entityClass) {
		if (this.userTransaction != null) {
			CustomCachingMutableLocalEntityProvider provider = new CustomCachingMutableLocalEntityProvider(entityClass, this.getEmf(), this.userTransaction);
			JPAContainer<?> container = new VaadinJPAContainer(entityClass, provider);
			return container;
		} else {
			return JPAContainerFactory.makeReadOnly(entityClass, this.getEmf().createEntityManager());
		}
	}

	@Override
	public Object createBeanContainer(Class<?> entityClass) {
		if (BaseEntity.class.isAssignableFrom(entityClass)) {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			VaadinBeanItemContainer container = new VaadinBeanItemContainer(entityClass);
			container.setDAOProvider(daoProvider);
			container.setPersistentAutoInstantiation(true);
			return container;
		}
		return null;
	}

	@Override
	public EntityManagerFactory getEmf() {
		return this.kernelSystemEntityManagerFactory;
	}

	@Override
	public void registerForPageFlowPageChanged(IPageFlowPageChangedListener listener) {
	}

	@Override
	public void unregisterForPageFlowPageChanged(IPageFlowPageChangedListener listener) {
	}

	@Override
	public void enablePageFlowPageChangedEventHandling(boolean enable) {
	}

	@Override
	public void fireOnPageFlowChanged(PageFlowPageChangedEvent event) {
	}
	
	@SuppressWarnings("rawtypes")
	public void bindDAOProvider(IDAOProvider daoProvider, Map properties) {
		this.daoProvider = daoProvider;
	}

	@SuppressWarnings("rawtypes")
	public void unbindDAOProvider(IDAOProvider daoProvider, Map properties) {
		this.daoProvider = null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object createNonCachingPersistenceContainer(Class<?> entityClass) {
		if (this.userTransaction != null) {
			CustomNonCachingMutableLocalEntityProvider provider = new CustomNonCachingMutableLocalEntityProvider(entityClass, this.getEmf(), this.userTransaction);
			JPAContainer<?> container = new VaadinJPAContainer(entityClass, provider);
			return container;
		} else {
			return JPAContainerFactory.makeReadOnly(entityClass, this.getEmf().createEntityManager());
		}
	}
}
