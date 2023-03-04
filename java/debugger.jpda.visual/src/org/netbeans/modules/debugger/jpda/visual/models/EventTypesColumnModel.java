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
package org.netbeans.modules.debugger.jpda.visual.models;

import org.netbeans.api.debugger.Properties;
import org.netbeans.spi.debugger.ui.ColumnModelRegistration;
import org.netbeans.spi.viewmodel.ColumnModel;
import org.openide.util.NbBundle;

/**
 * Column model representing event types.
 * 
 * @author Martin Entlicher
 */
@ColumnModelRegistration(path="netbeans-JPDASession/EventsView")
public class EventTypesColumnModel extends ColumnModel {
    
    static final String ID = "EventsViewTypeColumn";    // NOI18N

    private Properties properties = Properties.getDefault ().
            getProperties ("debugger").getProperties ("views"); // NOI18N
    
    @Override
    public String getID() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(EventTypesColumnModel.class, "LBL_EventTypesColumnName");
    }

    @Override
    public String getShortDescription() {
        return NbBundle.getMessage(EventTypesColumnModel.class, "LBL_EventTypesColumnDescr");
    }
    
    @Override
    public Class getType() {
        return String.class;
    }

    @Override
    public boolean isVisible() {
        return properties.getBoolean (getID () + ".visible", false);    // NOI18N
    }
    
    @Override
    public void setVisible (boolean visible) {
        properties.setBoolean (getID () + ".visible", visible);         // NOI18N
    }

    @Override
    public int getCurrentOrderNumber() {
        int cn = properties.getInt(getID() + ".currentOrderNumber", -1);
        return cn;
    }

    @Override
    public void setCurrentOrderNumber(int newOrderNumber) {
        properties.setInt(getID() + ".currentOrderNumber", newOrderNumber);
    }


}
