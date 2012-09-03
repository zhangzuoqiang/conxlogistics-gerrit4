package com.conx.logistics.kernel.ui.editors.entity.vaadin.mvp.attachment.view;

import java.util.Collection;

import com.conx.logistics.kernel.ui.editors.entity.vaadin.ext.FormMode;
import com.conx.logistics.kernel.ui.editors.entity.vaadin.mvp.IEntityEditorComponentView;
import com.conx.logistics.kernel.ui.editors.entity.vaadin.mvp.attachment.view.AttachmentEditorView.ICreateAttachmentListener;
import com.conx.logistics.kernel.ui.editors.entity.vaadin.mvp.attachment.view.AttachmentEditorView.IInspectAttachmentListener;
import com.conx.logistics.kernel.ui.editors.entity.vaadin.mvp.attachment.view.AttachmentEditorView.ISaveAttachmentListener;
import com.vaadin.addon.jpacontainer.EntityItem;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.ui.Button;

public interface IAttachmentEditorView extends IEntityEditorComponentView {
	public void setItemDataSource(Item item, FormMode mode);
	public void setContainerDataSource(Container container, Collection<?> visibleGridColumns, Collection<?> visibleFormFields);
	public void showContent();
	public void hideContent();
	public void showDetail();
	public void hideDetail();
	public void addCreateAttachmentListener(ICreateAttachmentListener listener);
	public void addSaveAttachmentListener(ISaveAttachmentListener listener);
	public void addInspectAttachmentListener(IInspectAttachmentListener listener);
	
	public void addItemClickListener(ItemClickListener clickListener);
	public void entityItemSingleClicked(EntityItem item);
	
	public void addNewItemListener(Button.ClickListener clickListener);
	public void newEntityItemActioned();
}