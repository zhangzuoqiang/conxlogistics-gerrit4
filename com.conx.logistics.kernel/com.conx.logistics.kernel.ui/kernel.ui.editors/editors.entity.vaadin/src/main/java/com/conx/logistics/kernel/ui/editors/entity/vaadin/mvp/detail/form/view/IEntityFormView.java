package com.conx.logistics.kernel.ui.editors.entity.vaadin.mvp.detail.form.view;

import java.util.Collection;

import com.conx.logistics.kernel.ui.components.domain.form.ConXDetailForm;
import com.conx.logistics.kernel.ui.editors.entity.vaadin.mvp.IEntityEditorComponentView;
import com.conx.logistics.kernel.ui.forms.vaadin.listeners.IFormChangeListener;
import com.vaadin.data.Item;

public interface IEntityFormView extends IEntityEditorComponentView {
	public void setItemDataSource(Item item);
	public void setItemDataSource(Item item, Collection<?> propertyIds);
	public void setForm(ConXDetailForm formComponent);
	public void setFormTitle(String title);
	public void addFormChangeListener(IFormChangeListener listener);
	public void saveForm();
	public boolean validateForm();
	public void resetForm();
	public void resizeForm(int height);
}