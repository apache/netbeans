/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
