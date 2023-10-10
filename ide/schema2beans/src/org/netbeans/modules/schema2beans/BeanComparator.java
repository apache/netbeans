/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.schema2beans;

import java.util.*;

/**
 *  The class BeanComparator is the default comparator implementation
 *  for comparing and merging schema2beans graphs. This class has mainly two
 *  methods. One for deciding if two BaseBean objects are identicals and
 *  a second one to decide if two properties are identicals.
 *
 *  These methods return either the original value to signal that they are
 *  the same or the second value, to signal that the two elements compared
 *  are not the same.
 *
 *  This default implementation compares both the propertie and attribute
 *  values to decide for equality. This implementation also uses the key
 *  information that might be defined in the mdd file (see user doc for this
 *  file usage). If the key is specified (default is all properties are keys),
 *  then only the properties defined as keys are used during the comparison.
 */
public class BeanComparator {
    //
    //	This is set to true if the processing of compareBean or
    //	compareProperty uses at least one key element.
    //	The caller can use this information to know if the result of a
    //	comparison is due to a real equality or simply a lack of key.
    //
    private boolean hasKey = false;
    
    //
    //	Specify if the bean comparator should use the keys specify
    //	in the mdd file. The default value is true.
    //	If this is set to false, any property is considered a key in
    //	in the comparison, regardless of what has been specified in the mdd.
    //
    private boolean useMddKeys = true;
    
    //	Same as above, but take precedence over it.
    static boolean useComparatorsMddKeys = true;
    
    /**
     *	Default comparison implementation for comparing two beans.
     *	The two beans are considered identical if all its non-bean properties
     *	are identicals.
     */
    public BaseBean compareBean(String 		beanName,
				BaseBean 	curBean,
				BaseBean 	newBean) {
				    
	BaseBean	ret = curBean;
	Iterator 	it = curBean.beanPropsIterator();
	boolean		useKeys = useMddKeys;
	
	this.hasKey = false;
	
	if (!useComparatorsMddKeys)
	    useKeys = false;

	if (curBean.getProperty() != null 
	    && curBean.getProperty().isKey()) {

	    //  Check the attributes first
	    BaseAttribute[] ba = curBean.listAttributes();

	    if (ba != null) {
		for(int j=0; j<ba.length; j++) {
		    if (!ba[j].isFixed()) {
			String attrName = ba[j].getName();
			String curValue = curBean.getAttributeValue(attrName);
			String otherValue = newBean.getAttributeValue(attrName);

			if (!Objects.equals(curValue, otherValue)) {
				//  Diffenrence found - not the same bean
				return newBean;
			}
		    }
		}
	    }
	}
	
	while (it.hasNext()) {
	    //	Get our next property (as a BeanProp)
	    BeanProp prop = (BeanProp)it.next();
	    
	    if (prop == null)
		continue;
	    
	    String 	name = prop.getBeanName();
	    boolean 	isArray = Common.isArray(prop.type);
	    boolean 	isBean = Common.isBean(prop.type);
	    boolean 	isKey = Common.isKey(prop.type) || !useKeys;
	    Object	o1, o2, o3;
	    
	    if (!this.hasKey && isKey)
		this.hasKey = true;
	    
	    if (isArray && !isBean && isKey) {
		//
		//	An array of non-bean properties. Do a trivial comparison
		//	of the array.
		//
		int 	size1 = prop.size();
		int 	size2 = newBean.size(name);
		
		if (size1 != size2) {
		    //	Diffenrence found - not the same bean
		    ret = newBean;
		    break;
		}
		
		for (int i=0; i<size1; i++) {
		    o1 = prop.getValue(i);
		    o2 = newBean.getValue(name, i);
		    o3 = this.compareProperty(name, curBean, o1, i,
					      newBean, o2, i);
		    if (o3 != o1) {
			//	Diffenrence found - not the same bean
			ret = newBean;
			break;
		    }
		}
	    }
	    else
		if (!isBean && isKey) {
		    o1 = prop.getValue(0);
		    o2 = newBean.getValue(name);
		    o3 = this.compareProperty(name, curBean, o1, -1,
					      newBean, o2, -1);
		    if (o3 != o1) {
			//	Diffenrence found - not the same bean
			ret = newBean;
		    }
		}
	}
	
	if (DDLogFlags.debug) {
	    TraceLogger.put(TraceLogger.DEBUG,
	    TraceLogger.SVC_DD,
	    DDLogFlags.DBG_BLD, 5,
	    DDLogFlags.BEANCOMP,
	    beanName + ": " +
	    ((ret == curBean)? "same":"different"));
	}
	
	return ret;
    }
    
    /**
     *	Default comparison implementation for comparing two property values.
     * @return curValue if the same, newValue if different.
     */
    public Object compareProperty(String 	propertyName,
                                  BaseBean 	curBean,
                                  Object 	curValue,
                                  int		curIndex,
                                  BaseBean	newBean,
                                  Object 	newValue,
                                  int		newIndex) {
        Object ret = curValue;
        BeanProp prop = curBean.beanProp(propertyName);
        boolean isKey = this.hasKeyDefined(prop);
	
        // Values are the same - check their attributes
        if (isKey) {
            if (curValue == null || !curValue.equals(newValue))
                ret = newValue;

            String[] attrs = curBean.getAttributeNames(propertyName);
            int i1 = 0;
            int i2 = 0;

            if (curIndex != -1) {
                i1 = curIndex;
                i2 = newIndex;
            }

            for(int j=0; j<attrs.length; j++) {
                String a = attrs[j];

                String v1 = curBean.getAttributeValue(propertyName, i1, a);
                String v2 = newBean.getAttributeValue(propertyName, i2, a);

                if (v1 != null) {
                    if (!v1.equals(v2)) {
                        ret = newValue;
                        break;
                    }
                } else if (v2 != v1) {
                    ret = newValue;
                    break;
                }
		
            }
        } else {
        }
	
        if (DDLogFlags.debug) {
            TraceLogger.put(TraceLogger.DEBUG,
                            TraceLogger.SVC_DD,
                            DDLogFlags.DBG_BLD, 5,
                            DDLogFlags.PROPCOMP,
                            propertyName + " - " +
                            ((curValue==null)?"<null>":curValue) +
                            ((curIndex==-1)?"":("."+curIndex)) + " / " +
                            ((newValue==null)?"<null>":newValue) +
                            ((newIndex==-1)?"":("."+newIndex)) + " " +
                            ((ret == curValue)? "same":"different") +
                            " (" +((isKey)?"Key":"!Key") + ")");
        }
	
        return ret;
    }
    
    //
    //	Returns true if one of the element compared during the last call
    //	of compareBean and/or compareProperty used a key.
    //
    protected boolean hasKey() {
	return this.hasKey;
    }
    
    //
    //	Return if the key should be used comparing this element.
    //
    boolean hasKeyDefined(BeanProp prop) {
	boolean	useKeys = useMddKeys;
	
	if (!useComparatorsMddKeys)
	    useKeys = false;
	
	this.hasKey = Common.isKey(prop.type) || !useKeys;
	return this.hasKey;
    }
    
    public void enableKey(boolean b) {
	this.useMddKeys = b;
    }
    
    public static void enableComparatorsKey(boolean b) {
	useComparatorsMddKeys = b;
    }
    
}












