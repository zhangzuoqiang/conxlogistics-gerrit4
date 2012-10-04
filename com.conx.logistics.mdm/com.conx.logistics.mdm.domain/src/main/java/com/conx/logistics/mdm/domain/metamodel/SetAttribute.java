package com.conx.logistics.mdm.domain.metamodel;

import javax.persistence.Entity;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.PluralAttribute.CollectionType;

@Entity
public class SetAttribute extends PluralAttribute {
	
	public SetAttribute(){
	}
	
	public SetAttribute(String name, EntityType elementType, EntityType parentEntityType, PersistentAttributeType at) {
		super(name, elementType,parentEntityType,at);
	}

	/**
	 * {@inheritDoc}
	 */
	public CollectionType getCollectionType() {
		return CollectionType.SET;
	}
}
