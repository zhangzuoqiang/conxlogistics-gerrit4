package com.conx.logistics.kernel.ui.editors.entity.vaadin.mvp.lineeditor.section.form.simple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.mvp.presenter.annotation.Presenter;

import com.conx.logistics.kernel.ui.components.domain.form.ConXSimpleForm;
import com.conx.logistics.kernel.ui.editors.entity.vaadin.mvp.ConfigurableBasePresenter;
import com.conx.logistics.kernel.ui.editors.entity.vaadin.mvp.lineeditor.section.EntityLineEditorSectionEventBus;
import com.conx.logistics.kernel.ui.editors.entity.vaadin.mvp.lineeditor.section.EntityLineEditorSectionPresenter;
import com.conx.logistics.kernel.ui.editors.entity.vaadin.mvp.lineeditor.section.form.simple.view.EntityLineEditorSimpleFormView;
import com.conx.logistics.kernel.ui.editors.entity.vaadin.mvp.lineeditor.section.form.simple.view.IEntityLineEditorSimpleFormView;
import com.conx.logistics.kernel.ui.factory.services.IEntityEditorFactory;
import com.conx.logistics.kernel.ui.forms.vaadin.listeners.IFormChangeListener;
import com.conx.logistics.mdm.domain.BaseEntity;
import com.vaadin.addon.jpacontainer.EntityItem;

@Presenter(view = EntityLineEditorSimpleFormView.class)
public class EntityLineEditorSimpleFormPresenter extends ConfigurableBasePresenter<IEntityLineEditorSimpleFormView, EntityLineEditorSimpleFormEventBus> implements IFormChangeListener {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	private boolean initialized = false;
	private ConXSimpleForm formComponent;
	private BaseEntity entity;

	private EntityLineEditorSectionPresenter lineEditorSectionPresenter;
	private EntityLineEditorSectionEventBus lineEditorSectionEventBus;

	public EntityLineEditorSimpleFormPresenter() {
	}

	private void initialize() {
		this.getView().init();
		this.getView().setForm(formComponent);
		this.getView().addFormChangeListener(this);
		this.setInitialized(true);
	}

	@Override
	public void bind() {
	}

	@SuppressWarnings("rawtypes")
	public void onEntityItemEdit(EntityItem item) {
		this.setEntity((BaseEntity) item.getEntity());
		if (!isInitialized()) {
			initialize();
		}
		this.getView().setItemDataSource(item);
	}

	public void onEntityItemAdded(EntityItem<?> item) {
		// this.entityContainer.refresh();
	}

	public boolean isInitialized() {
		return initialized;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	@Override
	public void configure() {
		this.lineEditorSectionPresenter = (EntityLineEditorSectionPresenter) getConfig().get(IEntityEditorFactory.FACTORY_PARAM_MVP_LINE_EDITOR_SECTION_PRESENTER);
		this.formComponent = (ConXSimpleForm) getConfig().get(IEntityEditorFactory.FACTORY_PARAM_MVP_COMPONENT_MODEL);
		this.lineEditorSectionEventBus = lineEditorSectionPresenter.getEventBus();
	}

	public BaseEntity getEntity() {
		return entity;
	}

	public void setEntity(BaseEntity entity) {
		this.entity = entity;
	}

	@Override
	public void onFormChanged() {
		lineEditorSectionEventBus.formChanged();
	}

	public void onFormValidated() {
		lineEditorSectionEventBus.formValidated();
	}

	public void onSaveForm() {
		this.getView().saveForm();
	}

	public void onValidateForm() {
		if (this.getView().validateForm()) {
			this.onFormValidated();
		}
	}

	public void onResetForm() {
		this.getView().resetForm();
	}

	public void onResizeForm(int newHeight) {
		this.getView().resizeForm(newHeight);
	}
}
