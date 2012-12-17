package com.conx.logistics.kernel.ui.editors.entity.vaadin.mvp.search.grid.view;

import com.conx.logistics.kernel.ui.editors.entity.vaadin.ext.table.EntityEditorGrid.IDepletedListener;
import com.conx.logistics.kernel.ui.editors.entity.vaadin.ext.table.EntityEditorGrid.IEditListener;
import com.conx.logistics.kernel.ui.editors.entity.vaadin.ext.table.EntityEditorGrid.ISelectListener;
import com.conx.logistics.kernel.ui.editors.entity.vaadin.mvp.IEntityEditorComponentView;
import com.vaadin.data.Container;
import com.vaadin.data.Item;

public interface IEntityGridView  extends IEntityEditorComponentView {
	public void setContainerDataSource(Container container);
	public void setVisibleColumns(Object[] columnIds);
	public void setVisibleColumnNames(String[] columnNames);
	public void addEditListener(IEditListener listener);
	public void addSelectListener(ISelectListener listener);
	public void addDepletedListener(IDepletedListener listener);
	public void deleteItem(Item item) throws Exception;
	public void printGrid();
	public Item getSelectedItem();
}