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

package org.netbeans.core.startup.preferences;

import java.io.IOException;
import org.openide.filesystems.FileUtil;
import org.openide.util.EditableProperties;

/**
 *
 * @author Radek Matous
 */
public class TestFileStorage extends NbPreferencesTest.TestBasicSetup {
    protected NbPreferences.FileStorage instance;
    
    
    public TestFileStorage(String testName) {
        super(testName);
    }
    
    protected @Override void setUp() throws Exception {
        super.setUp();
        instance = getInstance();
    }
    
    protected NbPreferences.FileStorage getInstance() {
        return PropertiesStorage.instance(FileUtil.getSystemConfigRoot(), "/FileStorageTest/" + getName());//NOI18N);
    }
    
    public void testBasic() throws IOException {
        //preconditions
        noFileRepresentationAssertion();
        
        //load doesn't change file layout
        EditableProperties p = instance.load();
        p.put("key", "value");//NOI18N
        noFileRepresentationAssertion();
        
        //save doesn't change file layout if not marked modified
        instance.save(p);
        noFileRepresentationAssertion();
        
        //marked modified but not saved
        instance.markModified();
        noFileRepresentationAssertion();
        
        if (!instance.isReadOnly()) {
            //saved after marked modified
            instance.save(p);            
            fileRepresentationAssertion();
        } else {
            try {
                //saved after marked modified
                instance.save(p);                
                fail();
            } catch (IOException ex) {}
            noFileRepresentationAssertion();
        }
    }
    
    void noFileRepresentationAssertion() throws IOException {
        assertFalse(instance.existsNode());
    }
    
    void fileRepresentationAssertion() throws IOException {
        assertTrue(instance.existsNode());
    }
}
