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
import org.netbeans.spi.tasklist.Task;

/**
 * This class represents a filter condition applied to a property.
 */
final class AppliedFilterCondition {

    private TaskProperty prop;
    private FilterCondition cond;

    AppliedFilterCondition(TaskProperty property, FilterCondition condition) {
        this.prop = property;
        this.cond = condition;
    }
    
    AppliedFilterCondition() {
    }

    public Object clone() {
        return new AppliedFilterCondition(prop, (FilterCondition)cond.clone());
    }

    public TaskProperty getProperty() { return prop;}
    public FilterCondition getCondition() { return cond;}
    
    /**
     * Tests if the condition is true on the property of task.
     * @param task the object to filter
     * @return true, if value of the property of <code>task</code>
     * defined by getProperty() passed the condition getCondition()
     */
    public boolean isTrue(Task task) {
        return cond.isTrue(prop.getValue(task));
    }
    
    public String toString() {
        return cond.toString() + " applied to " + prop.toString();//NOI18N
    }
    
    /**
     * Checks whether afc is of the same type.
     * This method will be used to replace a condition created with
     * Filter.getConditionsFor(Node.Property) with one contained in a filter.
     * This method should return true also if this and fc have different
     * constants for comparing with property values.
     *
     * @param fc another condition
     * @return true fc is of the same type as this
     */
    public boolean sameType(AppliedFilterCondition afc) {
        return getCondition().sameType(afc.getCondition()) && getProperty().equals(afc.getProperty());
    }

    void load( Preferences prefs, String prefix ) throws BackingStoreException {
        prop = TaskProperties.getProperty( prefs.get( prefix+"_propertyId", "" ) ); //NOI18N
        if( null == prop )
            throw new BackingStoreException( "Missing propertyId attribute" ); //NOI18N
        cond = KeywordsFilter.createCondition( prop );
        cond.load( prefs, prefix );
    }
    
    void save( Preferences prefs, String prefix ) throws BackingStoreException {
        prefs.put( prefix+"_propertyId", prop.getID() ); //NOI18N
        cond.save( prefs, prefix );
    }
}
