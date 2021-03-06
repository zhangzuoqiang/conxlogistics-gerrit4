package com.conx.logistics.kernel.ui.editors.entity.vaadin.mvp.detail.header;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.mvp.presenter.annotation.Presenter;

import com.conx.logistics.kernel.ui.editors.entity.vaadin.mvp.ConfigurableBasePresenter;
import com.conx.logistics.kernel.ui.editors.entity.vaadin.mvp.MultiLevelEntityEditorEventBus;
import com.conx.logistics.kernel.ui.editors.entity.vaadin.mvp.MultiLevelEntityEditorPresenter;
import com.conx.logistics.kernel.ui.editors.entity.vaadin.mvp.detail.header.view.EntityFormHeaderView;
import com.conx.logistics.kernel.ui.editors.entity.vaadin.mvp.detail.header.view.IEntityFormHeaderView;
import com.conx.logistics.kernel.ui.factory.services.IEntityEditorFactory;
import com.conx.logistics.kernel.ui.service.contribution.IMainApplication;
import com.vaadin.data.Item;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

@Presenter(view = EntityFormHeaderView.class)
public class EntityFormHeaderPresenter extends ConfigurableBasePresenter<IEntityFormHeaderView, EntityFormHeaderEventBus> {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private boolean initialized = false;
	private MultiLevelEntityEditorPresenter parentPresenter;

	private MultiLevelEntityEditorPresenter multiLevelEntityEditorPresenter;
	@SuppressWarnings("unused")
	private IMainApplication mainApplication;
	private MultiLevelEntityEditorEventBus entityEditorEventListener;

	public EntityFormHeaderPresenter() {
		super();
	}

	public MultiLevelEntityEditorPresenter getParentPresenter() {
		return parentPresenter;
	}

	public void setParentPresenter(MultiLevelEntityEditorPresenter parentPresenter) {
		this.parentPresenter = parentPresenter;
	}

	@Override
	public void bind() {
		this.setInitialized(true);
		this.getView().init();
		this.getView().addVerifyListener(new ClickListener() {
			private static final long serialVersionUID = -54023801L;

			@Override
			public void buttonClick(ClickEvent event) {
				validate();
			}
		});
		this.getView().addSaveListener(new ClickListener() {
			private static final long serialVersionUID = -52023801L;

			@Override
			public void buttonClick(ClickEvent event) {
				save();
			}
		});
		this.getView().addResetListener(new ClickListener() {
			private static final long serialVersionUID = -50023801L;

			@Override
			public void buttonClick(ClickEvent event) {
				reset();
			}
		});
	}
	
	public void validate() {
		this.getView().setVerifyEnabled(false);
		entityEditorEventListener.validateForm();
	}
	
	public void save() {
		entityEditorEventListener.saveForm();
	}
	
	public void reset() {
		this.getView().setVerifyEnabled(false);
		this.getView().setResetEnabled(false);
		entityEditorEventListener.resetForm();
	}

	public boolean isInitialized() {
		return initialized;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	@Override
	public void configure() {
		this.multiLevelEntityEditorPresenter = (MultiLevelEntityEditorPresenter) getConfig().get(IEntityEditorFactory.FACTORY_PARAM_MVP_CURRENT_MLENTITY_EDITOR_PRESENTER);
		this.mainApplication = (IMainApplication) getConfig().get(IEntityEditorFactory.FACTORY_PARAM_MAIN_APP);
		this.entityEditorEventListener = multiLevelEntityEditorPresenter.getEventBus();
	}
	
	public void onSetItemDataSource(Item item) {
	}
	
	public void onFormChanged() {
		if (this.getView().isSaveEnabled()) {
			this.getView().setSaveEnabled(false);
		}
		this.getView().setVerifyEnabled(true);
		this.getView().setResetEnabled(true);
	}
	
	public void onFormValidated() {
		this.getView().setSaveEnabled(true);
		this.getView().setVerifyEnabled(false);
	}
}
