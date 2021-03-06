package com.conx.logistics.kernel.ui.components.domain.masterdetail;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import com.conx.logistics.kernel.datasource.domain.DataSource;
import com.conx.logistics.kernel.ui.components.domain.AbstractConXComponent;
import com.conx.logistics.kernel.ui.components.domain.form.ConXForm;
import com.conx.logistics.kernel.ui.components.domain.table.ConXTable;

@Entity
public class MasterDetailComponent extends AbstractConXComponent {
	private static final long serialVersionUID = -3219294587155961706L;
	
	private boolean hasGrid;

	@ManyToOne
	private AbstractConXComponent masterComponent;
	
	@OneToOne
	private LineEditorContainerComponent lineEditorPanel;

	public MasterDetailComponent() {
		super("masterdetailcomponent");
	}
	
	public MasterDetailComponent(String code, String name, DataSource ds) {
		this(code,name);
		setDataSource(ds);
	}		
	
	public MasterDetailComponent(String code, String name) {
		this();
		setCode(code);
		setName(name);
	}	

	public MasterDetailComponent(String name,
			ConXTable table,
			LineEditorContainerComponent lineEditorPanel) {
		this();
		this.masterComponent = table;
		this.lineEditorPanel = lineEditorPanel;
	}

	public AbstractConXComponent getMasterComponent() {
		return masterComponent;
	}

	public void setMasterComponent(ConXTable masterComponent) {
		this.masterComponent = masterComponent;
		this.setDataSource(getDataSource());
		this.setHasGrid(true);
	}
	
	public void setMasterComponent(ConXForm masterComponent) {
		this.masterComponent = masterComponent;
		this.setDataSource(getDataSource());
		this.setHasGrid(false);
	}

	public LineEditorContainerComponent getLineEditorPanel() {
		return lineEditorPanel;
	}

	public void setLineEditorPanel(LineEditorContainerComponent lineEditorPanel) {
		this.lineEditorPanel = lineEditorPanel;
	}

	public boolean hasGrid() {
		return hasGrid;
	}

	public void setHasGrid(boolean hasGrid) {
		this.hasGrid = hasGrid;
	} 
}
