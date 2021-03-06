package com.conx.logistics.kernel.ui.editors.builder.vaadin;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.vaadin.mvp.eventbus.EventBus;
import org.vaadin.mvp.eventbus.EventBusManager;
import org.vaadin.mvp.presenter.IPresenter;

import com.conx.logistics.kernel.documentlibrary.remote.services.IRemoteDocumentRepository;
import com.conx.logistics.kernel.ui.components.domain.AbstractConXComponent;
import com.conx.logistics.kernel.ui.components.domain.attachment.AttachmentEditorComponent;
import com.conx.logistics.kernel.ui.components.domain.form.ConXCollapseableSectionForm;
import com.conx.logistics.kernel.ui.components.domain.form.ConXDetailForm;
import com.conx.logistics.kernel.ui.components.domain.form.ConXSimpleForm;
import com.conx.logistics.kernel.ui.components.domain.masterdetail.LineEditorComponent;
import com.conx.logistics.kernel.ui.components.domain.masterdetail.MasterDetailComponent;
import com.conx.logistics.kernel.ui.components.domain.note.NoteEditorComponent;
import com.conx.logistics.kernel.ui.components.domain.referencenumber.ReferenceNumberEditorComponent;
import com.conx.logistics.kernel.ui.components.domain.table.ConXDetailTable;
import com.conx.logistics.kernel.ui.components.domain.table.ConXTable;
import com.conx.logistics.kernel.ui.editors.entity.vaadin.mvp.ConfigurablePresenterFactory;
import com.conx.logistics.kernel.ui.editors.entity.vaadin.mvp.MultiLevelEntityEditorEventBus;
import com.conx.logistics.kernel.ui.editors.entity.vaadin.mvp.MultiLevelEntityEditorPresenter;
import com.conx.logistics.kernel.ui.editors.entity.vaadin.mvp.attachment.AttachmentEditorPresenter;
import com.conx.logistics.kernel.ui.editors.entity.vaadin.mvp.customizer.ConfigurablePresenterFactoryCustomizer;
import com.conx.logistics.kernel.ui.editors.entity.vaadin.mvp.detail.form.EntityFormPresenter;
import com.conx.logistics.kernel.ui.editors.entity.vaadin.mvp.lineeditor.section.EntityLineEditorSectionPresenter;
import com.conx.logistics.kernel.ui.editors.entity.vaadin.mvp.lineeditor.section.form.collapsible.EntityLineEditorCollapsibleFormPresenter;
import com.conx.logistics.kernel.ui.editors.entity.vaadin.mvp.lineeditor.section.form.simple.EntityLineEditorSimpleFormPresenter;
import com.conx.logistics.kernel.ui.editors.entity.vaadin.mvp.lineeditor.section.grid.EntityLineEditorGridPresenter;
import com.conx.logistics.kernel.ui.editors.entity.vaadin.mvp.notes.NotesEditorPresenter;
import com.conx.logistics.kernel.ui.editors.entity.vaadin.mvp.refnum.ReferenceNumberEditorPresenter;
import com.conx.logistics.kernel.ui.editors.entity.vaadin.mvp.search.grid.EntityGridPresenter;
import com.conx.logistics.kernel.ui.factory.services.IEntityEditorFactory;

@Transactional
@Repository
public class VaadinEntityEditorFactoryImpl implements IEntityEditorFactory {

	@SuppressWarnings("unused")
	private IRemoteDocumentRepository remoteDocumentRepository;

	private ConfigurablePresenterFactory factory;

	public VaadinEntityEditorFactoryImpl() {
	}

	public VaadinEntityEditorFactoryImpl(ConfigurablePresenterFactory factory) {
		this.factory = factory;
	}

