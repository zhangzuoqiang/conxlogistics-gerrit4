package com.conx.logistics.app.whse.rcv.rcv.workitems.expected;

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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.conx.logistics.app.whse.dao.services.ILocationDAOService;
import com.conx.logistics.app.whse.rcv.rcv.dao.services.IArrivalDAOService;
import com.conx.logistics.app.whse.rcv.rcv.dao.services.IReceiveDAOService;
import com.conx.logistics.app.whse.rcv.rcv.domain.Arrival;
import com.conx.logistics.app.whse.rcv.rcv.domain.Receive;
import com.conx.logistics.kernel.ui.factory.services.data.IDAOProvider;

@Transactional
@Repository
public class AttachNewArrivalWIH implements WorkItemHandler {
	private static final Logger logger = LoggerFactory
			.getLogger(AttachNewArrivalWIH.class);

/*	@Autowired
	private IOrganizationDAOService orgDao;

	@Autowired
	private IReceiveDAOService receiveDAOService;*/

	@Autowired
	private PlatformTransactionManager kernelSystemTransManager;

	@Autowired
	private IDAOProvider daoProvider;

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		try {
			Receive rcv = (Receive) workItem.getParameter("receiveIn");
			Arrival arrival = new Arrival();

			DefaultTransactionDefinition def = new DefaultTransactionDefinition();
			def.setName("whse.app.page.sk");
			def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
			TransactionStatus status = this.kernelSystemTransManager
					.getTransaction(def);
			try {
				rcv = this.daoProvider.provideByDAOClass(
						IReceiveDAOService.class).add(rcv);
				arrival = this.daoProvider.provideByDAOClass(
						IArrivalDAOService.class).add(arrival, rcv);
				this.daoProvider.provideByDAOClass(ILocationDAOService.class)
						.provide(1, 1, "A");
				this.kernelSystemTransManager.commit(status);
			} catch (Exception e) {
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				String stacktrace = sw.toString();
				logger.error(stacktrace);
				this.kernelSystemTransManager.rollback(status);
				throw e;
			}

			Map<String, Object> results = new HashMap<String, Object>();
			results.put("arrivalOut", arrival);
			manager.completeWorkItem(workItem.getId(), results);
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String stacktrace = sw.toString();
			logger.error(stacktrace);

			throw new IllegalStateException("AttachNewArrivalWIH:\r\n"
					+ stacktrace, e);
		} catch (Error e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String stacktrace = sw.toString();
			logger.error(stacktrace);

			throw new IllegalStateException("AttachNewArrivalWIH:\r\n"
					+ stacktrace, e);
		}
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		// TODO Auto-generated method stub

	}

}
