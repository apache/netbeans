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

package org.netbeans.core.windows;


import java.awt.GraphicsEnvironment;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

/** 
 * Test PersistenceHandler functionality.
 * 
 * @author Marek Slama
 * 
 */
public class PersistenceHandlerTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(PersistenceHandlerTest.class);
    }

    public PersistenceHandlerTest (String name) {
        super (name);
    }
    
    protected boolean runInEQ () {
        return true;
    }
    
    /**
     * Make sure that closed TCs are not deserialized during saving window system ie. also
     * during IDE exit. Test creates test TC and overwrites method readExternal. This method
     * should not be called.
     */
    public void testSaveWindowSystem () throws Exception {
        Lookup.getDefault().lookup(ModuleInfo.class);
        
        IDEInitializer.addLayers
        (new String [] {"org/netbeans/core/windows/resources/layer-PersistenceHandlerTest.xml"});
        
        //Verify that test layer was added to default filesystem
        assertNotNull(FileUtil.getConfigFile("Windows2/Modes/editor/component00.wstcref"));
        
        PersistenceHandler.getDefault().load();
                
        //Check that test TopComponent is not instantiated before
        assertFalse
        ("Closed TopComponent was instantiated before window system save but it should not.",
         Component00.wasDeserialized());
        
        PersistenceHandler.getDefault().save();
        
        //Check if test TopComponent was instantiated
        assertFalse
        ("Closed TopComponent was instantiated during window system save but it should not.",
         Component00.wasDeserialized());
        
        IDEInitializer.removeLayers();
    }
    
}

