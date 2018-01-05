package com.tobot.tobot.sqlite.sql;

import java.util.Map;

import com.tobot.tobot.sqlite.annotation.Id;

public class Select extends Operate {
	String order;
	String limit;
	Object id;
	private Map<String, String> where;

	public Select(Class clazz) {
		super(clazz);
	}

	public Select(Class clazz, Object id, Id I) {
		super(clazz);
		this.id = id;
	}

	public Select(Class clazz, Map<String, String> where) {
		super(clazz);
		this.where = where;
	}

	public Select(Class clazz, Map<String, String> where, String order, String limit) {
		super(clazz);
		this.where = where;
		this.order = order;
		this.limit = limit;
	}

	public String toStatementString() {

		if (order != null) {
			return buildSelectSql(getTableName(), where, order, limit);
		}
		if (id != null) {
			return buildSelectSql(getTableName(), id);
		}

		return buildSelectSql(getTableName(), where);

	}

}
