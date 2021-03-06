package com.conx.logistics.kernel.ui.common.mvp;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.mvp.eventbus.EventBus;
import org.vaadin.mvp.eventbus.EventBusManager;
import org.vaadin.mvp.presenter.IPresenter;
import org.vaadin.mvp.presenter.IPresenterFactory;
import org.vaadin.mvp.presenter.PresenterFactory;
import org.vaadin.mvp.uibinder.UiBinderException;

import com.conx.logistics.common.utils.Validator;
import com.conx.logistics.kernel.documentlibrary.remote.services.IRemoteDocumentRepository;
import com.conx.logistics.kernel.pageflow.services.IPageFlowManager;
import com.conx.logistics.kernel.persistence.services.IEntityManagerFactoryManager;
import com.conx.logistics.kernel.portal.remote.services.IPortalOrganizationService;
import com.conx.logistics.kernel.portal.remote.services.IPortalRoleService;
import com.conx.logistics.kernel.portal.remote.services.IPortalUserService;
import com.conx.logistics.kernel.ui.common.entityprovider.jta.CustomNonCachingMutableLocalEntityProvider;
import com.conx.logistics.kernel.ui.common.ui.menu.app.AppMenuEntry;
import com.conx.logistics.kernel.ui.factory.services.IEntityEditorFactory;
import com.conx.logistics.kernel.ui.factory.services.data.IDAOProvider;
import com.conx.logistics.kernel.ui.service.IUIContributionManager;
import com.conx.logistics.kernel.ui.service.contribution.IActionContribution;
import com.conx.logistics.kernel.ui.service.contribution.IApplicationViewContribution;
import com.conx.logistics.kernel.ui.service.contribution.IMainApplication;
import com.conx.logistics.kernel.ui.service.contribution.IViewContribution;
import com.conx.logistics.mdm.dao.services.IEntityMetadataDAOService;
import com.conx.logistics.mdm.dao.services.documentlibrary.IFolderDAOService;
import com.conx.logistics.mdm.domain.user.User;
import com.conx.logistics.reporting.remote.services.IReportGenerator;
import com.vaadin.Application;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.util.EntityManagerPerRequestHelper;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;

@SuppressWarnings("serial")
public class MainMVPApplication extends Application implements IMainApplication, HttpServletRequestListener {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	private PresenterFactory presenterFactory = null;
	private IPresenter<?, ? extends EventBus> mainPresenter;
	private User currentUser = null;
	private EntityManagerPerRequestHelper entityManagerPerRequestHelper;
	private HashMap<String, Object> entityEditorFactoryParams;

	@Autowired
	private IUIContributionManager contributionManager;
	@Autowired
	private IPageFlowManager pageFlowEngine;
	@Autowired
	private IDAOProvider daoProvider;
	@Autowired
	private IPortalUserService portalUserService;
	@Autowired
	private IPortalRoleService portalRoleService;
	@Autowired
	private IPortalOrganizationService portalOrganizationService;
	@Autowired
	private IEntityEditorFactory entityEditorFactory;
	@Autowired
	private IEntityManagerFactoryManager entityManagerFactoryManager;
	@Autowired
	private UserTransaction userTransaction;
	@Autowired
	private IReportGenerator reportingGenerator;

	@Override
	public void init() {
		try {
			setTheme("conx");

			// Presenter factory
			this.presenterFactory = new PresenterFactory(new EventBusManager(), getLocale());
			this.presenterFactory.setApplication(this);

			// Create container manager/helper
			this.entityManagerPerRequestHelper = new EntityManagerPerRequestHelper();

			// request an instance of MainPresenter
			mainPresenter = this.presenterFactory.createPresenter(MainPresenter.class);
			
			// Create EntityFactory Presenter params
			((MainEventBus) mainPresenter.getEventBus()).start(this);
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String stacktrace = sw.toString();
			logger.error(stacktrace);
		}

	}

