package com.conx.logistics.kernel.ui.editors.entity.vaadin.ext;

public abstract interface IAbstractGridParentEventBus {
	public abstract void createItem();
	public abstract void editItem();
	public abstract void deleteItem();
	public abstract void printGrid();
	public abstract void itemSelected();
	public abstract void itemsDepleted();
}
