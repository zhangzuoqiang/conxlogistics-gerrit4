package com.conx.logistics.kernel.pageflow.ui.mvp.lineeditor.section.grid.header.view;

import org.vaadin.mvp.uibinder.annotation.UiField;

import com.conx.logistics.kernel.ui.editors.entity.vaadin.ext.EntityEditorToolStrip;
import com.conx.logistics.kernel.ui.editors.entity.vaadin.ext.EntityEditorToolStrip.EntityEditorToolStripButton;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.VerticalLayout;

public class EntityLineEditorGridHeaderView extends VerticalLayout implements IEntityLineEditorGridHeaderView {
	private static final long serialVersionUID = -8556644797413509062L;
	
	private EntityEditorToolStrip toolStrip;
	private EntityEditorToolStripButton createButton;
	private EntityEditorToolStripButton editButton;
	private EntityEditorToolStripButton deleteButton;
	private EntityEditorToolStripButton printButton;
	private EntityEditorToolStripButton reportButton;
	
	@UiField
	VerticalLayout mainLayout;

	public EntityLineEditorGridHeaderView() {
		setWidth("100%");
		setHeight("40px");
		
		this.toolStrip = new EntityEditorToolStrip();
		
		this.createButton = this.toolStrip.addToolStripButton(EntityEditorToolStrip.TOOLSTRIP_IMG_CREATE_PNG);
		this.createButton.setEnabled(false);
		
		this.editButton = this.toolStrip.addToolStripButton(EntityEditorToolStrip.TOOLSTRIP_IMG_EDIT_PNG);
		this.editButton.setEnabled(false);
		
		this.deleteButton = this.toolStrip.addToolStripButton(EntityEditorToolStrip.TOOLSTRIP_IMG_DELETE_PNG);
		this.deleteButton.setEnabled(false);
		
		this.printButton = this.toolStrip.addToolStripButton(EntityEditorToolStrip.TOOLSTRIP_IMG_PRINT_PNG);
		this.printButton.setEnabled(false);
		
		this.reportButton = this.toolStrip.addToolStripButton(EntityEditorToolStrip.TOOLSTRIP_IMG_REPORT_PNG);
		this.reportButton.setEnabled(false);
	}
	
	@Override
	public void init() {
		this.mainLayout.addComponent(this.toolStrip);
	}

	@Override
	public void addCreateListener(ClickListener listener) {
		this.createButton.addListener(listener);
	}

	@Override
	public void addEditListener(ClickListener listener) {
		this.editButton.addListener(listener);
	}

	@Override
	public void addDeleteListener(ClickListener listener) {
		this.deleteButton.addListener(listener);
	}

	@Override
	public void addPrintListener(ClickListener listener) {
		this.printButton.addListener(listener);
	}

	@Override
	public void setCreateEnabled(boolean isEnabled) {
		this.createButton.setEnabled(isEnabled);
	}

	@Override
	public boolean isCreateEnabled() {
		return this.createButton.isEnabled();
	}

	@Override
	public void setEditEnabled(boolean isEnabled) {
		this.editButton.setEnabled(isEnabled);
	}

	@Override
	public boolean isEditEnabled() {
		return this.editButton.isEnabled();
	}

	@Override
	public void setDeleteEnabled(boolean isEnabled) {
		this.deleteButton.setEnabled(isEnabled);
	}

	@Override
	public boolean isDeleteEnabled() {
		return this.deleteButton.isEnabled();
	}

	@Override
	public void setPrintEnabled(boolean isEnabled) {
		this.printButton.setEnabled(isEnabled);
	}

	@Override
	public boolean isPrintEnabled() {
		return this.printButton.isEnabled();
	}
	
	@Override
	public void setReportEnabled(boolean isEnabled) {
		this.reportButton.setEnabled(isEnabled);
	}

	@Override
	public boolean isReportEnabled() {
		return this.reportButton.isEnabled();
	}

	@Override
	public void addReportListener(ClickListener listener) {
		this.reportButton.addListener(listener);
	}
	
}