	public Map<String, Object> provideEntityEditorFactoryParams() {
		if (this.entityEditorFactoryParams == null) {
			this.entityEditorFactoryParams = new HashMap<String, Object>();
			this.entityEditorFactoryParams = new HashMap<String, Object>();
			this.entityEditorFactoryParams.put(IEntityEditorFactory.FACTORY_PARAM_MVP_EVENTBUS_MANAGER, this.presenterFactory.getEventBusManager());
			this.entityEditorFactoryParams.put(IEntityEditorFactory.FACTORY_PARAM_MVP_LOCALE, getLocale());
			this.entityEditorFactoryParams.put(IEntityEditorFactory.FACTORY_PARAM_MVP_ENTITYMANAGERPERREQUESTHELPER,
					this.entityManagerPerRequestHelper);
			
			this.entityEditorFactoryParams.put(IEntityEditorFactory.FACTORY_PARAM_MVP_ENTITY_MANAGER_FACTORY, this.entityManagerFactoryManager.getKernelSystemEmf());
			this.entityEditorFactoryParams.put(IEntityEditorFactory.FACTORY_PARAM_IDOCLIB_REPO_SERVICE, this.daoProvider.provideByDAOClass(IRemoteDocumentRepository.class));
			this.entityEditorFactoryParams.put(IEntityEditorFactory.FACTORY_PARAM_IFOLDER_SERVICE, this.daoProvider.provideByDAOClass(IFolderDAOService.class));
			this.entityEditorFactoryParams.put(IEntityEditorFactory.FACTORY_PARAM_MAIN_APP, this);
			this.entityEditorFactoryParams.put(IEntityEditorFactory.FACTORY_PARAM_IENTITY_METADATA_SERVICE, this.daoProvider.provideByDAOClass(IEntityMetadataDAOService.class));
		}
		
		return this.entityEditorFactoryParams;
	}

