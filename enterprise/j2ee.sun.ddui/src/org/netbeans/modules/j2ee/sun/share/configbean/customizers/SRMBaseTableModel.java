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
 * GroupTableModel.java
 *
 * Created on April 14, 2006, 10:29 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers;

import java.util.ResourceBundle;
import javax.swing.table.AbstractTableModel;
import org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer;
import org.openide.util.NbBundle;


/**
 *
 * @author Peter Williams
 */
public abstract class SRMBaseTableModel extends AbstractTableModel {
    
    protected final ResourceBundle customizerBundle = NbBundle.getBundle(
            "org.netbeans.modules.j2ee.sun.share.configbean.customizers.Bundle"); // NOI18N
    
    /** Model synchronizer
     */
    private final XmlMultiViewDataSynchronizer synchronizer;
    
    /** Reference to security-role-mapping instance
     */
    protected final SecurityRoleMapping mapping;
    
    public SRMBaseTableModel(XmlMultiViewDataSynchronizer s, SecurityRoleMapping m) {
        assert m != null;
        
        synchronizer = s;
        mapping = m;
    }

    protected void modelUpdatedFromUI() {
        if (synchronizer != null) {
            synchronizer.requestUpdateData();
        }
    }

    /** Model manipulation
     */
	public abstract void removeElements(int[] indices);
    
    
}
