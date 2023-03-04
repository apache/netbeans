/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/*
 * GenericTableModel.java
 *
 * Created on October 7, 2003, 11:58 AM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.common;

import java.util.List;
import java.util.ResourceBundle;
import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;


/** This is a messy class. My apologies, it should have been created on it's own,
 *  directly from the TableModel interface, but for some godforsaken reason I
 *  chose to derive it from BeanTableModel and I've been paying for it ever since.
 *
 *  That, and our schema2beans can't or won't adhere to any reasonable standard
 *  when generating child properties and/or attributes.
 *
 *  Proper design should have been Model interface wrapped around an embedded
 *  object to handle data storage in one of the three ways we handle it:  (or
 *  alternatively, an abstract interchangable model with three derivatives.)
 *
 *    (a) fields are child values and attributes of an owner CommonDDBean and
 *        that owner's parent owns a list of these beans.  (SunWebApp.WebProperties)
 *    (b) Same as (a) except that the beans owner is a single bean that owns nothing
 *        else and is owned by a larger parent.  (SunWebApp.JspConfig)
 *    (c) fields are child attributes (no values) of an owner CommonDDBean.  The
 *        attributes are indexed within this bean, so there is no extra bean
 *        being created for each row. (SunWebApp.Cache.CacheMapping:KeyField)
 *
 *
 * @author  Peter Williams
 * @version %I%, %G%
 */
public class GenericTableModel extends BeanTableModel {

	private String parentPropertyName;
	private ParentPropertyFactory parentPropertyFactory;
	private boolean indexedChildren;
	private int keyIndex;

	private List properties;
	private String [] columnNames;

    private ASDDVersion appServerVersion;
    
	/** Use this constructor when there is not going to be a parent CommonDDBean for
	 *  rows to be added to as they are created (and thus parent property name
	 *  for the items stored in the table.)  For example, several BaseBeans store
	 *  an array of items instead of having a single property that contains the array.
	 *
	 * @param ppc The class name of the CommonDDBean that represents a row in the table
	 * @param p The list of table entry objects.  This determines the columns in
	 *    the table and provides the appropriate set/get code for each value
	 */
	public GenericTableModel(ParentPropertyFactory ppf, List p) {
		this(null, ppf, p, false, 0);
	}

	/** Use this constructor when there is a parent basebean that will own all the
	 *  rows as properties.  Some BaseBeans own a property, which in turn owns
	 *  another property that forms the rows in the table.  In this case, the model
	 *  can add the rows to an instance of that middle property as the rows are
	 *  created.
	 *
	 * @param ppn The property name that the parent CommonDDBean uses to represent
	 *    the CommonDDBean that stores each row.
	 * @param ppc The class name of the CommonDDBean that represents a row in the table
	 * @param p The list of table entry objects.  This determines the columns in
	 *    the table and provides the appropriate set/get code for each value
	 */
	public GenericTableModel(String ppn, ParentPropertyFactory ppf, List p) {
		this(ppn, ppf, p, false, 0);
	}

	/** Use this constructor when there isn't a child bean.  If all parameters
	 *  in the table are attributes, then schema2beans doesn't generate a child
	 *  bean to hold them, it tacks them on all as indexed attributes of a single
	 *  parent bean.  See CacheMapping.KeyField and ConstraintField.ConstraintFieldValue
	 *  for examples of this.
	 *
	 */
	public GenericTableModel(String ppn, List p) {
		this(ppn, null, p, true, 0);
	}

