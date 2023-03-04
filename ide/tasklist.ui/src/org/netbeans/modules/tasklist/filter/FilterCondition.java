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

package org.netbeans.modules.tasklist.filter;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JComponent;


/**
 * A condition (type and relation pair) in the filter.
 * Don't forget to override sameType if you extend this class!
 *
 * @author Tor Norbye
 */
abstract class FilterCondition {

    /**
     * Creates a condition.     
     */
    public FilterCondition() {
    }

    /**
     * Copy constructor. Use from subclassed clone.
     */
    protected FilterCondition(final FilterCondition rhs) {
    }

    /** 
     * Deep clone, please implement in subclass.
     */
    public abstract Object clone() ;
    
    
    /**
     * Compares two objects.
     *
     * @param obj value of the property
     */
    public abstract boolean isTrue(Object obj);

    /**
     * Creates a component that will represent a constant within the 
     * filter dialog. It should support {@link #PROP_VALUE_VALID}
     * client property.
     *
     * @return created component or null if no component 
     */
    public JComponent createConstantComponent() {
        return null;
    }

    /**
     * Gets constant from the specified component and save it.
     * This method should be also implemented if createConstantComponent()
     * is implemented.
     *
     * @param cmp with createConstantComponent() create component
     */
    public void getConstantFrom(JComponent cmp) {
        assert cmp != null : "getConstantFrom() is not implemented!"; //NOI18N
    }

    final boolean isValueValid(JComponent cmp) {
        Boolean valid = (Boolean) cmp.getClientProperty(PROP_VALUE_VALID);
        if (valid == null) {
            return true;
        } else {
            return valid.booleanValue();
        }
    }

    /**
     * Checks whether fc is of the same type.
     * This method will be used to replace a condition created with
     * Filter.getConditionsFor(Node.Property) with one contained in a filter.
     * This method should return true also if this and fc have different 
     * constants for comparing with property values.
     *
     * @param fc another condition
     * @return true fc is of the same type as this
     */
     public boolean sameType(FilterCondition fc) {
       return fc.getClass() == getClass();
     }
    
    public String toString() {
        return getClass().getName() + 
            "[name=" + getDisplayName() + "]"; // NOI18N
    }

    /** Use this client property on value/contant components to indicate valid user data.*/
    public static final String PROP_VALUE_VALID = "value-valid"; //NOI18N


    protected abstract String getDisplayName();
    
    abstract void load( Preferences prefs, String prefix ) throws BackingStoreException;
    
    abstract void save( Preferences prefs, String prefix ) throws BackingStoreException;
}

