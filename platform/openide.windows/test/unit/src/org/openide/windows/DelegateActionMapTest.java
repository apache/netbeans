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
package org.openide.windows;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author S. Aubrecht
 */
public class DelegateActionMapTest {

    public DelegateActionMapTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of allKeys method, of class DelegateActionMap.
     */
    @Test
    public void testNPEinAllKeys() {
        System.out.println( "allKeys" );
        TopComponent tc = new TopComponent();
        ActionMap delegate = new ActionMap();
        delegate.put( "test", new AbstractAction() {

            @Override
            public void actionPerformed( ActionEvent e ) {
                throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
            }
        });
        assertNotNull( delegate.allKeys() );
        DelegateActionMap instance = new DelegateActionMap( tc, delegate );
        Object[] result = instance.allKeys();
        assertNotNull( result );
        instance.clear();
        result = instance.allKeys();
        assertNotNull( result );
    }

}