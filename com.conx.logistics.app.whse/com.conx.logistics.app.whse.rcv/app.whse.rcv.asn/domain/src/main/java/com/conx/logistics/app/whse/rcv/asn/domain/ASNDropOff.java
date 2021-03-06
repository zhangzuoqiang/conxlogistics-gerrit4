package com.conx.logistics.app.whse.rcv.asn.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.format.annotation.DateTimeFormat;

import com.conx.logistics.app.whse.domain.docktype.DockType;
import com.conx.logistics.app.whse.rcv.asn.shared.type.DROPMODE;
import com.conx.logistics.mdm.domain.MultitenantBaseEntity;
import com.conx.logistics.mdm.domain.geolocation.AddressTypeAddress;
import com.conx.logistics.mdm.domain.organization.ContactTypeContact;
import com.conx.logistics.mdm.domain.organization.Organization;

@Entity
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
@Table(name="whasnpickup")
public class ASNDropOff extends MultitenantBaseEntity implements Serializable {

    @ManyToOne(targetEntity = Organization.class, fetch = FetchType.EAGER)
    @JoinColumn
    private Organization cfs;

    @ManyToOne(targetEntity = AddressTypeAddress.class, fetch = FetchType.EAGER)
    @JoinColumn
    private AddressTypeAddress cfsAddress;
    
    @ManyToOne(targetEntity = ContactTypeContact.class, fetch = FetchType.EAGER)
    @JoinColumn
    private ContactTypeContact cfsContact;

    @ManyToOne(targetEntity = Organization.class, fetch = FetchType.EAGER)
    @JoinColumn
    private Organization dropOffAt;

    @ManyToOne(targetEntity = AddressTypeAddress.class, fetch = FetchType.EAGER)
    @JoinColumn
    private AddressTypeAddress dropOffAtAddress;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "S-")
    private Date estimatedDropOff;
    
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "S-")
    private Date actualDropOff;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "S-")
    private Date dropOffRequiredBy;

    private String shippersRef;

    @Enumerated(EnumType.STRING)
    private DROPMODE dropMode;
    
    @ManyToOne(targetEntity = DockType.class, cascade={CascadeType.PERSIST,CascadeType.MERGE},fetch = FetchType.EAGER)
    @JoinColumn
    private DockType dockType;

	public Organization getCfs() {
		return cfs;
	}

	public void setCfs(Organization cfs) {
		this.cfs = cfs;
	}

	public AddressTypeAddress getCfsAddress() {
		return cfsAddress;
	}

	public void setCfsAddress(AddressTypeAddress cfsAddress) {
		this.cfsAddress = cfsAddress;
	}

	public Organization getDropOffAt() {
		return dropOffAt;
	}

	public void setDropOffAt(Organization dropOffAt) {
		this.dropOffAt = dropOffAt;
	}

	public AddressTypeAddress getDropOffAtAddress() {
		return dropOffAtAddress;
	}

	public void setDropOffAtAddress(AddressTypeAddress dropOffAtAddress) {
		this.dropOffAtAddress = dropOffAtAddress;
	}

	public Date getEstimatedDropOff() {
		return estimatedDropOff;
	}

	public void setEstimatedDropOff(Date estimatedDropOff) {
		this.estimatedDropOff = estimatedDropOff;
	}

	public Date getActualDropOff() {
		return actualDropOff;
	}

	public void setActualDropOff(Date actualDropOff) {
		this.actualDropOff = actualDropOff;
	}

	public Date getDropOffRequiredBy() {
		return dropOffRequiredBy;
	}

	public void setDropOffRequiredBy(Date dropOffRequiredBy) {
		this.dropOffRequiredBy = dropOffRequiredBy;
	}

	public String getShippersRef() {
		return shippersRef;
	}

	public void setShippersRef(String shippersRef) {
		this.shippersRef = shippersRef;
	}

	public DROPMODE getDropMode() {
		return dropMode;
	}

	public void setDropMode(DROPMODE dropMode) {
		this.dropMode = dropMode;
	}

	public DockType getDockType() {
		return dockType;
	}

	public void setDockType(DockType dockType) {
		this.dockType = dockType;
	}

	public ContactTypeContact getCfsContact() {
		return cfsContact;
	}

	public void setCfsContact(ContactTypeContact cfsContact) {
		this.cfsContact = cfsContact;
	}
}
