package com.conx.logistics.kernel.pageflow.ui.mvp.lineeditor.section.refnum.view;

import java.util.Collection;

import com.conx.logistics.kernel.pageflow.ui.mvp.lineeditor.section.refnum.view.ReferenceNumberEditorView.ICreateReferenceNumberListener;
import com.conx.logistics.kernel.pageflow.ui.mvp.lineeditor.section.refnum.view.ReferenceNumberEditorView.ISaveReferenceNumberListener;
import com.conx.logistics.kernel.ui.editors.entity.vaadin.mvp.IEntityEditorComponentView;
import com.conx.logistics.kernel.ui.forms.vaadin.FormMode;
import com.conx.logistics.kernel.ui.forms.vaadin.listeners.IFormChangeListener;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.ui.Layout;

public interface IReferenceNumberEditorView extends IEntityEditorComponentView {
	public Layout getMainLayout();
	public void setItemDataSource(Item item, FormMode mode);
	public void setContainerDataSource(Container container, Collection<?> visibleGridColumns, Collection<?> visibleFormFields);
	public void showContent();
	public void hideContent();
	public void showDetail();
	public void hideDetail();
	public void addCreateReferenceNumberListener(ICreateReferenceNumberListener listener);
	public void addSaveReferenceNumberListener(ISaveReferenceNumberListener listener);
	public void addFormChangeListener(IFormChangeListener listener);
}