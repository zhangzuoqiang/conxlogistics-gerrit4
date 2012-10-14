package com.conx.logistics.kernel.pageflow.ui.mvp.lineeditor.section.grid;

import java.util.Map;

import org.vaadin.mvp.eventbus.EventBus;
import org.vaadin.mvp.eventbus.annotation.Event;

import com.vaadin.data.Item;

public interface EntityLineEditorGridEventBus extends EventBus {
	@Event(handlers = { EntityLineEditorGridPresenter.class })
	public void configure(Map<String, Object> params);
	
	@Event(handlers = { EntityLineEditorGridPresenter.class })
	public void delete(Item item);
}