	/** Use this constructor when the key field to check to prevent duplication
	 *  is not field 0.  Currently uses string equality based on the results of
	 *  toString() on the field.  Does not support keys spanning multiple columns.
	 *
	 * @param ppn The property name that the parent CommonDDBean uses to represent
	 *    the CommonDDBean that stores each row.
	 * @param ppc The class name of the CommonDDBean that represents a row in the table
	 * @param p The list of table entry objects.  This determines the columns in
	 *    the table and provides the appropriate set/get code for each value
	 * @param key The field index that represents the key field.  Use -1 to indicate
	 *    duplicates are ok, otherwise use an integer from 0 to "columns-1" to
	 *    indicate which field value to check.
	 */
	public GenericTableModel(String ppn, ParentPropertyFactory ppf, List p, boolean indexed, int key) {
		parentPropertyName = ppn;
		parentPropertyFactory = ppf;
		properties = p;
		indexedChildren = indexed;
		keyIndex = key;

		columnNames = new String [p.size()];
		for(int i = 0; i < properties.size(); i++) {
			columnNames[i] = ((TableEntry) properties.get(i)).getColumnName();
		}
	}

	/** Initialize the model based on the contents of the parent bean.
	 *  This method is for models that specified a parent property name during
	 *  construction.
	 *
	 * @param parent The parent base bean that is storing the properties.  The
	 *   properties are expected to be stored under the property name specified
	 *   by the constructor parameter 'parentPropertyName' which means you must
	 *   use that variant for this method to make sense.
	 */
	public void setData(CommonDDBean parent, ASDDVersion asVersion) {
        assert asVersion != null : "Application Server version cannot be null!!!";
        appServerVersion = asVersion;
        
		if(parentPropertyName != null) {
			CommonDDBean [] children = (CommonDDBean []) parent.getValues(parentPropertyName);
			setData(parent, children);
		}
	}

	/** Initialize the model from the list of objects passed in.  The items in
	 *  the list are expected to be instances of the property class specified in
	 *  the constructor.
	 *
	 * @param rows Generally this method is used when there is no parent CommonDDBean,
	 *   though that is not required.  What is required is that all objects
	 *   in the List are castable to the class type that is created by the factory
     *   method in the instance of ParentPropertyFactory passed in.
	 */
	public void setData(List rows, ASDDVersion asVersion) {
        assert asVersion != null : "Application Server version cannot be null!!!";
        appServerVersion = asVersion;
        
		CommonDDBean [] children = null;

		if(rows != null) {
			children = (CommonDDBean []) rows.toArray(new CommonDDBean[0]);
		}

		setData(null, children);
	}

	/** Initialize the model from the array of CommonDDBeans passed in.  The items in
	 *  the array are expected to be instances of the property class specified in
	 *  the constructor.
	 *
	 * @param rows Generally this method is used when there is no parent CommonDDBean,
	 *   though that is not required.  What is required is that all objects
	 *   in the List are castable to the class type that is created by the factory
     *   method in the instance of ParentPropertyFactory passed in.
	 */
	public void setData(CommonDDBean [] rows, ASDDVersion asVersion) {
        assert asVersion != null : "Application Server version cannot be null!!!";
        appServerVersion = asVersion;
        
		CommonDDBean [] children = null;

		if(rows != null && rows.length > 0) {
            children = new CommonDDBean [rows.length];
            System.arraycopy(rows, 0, children, 0, rows.length);
		}

		setData(null, children);
	}

	/** Initialize the model from the parent property bean, but in this case,
	 *  the parent is the actual owner of the list of properties because all
	 *  the fields are attributes of the parent, not of a child bean.  This
	 *  method is for models that specified only a parent property name but
	 *  no parent property class, so by definition the children are stored
	 *  as indexed properties of the bean instance passed in here.
	 */
	public void setDataBaseBean(CommonDDBean parent, ASDDVersion asVersion) {
        assert asVersion != null : "Application Server version cannot be null!!!";
        appServerVersion = asVersion;
        
		setData(parent, new CommonDDBean [0]);
		fireTableDataChanged();
	}

	public List getData() {
		return getChildren();
	}

	public CommonDDBean getDataBaseBean() {
		CommonDDBean bbParent = null;

		Object parent = getParent();
		if(parent instanceof CommonDDBean) {
			bbParent = (CommonDDBean) parent;
		}

		return bbParent;
	}
    
	public ASDDVersion getAppServerVersion() {
		return appServerVersion;
	}

