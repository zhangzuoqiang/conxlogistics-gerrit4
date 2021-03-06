package com.conx.logistics.mdm.domain.geolocation;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import com.conx.logistics.mdm.domain.MultitenantBaseEntity;

@SuppressWarnings("serial")
@Entity
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
@Table(name="refaddresstype")
public class AddressType extends MultitenantBaseEntity {
}
