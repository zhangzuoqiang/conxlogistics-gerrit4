package com.conx.logistics.kernel.pageflow.ui.ext.grid;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.conx.logistics.kernel.pageflow.ui.builder.VaadinPageDataBuilder;
import com.conx.logistics.kernel.ui.components.domain.table.EntityMatchGrid;
import com.conx.logistics.kernel.ui.editors.entity.vaadin.ext.EntityEditorToolStrip;
import com.conx.logistics.kernel.ui.editors.entity.vaadin.ext.EntityEditorToolStrip.EntityEditorToolStripButton;
import com.conx.logistics.kernel.ui.editors.entity.vaadin.ext.table.EntityEditorGrid;
import com.conx.logistics.kernel.ui.editors.entity.vaadin.ext.table.EntityEditorGrid.ISelectListener;
import com.conx.logistics.kernel.ui.factory.services.data.IDAOProvider;
import com.conx.logistics.kernel.ui.forms.vaadin.impl.VaadinFormAlertPanel;
import com.conx.logistics.kernel.ui.forms.vaadin.impl.VaadinFormAlertPanel.AlertType;
import com.conx.logistics.kernel.ui.vaadin.common.ConXVerticalSplitPanel;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerItem;
import com.vaadin.data.Container;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.terminal.UserError;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.VerticalLayout;

public class VaadinMatchGrid extends VerticalLayout {
	private static final long serialVersionUID = 7528472251924397821L;

	private EntityEditorToolStrip unmatchedToolStrip;
	private EntityEditorToolStrip matchedToolStrip;
	private EntityEditorToolStripButton newMatchedItemButton;
	private EntityEditorToolStripButton unmatchButton;
	private EntityEditorToolStripButton matchButton;
	private EntityEditorGrid unmatchedGrid;
	private Item unmatchedGridItem;
	private EntityEditorGrid matchedGrid;
	private Item matchedGridItem;
	private EntityMatchGrid componentModel;
	private Set<IMatchListener> subscribers;
	private Map<Item, Filter> unmatchedItemIdFilterMap;
	private ConXVerticalSplitPanel content;
	private VaadinFormAlertPanel unmatchedAlertPanel;
	private VaadinFormAlertPanel matchedAlertPanel;
	private Object itemBean;
	private IDAOProvider daoProvider;

	public VaadinMatchGrid(EntityMatchGrid componentModel) {
		this.componentModel = componentModel;
		this.unmatchedToolStrip = new EntityEditorToolStrip();
		this.matchedToolStrip = new EntityEditorToolStrip();
		this.unmatchedGrid = new EntityEditorGrid();
		this.matchedGrid = new EntityEditorGrid();
		this.subscribers = new HashSet<VaadinMatchGrid.IMatchListener>();
		this.unmatchedItemIdFilterMap = new HashMap<Item, Container.Filter>();
		this.content = new ConXVerticalSplitPanel();
		this.unmatchedAlertPanel = new VaadinFormAlertPanel();
		this.matchedAlertPanel = new VaadinFormAlertPanel();

		initialize();
	}

	public Object getItemBean() {
		return this.itemBean;
	}

	public void setItemBean(Object itemBean) {
		this.itemBean = itemBean;
	}