	public int getRowCount() {
		int result = super.getRowCount();

		// If we are using the parent property object to hold the children, then
		// this is the correct way to get the number of rows.
		if(indexedChildren) {
			CommonDDBean parent = (CommonDDBean) getParent();
			// !PW I found this getting called during initialization phase (when the
			// parent has not been assigned yet.)
			if(parent != null) {
				result = parent.size(parentPropertyName);
			}
		}

		return result;
	}

	protected String[] getColumnNames() {
		return columnNames;
	}

	protected List getPropertyDefinitions() {
		return properties;
	}

	public Object getValueAt(int row, int column){
		Object result = null;
		TableEntry columnEntry = (TableEntry) properties.get(column);

		if(indexedChildren) {
			CommonDDBean parent = (CommonDDBean) getParent();
//			result = parent.getAttributeValue(parentPropertyName, row, columnEntry.getPropertyName());
			result = columnEntry.getEntry(parent, row);
		} else {
			CommonDDBean param = (CommonDDBean) getChildren().get(row);
			if(param != null) {
				result = columnEntry.getEntry(param);
			}
		}

		return result;
	}

	/** BeanTableModel Methods
	 */
	public Object addRow(Object[] values) {
		CommonDDBean param = null;

		if(parentPropertyFactory != null) {
			param = parentPropertyFactory.newParentProperty(appServerVersion);
			for(int i = 0, max = properties.size(); i < max; i++) {
				((TableEntry) properties.get(i)).setEntry(param, values[i]);
			}
			if(parentPropertyName != null) {
				((CommonDDBean) getParent()).addValue(parentPropertyName, param);
			}

			getChildren().add(param);
		} else if(indexedChildren) {
			CommonDDBean parent = (CommonDDBean) getParent();
			int newRow = parent.addValue(parentPropertyName, Boolean.TRUE);
			for(int i = 0, max = properties.size(); i < max; i++) {
				TableEntry columnEntry = (TableEntry) properties.get(i);
//				parent.setAttributeValue(parentPropertyName, newRow, columnEntry.getPropertyName(), (String) values[i]);
				columnEntry.setEntry(parent, newRow, values[i]);
			}
		}
                
		int rowChanged = getRowCount() - 1;
		fireTableRowsInserted(rowChanged, rowChanged);

		return param;
	}

	public void editRow(int row, Object[] values) {
		if(indexedChildren) {
			CommonDDBean parent = (CommonDDBean) getParent();
			for(int i = 0, max = properties.size(); i < max; i++) {
				TableEntry columnEntry = (TableEntry) properties.get(i);
//				parent.setAttributeValue(parentPropertyName, row, columnEntry.getPropertyName(), (String) values[i]);
				columnEntry.setEntry(parent, row, values[i]);
			}
		} else {
			CommonDDBean param = (CommonDDBean) getChildren().get(row);

			if(param != null) {
				for(int i = 0; i < properties.size(); i++) {
					((TableEntry) properties.get(i)).setEntry(param, values[i]);
				}
			}
		}

		fireTableRowsUpdated(row, row);
	}

	public void removeRow(int row) {
		CommonDDBean parent = (CommonDDBean) getParent();

		if(indexedChildren) {
			parent.removeValue(parentPropertyName, row);
		} else {
			List children = getChildren();

			if(parentPropertyName != null) {
				parent.removeValue(parentPropertyName, children.get(row));
			}

			children.remove(row);
		}

		fireTableRowsDeleted(row, row);
	}


	public Object[] getValues(int row) {
		int numColumns = properties.size();
		Object[] values = new Object[numColumns];

		if(indexedChildren) {
			CommonDDBean parent = (CommonDDBean) getParent();

			for(int i = 0; i < numColumns; i++) {
				TableEntry columnEntry = (TableEntry) properties.get(i);
//				values[i] = parent.getAttributeValue(parentPropertyName, row, columnEntry.getPropertyName());
				values[i] = columnEntry.getEntry(parent, row);
			}
		} else {
			CommonDDBean param = (CommonDDBean) getChildren().get(row);

			if(param != null) {
				for(int i = 0; i < numColumns; i++) {
					values[i] = ((TableEntry) properties.get(i)).getEntry(param);
				}
			}
		}

		return values;
	}

