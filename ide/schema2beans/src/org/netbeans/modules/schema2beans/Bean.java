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

package org.netbeans.modules.schema2beans;

import java.beans.*;

/**
 * @author cliffwd
 *
 * All generated beans that use the runtime will implement this interface.
 * It allows for some navigation and reflection.
 */
public interface Bean {
    public void addPropertyChangeListener(PropertyChangeListener l);
    public void removePropertyChangeListener(PropertyChangeListener l);

    /**
     * @return a representation of the property.  This method does not return
     * null.  If there is no object available for the specified
     * property name, an exception is thrown.
     */
    public BeanProp beanProp(String name);

    /**
     * @return the schema name of this bean as define by this bean's parent.
     */
    public String dtdName();
    public boolean isRoot();
    public Bean _getParent();
    public Bean _getRoot();

    /**
     *	Return the bean name of this graph node.
     */
    public String name();
    public boolean hasName(String name);
    public int indexToId(String name, int index);
    public int idToIndex(String name, int id);
    public Bean propertyById(String name, int id);
    public Object getValueById(String name, int id);
    public void setValue(String name, Object value);
    public void setValue(String name, int index, Object value);
    public void setValueById(String name, int id, Object value);
    public int removeValue(String name, Object value);
    public int addValue(String name, Object value);
    public Object getValue(String name);
    public Object getValue(String name, int index);
    public Object[] getValues(String name);
    public BaseProperty getProperty();
    public BaseProperty getProperty(String propName);
    public BaseProperty[] listProperties();

    /**
     * Find all child beans and put them into the give beans List.
     */
    public void childBeans(boolean recursive, java.util.List beans);
}
