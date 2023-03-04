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

package org.netbeans.modules.j2ee.common;

import java.util.HashSet;
import java.util.Set;
import javax.swing.JComboBox;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;

/**
 *
 * @author Libor Kotouc
 */
public class DatasourceUIHelperTest extends NbTestCase {
    
    private DatasourceImpl mds1;
    private DatasourceImpl mds2;
    private DatasourceImpl mds3;
    private DatasourceImpl sds1;
    private DatasourceImpl sds2;
    private DatasourceImpl sds3;
    private DatasourceImpl sds4;
    
    J2eeModuleProviderImpl provider;
    
    public DatasourceUIHelperTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    private void initDatasources() {
        mds1 = new DatasourceImpl("mds1", "mds1_url", "mds1_user", "mds1_pass", "mds1_clz"); // NOI18N
        mds2 = new DatasourceImpl("mds2", "mds2_url", "mds2_user", "mds2_pass", "mds2_clz"); // NOI18N
        mds3 = new DatasourceImpl("mds3", "mds3_url", "mds3_user", "mds3_pass", "mds3_clz"); // NOI18N
        Set<Datasource> moduleDatasources = new HashSet<Datasource>();
        moduleDatasources.add(mds1);
        moduleDatasources.add(mds2);
        moduleDatasources.add(mds3);
        sds1 = new DatasourceImpl("sds1", "sds1_url", "sds1_user", "sds1_pass", "sds1_clz"); // NOI18N
        sds2 = new DatasourceImpl("sds2", "sds2_url", "sds2_user", "sds2_pass", "sds2_clz"); // NOI18N
        sds3 = new DatasourceImpl("sds3", "sds3_url", "sds3_user", "sds3_pass", "sds3_clz"); // NOI18N
        //copy of mds3 for merging verification
        sds4 = new DatasourceImpl("mds3", "mds3_url", "mds3_user", "mds3_pass", "mds3_clz"); // NOI18N
        Set<Datasource> serverDatasources = new HashSet<Datasource>();
        serverDatasources.add(sds1);
        serverDatasources.add(sds2);
        serverDatasources.add(sds3);
        serverDatasources.add(sds4);
        
        provider = new J2eeModuleProviderImpl(moduleDatasources, serverDatasources);
    }
    
    private JComboBox connect() {
        JComboBox combo = new JComboBox();
        DatasourceUIHelper.connect(provider, combo);
        return combo;
    }

    public void testEmptyComboboxContentWithCreation() {
        provider = new J2eeModuleProviderImpl(new HashSet<Datasource>(), new HashSet<Datasource>());
        
        JComboBox combo = connect();
        
        assertTrue("Wrong number of items in the empty combobox", combo.getItemCount() == 1);
        assertTrue("null is not selected by default.", combo.getSelectedItem() == null);
        assertTrue("NEW_ITEM must be the only item in the empty combobox", combo.getItemAt(0) == DatasourceUIHelper.NEW_ITEM);
        
    }   

    public void testEmptyComboboxContentWithoutCreation() {
        provider = new J2eeModuleProviderImpl(new HashSet<Datasource>(), new HashSet<Datasource>(), false);//do not allow creation of new data source
        
        JComboBox combo = connect();
        
        assertTrue("Wrong number of items in the empty combobox", combo.getItemCount() == 0);
    
    }
    
    public void testNonEmptyCombobox() {
        
        initDatasources();
        
        JComboBox combo = connect();
        
        assertTrue("Wrong number of items in the empty combobox", combo.getItemCount() == 8);
        for (int i = 0; i < 5; i++) { // number of items w/o separator and add new... == 6
            String jndiName_i = ((Datasource)combo.getItemAt(i)).getJndiName();
            String jndiName_ipp = ((Datasource)combo.getItemAt(i+1)).getJndiName();
            assertTrue("Items in combobox are not alphabetically ordered by JNDI name", jndiName_i.compareToIgnoreCase(jndiName_ipp) < 0);
        }
        
    }
    
    protected boolean runInEQ() {
        return true;
    }
    
}
