package com.conx.logistics.app.whse.rcv.rcv.workitems;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.drools.process.instance.WorkItemHandler;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.conx.logistics.app.whse.rcv.rcv.dao.services.IArrivalDAOService;
import com.conx.logistics.app.whse.rcv.rcv.domain.Arrival;
import com.conx.logistics.mdm.dao.services.IOrganizationDAOService;

@Transactional
@Repository
public class SaveArrivalWIH implements WorkItemHandler {
	private static final Logger logger = LoggerFactory.getLogger(SaveArrivalWIH.class);
	
	@Autowired
	private IOrganizationDAOService orgDao;

	@Autowired
	private IArrivalDAOService arrivalDAOService;

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		Arrival arrival = null;
		try
		{
			Arrival arvl = (Arrival)workItem.getParameter("arrivalIn");
			
			arvl = arrivalDAOService.update(arvl);
			
			Map<String, Object> results = new HashMap<String, Object>();
			results.put("arrivalOut",arrival);
			manager.completeWorkItem(workItem.getId(), results);
		}
		catch (Exception e)
		{
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String stacktrace = sw.toString();
			logger.error(stacktrace);
			
			throw new IllegalStateException("SaveArrivalWIH:\r\n"+stacktrace, e);
		}	
		catch (Error e)
		{
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String stacktrace = sw.toString();
			logger.error(stacktrace);
			
			throw new IllegalStateException("SaveArrivalWIH:\r\n"+stacktrace, e);			
		}			
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		// TODO Auto-generated method stub

	}

}
