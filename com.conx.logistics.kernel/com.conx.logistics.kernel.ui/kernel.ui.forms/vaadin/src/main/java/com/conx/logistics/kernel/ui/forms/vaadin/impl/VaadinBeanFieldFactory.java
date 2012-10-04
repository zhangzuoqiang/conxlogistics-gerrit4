package com.conx.logistics.kernel.ui.forms.vaadin.impl;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.Type;

import com.conx.logistics.kernel.ui.forms.vaadin.impl.field.VaadinSelectDetail;
import com.vaadin.addon.jpacontainer.EntityContainer;
import com.vaadin.addon.jpacontainer.JPAContainer;
import com.vaadin.addon.jpacontainer.JPAContainerFactory;
import com.vaadin.addon.jpacontainer.fieldfactory.MultiSelectTranslator;
import com.vaadin.addon.jpacontainer.fieldfactory.OneToOneForm;
import com.vaadin.addon.jpacontainer.fieldfactory.SingleSelectTranslator;
import com.vaadin.addon.jpacontainer.metadata.PropertyKind;
import com.vaadin.addon.jpacontainer.util.EntityManagerPerRequestHelper;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Table;

@SuppressWarnings("serial")
public class VaadinBeanFieldFactory extends DefaultFieldFactory {

	private HashMap<Class<?>, String[]> propertyOrders;
	private EntityManagerPerRequestHelper entityManagerPerRequestHelper;
	private HashMap<Class<?>, Class<? extends AbstractSelect>> multiselectTypes;
	private HashMap<Class<?>, Class<? extends AbstractSelect>> singleselectTypes;
	@SuppressWarnings("rawtypes")
	private BeanItemContainer beanContainer;
	private EntityManagerFactory factory;

	/**
	 * Creates a new JPAContainerFieldFactory. For referece/collection types
	 * ComboBox or multiselects are created by default.
	 */
	public VaadinBeanFieldFactory() {
	}

