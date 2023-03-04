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

package org.netbeans.modules.websvc.manager.model;

import java.util.LinkedList;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.websvc.manager.test.EventFlag;
import org.netbeans.modules.websvc.manager.test.SetupData;
import org.netbeans.modules.websvc.manager.test.SetupUtil;

/**
 *
 * @author quynguyen
 */
public class WebServiceListModelTest extends NbTestCase {
    private SetupData setupData;
    
    public WebServiceListModelTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setupData = SetupUtil.commonSetUp(getWorkDir());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        SetupUtil.commonTearDown();
    }

    /**
     * Test of getInstance method, of class WebServiceListModel.
     */
    public void testGetInstance() {
        System.out.println("getInstance");
        WebServiceListModel expResult = WebServiceListModel.getInstance();
        WebServiceListModel result = WebServiceListModel.getInstance();
        assertEquals(expResult, result);
    }

    /**
     * Test of addWebServiceListModelListener method, of class WebServiceListModel.
     */
    public void testAddWebServiceListModelListener()  {
        System.out.println("addWebServiceListModelListener");
        try {
            final EventFlag<Boolean> groupAdded = new EventFlag<Boolean>(Boolean.FALSE);
            final EventFlag<Boolean> groupRemoved = new EventFlag<Boolean>(Boolean.FALSE);

            WebServiceListModelListener listener = new WebServiceListModelListener() {

                        public void webServiceGroupAdded(WebServiceListModelEvent modelEvent) {
                            groupAdded.setData(Boolean.TRUE);
                            groupRemoved.setData(Boolean.FALSE);
                        }

                        public void webServiceGroupRemoved(WebServiceListModelEvent modelEvent) {
                            groupRemoved.setData(Boolean.TRUE);
                            groupAdded.setData(Boolean.FALSE);
                        }
                    };

            WebServiceListModel instance = WebServiceListModel.getInstance();
            instance.addWebServiceListModelListener(listener);

            assertTrue("Listener not added to set", instance.listeners.contains(listener));
            assertTrue("Listener list size not correct after add", instance.listeners.size() == 1);        
            
            WebServiceGroup testGroup = new WebServiceGroup("testid_01110"); 
            instance.addWebServiceGroup(testGroup);
            
            final int COUNTER_MAX = 1500;
            int counter = 0;
            
            while (counter < COUNTER_MAX && groupAdded.getData().equals(Boolean.FALSE)) {
                counter += 100;
                try {
                    Thread.sleep(100);
                }catch (Exception ex) {
                }
            }
            
            assertTrue("listener webServiceGroupAdded event not fired", groupAdded.getData().equals(Boolean.TRUE));
            assertTrue("listener webServiceGroupRemoved event fired when not needed", groupRemoved.getData().equals(Boolean.FALSE));
            
            instance.removeWebServiceGroup(testGroup.getId());
            counter = 0;
            while (counter < COUNTER_MAX && groupRemoved.getData().equals(Boolean.FALSE)) {
                counter += 100;
                try {
                    Thread.sleep(100);
                }catch (Exception ex) {
                }
            }

            assertTrue("listener webServiceGroupRemoved event not fired", groupRemoved.getData().equals(Boolean.TRUE));
            assertTrue("listener webServiceGroupAdded event fired when not needed", groupAdded.getData().equals(Boolean.FALSE));
        }finally {
            WebServiceListModel instance = WebServiceListModel.getInstance();
            instance.listeners.clear();
            instance.getWebServiceGroupSet().clear();
        }
    }

    /**
     * Test of getUniqueWebServiceId method, of class WebServiceListModel.
     */
    public void testGetUniqueWebServiceId() {
        System.out.println("getUniqueWebServiceId");
        WebServiceListModel instance = WebServiceListModel.getInstance();
        List<String> generatedIds = new LinkedList<String>();
        
        for (int i = 0; i < 5; i++) {
            generatedIds.add(instance.getUniqueWebServiceId());
        }
        
        for (int i = 0; i < 5; i++) {
            String id = generatedIds.remove(0);
            boolean exists = false;
            for (String remaining : generatedIds) {
                exists = exists || remaining.equals(id);
            }
            
            assertFalse("Generated a non-unique web service id", exists);
        }
    }

    /**
     * Test of getUniqueWebServiceGroupId method, of class WebServiceListModel.
     */
    public void testGetUniqueWebServiceGroupId() {
        System.out.println("getUniqueWebServiceGroupId");
        WebServiceListModel instance = WebServiceListModel.getInstance();
        List<String> generatedIds = new LinkedList<String>();
        
        for (int i = 0; i < 5; i++) {
            generatedIds.add(instance.getUniqueWebServiceGroupId());
        }
        
        for (int i = 0; i < 5; i++) {
            String id = generatedIds.remove(0);
            boolean exists = false;
            for (String remaining : generatedIds) {
                exists = exists || remaining.equals(id);
            }
            
            assertFalse("Generated a non-unique web service id", exists);
        }
    }
}
