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

package org.netbeans.modules.quicksearch;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.net.URL;
import org.junit.Assert;
import org.openide.filesystems.Repository;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.xml.sax.SAXException;

/**
 * Allows tests to install own layers for testing.
 * Copied from org.netbeans.api.project.TestUtil.
 *
 * @author Dafe Simonek
 */
public class UnitTestUtils extends ProxyLookup {

    public static UnitTestUtils DEFAULT_LOOKUP = null;

    /** Creates a new instance of UnitTestUtils */
    public UnitTestUtils() {
        Assert.assertNull(DEFAULT_LOOKUP);
        DEFAULT_LOOKUP = this;
    }
    
    /** Makes global layer from given string resource info */
    public static void prepareTest(String[] stringLayers) 
                throws IOException, SAXException, PropertyVetoException {
        prepareTest(stringLayers, null);
    }
    
    public static void prepareTest (String[] stringLayers, Lookup lkp) 
                throws IOException, SAXException, PropertyVetoException {
        URL[] layers = new URL[stringLayers.length];
        
        for (int cntr = 0; cntr < layers.length; cntr++) {
            layers[cntr] = UnitTestUtils.class.getClassLoader().getResource(stringLayers[cntr]);
        }
        
        XMLFileSystem system = new XMLFileSystem();
        system.setXmlUrls(layers);
        
        Repository repository = new Repository(system);
        
        if (lkp == null) {
            UnitTestUtils.setLookup(new Object[] { repository }, UnitTestUtils.class.getClassLoader());
        } else {
            UnitTestUtils.setLookup(new Object[] { repository }, lkp, UnitTestUtils.class.getClassLoader());
        }
    }
    
    /**
     * Set the global default lookup with some fixed instances including META-INF/services/*.
     */
    private static void setLookup(Object[] instances, ClassLoader cl) {
        DEFAULT_LOOKUP.setLookups(new Lookup[] {
            Lookups.fixed(instances),
            Lookups.metaInfServices(cl),
            Lookups.singleton(cl),
        });
    }
    
    private static void setLookup(Object[] instances, Lookup lkp, ClassLoader cl) {
        DEFAULT_LOOKUP.setLookups(new Lookup[] {
            lkp,        
            Lookups.fixed(instances),
            Lookups.metaInfServices(cl),
            Lookups.singleton(cl),
        });
    }
    
    
    static {
        UnitTestUtils.class.getClassLoader().setDefaultAssertionStatus(true);
        System.setProperty("org.openide.util.Lookup", UnitTestUtils.class.getName());
        Assert.assertEquals(UnitTestUtils.class, Lookup.getDefault().getClass());
    }
    
    public static void initLookup() {
        //currently nothing.
    }

}
