package com.conx.logistics.kernel.ui.editors.entity.vaadin.ext.refNum;

import java.util.Collection;

import com.conx.logistics.kernel.ui.forms.vaadin.FormMode;
import com.conx.logistics.kernel.ui.forms.vaadin.impl.VaadinFormHeader;
import com.vaadin.ui.Form;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

public class ReferenceNumberEditorForm extends Form {
	private static final long serialVersionUID = -1298561365125628304L;

	private VaadinFormHeader header;
	private VerticalLayout layout;
	private GridLayout innerLayout;
	private Panel innerLayoutPanel;
	
	private FormMode mode;
	
	public ReferenceNumberEditorForm() {
		this.header = new VaadinFormHeader();
		this.layout = new VerticalLayout();
		this.innerLayout = new GridLayout(4, 1);
		this.innerLayoutPanel = new Panel();
		
		initialize();
	}
	
	@SuppressWarnings("deprecation")
	private void initialize() {
		this.header = new VaadinFormHeader();
		this.header.setTitle("Reference Number");
		
		this.innerLayout.setWidth("100%");
		this.innerLayout.setSpacing(true);
		this.innerLayout.setMargin(true);
		this.innerLayout.setHeight(-1, UNITS_PIXELS);
		
		this.innerLayoutPanel = new Panel();
		this.innerLayoutPanel.setSizeFull();
		this.innerLayoutPanel.getLayout().setMargin(false, true, false, true);
		this.innerLayoutPanel.setStyleName("light");
		this.innerLayoutPanel.addComponent(innerLayout);
		
		this.layout.setWidth("100%");
		this.layout.setStyleName("conx-entity-editor-form");
		this.layout.addComponent(header);
		this.layout.addComponent(innerLayoutPanel);
		this.layout.setExpandRatio(innerLayoutPanel, 1.0f);
		
		setMode(FormMode.CREATING);
		setLayout(layout);
		// False so that commit() must be called explicitly
		setWriteThrough(false);
		// Disallow invalid data from acceptance by the container
		setInvalidCommitted(false);
	}
	
	@Override
	protected void attachField(Object propertyId, com.vaadin.ui.Field field) {
		if (propertyId == null || field == null) {
            return;
        }
        innerLayout.addComponent(field);
	}
	
	public FormMode getMode() {
		return mode;
	}
	
	public void setMode(FormMode mode) {
		this.mode = mode;
		switch (mode) {
		case CREATING:
			this.header.setAction("Creating");
			break;
		case EDITING:
			this.header.setAction("Editing");
			break;
		}
	}
	
	@Override
	public void setItemDataSource(com.vaadin.data.Item newDataSource, Collection<?> propertyIds) {
		innerLayout.removeAllComponents();
		super.setItemDataSource(newDataSource, propertyIds);
	}
}
