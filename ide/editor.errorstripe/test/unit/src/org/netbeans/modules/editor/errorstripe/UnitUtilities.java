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
package org.netbeans.modules.editor.errorstripe;


import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.swing.text.Utilities;
import org.junit.Assert;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.xml.sax.SAXException;

/**Inspired by org.netbeans.api.project.TestUtil.
 *
 * @author Jan Lahoda
 */
public class UnitUtilities extends ProxyLookup {
    
    public static UnitUtilities DEFAULT_LOOKUP = null;
    
    public UnitUtilities() {
        Assert.assertNull(DEFAULT_LOOKUP);
        DEFAULT_LOOKUP = this;
    }
    
    /**
     * Set the global default lookup with some fixed instances including META-INF/services/*.
     */
    public static void setLookup(Object[] instances, ClassLoader cl) {
        // XXX consider using MockServices
        DEFAULT_LOOKUP.setLookups(new Lookup[] {
            Lookups.fixed(instances),
            Lookups.metaInfServices(cl),
            Lookups.singleton(cl),
        });
    }
    
    public static void prepareTest(String[] additionalLayers, Object[] additionalLookupContent) throws IOException, SAXException, PropertyVetoException {
        URL[] layers = new URL[additionalLayers.length + 1];
        
        layers[0] = Utilities.class.getResource("/org/netbeans/modules/editor/errorstripe/resources/layer.xml");
        
        for (int cntr = 0; cntr < additionalLayers.length; cntr++) {
            layers[cntr + 1] = Utilities.class.getResource(additionalLayers[cntr]);
        }
        
        for (int cntr = 0; cntr < layers.length; cntr++) {
            if (layers[cntr] == null) {
                throw new IllegalStateException("layers[" + cntr + "]=null");
            }
        }
        
        XMLFileSystem system = new XMLFileSystem();
        system.setXmlUrls(layers);
        
        Repository repository = new Repository(system);
        Object[] lookupContent = new Object[additionalLookupContent.length + 1];
        
        System.arraycopy(additionalLookupContent, 0, lookupContent, 1, additionalLookupContent.length);
        
        lookupContent[0] = repository;
        
        DEFAULT_LOOKUP.setLookup(lookupContent, UnitUtilities.class.getClassLoader());
    }
    
    static {
        UnitUtilities.class.getClassLoader().setDefaultAssertionStatus(true);
        System.setProperty("org.openide.util.Lookup", UnitUtilities.class.getName());
        Assert.assertEquals(UnitUtilities.class, Lookup.getDefault().getClass());
    }
    
    public static void initLookup() {
        //currently nothing.
    }
    
    /**Copied from org.netbeans.api.project.
     * Create a scratch directory for tests.
     * Will be in /tmp or whatever, and will be empty.
     * If you just need a java.io.File use clearWorkDir + getWorkDir.
     */
    public static FileObject makeScratchDir(NbTestCase test) throws IOException {
        test.clearWorkDir();
        File root = test.getWorkDir();
        assert root.isDirectory() && root.list().length == 0;
        FileObject fo = FileUtil.toFileObject(root);
        if (fo != null) {
            // Presumably using masterfs.
            return fo;
        } else {
            // For the benefit of those not using masterfs.
            LocalFileSystem lfs = new LocalFileSystem();
            try {
                lfs.setRootDirectory(root);
            } catch (PropertyVetoException e) {
                assert false : e;
            }
            Repository.getDefault().addFileSystem(lfs);
            return lfs.getRoot();
        }
    }
    
}
