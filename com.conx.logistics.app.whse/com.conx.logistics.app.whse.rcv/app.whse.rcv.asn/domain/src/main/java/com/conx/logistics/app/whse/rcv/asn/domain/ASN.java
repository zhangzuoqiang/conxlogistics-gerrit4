package com.conx.logistics.app.whse.rcv.asn.domain;

import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.format.annotation.DateTimeFormat;

import com.conx.logistics.app.whse.domain.warehouse.Warehouse;
import com.conx.logistics.app.whse.rcv.asn.shared.type.ASNSTATUS;
import com.conx.logistics.app.whse.rcv.asn.shared.type.TRANSMODE;
import com.conx.logistics.mdm.domain.MultitenantBaseEntity;
import com.conx.logistics.mdm.domain.geolocation.Address;
import com.conx.logistics.mdm.domain.geolocation.AddressTypeAddress;
import com.conx.logistics.mdm.domain.organization.Organization;
import com.conx.logistics.mdm.domain.product.DimUnit;
import com.conx.logistics.mdm.domain.product.PackUnit;
import com.conx.logistics.mdm.domain.product.WeightUnit;
import com.conx.logistics.mdm.domain.referencenumber.ReferenceNumber;

@Entity
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
@Table(name="whasn")
public class ASN extends MultitenantBaseEntity {
    @ManyToOne(targetEntity = PackUnit.class)
    private PackUnit outerPackUnit;

    @ManyToOne(targetEntity = WeightUnit.class)
    private WeightUnit weightUnit;

    @ManyToOne(targetEntity = DimUnit.class)
    private DimUnit dimUnit;

    @ManyToOne(targetEntity = DimUnit.class)
    private DimUnit volUnit;

    @OneToOne(targetEntity = Organization.class)
    private Organization shippedFrom;

    @ManyToOne(targetEntity = Organization.class)
    private Organization consignee;

    @ManyToOne(targetEntity = Organization.class)
    private Organization consignor;

    @ManyToOne(targetEntity = Address.class)
    private AddressTypeAddress shipperAddress;

