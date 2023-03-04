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

//import org.netbeans.modules.debugger.jpda.ui.models.SourcesModel.AbstractColumn;
import org.netbeans.api.debugger.Properties;
import org.netbeans.spi.debugger.ui.ColumnModelRegistration;
import org.netbeans.spi.viewmodel.ColumnModel;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Entlicher
 */
@ColumnModelRegistration(path="netbeans-JPDASession/EventsView")
public class EventsColumnModel extends ColumnModel {

    private Properties properties = Properties.getDefault ().
            getProperties ("debugger").getProperties ("views"); // NOI18N
    
    @Override
    public String getID() {
        return "EventsViewNameColumn";
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(EventsColumnModel.class, "LBL_EventsColumnName");
    }

    @Override
    public Class getType() {
        return null;
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