	private Object getUnmatchedBeanById(Item item) {
		Object bean = null;
		if (item instanceof JPAContainerItem) {
			bean = ((JPAContainerItem<?>) item).getEntity();
		} else if (item instanceof BeanItem) {
			bean = ((BeanItem<?>) item).getBean();
		}
		return bean;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void match(Item item) throws Exception {
		Object unmatchedBean = getUnmatchedBeanById(item);
		if (unmatchedBean != null) {
			Object matchedBean = VaadinPageDataBuilder.saveInstance(getMatchedContainerType().newInstance(), this.daoProvider, unmatchedBean, itemBean);
			if (matchedBean != null) {
				Container matchedContainer = this.matchedGrid.getContainerDataSource();
				Item matchedItem = null;
				if (matchedContainer instanceof BeanItemContainer) {
					matchedItem = ((BeanItemContainer) matchedContainer).addBean(matchedBean);
					onMatch(matchedItem, item);

					if (item instanceof JPAContainerItem<?>) {
						// ((JPAContainer<?>)this.unmatchedGrid.getContainerDataSource()).refreshItem(((JPAContainerItem)
						// item).getItemId());
						((JPAContainer<?>) this.unmatchedGrid.getContainerDataSource()).refresh();
						// ((JPAContainer<?>)this.unmatchedGrid.getContainerDataSource()).applyFilters();
					} else {
						throw new Exception(
								"The unmatched container must inherit JPAContainer<?> and all items must inherit JPAContainerItem<?>.");
					}

					String message = "";
					if (matchedItem.getItemProperty("name") != null && matchedItem.getItemProperty("name").getValue() != null) {
						message = matchedItem.getItemProperty("name").getValue() + " was matched successfully.";
					} else {
						message = "The new matched item was created successfully.";
					}
					this.matchedAlertPanel.setAlertType(AlertType.SUCCESS);
					this.matchedAlertPanel.setMessage(message);
					this.matchedAlertPanel.setVisible(true);
				} else {
					String message = "";
					if (item.getItemProperty("name") != null && item.getItemProperty("name").getValue() != null) {
						message = "Could not match " + item.getItemProperty("name").getValue() + ". The matched container is incompatible.";
					} else {
						message = "Could not match the selected item. The matched container is incompatible.";
					}
					this.unmatchedAlertPanel.setAlertType(AlertType.ERROR);
					this.unmatchedAlertPanel.setMessage(message);
					throw new Exception(message);
				}
				this.matchButton.setEnabled(false);
			} else {
				String message = "";
				if (item.getItemProperty("name") != null && item.getItemProperty("name").getValue() != null) {
					message = "Could not match " + item.getItemProperty("name").getValue()
							+ ". Could not get the bean from matched grid with the bean converter.";
				} else {
					message = "Could not match the selected item. Could not get the bean from matched grid with the bean converter.";
				}
				this.unmatchedAlertPanel.setAlertType(AlertType.ERROR);
				this.unmatchedAlertPanel.setMessage(message);
				throw new Exception(message);
			}
		} else {
			String message = "";
			if (item.getItemProperty("name") != null && item.getItemProperty("name").getValue() != null) {
				message = "Could not match " + item.getItemProperty("name").getValue() + ". Could not get the bean from unmatched grid.";
			} else {
				message = "Could not match the selected item. Could not get the bean from unmatched grid.";
			}
			this.unmatchedAlertPanel.setAlertType(AlertType.ERROR);
			this.unmatchedAlertPanel.setMessage(message);
			throw new Exception(message);
		}
		String message = "";
		if (item.getItemProperty("name") != null && item.getItemProperty("name").getValue() != null) {
			message = item.getItemProperty("name").getValue() + " was matched successfully.";
		} else {
			message = "The selected item was matched successfully.";
		}
		this.unmatchedAlertPanel.setAlertType(AlertType.SUCCESS);
		this.unmatchedAlertPanel.setMessage(message);
	}

	private void unhideMatchableItem(Item matchedItem) throws Exception {
		Container unmatchedContainer = this.unmatchedGrid.getContainerDataSource();
		Filter unmatchedItemFilter = this.unmatchedItemIdFilterMap.get(matchedItem);
		if (unmatchedItemFilter != null) {
			if (unmatchedContainer instanceof JPAContainer) {
				((JPAContainer<?>) unmatchedContainer).removeContainerFilter(unmatchedItemFilter);
				((JPAContainer<?>) unmatchedContainer).applyFilters();
				this.unmatchedItemIdFilterMap.remove(matchedItem);
			} else if (unmatchedContainer instanceof BeanItemContainer) {
				((BeanItemContainer<?>) unmatchedContainer).removeContainerFilter(unmatchedItemFilter);
				this.unmatchedItemIdFilterMap.remove(matchedItem);
			} else {
				String message = "";
				if (matchedItem.getItemProperty("name") != null && matchedItem.getItemProperty("name").getValue() != null) {
					message = "Could not match " + matchedItem.getItemProperty("name").getValue()
							+ ". The unmatched container is incompatible.";
				} else {
					message = "Could not match the selected item. The unmatched container is incompatible.";
				}
				this.matchedAlertPanel.setAlertType(AlertType.ERROR);
				this.matchedAlertPanel.setMessage(message);
				this.matchedAlertPanel.setVisible(true);
				throw new Exception(message);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private void unmatch(Item item) throws Exception {
		this.matchedGrid.getContainerDataSource().removeItem(((BeanItem) item).getBean());
		unhideMatchableItem(item);
		this.unmatchButton.setEnabled(false);
		onUnmatch(item);
	}

	public void addListener(IMatchListener listener) {
		this.subscribers.add(listener);
	}

	public void removeListener(IMatchListener listener) {
		this.subscribers.remove(listener);
	}

	private void onMatch(Item item, Item parent) {
		for (IMatchListener listener : this.subscribers) {
			listener.onMatch(item, parent);
		}
	}

	private void onUnmatch(Item item) {
		for (IMatchListener listener : this.subscribers) {
			listener.onUnmatch(item);
		}
	}

	private void initialize() {
		if (this.componentModel.isDynamic()) {
			this.newMatchedItemButton = this.matchedToolStrip.addToolStripButton(EntityEditorToolStrip.TOOLSTRIP_IMG_CREATE_PNG);
		}
		
		this.matchedToolStrip.setTitle(this.componentModel.getMatchedDataSource().getEntityType().getName() + "s");
		this.unmatchButton = this.matchedToolStrip.addToolStripButton(EntityEditorToolStrip.TOOLSTRIP_IMG_UNMATCH_PNG);
		this.unmatchButton.setEnabled(false);
		this.unmatchedToolStrip.setTitle(this.componentModel.getUnmatchedDataSource().getEntityType().getName() + "s");
		this.matchButton = this.unmatchedToolStrip.addToolStripButton(EntityEditorToolStrip.TOOLSTRIP_IMG_MATCH_PNG);
		this.matchButton.setEnabled(false);

		this.unmatchButton.addListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				try {
					if (VaadinMatchGrid.this.matchedGridItem == null) {
						VaadinMatchGrid.this.unmatchButton.setEnabled(false);
					} else {
						unmatch(VaadinMatchGrid.this.matchedGridItem);
					}
				} catch (Exception e) {
					VaadinMatchGrid.this.unmatchButton.setComponentError(new UserError(e.getMessage()));
					e.printStackTrace();
				}
			}
		});
		this.unmatchedGrid.addSelectListener(new ISelectListener() {

			@Override
			public void onSelect(Item item) {
				VaadinMatchGrid.this.unmatchedGridItem = item;
				VaadinMatchGrid.this.matchButton.setEnabled(true);
			}
		});
		this.matchButton.addListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				try {
					if (VaadinMatchGrid.this.unmatchedGridItem == null) {
						VaadinMatchGrid.this.matchButton.setEnabled(false);
					} else {
						match(VaadinMatchGrid.this.unmatchedGridItem);
					}
				} catch (Exception e) {
					VaadinMatchGrid.this.matchButton.setComponentError(new UserError(e.getMessage()));
					e.printStackTrace();
				}
			}
		});
		this.matchedGrid.addSelectListener(new ISelectListener() {

			@Override
			public void onSelect(Item item) {
				VaadinMatchGrid.this.matchedGridItem = item;
				VaadinMatchGrid.this.unmatchButton.setEnabled(true);
			}
		});

		this.unmatchedGrid.setSizeFull();
		this.matchedGrid.setSizeFull();

		this.unmatchedAlertPanel.setVisible(!this.componentModel.isDynamic());
		this.unmatchedAlertPanel.setCloseable(false);
		this.unmatchedAlertPanel.setAlertType(AlertType.ERROR);
		this.unmatchedAlertPanel.setMessage("No " + this.componentModel.getUnmatchedDataSource().getEntityType().getName()
				+ " has been matched yet.");
		this.matchedAlertPanel.setCloseable(false);
		this.matchedAlertPanel.setVisible(false);
		this.matchedAlertPanel.setAlertType(AlertType.ERROR);
		this.matchedAlertPanel.setMessage("No " + this.componentModel.getUnmatchedDataSource().getEntityType().getName()
				+ " has been created yet.");

		VerticalLayout unmatchedGridLayout = new VerticalLayout();
		unmatchedGridLayout.addComponent(this.unmatchedToolStrip);
		unmatchedGridLayout.addComponent(this.unmatchedAlertPanel);
		unmatchedGridLayout.addComponent(this.unmatchedGrid);
		unmatchedGridLayout.setExpandRatio(this.unmatchedGrid, 1.0f);

		VerticalLayout matchedGridLayout = new VerticalLayout();
		matchedGridLayout.addComponent(this.matchedToolStrip);
		matchedGridLayout.addComponent(this.matchedAlertPanel);
		matchedGridLayout.addComponent(this.matchedGrid);
		matchedGridLayout.setExpandRatio(this.matchedGrid, 1.0f);

		this.content = new ConXVerticalSplitPanel();
		this.content.setFirstComponent(unmatchedGridLayout);
		this.content.setSecondComponent(matchedGridLayout);
		this.content.setSizeFull();

		addComponent(this.content);
		setExpandRatio(this.content, 1.0f);

		setSizeFull();
	}

	public Class<?> getMatchedContainerType() throws ClassNotFoundException {
		return this.componentModel.getMatchedDataSource().getEntityType().getJavaType();
	}

	public Class<?> getUnmatchedContainerType() throws ClassNotFoundException {
		return this.componentModel.getUnmatchedDataSource().getEntityType().getJavaType();
	}

	public void setUnMatchedContainer(Container container) {
		this.unmatchedGrid.setContainerDataSource(container);
		this.unmatchedGrid.setVisibleColumns(this.componentModel.getUnmatchedDataSource().getVisibleFieldNames().toArray(new String[0]));
		this.unmatchedGrid.setColumnTitles(this.componentModel.getUnmatchedDataSource().getVisibleFieldTitles().toArray(new String[0]));
	}

	public void setMatchedContainer(Container container) {
		this.matchedGrid.setContainerDataSource(container);
		this.matchedGrid.setVisibleColumns(this.componentModel.getMatchedDataSource().getVisibleFieldNames().toArray(new String[0]));
		this.matchedGrid.setColumnTitles(this.componentModel.getMatchedDataSource().getVisibleFieldTitles().toArray(new String[0]));
	}

	public interface IMatchListener {
		public void onMatch(Item matchedItem, Item matchedItemParent);

		public void onUnmatch(Item matchedItemId);
	}

	public EntityMatchGrid getComponentModel() {
		return this.componentModel;
	}

	public void addListener(ISelectListener listener) {
		this.matchedGrid.addSelectListener(listener);
	}

	public void addParentConsumptionFilter(Filter filter) throws Exception {
		if (this.unmatchedGrid.getContainerDataSource() instanceof JPAContainer<?>) {
			((JPAContainer<?>) this.unmatchedGrid.getContainerDataSource()).addContainerFilter(filter);
			((JPAContainer<?>) this.unmatchedGrid.getContainerDataSource()).applyFilters();
		} else if (this.unmatchedGrid.getContainerDataSource() instanceof BeanItemContainer<?>) {
			((BeanItemContainer<?>) this.unmatchedGrid.getContainerDataSource()).addContainerFilter(filter);
		} else {
			throw new Exception("Could not add consumption filter, the parent grid container was invalid.");
		}
	}

	public EntityEditorToolStripButton getNewMatchedItemButton() {
		return newMatchedItemButton;
	}

	public EntityEditorGrid getMatchedGrid() {
		return matchedGrid;
	}
	
	public EntityEditorGrid getUnmatchedGrid() {
		return unmatchedGrid;
	}
	
	public boolean isDynamic() {
		return this.componentModel.isDynamic();
	}

	public void setDaoProvider(IDAOProvider daoProvider) {
		this.daoProvider = daoProvider;
	}
}
