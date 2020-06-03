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

package org.netbeans.modules.cnd.debugger.common2.debugger;

import java.beans.PropertyEditor;
import org.netbeans.spi.viewmodel.ColumnModel;
import org.netbeans.api.debugger.Properties;


/**
 * Viewmodel.ColumnModel seems to have less-than-useful default
 * implementations. So we have this to control our default implementations.
 *
 * See also
 * org.netbeans.modules.debugger.ui.models.ColumnModels.AbstractColumn
 */

abstract public class AbstractColumnModel extends ColumnModel {

    private String id = null;
    private boolean defaultVisible = false;
    private String displayName;
    private String shortDescription;
    private Class type;
    private PropertyEditor propertyEditor;
    
    private boolean sorted = false;
    private boolean sortedDescending = false;
    
    Properties properties = Properties.getDefault ().
		getProperties ("debugger").getProperties ("views"); // NOI18N

    protected AbstractColumnModel () {
    }

    protected AbstractColumnModel (String id, 
				   String displayName,
				   String shortDescription,
				   Class type,
				   boolean defaultVisible,
				   PropertyEditor propertyEditor) {
	this.id = id;
	this.displayName = displayName;
	this.shortDescription = shortDescription;
	this.type = type;
	this.defaultVisible = defaultVisible;
	this.propertyEditor = propertyEditor;
    }

    // interface ColumnModel
    @Override
    public void setCurrentOrderNumber(int newOrderNumber) {
	// 6688737
	properties.setInt (getID () + ".currentOrderNumber",newOrderNumber); // NOI18N
    }

    // interface ColumnModel
    @Override
    public int getCurrentOrderNumber () {
	return properties.getInt (getID () + ".currentOrderNumber",-1); // NOI18N
    }

    // interface ColumnModel
    @Override
    public boolean isVisible() {
        return properties.getBoolean (getID () + ".visible", defaultVisible);
    }

    // interface ColumnModel
    @Override
    public void setVisible(boolean visible) {
        properties.setBoolean (getID () + ".visible", visible); // NOI18N
    } 


    // interface ColumnModel
    @Override
    public String getID() {
	return id;
    }

    // interface ColumnModel
    @Override
    public String getDisplayName() {
        return displayName; // NOI18N
    }

    // interface ColumnModel
    @Override
    public String getShortDescription() {
        return shortDescription; // NOI18N
    }


    // interface ColumnModel
    @Override
    public Class getType() {
	return type;
    }

    // interface ColumnModel
    @Override
    public java.beans.PropertyEditor getPropertyEditor() {
	return propertyEditor;
    }



    // interface ColumnModel
    @Override
    public boolean isSortable() {
	return false;
    }

    // interface ColumnModel
    @Override
    public void setSorted(boolean sorted) {
	properties.setBoolean (getID () + ".sorted", sorted); // NOI18N
    }

    // interface ColumnModel
    @Override
    public boolean isSorted() {
	return properties.getBoolean (getID () + ".sorted", false);
    }

    // interface ColumnModel
    @Override
    public void setSortedDescending(boolean sortedDescending) {
	properties.setBoolean (
            getID () + ".sortedDescending", // NOI18N
            sortedDescending
        );
    }

    // interface ColumnModel
    @Override
    public boolean isSortedDescending() {
        return properties.getBoolean (
            getID () + ".sortedDescending", // NOI18N
            false
	);
    }

    // interface ColumnModel
    @Override
    public void setColumnWidth(int columnWidth) {
	properties.setInt (getID () + ".columnWidth", columnWidth); // NOI18N
    }

    // interface ColumnModel
    @Override
    public int getColumnWidth() {
	int width = properties.getInt (getID () + ".columnWidth", 150); // NOI18N
	return width;
    }
}
