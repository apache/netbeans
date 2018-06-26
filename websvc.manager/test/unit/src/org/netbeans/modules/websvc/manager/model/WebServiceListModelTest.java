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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