    @OneToMany(mappedBy="parentASN",cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<ASNLine> asnLines = new java.util.HashSet<ASNLine>();

    private Integer expectedTotalOuterPackCount;

    private Double expectedTotalweight;

    private Double expectedTotalLen;

    private Double expectedTotalWidth;

    private Double expectedTotalHeight;

    private Double expectedTotalVolume;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "S-")
    private Date estimatedFirstArrival;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "S-")
    private Date actualFirstArrival;

    @Enumerated(EnumType.STRING)
    private TRANSMODE mode;

    @Enumerated(EnumType.STRING)
    private ASNSTATUS status;

    private boolean master;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "S-")
    private Date dateImported;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "S-")
    private Date dateLastImportUpdated;
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<ReferenceNumber> refNumbers = new java.util.HashSet<ReferenceNumber>();

    /**
     * Pickup and Delivery
     */
    @OneToOne(targetEntity = ASNPickup.class,fetch=FetchType.EAGER)
    private ASNPickup pickup;
    
    @OneToOne(targetEntity = ASNDropOff.class,fetch=FetchType.EAGER)
    private ASNDropOff dropOff;

    @OneToOne
    private Warehouse warehouse;
    
	public PackUnit getOuterPackUnit() {
		return outerPackUnit;
	}


	public void setOuterPackUnit(PackUnit outerPackUnit) {
		this.outerPackUnit = outerPackUnit;
	}


	public WeightUnit getWeightUnit() {
		return weightUnit;
	}


	public void setWeightUnit(WeightUnit weightUnit) {
		this.weightUnit = weightUnit;
	}


	public DimUnit getDimUnit() {
		return dimUnit;
	}


	public void setDimUnit(DimUnit dimUnit) {
		this.dimUnit = dimUnit;
	}


	public DimUnit getVolUnit() {
		return volUnit;
	}


	public void setVolUnit(DimUnit volUnit) {
		this.volUnit = volUnit;
	}


	public Organization getShippedFrom() {
		return shippedFrom;
	}


	public void setShippedFrom(Organization shippedFrom) {
		this.shippedFrom = shippedFrom;
	}

	public Organization getConsignee() {
		return consignee;
	}


	public void setConsignee(Organization consignee) {
		this.consignee = consignee;
	}

	public Organization getConsignor() {
		return consignor;
	}


	public void setConsignor(Organization consignor) {
		this.consignor = consignor;
	}


	public AddressTypeAddress getShipperAddress() {
		return shipperAddress;
	}

	public void setShipperAddress(AddressTypeAddress shipperAddress) {
		this.shipperAddress = shipperAddress;
	}

	public Set<ASNLine> getAsnLines() {
		return asnLines;
	}

	public void setAsnLines(Set<ASNLine> asnLines) {
		this.asnLines = asnLines;
	}

	public Integer getExpectedTotalOuterPackCount() {
		return expectedTotalOuterPackCount;
	}


	public void setExpectedTotalOuterPackCount(Integer expectedTotalOuterPackCount) {
		this.expectedTotalOuterPackCount = expectedTotalOuterPackCount;
	}


	public Double getExpectedTotalweight() {
		return expectedTotalweight;
	}


	public void setExpectedTotalweight(Double expectedTotalweight) {
		this.expectedTotalweight = expectedTotalweight;
	}


	public Double getExpectedTotalLen() {
		return expectedTotalLen;
	}


	public void setExpectedTotalLen(Double expectedTotalLen) {
		this.expectedTotalLen = expectedTotalLen;
	}


	public Double getExpectedTotalWidth() {
		return expectedTotalWidth;
	}


	public void setExpectedTotalWidth(Double expectedTotalWidth) {
		this.expectedTotalWidth = expectedTotalWidth;
	}


	public Double getExpectedTotalHeight() {
		return expectedTotalHeight;
	}


	public void setExpectedTotalHeight(Double expectedTotalHeight) {
		this.expectedTotalHeight = expectedTotalHeight;
	}


	public Double getExpectedTotalVolume() {
		return expectedTotalVolume;
	}


	public void setExpectedTotalVolume(Double expectedTotalVolume) {
		this.expectedTotalVolume = expectedTotalVolume;
	}


	public Date getEstimatedFirstArrival() {
		return estimatedFirstArrival;
	}


	public void setEstimatedFirstArrival(Date estimatedFirstArrival) {
		this.estimatedFirstArrival = estimatedFirstArrival;
	}


	public Date getActualFirstArrival() {
		return actualFirstArrival;
	}


	public void setActualFirstArrival(Date actualFirstArrival) {
		this.actualFirstArrival = actualFirstArrival;
	}


	public TRANSMODE getMode() {
		return mode;
	}


	public void setMode(TRANSMODE mode) {
		this.mode = mode;
	}


	public ASNSTATUS getStatus() {
		return status;
	}


	public void setStatus(ASNSTATUS status) {
		this.status = status;
	}


	public boolean isMaster() {
		return master;
	}


	public void setMaster(boolean master) {
		this.master = master;
	}


	public Date getDateImported() {
		return dateImported;
	}


	public void setDateImported(Date dateImported) {
		this.dateImported = dateImported;
	}


	public Date getDateLastImportUpdated() {
		return dateLastImportUpdated;
	}


	public void setDateLastImportUpdated(Date dateLastImportUpdated) {
		this.dateLastImportUpdated = dateLastImportUpdated;
	}


	public ASNPickup getPickup() {
		return pickup;
	}


	public void setPickup(ASNPickup pickup) {
		this.pickup = pickup;
	}


	public Set<ReferenceNumber> getRefNumbers() {
		return refNumbers;
	}


	public void setRefNumbers(Set<ReferenceNumber> refNumbers) {
		this.refNumbers = refNumbers;
	}


	public ASNDropOff getDropOff() {
		return dropOff;
	}

	
	public void setDropOff(ASNDropOff dropOff) {
		this.dropOff = dropOff;
	}

	public Warehouse getWarehouse() {
		return warehouse;
	}


	public void setWarehouse(Warehouse warehouse) {
		this.warehouse = warehouse;
	}
	
	
}