	/**
	 * Creates a new JPAContainerFieldFactory. For referece/collection types
	 * ComboBox or multiselects are created by default.
	 * 
	 * @param emprHelper
	 *            the {@link EntityManagerPerRequestHelper} to use for updating
	 *            the entity manager in internally generated JPAContainers for
	 *            each request.
	 */
	public VaadinBeanFieldFactory(EntityManagerPerRequestHelper emprHelper) {
		setEntityManagerPerRequestHelper(emprHelper);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Field createField(Item item, Object propertyId, Component uiContext) {
		if (this.beanContainer != null && this.factory != null) {
			if (item instanceof BeanItem) {
				BeanItem jpaitem = (BeanItem) item;
				Property property = jpaitem.getItemProperty("id");
				if (property != null) {
					// BeanItemContainer container = jpaitem.get
					Field field = createJPAContainerBackedField(property.getValue(), propertyId, beanContainer, uiContext);
					if (field != null) {
						return field;
					}
				}
			}
		}
		return configureBasicFields(super.createField(item, propertyId, uiContext));
	}

	/**
	 * This method can be used to configure field generated by the
	 * DefaultFieldFactory. By default it sets null representation of textfields
	 * to empty string instead of 'null'.
	 * 
	 * @param field
	 * @return
	 */
	protected Field configureBasicFields(Field field) {
		if (field instanceof AbstractTextField) {
			((AbstractTextField) field).setNullRepresentation("");
		}
		return field;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Field createField(Container container, Object itemId, Object propertyId, Component uiContext) {
		if (this.factory != null) {
			if (container instanceof BeanItemContainer) {
				BeanItemContainer jpacontainer = (BeanItemContainer) container;
				Field field = createJPAContainerBackedField(itemId, propertyId, jpacontainer, uiContext);
				if (field != null) {
					return field;
				}
			}
		}
		return configureBasicFields(super.createField(container, itemId, propertyId, uiContext));
	}

	@SuppressWarnings("rawtypes")
	private PropertyKind getPropertyKind(BeanItemContainer container, Object propertyId) {
		try {
			Class<?> beanType = container.getBeanType();
			Annotation[] propertyAnnotaitions = beanType.getField(propertyId.toString()).getAnnotations();
			for (Annotation annotation : propertyAnnotaitions) {
				if (annotation.annotationType().isAssignableFrom(ManyToOne.class)) {
					return PropertyKind.MANY_TO_ONE;
				} else if (annotation.annotationType().isAssignableFrom(OneToOne.class)) {
					return PropertyKind.ONE_TO_ONE;
				} else if (annotation.annotationType().isAssignableFrom(OneToMany.class)) {
					return PropertyKind.ONE_TO_MANY;
				} else if (annotation.annotationType().isAssignableFrom(ManyToMany.class)) {
					return PropertyKind.MANY_TO_MANY;
				}
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}

		return PropertyKind.SIMPLE;
	}

	@SuppressWarnings("rawtypes")
	private Field createJPAContainerBackedField(Object itemId, Object propertyId, BeanItemContainer jpacontainer, Component uiContext) {
		Field field = null;
		PropertyKind propertyKind = getPropertyKind(jpacontainer, propertyId);
		switch (propertyKind) {
		case MANY_TO_ONE:
			field = createReferenceSelect(jpacontainer, itemId, propertyId, uiContext);
			break;
		case ONE_TO_ONE:
			field = createOneToOneField(jpacontainer, itemId, propertyId, uiContext);
			break;
		case ONE_TO_MANY:
			field = createMasterDetailEditor(jpacontainer, itemId, propertyId, uiContext);
			break;
		case MANY_TO_MANY:
			field = createCollectionSelect(jpacontainer, itemId, propertyId, uiContext);
			break;
		default:
			break;
		}
		return field;
	}

	@SuppressWarnings("rawtypes")
	protected OneToOneForm createOneToOneField(BeanItemContainer jpacontainer, Object itemId, Object propertyId, Component uiContext) {
		OneToOneForm oneToOneForm = new OneToOneForm();
		oneToOneForm.setBackReferenceId(jpacontainer.getBeanType().getSimpleName().toLowerCase());
		oneToOneForm.setCaption(DefaultFieldFactory.createCaptionByPropertyId(propertyId));
		oneToOneForm.setFormFieldFactory(this);
		if (uiContext instanceof Form) {
			// write buffering is configure by Form after binding the data
			// source. Yes, you may read the previous sentence again or verify
			// this from the Vaadin code if you don't believe what you just
			// read.
			// As oneToOneForm creates the referenced type on demand if required
			// the buffering state needs to be available when proeprty is set
			// (otherwise the original master entity will be modified once the
			// form is opened).
			Form f = (Form) uiContext;
			oneToOneForm.setWriteThrough(f.isWriteThrough());
		}
		return oneToOneForm;
	}

	@SuppressWarnings({ "rawtypes" })
	protected Field createCollectionSelect(BeanItemContainer containerForProperty, Object itemId, Object propertyId, Component uiContext) {
		/*
		 * Detect what kind of reference type we have
		 */
		Class masterEntityClass = containerForProperty.getBeanType();
		Class referencedType = detectReferencedType(this.factory, propertyId, masterEntityClass);
		final JPAContainer container = createJPAContainerFor(referencedType, false);
		final AbstractSelect select = constructCollectionSelect(containerForProperty, itemId, propertyId, uiContext, referencedType);
		select.setCaption(DefaultFieldFactory.createCaptionByPropertyId(propertyId));
		select.setContainerDataSource(container);
		// many to many, selectable from table listing all existing pojos
		select.setPropertyDataSource(new MultiSelectTranslator(select));
		select.setMultiSelect(true);
		if (select instanceof Table) {
			Table t = (Table) select;
			t.setSelectable(true);
			Object[] visibleProperties = getVisibleProperties(referencedType);
			if (visibleProperties == null) {
				List<Object> asList = new ArrayList<Object>(Arrays.asList(t.getVisibleColumns()));
				asList.remove("id");
				// TODO this should be the true "back reference" field from the
				// opposite direction, now we expect convention
				final String backReferencePropertyId = masterEntityClass.getSimpleName().toLowerCase() + "s";
				asList.remove(backReferencePropertyId);
				visibleProperties = asList.toArray();
			}
			t.setVisibleColumns(visibleProperties);
		} else {
			select.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_ITEM);
		}

		return select;
	}

	@SuppressWarnings({ "rawtypes" })
	private Field createMasterDetailEditor(BeanItemContainer containerForProperty, Object itemId, Object propertyId, Component uiContext) {
		// return new MasterDetailEditor(this, containerForProperty, itemId,
		// propertyId, uiContext);
		// TODO fix master detail editor
		return new VaadinSelectDetail(this, containerForProperty, itemId, propertyId, uiContext);
	}

	/**
	 * Detects the type entities in "collection types" (oneToMany, ManyToMany).
	 * 
	 * @param propertyId
	 * @param masterEntityClass
	 * @return the type of entities in collection type
	 */
	@SuppressWarnings("rawtypes")
	public Class detectReferencedType(EntityManagerFactory emf, Object propertyId, Class masterEntityClass) {
		Class referencedType = null;
		Metamodel metamodel = emf.getMetamodel();
		Set<EntityType<?>> entities = metamodel.getEntities();
		for (EntityType<?> entityType : entities) {
			Class<?> javaType = entityType.getJavaType();
			if (javaType == masterEntityClass) {
				Attribute<?, ?> attribute = entityType.getAttribute(propertyId.toString());
				PluralAttribute pAttribute = (PluralAttribute) attribute;
				Type elementType = pAttribute.getElementType();
				referencedType = elementType.getJavaType();
				break;
			}
		}
		return referencedType;
	}

	protected EntityManagerFactory getEntityManagerFactory(EntityContainer<?> containerForProperty) {
		return containerForProperty.getEntityProvider().getEntityManager().getEntityManagerFactory();
	}

	/**
	 * Creates a field for simple reference (ManyToOne)
	 * 
	 * @param containerForProperty
	 * @param propertyId
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	protected Field createReferenceSelect(BeanItemContainer containerForProperty, Object itemId, Object propertyId, Component uiContext) {
		Class<?> type = containerForProperty.getType(propertyId);
		JPAContainer container = createJPAContainerFor(type, false);

		AbstractSelect nativeSelect = constructReferenceSelect(itemId, propertyId, uiContext, type);
		nativeSelect.setMultiSelect(false);
		nativeSelect.setCaption(DefaultFieldFactory.createCaptionByPropertyId(propertyId));
		nativeSelect.setItemCaptionMode(NativeSelect.ITEM_CAPTION_MODE_ITEM);
		nativeSelect.setContainerDataSource(container);
		nativeSelect.setPropertyDataSource(new SingleSelectTranslator(nativeSelect));
		return nativeSelect;
	}

	protected AbstractSelect constructReferenceSelect(Object itemId, Object propertyId, Component uiContext, Class<?> type) {
		if (singleselectTypes != null) {
			Class<? extends AbstractSelect> class1 = singleselectTypes.get(type);
			if (class1 != null) {
				try {
					return class1.newInstance();
				} catch (Exception e) {
					Logger.getLogger(getClass().getName()).warning("Could not create select of type " + class1.getName());
				}
			}
		}
		return new NativeSelect();
	}

	@SuppressWarnings("rawtypes")
	protected AbstractSelect constructCollectionSelect(BeanItemContainer containerForProperty, Object itemId, Object propertyId, Component uiContext, Class<?> type) {
		if (multiselectTypes != null) {
			Class<? extends AbstractSelect> class1 = multiselectTypes.get(type);
			try {
				return class1.newInstance();
			} catch (Exception e) {
				Logger.getLogger(getClass().getName()).warning("Could not create select of type " + class1.getName());
			}
		}
		return new Table();
	}

	protected EntityManager getEntityManagerFor(EntityContainer<?> containerForProperty) {
		return containerForProperty.getEntityProvider().getEntityManager();
	}

	public JPAContainer<?> createJPAContainerFor(Class<?> type, boolean buffered) {
		JPAContainer<?> container = null;
		EntityManager em = this.factory.createEntityManager();
		if (buffered) {
			container = JPAContainerFactory.makeBatchable(type, em);
		} else {
			container = JPAContainerFactory.make(type, em);
		}
		// Set the lazy loading delegate to the same as the parent.
		// container.getEntityProvider().setLazyLoadingDelegate(containerForProperty.getEntityProvider().getLazyLoadingDelegate());
		if (entityManagerPerRequestHelper != null) {
			entityManagerPerRequestHelper.addContainer(container);
		}
		return container;
	}

	/**
	 * Configures visible properties and their order for fields created for
	 * reference/collection types referencing to given entity type. This order
	 * is for example used by Table's created for OneToMany or ManyToMany
	 * reference types.
	 * 
	 * @param containerType
	 *            the entity type for which the visible properties will be set
	 * @param propertyIdentifiers
	 *            the identifiers in wished order to be displayed
	 */
	public void setVisibleProperties(Class<?> containerType, String... propertyIdentifiers) {
		if (propertyOrders == null) {
			propertyOrders = new HashMap<Class<?>, String[]>();
		}
		propertyOrders.put(containerType, propertyIdentifiers);
	}

	public void setMultiSelectType(Class<?> referenceType, Class<? extends AbstractSelect> selectType) {
		if (multiselectTypes == null) {
			multiselectTypes = new HashMap<Class<?>, Class<? extends AbstractSelect>>();
		}
		multiselectTypes.put(referenceType, selectType);
	}

	public void setSingleSelectType(Class<?> referenceType, Class<? extends AbstractSelect> selectType) {
		if (singleselectTypes == null) {
			singleselectTypes = new HashMap<Class<?>, Class<? extends AbstractSelect>>();
		}
		singleselectTypes.put(referenceType, selectType);
	}

	/**
	 * Returns customized visible properties (and their order) for given entity
	 * type.
	 * 
	 * @param containerType
	 * @return property identifiers that are configured to be displayed
	 */
	public String[] getVisibleProperties(Class<?> containerType) {
		if (propertyOrders != null) {
			return propertyOrders.get(containerType);
		}
		return null;
	}

	/**
	 * @return The {@link EntityManagerPerRequestHelper} that is used for
	 *         updating the entity managers for all JPAContainers generated by
	 *         this field factory.
	 */
	public EntityManagerPerRequestHelper getEntityManagerPerRequestHelper() {
		return entityManagerPerRequestHelper;
	}

	/**
	 * Sets the {@link EntityManagerPerRequestHelper} that is used for updating
	 * the entity manager of JPAContainers generated by this field factory.
	 * 
	 * @param entityManagerPerRequestHelper
	 */
	public void setEntityManagerPerRequestHelper(EntityManagerPerRequestHelper entityManagerPerRequestHelper) {
		this.entityManagerPerRequestHelper = entityManagerPerRequestHelper;
	}

	@SuppressWarnings("rawtypes")
	public BeanItemContainer getContainer() {
		return beanContainer;
	}

	@SuppressWarnings("rawtypes")
	public void setContainer(BeanItemContainer container) {
		this.beanContainer = container;
	}

	public EntityManagerFactory getFactory() {
		return factory;
	}

	public void setFactory(EntityManagerFactory factory) {
		this.factory = factory;
	}
}
