/*******************************************************************************
 * Copyright (c) 2013, 2015 Dr. Philip Wenig.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package org.eclipse.chemclipse.database.documents;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;

@SuppressWarnings("unchecked")
public abstract class AbstractDocument extends ODocument implements IDocument {

	/**
	 * Renew this uid on each change.
	 */
	private static final long serialVersionUID = -7689772223940367911L;
	private ODocument document;

	public AbstractDocument(String className) {

		super(className);
	}

	public AbstractDocument(ODocument document) {

		super(document.getIdentity());
		this.document = document;
		setFields();
	}

	@Override
	public ODocument save() {

		/*
		 * Store a local reference of the document
		 * Otherwise, you would get the wrong values.
		 */
		document = super.save();
		return document;
	}

	@Override
	public ODocument getDocument() {

		return document;
	}

	@Override
	public long getId(String fieldName) {

		return getLong(fieldName);
	}

	@Override
	public long getDocumentId() {

		return getIdentity().getClusterPosition();
	}

	@Override
	public Set<Long> getIds(String fieldName) {

		Set<Long> ids = new HashSet<Long>();
		Object object = getObject(fieldName);
		if(object != null) {
			/*
			 * List
			 */
			if(object instanceof List) {
				@SuppressWarnings("rawtypes")
				List list = (List)object;
				for(Object id : list.toArray()) {
					ids.add(OType.LONG.asLong(id));
				}
				/*
				 * Set
				 */
			} else if(object instanceof Set) {
				@SuppressWarnings("rawtypes")
				Set set = (Set)object;
				for(Object id : set.toArray()) {
					ids.add(OType.LONG.asLong(id));
				}
			}
		}
		return ids;
	}

	@Override
	public String getString(String fieldName) {

		String result = "";
		Object object = getObject(fieldName);
		if(object != null) {
			result = object.toString().replaceAll(ESCAPED_SINGLE_QUOTE, SINGLE_QUOTE);
		}
		return result;
	}

	@Override
	public int getInteger(String fieldName) {

		int value = -1;
		Object object = getObject(fieldName);
		if(object != null) {
			value = OType.INTEGER.asInt(object);
		}
		return value;
	}

	@Override
	public float getFloat(String fieldName) {

		float value = -1.0f;
		Object object = getObject(fieldName);
		if(object != null) {
			value = OType.FLOAT.asFloat(object);
		}
		return value;
	}

	@Override
	public double getDouble(String fieldName) {

		double value = -1;
		Object object = getObject(fieldName);
		if(object != null) {
			value = OType.DOUBLE.asDouble(object);
		}
		return value;
	}

	@Override
	public long getLong(String fieldName) {

		long value = -1;
		Object object = getObject(fieldName);
		if(object != null) {
			value = OType.LONG.asLong(object);
		}
		return value;
	}

	@Override
	public Object getObject(String fieldName) {

		if(document != null) {
			return document.field(fieldName);
		} else {
			return null;
		}
	}

	@Override
	public boolean getBoolean(String fieldName) {

		boolean value = true;
		Object object = getObject(fieldName);
		if(object != null) {
			value = (OType.BOOLEAN.asInt(object) == 1) ? true : false;
		}
		return value;
	}

	// -------------------------------------------------
	public ODocument field(String arg0, Object arg1, OType arg2) {

		if(arg2 == OType.STRING && arg1 instanceof String) {
			arg1 = ((String)arg1).replaceAll(SINGLE_QUOTE, ESCAPED_SINGLE_QUOTE);
		}
		return super.field(arg0, arg1, arg2);
	}
}