	/**
	 * 
	 * HttpServletRequestListener
	 * 
	 */
	@SuppressWarnings("rawtypes")
	@Override
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
				screenName = "test";
				currentUser = new User();
				currentUser.setEmailAddress(email);
				currentUser.setScreenName(screenName);
			}
		}

		// Start request helper
		if (this.entityManagerPerRequestHelper != null)// Init called already
			this.entityManagerPerRequestHelper.requestStart();
	}

	// private boolean authenticate(HttpServletRequest req) {
	// String authhead = req.getHeader("Authorization");
	//
	// if (authhead != null) {
	// // *****Decode the authorisation String*****
	// String usernpass = Base64.decode(authhead.substring(6));
	// // *****Split the username from the password*****
	// String user = usernpass.substring(0, usernpass.indexOf(":"));
	// String password = usernpass.substring(usernpass.indexOf(":") + 1);
	//
	// if (user.equals("user") && password.equals("pass"))
	// return true;
	// }
	//
	// return false;
	// }

	@Override
	public void onRequestEnd(HttpServletRequest request, HttpServletResponse response) {
		if (this.entityManagerPerRequestHelper != null)// Init called already
			this.entityManagerPerRequestHelper.requestEnd();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object createPersistenceContainer(Class entityClass) {
		CustomNonCachingMutableLocalEntityProvider provider = new CustomNonCachingMutableLocalEntityProvider(entityClass, this.entityManagerFactoryManager.getKernelSystemEmf(), this.userTransaction);
		JPAContainer<?> container = new JPAContainer(entityClass);
		container.setEntityProvider(provider);
		return container;
	}

	public IPresenterFactory getPresenterFactory() {
		return this.presenterFactory;
	}

	public AppMenuEntry[] createAppMenuEntries() throws UiBinderException {
		// TODO Why are we not using collections here?
		IApplicationViewContribution[] appContributions = this.contributionManager.getCurrentApplicationContributions();
		if (appContributions.length == 0) {
			return new AppMenuEntry[] {};
		} else {
			ArrayList<AppMenuEntry> entries = new ArrayList<AppMenuEntry>();
			for (IApplicationViewContribution ac : appContributions) {
				entries.add(new AppMenuEntry(ac.getCode(), ac.getName(), ac.getIcon(), ac.getApplicationComponent(this), ac.getPresenterClass()));
			}
			return entries.toArray(new AppMenuEntry[] {});
		}
	}

	// private void initAppService() {
	// if (Validator.isNotNull(this.kernelSystemTransManager) &&
	// Validator.isNotNull(this.applicationDAOService)) {
	// DefaultTransactionDefinition def = new DefaultTransactionDefinition();
	// // explicitly setting the transaction name is something that can
	// // only be done programmatically
	// def.setName("sendFlightScheduleUpdate");
	// def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
	// TransactionStatus status =
	// this.kernelSystemTransManager.getTransaction(def);
	// try {
	// this.applicationDAOService.provideControlPanelApplication();
	// this.kernelSystemTransManager.commit(status);
	// } catch (Exception e) {
	// StringWriter sw = new StringWriter();
	// e.printStackTrace(new PrintWriter(sw));
	// String stacktrace = sw.toString();
	// logger.error(stacktrace);
	//
	// this.kernelSystemTransManager.rollback(status);
	// }
	// appServiceInititialized = true;
	// }
	// }

	// public void bindApplicationDAOService(IApplicationDAOService
	// applicationDAOService, Map properties) {
	// logger.debug("bindApplicationDAOService()");
	// this.applicationDAOService = applicationDAOService;
	// if (!appServiceInititialized)
	// initAppService();
	// }
	//
	// public void unbindApplicationDAOService(IApplicationDAOService
	// applicationDAOService, Map properties) {
	// logger.debug("unbindApplicationDAOService()");
	// this.applicationDAOService = null;
	// appServiceInititialized = false;
	// }
	//
	// public void bindFeatureDAOService(IFeatureDAOService featureDAOService,
	// Map properties) {
	// logger.debug("bindFeatureDAOService()");
	// this.featureDAOService = featureDAOService;
	// if (!appServiceInititialized)
	// initAppService();
	// }
	//
	// public void unbindFeatureDAOService(IFeatureDAOService featureDAOService,
	// Map properties) {
	// logger.debug("unbindFeatureDAOService()");
	// this.featureDAOService = null;
	// appServiceInititialized = false;
	// }
	//
	// public void unbindPageFlowEngine(IPageFlowManager pageflowEngine, Map
	// properties) {
	// logger.debug("unbindPageFlowEngine()");
	// this.setPageFlowEngine(null);
	// }
	//
	// public void bindPageFlowEngine(IPageFlowManager pageflowEngine, Map
	// properties) {
	// logger.debug("bindPageFlowEngine()");
	// this.setPageFlowEngine(pageflowEngine);
	// // this.pageFlowEngine.setMainApplication(this);
	// }
	//
	// public void bindKernelSystemTransManager(PlatformTransactionManager
	// kernelSystemTransManager, Map properties) {
	// logger.debug("bindKernelSystemTransManager()");
	// this.kernelSystemTransManager = kernelSystemTransManager;
	// if (!appServiceInititialized)
	// initAppService();
	// }
	//
	// public void unbindKernelSystemTransManager(PlatformTransactionManager
	// kernelSystemTransManager, Map properties) {
	// logger.debug("unbindKernelSystemTransManager()");
	// this.kernelSystemTransManager = null;
	// appServiceInititialized = false;
	// }
	//
	// public void bindUserTransaction(UserTransaction userTransaction, Map
	// properties) {
	// logger.debug("bindUserTransaction()");
	// this.userTransaction = userTransaction;
	// }
	//
	// public void unbindUserTransaction(UserTransaction userTransaction, Map
	// properties) {
	// logger.debug("unbindUserTransaction()");
	// this.userTransaction = null;
	// }
	//
	// public void bindKernelSystemEntityManagerFactory(EntityManagerFactory
	// kernelSystemEntityManagerFactory, Map properties) {
	// logger.debug("bindKernelSystemEntityManagerFactory()");
	// this.entityTypeContainerFactory = new
	// EntityTypeContainerFactory(kernelSystemEntityManagerFactory.createEntityManager());
	// this.kernelSystemEntityManagerFactory = kernelSystemEntityManagerFactory;
	// }
	//
	// public void
	// unbindKernelSystemEntityManagerFactory(PlatformTransactionManager
	// kernelSystemTransManager, Map properties) {
	// logger.debug("unbindKernelSystemEntityManagerFactory()");
	// this.entityTypeContainerFactory = null;
	// this.kernelSystemEntityManagerFactory = null;
	// }
	//
	// public void bindConxJbpmEntityManagerFactory(EntityManagerFactory
	// conxJBpmEntityManagerFactory, Map properties) {
	// logger.debug("bindConxJbpmEntityManagerFactory()");
	// this.conxJBpmEntityManagerFactory = conxJBpmEntityManagerFactory;
	// }
	//
	// public void unbindConxJbpmEntityManagerFactory(PlatformTransactionManager
	// conxJBpmEntityManagerFactory, Map properties) {
	// logger.debug("unbindConxJbpmEntityManagerFactory()");
	// this.conxJBpmEntityManagerFactory = null;
	// }
	//
	// public void bindConxHumanTaskEntityManagerFactory(EntityManagerFactory
	// conxHumanTaskEntityManagerFactory, Map properties) {
	// logger.debug("bindConxHumanTaskEntityManagerFactory()");
	// this.conxHumanTaskEntityManagerFactory =
	// conxHumanTaskEntityManagerFactory;
	// }
	//
	// public void
	// unbindConxHumanTaskEntityManagerFactory(PlatformTransactionManager
	// conxHumanTaskEntityManagerFactory, Map properties) {
	// logger.debug("unbindConxHumanTaskntityManagerFactory()");
	// this.conxHumanTaskEntityManagerFactory = null;
	// }
	//
	// public void bindRemoteDocumentRepository(IRemoteDocumentRepository
	// remoteDocumentRepository, Map properties) {
	// logger.debug("bindRemoteDocumentRepository()");
	// this.remoteDocumentRepository = remoteDocumentRepository;
	// }
	//
	// public void unbindRemoteDocumentRepository(IRemoteDocumentRepository
	// remoteDocumentRepository, Map properties) {
	// logger.debug("unbindRemoteDocumentRepository()");
	// this.remoteDocumentRepository = null;
	// }
	//
	// public void bindFolderDAOService(IFolderDAOService folderDAOService, Map
	// properties) {
	// logger.debug("bindFolderDAOService()");
	// this.folderDAOService = folderDAOService;
	// }
	//
	// public void unbindFolderDAOService(IFolderDAOService folderDAOService,
	// Map properties) {
	// logger.debug("unbindFolderDAOService()");
	// this.folderDAOService = null;
	// }

	// @SuppressWarnings("rawtypes")
	// public EventBus createEventBuss(Class eventBusClass, IPresenter
	// presenter) {
	// return ebm.register(eventBusClass, presenter);
	// }

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

	public IDAOProvider getDaoProvider() {
		return daoProvider;
	}

	public void setDaoProvider(IDAOProvider daoProvider) {
		this.daoProvider = daoProvider;
	}

	public IPageFlowManager getPageFlowEngine() {
		return pageFlowEngine;
	}

	public void setPageFlowEngine(IPageFlowManager pageFlowEngine) {
		this.pageFlowEngine = pageFlowEngine;
	}

	@SuppressWarnings("rawtypes")
	public void bindDAOProvider(IDAOProvider daoProvider, Map properties) {
		this.daoProvider = daoProvider;
	}

	@SuppressWarnings("rawtypes")
	public void unbindDAOProvider(IDAOProvider daoProvider, Map properties) {
		this.daoProvider = null;
	}

	public void setContributionManager(IUIContributionManager contributionManager) {
		this.contributionManager = contributionManager;
	}

	@Override
	public IUIContributionManager getUiContributionManager() {
		return this.contributionManager;
	}

	@Override
	public IApplicationViewContribution getApplicationContributionByCode(String code) {
		return this.contributionManager.getApplicationContributionByCode(this, code);
	}

	@Override
	public IViewContribution getViewContributionByCode(String code) {
		return this.contributionManager.getViewContributionByCode(this, code);
	}

	public IActionContribution getActionContributionByCode(String code) {
		return this.contributionManager.getActionContributionByCode(this, code);
	}

	public IEntityEditorFactory getEntityEditorFactory() {
		return entityEditorFactory;
	}

	public IEntityManagerFactoryManager getEntityManagerFactoryManager() {
		return entityManagerFactoryManager;
	}
	
	public IPresenter<?, ? extends EventBus> getMainPresenter() {
		return this.mainPresenter;
	}
	
	public UserTransaction getUserTransaction() {
		return userTransaction;
	}

	public void setUserTransaction(UserTransaction userTransaction) {
		this.userTransaction = userTransaction;
	}

	public IReportGenerator getReportingGenerator() {
		return reportingGenerator;
	}

	public void setReportingGenerator(IReportGenerator reportingGenerator) {
		this.reportingGenerator = reportingGenerator;
	}

	@Override
	public String getReportingUrl() {
		assert (this.reportingGenerator != null) : "The reporting service was null";
		String baseUrl = getURL().getProtocol() + "://" + getURL().getHost();
		if (getURL().getPort() != -1) {
			baseUrl += ":" + getURL().getPort();
		}
		
		return this.reportingGenerator.getUrlPathForPDFGenerator(baseUrl);
	}
}