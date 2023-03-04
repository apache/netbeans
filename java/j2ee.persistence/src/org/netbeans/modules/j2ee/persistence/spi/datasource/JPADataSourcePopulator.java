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

package org.netbeans.modules.j2ee.persistence.spi.datasource;

import javax.swing.JComboBox;

/**
 * This interface provides support for populating a combo box that
 * represents available data sources. Itshould be implemented by projects 
 * where it is possible to use data sources.  
 * 
 * @author Erno Mononen
 */
public interface JPADataSourcePopulator {

    /**
     * Populates the given <code>comboBox</code> with <code>JPADataSource</code>s
     * and with items for managing data sources. The items representing the actual
     * data sources must be instances of <code>JPDDataSource</code>. 
     * @param comboBox the combo box to be populated.
     */ 
    void connect(JComboBox comboBox);
    
}
