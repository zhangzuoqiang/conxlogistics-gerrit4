package com.conx.logistics.app.whse.rcv.asn.dao.services;

import java.util.List;
import java.util.Set;

import com.conx.logistics.app.whse.rcv.asn.domain.ASN;
import com.conx.logistics.app.whse.rcv.asn.domain.ASNDropOff;
import com.conx.logistics.app.whse.rcv.asn.domain.ASNLine;
import com.conx.logistics.app.whse.rcv.asn.domain.ASNPickup;
import com.conx.logistics.mdm.domain.referencenumber.ReferenceNumber;

public interface IASNDAOService {
	public List<ASN> getAll();

	public ASN add(ASN record);
	
	public ASNLine addLine(ASNLine lines, Long asnId) throws Exception;
	
	public ASN addLines(Long asnId, Set<ASNLine> lines) throws Exception;
	
	public ASN addLines(ASN asn, Set<ASNLine> lines) throws Exception;
	
	public ASN addRefNums(Long asnId, Set<ReferenceNumber> numbers) throws Exception;
	
	public ASN addRefNums(ASN asn, Set<ReferenceNumber> numbers) throws Exception;
	
	public ASN addLocalTrans(Long asnId, ASNPickup pickUp, ASNDropOff dropOff) throws Exception;
	
	public ASN addLocalTrans(ASN asn, ASNPickup pickUp, ASNDropOff dropOff) throws Exception;

	public void delete(ASN record);

	public ASN update(ASN record);
	
	public ASNLine update(ASNLine record);

	public ASN get(long id);

	public ASN getByCode(String string);
}
