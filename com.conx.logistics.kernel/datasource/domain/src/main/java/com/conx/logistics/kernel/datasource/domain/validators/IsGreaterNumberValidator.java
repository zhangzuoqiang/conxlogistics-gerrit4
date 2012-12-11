package com.conx.logistics.kernel.datasource.domain.validators;

import javax.persistence.Entity;

import com.conx.logistics.kernel.datasource.domain.DataSourceField;
import com.conx.logistics.kernel.datasource.domain.DataSourceFieldValidator;

@Entity
public class IsGreaterNumberValidator extends DataSourceFieldValidator {
	private static final long serialVersionUID = -7496310890917438800L;

	public IsGreaterNumberValidator(DataSourceField field, float lowerBound) {
		String fieldValue = "#form.getField(" + getPropertyId(field) + ").getValue()";
		this.setValidationExpression("#isNumber(" + fieldValue + ") AND (#toNumber(" + fieldValue + ") > " + String.valueOf(lowerBound) + ")");
		this.setErrorMessage(fieldNameToTitle(field.getName()) + " was not greater than zero.");
		this.setField(field);
	}
	
	private String getPropertyId(DataSourceField dsField) {
		if (dsField.getJPAPath() != null) {
			return dsField.getJPAPath();
		} else {
			return dsField.getName();
		}
	}
	
	private static String fieldNameToTitle(String field) {
		String simpleName = field, title = "";
		String[] sections = simpleName.split("(?=\\p{Upper})");
		boolean isFirst = true;
		for (String section : sections) {
			if (!isFirst) {
				if (section.length() > 1) {
					title += " ";
				}
			}
			title += section;
			if (!"".equals(section)) {
				isFirst = false;
			}
		}
		return title;
	}
}
