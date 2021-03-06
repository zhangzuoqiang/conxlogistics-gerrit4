package com.conx.logistics.kernel.persistence.services;

import javax.persistence.EntityManagerFactory;

public interface IEntityManagerFactoryManager {
	public EntityManagerFactory getHumanTaskEmf();
	public EntityManagerFactory getJbpmEmf();
	public EntityManagerFactory getKernelSystemEmf();
}
