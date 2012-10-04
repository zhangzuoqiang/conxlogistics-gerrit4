package com.conx.logistics.kernel.datasource.dao.services.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Type.PersistenceType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.conx.logistics.kernel.datasource.dao.services.IDataSourceDAOService;
import com.conx.logistics.kernel.datasource.domain.DataSource;
import com.conx.logistics.kernel.datasource.domain.DataSourceField;
import com.conx.logistics.kernel.metamodel.dao.services.IBasicTypeDAOService;
import com.conx.logistics.kernel.metamodel.dao.services.IEntityTypeDAOService;
import com.conx.logistics.mdm.domain.metamodel.AbstractAttribute;
import com.conx.logistics.mdm.domain.metamodel.BasicAttribute;
import com.conx.logistics.mdm.domain.metamodel.BasicType;
import com.conx.logistics.mdm.domain.metamodel.EntityType;
import com.conx.logistics.mdm.domain.metamodel.EntityTypeAttribute;

/**
 * Implementation of {@link DataSource} that uses JPA for persistence.
 * <p />
 * <p/>
 * This class is marked as {@link Transactional}. The Spring configuration for
 * this module, enables AspectJ weaving for adding transaction demarcation to
 * classes annotated with <code>@Transactional</code>.
 */
@Transactional
@Repository
public class DataSourceDAOImpl implements IDataSourceDAOService {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	private transient Map<String, DataSource> cache = new HashMap<String, DataSource>();

	/**
	 * Spring will inject a managed JPA {@link EntityManager} into this field.
	 */
	@PersistenceContext
	private EntityManager em;

	@Autowired
	private IEntityTypeDAOService entityTypeDao;

	@Autowired
	private IBasicTypeDAOService basicTypeDao;

	public void setEm(EntityManager em) {
		this.em = em;
	}

	@Override
	public DataSource get(long id) {
		return em.getReference(DataSource.class, id);
	}

	@Override
	public List<DataSource> getAll() {
		return em.createQuery("select o from com.conx.logistics.kernel.datasource.domain.DataSource o record by o.id", DataSource.class).getResultList();
	}

	@Override
	public List<DataSourceField> getFields(DataSource parentDataSource) {
		TypedQuery<DataSourceField> q = em.createQuery("select o from com.conx.logistics.kernel.datasource.domain.DataSourceField o WHERE o.parentDataSource = :parentDataSource",
				DataSourceField.class);
		q.setParameter("parentDataSource", parentDataSource);
		return q.getResultList();
	}

	@Override
	public DataSourceField getFieldByName(DataSource parentDataSource, String attrName) {
		DataSourceField dsf = null;
		try
		{
			TypedQuery<DataSourceField> q = em.createQuery("select o from com.conx.logistics.kernel.datasource.domain.DataSourceField o WHERE o.parentDataSource = :parentDataSource AND o.name = :name",
					DataSourceField.class);
			q.setParameter("parentDataSource", parentDataSource);
			q.setParameter("name", attrName);
			
			dsf = q.getSingleResult();
		}
		catch(NoResultException e){}
		
		return dsf;
	}

	@Override
	public DataSource add(DataSource record) {
		record = em.merge(record);

		return record;
	}

	@Override
	public DataSource addField(DataSource record, DataSourceField dsf) {
		record = em.merge(record);
		dsf.setParentDataSource(record);
		record.getDSFields().add(dsf);

		return em.merge(record);
	}

	@Override
	public DataSource addFields(DataSource record, Set<DataSourceField> dsfs) {
		record = em.merge(record);
		for (DataSourceField dsf : dsfs) {
			dsf.setParentDataSource(record);
			dsf = em.merge(dsf);
		}
		record.getDSFields().addAll(dsfs);
		record = em.merge(record);

		return em.merge(record);
	}

	@Override
	public DataSource provide(EntityType entityType) throws ClassNotFoundException {
		System.out.println("providing DataSource for (" + entityType.getJpaEntityName() + ")");

		DataSource targetDS = null;

//		Set<EntityTypeAttribute> entityTypeAttrs = entityType.getAllDeclaredAttributes();

		String simpleType = entityType.getJavaType().getSimpleName();
		simpleType = simpleType.substring(0, 1).toLowerCase() + simpleType.substring(1);

		targetDS = getByCode(simpleType);
		if (targetDS == null) {
			targetDS = new DataSource(simpleType, entityType);
			targetDS = em.merge(targetDS);
			targetDS = updateDataSourceFields(targetDS);
		}

		return targetDS;
	}

	@Override
	public DataSource getByCode(String code) {
		DataSource ds = null;

		try {
			CriteriaBuilder builder = em.getCriteriaBuilder();
			CriteriaQuery<DataSource> query = builder.createQuery(DataSource.class);
			Root<DataSource> rootEntity = query.from(DataSource.class);
			ParameterExpression<String> p = builder.parameter(String.class);
			query.select(rootEntity).where(builder.equal(rootEntity.get("code"), p));

			TypedQuery<DataSource> typedQuery = em.createQuery(query);
			typedQuery.setParameter(p, code);

			ds = typedQuery.getSingleResult();
		} catch (NoResultException e) {
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Error e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			String stacktrace = sw.toString();
			logger.error(stacktrace);
		}

		return ds;
	}