	public boolean alreadyExists(Object[] values) {
		boolean exists = false;

		if(keyIndex != -1 && values != null && values[keyIndex] != null) {
			return alreadyExistsImpl(values[keyIndex].toString());
		}

		return exists;
	}

	public boolean alreadyExists(String keyPropertyValue) {
		boolean exists = false;

		if(keyIndex != -1) {
			exists = alreadyExistsImpl(keyPropertyValue);
		}

		return exists;
	}

	private boolean alreadyExistsImpl(String keyPropertyValue) {
		boolean exists = false;

		if(keyPropertyValue != null) {
			TableEntry entry = (TableEntry) properties.get(keyIndex);
			if(indexedChildren) {
				CommonDDBean parent = (CommonDDBean) getParent();
//				String indexPropertyName = entry.getPropertyName();

				for(int i = 0, count = getRowCount(); i < count; i++) {
//						if(keyPropertyValue.equals((String) parent.getAttributeValue(parentPropertyName, i, indexPropertyName))) {
					if(keyPropertyValue.equals((String) entry.getEntry(parent, i))) {
						exists = true;
						break;
					}
				}
			} else {
				for(int i = 0, count = getRowCount(); i < count; i++) {
					CommonDDBean rowBean = (CommonDDBean) getChildren().get(i);
					if(rowBean != null) {
						if(keyPropertyValue.equals(entry.getEntry(rowBean))) {
							exists = true;
							break;
						}
					}
				}
			}
		}

		return exists;
	}

	/** Nested classes used to define columns and how to manipulate the data
	 *  stored there.
	 */
	public static abstract class TableEntry {
		protected final ResourceBundle bundle;
		protected final String resourceBase;
		protected final String parentPropertyName;
		protected final String propertyName;
		protected final String columnName;
		protected final boolean requiredFieldFlag;
		protected final boolean nameFieldFlag;

		public TableEntry(String pn, String c) {
			this(pn, c, false);
		}

		public TableEntry(String pn, String c, boolean required) {
			this(null, pn, c, required);
		}

		public TableEntry(String ppn, String pn, String c, boolean required) {
			this(ppn, pn, c, required, false);
		}

		public TableEntry(String ppn, String pn, String c, boolean required, boolean isName) {
			parentPropertyName = ppn;
			bundle = null;
			resourceBase = null;
			propertyName = pn;
			columnName = c;
			requiredFieldFlag = required;
			nameFieldFlag = isName;
		}

		public TableEntry(String ppn, String pn, ResourceBundle resBundle,
				String base, boolean required, boolean isName) {
			parentPropertyName = ppn;
			propertyName = pn;
			bundle = resBundle;
			resourceBase = base;
			columnName = bundle.getString("LBL_" + resourceBase);	// NOI18N
			requiredFieldFlag = required;
			nameFieldFlag = isName;
		}

		public String getPropertyName() {
			return propertyName;
		}

		public String getColumnName() {
			return columnName;
		}

		public boolean isRequiredField() {
			return requiredFieldFlag;
		}

		public boolean isNameField() {
			return nameFieldFlag;
		}
                
		public String getLabelName() {
			return columnName + " :";	// NOI18N
		}

		public char getLabelMnemonic() {
			assert bundle != null : "Coding error: incorrect column definition for " + columnName;	// NOI18N
			return bundle.getString("MNE_" + resourceBase).charAt(0);	// NOI18N
		}

		public String getAccessibleName() {
			assert bundle != null : "Coding error: incorrect column definition for " + columnName;	// NOI18N
			return bundle.getString("ACSN_" + resourceBase);	// NOI18N
		}

		public String getAccessibleDescription() {
			assert bundle != null : "Coding error: incorrect column definition for " + columnName;	// NOI18N
			return bundle.getString("ACSD_" + resourceBase);	// NOI18N
		}