	@Override
	public Map<IPresenter<?, ? extends EventBus>, EventBus> create(AbstractConXComponent conXComponent, Map<String, Object> params) {
		Map<IPresenter<?, ? extends EventBus>, EventBus> res = null;

		if (conXComponent instanceof AttachmentEditorComponent) {
			params.put(IEntityEditorFactory.FACTORY_PARAM_MVP_COMPONENT_MODEL, conXComponent);
			final IPresenter<?, ? extends EventBus> presenter = factory.createPresenter(AttachmentEditorPresenter.class);
			final EventBus eventBus = presenter.getEventBus();

			res = new HashMap<IPresenter<?, ? extends EventBus>, EventBus>();
			res.put(presenter, eventBus);
		} else if (conXComponent instanceof NoteEditorComponent) {
			params.put(IEntityEditorFactory.FACTORY_PARAM_MVP_COMPONENT_MODEL, conXComponent);
			IPresenter<?, ? extends EventBus> presenter = factory.createPresenter(NotesEditorPresenter.class);
			EventBus eventBus = presenter.getEventBus();

			res = new HashMap<IPresenter<?, ? extends EventBus>, EventBus>();
			res.put(presenter, eventBus);
		} else if (conXComponent instanceof ReferenceNumberEditorComponent) {
			params.put(IEntityEditorFactory.FACTORY_PARAM_MVP_COMPONENT_MODEL, conXComponent);
			IPresenter<?, ? extends EventBus> presenter = factory.createPresenter(ReferenceNumberEditorPresenter.class);
			EventBus eventBus = presenter.getEventBus();

			res = new HashMap<IPresenter<?, ? extends EventBus>, EventBus>();
			res.put(presenter, eventBus);
		} else if (conXComponent instanceof MasterDetailComponent) {
			params.put(IEntityEditorFactory.FACTORY_PARAM_MVP_COMPONENT_MODEL, conXComponent);
			ConfigurablePresenterFactory presenterFactory = null;
			EventBusManager ebm = (EventBusManager) params.get(IEntityEditorFactory.FACTORY_PARAM_MVP_EVENTBUS_MANAGER);

			if (params.containsKey(IEntityEditorFactory.FACTORY_PARAM_MVP_PRESENTER_FACTORY)) {
				presenterFactory = (ConfigurablePresenterFactory) params.get(IEntityEditorFactory.FACTORY_PARAM_MVP_PRESENTER_FACTORY);
			} else {
				ebm = (EventBusManager) params.get(IEntityEditorFactory.FACTORY_PARAM_MVP_EVENTBUS_MANAGER);
				Locale locale = (Locale) params.get(IEntityEditorFactory.FACTORY_PARAM_MVP_LOCALE);
				presenterFactory = new ConfigurablePresenterFactory(ebm, locale);
				presenterFactory.setCustomizer(new ConfigurablePresenterFactoryCustomizer(params));
				params.put(IEntityEditorFactory.FACTORY_PARAM_MVP_PRESENTER_FACTORY, presenterFactory);
			}

			final IPresenter<?, ? extends EventBus> mainPresenter = presenterFactory.createPresenter(MultiLevelEntityEditorPresenter.class);
			final MultiLevelEntityEditorEventBus mainEventBus = (MultiLevelEntityEditorEventBus) mainPresenter.getEventBus();

			res = new HashMap<IPresenter<?, ? extends EventBus>, EventBus>();
			res.put(mainPresenter, mainEventBus);
		} else if (conXComponent instanceof ConXDetailForm) {
			params.put(IEntityEditorFactory.FACTORY_PARAM_MVP_COMPONENT_MODEL, conXComponent);
			IPresenter<?, ? extends EventBus> presenter = factory.createPresenter(EntityFormPresenter.class);
			EventBus eventBus = presenter.getEventBus();

			res = new HashMap<IPresenter<?, ? extends EventBus>, EventBus>();
			res.put(presenter, eventBus);
		} else if (conXComponent instanceof ConXCollapseableSectionForm) {
			params.put(IEntityEditorFactory.FACTORY_PARAM_MVP_COMPONENT_MODEL, conXComponent);
			IPresenter<?, ? extends EventBus> presenter = factory.createPresenter(EntityLineEditorCollapsibleFormPresenter.class);
			EventBus eventBus = presenter.getEventBus();

			res = new HashMap<IPresenter<?, ? extends EventBus>, EventBus>();
			res.put(presenter, eventBus);
		} else if (conXComponent instanceof ConXSimpleForm) {
			params.put(IEntityEditorFactory.FACTORY_PARAM_MVP_COMPONENT_MODEL, conXComponent);
			IPresenter<?, ? extends EventBus> presenter = factory.createPresenter(EntityLineEditorSimpleFormPresenter.class);
			EventBus eventBus = presenter.getEventBus();

			res = new HashMap<IPresenter<?, ? extends EventBus>, EventBus>();
			res.put(presenter, eventBus);
		} else if (conXComponent instanceof ConXDetailTable) {
			params.put(IEntityEditorFactory.FACTORY_PARAM_MVP_COMPONENT_MODEL, conXComponent);
			IPresenter<?, ? extends EventBus> presenter = factory.createPresenter(EntityLineEditorGridPresenter.class);
			EventBus eventBus = presenter.getEventBus();

			res = new HashMap<IPresenter<?, ? extends EventBus>, EventBus>();
			res.put(presenter, eventBus);
		} else if (conXComponent instanceof ConXTable) {
			params.put(IEntityEditorFactory.FACTORY_PARAM_MVP_COMPONENT_MODEL, conXComponent);
			IPresenter<?, ? extends EventBus> presenter = factory.createPresenter(EntityGridPresenter.class);
			EventBus eventBus = presenter.getEventBus();

			res = new HashMap<IPresenter<?, ? extends EventBus>, EventBus>();
			res.put(presenter, eventBus);
		}  else if (conXComponent instanceof LineEditorComponent) {
			params.put(IEntityEditorFactory.FACTORY_PARAM_MVP_COMPONENT_MODEL, conXComponent);
			IPresenter<?, ? extends EventBus> presenter = factory.createPresenter(EntityLineEditorSectionPresenter.class);
			EventBus eventBus = presenter.getEventBus();

			res = new HashMap<IPresenter<?, ? extends EventBus>, EventBus>();
			res.put(presenter, eventBus);
		}
		return res;
	}

	public void setRemoteDocumentRepository(IRemoteDocumentRepository remoteDocumentRepository) {
		this.remoteDocumentRepository = remoteDocumentRepository;
	}
}
