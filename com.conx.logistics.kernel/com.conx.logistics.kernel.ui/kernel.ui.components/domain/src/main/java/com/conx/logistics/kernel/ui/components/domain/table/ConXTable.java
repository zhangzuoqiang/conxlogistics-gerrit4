package com.conx.logistics.kernel.ui.components.domain.table;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.conx.logistics.kernel.datasource.domain.DataSource;
import com.conx.logistics.kernel.ui.components.domain.AbstractConXField;
import com.conx.logistics.kernel.ui.components.domain.masterdetail.MasterDetailComponent;

@Entity
public class ConXTable extends AbstractConXField {
	private static final long serialVersionUID = 8096124154666924743L;

	@OneToMany(mappedBy="table")
	protected Set<ColumnWidth> columnWidths = new HashSet<ColumnWidth>();
	
	//@OneToMany
	//protected Set<ColumnAlignment> columnAlignments = new HashSet<ColumnAlignment>();	
	
	@OneToMany(mappedBy="table")
	protected Set<ColumnIconPath> columnIcoPaths = new HashSet<ColumnIconPath>();	
	
    /**
     * Holds value of property pageLength. 0 disables paging.
     */
    protected int pageLength = 15;

    /**
     * Index of the first item on the current page.
     */
    protected int currentPageFirstItemIndex = 0;

    /**
     * Holds value of property selectable.
     */
    protected boolean selectable = false;

    /**
     * Should the Table footer be visible?
     */
    protected boolean columnFootersVisible = false;

    /**
     * True iff the row captions are hidden.
     */
    protected boolean rowCaptionsAreHidden = true;

    /**
     * Is table editable.
     */
    protected boolean editable = false;

    /**
     * Current sorting direction.
     */
    protected boolean sortAscending = true;

    /**
     * Is table sorting disabled alltogether; even if some of the properties
     * would be sortable.
     */
    protected boolean sortDisabled = false;
    
    /**
     * Editor for each row. Usually used when a row is double clicked.
     */
    @OneToOne
    protected MasterDetailComponent recordEditor;

	public ConXTable() {
		super("table");
	}
	
	public ConXTable(String typeId) {
		super(typeId);
	}	
	
	public ConXTable(DataSource dataSource) {
		this();
		super.setDataSource(dataSource);
	}	


	public Set<String> getVisiblePropertyids() {
		return getVisiblePropertyids();
	}

	public Set<ColumnWidth> getColumnWidths() {
		return columnWidths;
	}

	public void setColumnWidths(Set<ColumnWidth> columnWidths) {
		this.columnWidths = columnWidths;
	}

	public Set<ColumnIconPath> getColumnIcoPaths() {
		return columnIcoPaths;
	}

	public void setColumnIcoPaths(Set<ColumnIconPath> columnIcoPaths) {
		this.columnIcoPaths = columnIcoPaths;
	}

	public int getPageLength() {
		return pageLength;
	}

	public void setPageLength(int pageLength) {
		this.pageLength = pageLength;
	}

	public int getCurrentPageFirstItemIndex() {
		return currentPageFirstItemIndex;
	}

	public void setCurrentPageFirstItemIndex(int currentPageFirstItemIndex) {
		this.currentPageFirstItemIndex = currentPageFirstItemIndex;
	}

	public boolean isSelectable() {
		return selectable;
	}

	public void setSelectable(boolean selectable) {
		this.selectable = selectable;
	}

	public boolean isColumnFootersVisible() {
		return columnFootersVisible;
	}

	public void setColumnFootersVisible(boolean columnFootersVisible) {
		this.columnFootersVisible = columnFootersVisible;
	}

	public boolean isRowCaptionsAreHidden() {
		return rowCaptionsAreHidden;
	}

	public void setRowCaptionsAreHidden(boolean rowCaptionsAreHidden) {
		this.rowCaptionsAreHidden = rowCaptionsAreHidden;
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public boolean isSortAscending() {
		return sortAscending;
	}

	public void setSortAscending(boolean sortAscending) {
		this.sortAscending = sortAscending;
	}


	public boolean isSortDisabled() {
		return sortDisabled;
	}

	public void setSortDisabled(boolean sortDisabled) {
		this.sortDisabled = sortDisabled;
	}

	public MasterDetailComponent getRecordEditor() {
		return recordEditor;
	}

	public void setRecordEditor(MasterDetailComponent childEntityEditor) {
		this.recordEditor = childEntityEditor;
	}
}