		public abstract Object getEntry(CommonDDBean parent);
		public abstract void setEntry(CommonDDBean parent, Object value);

		public abstract Object getEntry(CommonDDBean parent, int row);
		public abstract void setEntry(CommonDDBean parent, int row, Object value);

	}

	/** Use this class for a column if the entry is stored as a value in
	 *  the parent bean class.
	 */
	public static class ValueEntry extends TableEntry {
		public ValueEntry(String pn, String c) {
			super(pn, c, false);
		}

		public ValueEntry(String pn, String c, boolean required) {
			super(pn, c, required);
		}

		public ValueEntry(String ppn, String pn, String c, boolean required) {
			super(ppn, pn, c, required);
		}

		public ValueEntry(String ppn, String pn, String c, boolean required, boolean isName) {
			super(ppn, pn, c, required, isName);
		}

		public ValueEntry(String ppn, String pn, ResourceBundle resBundle,
				String resourceBase, boolean required, boolean isName) {
			super(ppn, pn, resBundle, resourceBase, required, isName);
		}

		public Object getEntry(CommonDDBean parent) {
			return parent.getValue(propertyName);
		}

		public void setEntry(CommonDDBean parent, Object value) {
            if(value instanceof String && ((String) value).length() == 0) {
                value = null;
            }
			parent.setValue(propertyName, value);
		}

		public Object getEntry(CommonDDBean parent, int row) {
			return parent.getValue(propertyName, row);
		}

		public void setEntry(CommonDDBean parent, int row, Object value) {
			parent.setValue(propertyName, row, value);
		}

	}

	/** Use this class for a column if the entry is stored as an attribute in
	 *  the parent bean class.
	 */
	public static class AttributeEntry extends TableEntry {
		public AttributeEntry(String pn, String c) {
			super(pn, c);
		}

		public AttributeEntry(String pn, String c, boolean required) {
			super(pn, c, required);
		}

		public AttributeEntry(String ppn, String pn, String c, boolean required) {
			super(ppn, pn, c, required);
		}

		public AttributeEntry(String ppn, String pn, String c, boolean required, boolean isName) {
			super(ppn, pn, c, required, isName);
		}

		public AttributeEntry(String ppn, String pn, ResourceBundle resBundle,
				String resourceBase, boolean required, boolean isName) {
			super(ppn, pn, resBundle, resourceBase, required, isName);
		}

                public Object getEntry(CommonDDBean parent) {
			return parent.getAttributeValue(propertyName);
		}

		public void setEntry(CommonDDBean parent, Object value) {
			String attrValue = null;
			if(value != null) {
				attrValue = value.toString();
			}
			parent.setAttributeValue(propertyName, attrValue);
		}

		public Object getEntry(CommonDDBean parent, int row) {
			return parent.getAttributeValue(parentPropertyName, row, propertyName);
		}

		public void setEntry(CommonDDBean parent, int row, Object value) {
			String attrValue = null;
			if(value != null) {
				attrValue = value.toString();
			}

			parent.setAttributeValue(parentPropertyName, row, propertyName, attrValue);
            // !PW FIXME I think Cliff Draper fixed the bug this was put in for... we'll see.
            // The issue was that attributes that were children of non-property objects and
            // thus were attached to a boolean array, needed to have the boolean set to true
            // in order to be recognized, otherwise, it was as if they did not exist.
            // attributes of real properties (those that have non-attribute children as well)
            // work fine regardless.
//			if(Common.isBoolean(parent.beanProp(parentPropertyName).getType())) {
//				parent.setValue(parentPropertyName, row, Boolean.TRUE);
//			}
		}
	}


    /** New interface added for migration to sun-* DD API model.  If the backing
     *  model stores the properties in a parent property, then this is the factory
     *  for creating instances of the parent to store each row, as added by the
     *  user.
     */
    public interface ParentPropertyFactory {

        /* Implement this method to return a new blank instance of the correct
         * bean type, e.g. WebserviceEndpoint, etc.
         */
        public CommonDDBean newParentProperty(ASDDVersion asVersion);

    }
}
