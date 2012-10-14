package com.conx.logistics.mdm.domain.organization;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import com.conx.logistics.mdm.domain.MultitenantBaseEntity;

@SuppressWarnings("serial")
@Entity
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
@Table(name="refcontacttype")
public class ContactType extends MultitenantBaseEntity {
}