	private DataSource updateDataSourceFields(DataSource targetDataSource) throws ClassNotFoundException {
		// Process attributes
		Set<EntityTypeAttribute> aattrs = new HashSet<EntityTypeAttribute>(targetDataSource.getEntityType().getAllDeclaredAttributes());
		DataSourceField entityDSField = null;
		try {
			for (EntityTypeAttribute aattr : aattrs) {
				entityDSField = provideDataSourceField(targetDataSource, aattr);
				targetDataSource.getDSFields().add(entityDSField);
			}
		} catch (ConcurrentModificationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		targetDataSource = em.merge(targetDataSource);

		return targetDataSource;
	}

	@Override
	public DataSourceField provideDataSourceField(DataSource targetDataSource, EntityTypeAttribute aattr) throws ClassNotFoundException {
		DataSourceField entityDSField = null;
		DataSource attrDataSource;
		PersistenceType pt;
		pt = aattr.getAttribute().getPersistenceType();
		if (pt == PersistenceType.BASIC) {
			// - provide BasicType
			BasicType basicType = basicTypeDao.provide(aattr.getAttribute().getJavaType());

			// - create DataSourceField
			entityDSField = new DataSourceField(((BasicAttribute) aattr.getAttribute()).isId(), aattr.getAttribute().getName(), targetDataSource, basicType, aattr.getAttribute().getName());
			entityDSField = em.merge(entityDSField);
			// targetDataSource.getDSFields().add(entityDSField);

			System.out.println("Adding Basic-type DataSourceField (" + aattr.getAttribute().getName() + ") of type (" + basicType.getEntityJavaType() + ")");
		} else if (pt == PersistenceType.ENTITY) {
			if (aattr.getAttribute() instanceof com.conx.logistics.mdm.domain.metamodel.SingularAttribute) {
				// - provide DataSource for entity type
				EntityType entityType = aattr.getAttribute().getEntityType();
				attrDataSource = provide(aattr.getAttribute().getEntityType());

				// - create Entity attribute
				entityDSField = new DataSourceField(aattr.getAttribute().getName(), targetDataSource, attrDataSource, entityType, aattr.getAttribute().getName(),
						((com.conx.logistics.mdm.domain.metamodel.SingularAttribute) aattr.getAttribute()).getAttributeType());
				entityDSField = em.merge(entityDSField);
				// targetDataSource.getDSFields().add(entityDSField);

				System.out.println("Adding Entity-type DataSourceField (" + aattr.getAttribute().getName() + ") of type (" + entityType.getEntityJavaType() + ")");

			} else if (aattr.getAttribute() instanceof com.conx.logistics.mdm.domain.metamodel.PluralAttribute) {
				// - provide DataSource for entity type
				EntityType entityType = aattr.getAttribute().getEntityType();
				attrDataSource = provide(aattr.getAttribute().getEntityType());

				// - create Entity attribute
				entityDSField = new DataSourceField(aattr.getAttribute().getName(), targetDataSource, attrDataSource, entityType, aattr.getAttribute().getName(),
						((com.conx.logistics.mdm.domain.metamodel.PluralAttribute) aattr.getAttribute()).getAttributeType());
				entityDSField = em.merge(entityDSField);
				// targetDataSource.getDSFields().add(entityDSField);

				System.out.println("Adding Collection Entity-type DataSourceField (" + aattr.getAttribute().getName() + ") of type (" + entityType.getEntityJavaType() + ")");
			}
		}

		return entityDSField;
	}

	@Override
	public DataSourceField provideDataSourceFieldByAttrName(DataSource targetDataSource, String aattrName) throws ClassNotFoundException {
		DataSourceField entityDSField = getFieldByName(targetDataSource, aattrName);

		if (entityDSField == null) {
			AbstractAttribute attr = entityTypeDao.getAttribute(targetDataSource.getEntityType().getId(), aattrName);
			entityDSField = provideDataSourceField(targetDataSource, new EntityTypeAttribute(targetDataSource.getEntityType(), attr));
		}

		return entityDSField;
	}

	@Override
	public DataSource addField(Long dataSourceId, DataSourceField field) {
		DataSource ds = em.getReference(DataSource.class, dataSourceId);
		ds.getDSFields().add(field);

		ds = em.merge(ds);

		return ds;
	}

	@Override
	public DataSource addFields(Long dataSourceId, List<DataSourceField> fields) {
		DataSource ds = em.getReference(DataSource.class, dataSourceId);
		ds.getDSFields().addAll(fields);

		ds = em.merge(ds);

		return ds;
	}

	@Override
	public DataSource deleteField(Long dataSourceId, DataSourceField field) {
		DataSource ds = em.getReference(DataSource.class, dataSourceId);
		ds.getDSFields().remove(field);

		ds = em.merge(ds);

		return ds;
	}

	@Override
	public DataSource deleteFields(Long dataSourceId, List<DataSourceField> fields) {
		DataSource ds = em.getReference(DataSource.class, dataSourceId);
		ds.getDSFields().removeAll(fields);

		ds = em.merge(ds);

		return ds;
	}

	@Override
	public DataSourceField getField(Long dataSourceId, String name) {
		DataSource ds = em.getReference(DataSource.class, dataSourceId);
		return getFieldByName(ds, name);
	}

	@Override
	public void delete(DataSource record) {
		// TODO Auto-generated method stub

	}

	@Override
	public DataSource update(DataSource record) {
		return em.merge(record);
	}

	@Override
	public DataSourceField update(DataSourceField record) {
		return em.merge(record);
	}

	@Override
	public DataSource getByEntityType(EntityType entityType) {
		TypedQuery<DataSource> q = em.createQuery("select o from com.conx.logistics.kernel.datasource.domain.DataSource o WHERE o.entityType = :entityType", DataSource.class);
		q.setParameter("entityType", entityType);
		return q.getSingleResult();
	}
}
