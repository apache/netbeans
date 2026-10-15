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

package org.netbeans.upgrade.systemoptions;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author Radek Matous
 */
public abstract class BasicTestForImport extends NbTestCase {
    private FileObject f;
    private String fileName;
    
    
    public BasicTestForImport(String testName, String fileName) {
        super(testName);
        this.fileName = fileName;
    }
    
    @Override
    protected void setUp() throws Exception {
        URL u = getClass().getResource(getFileName());
        File ff = new File(u.getFile());//getDataDir(),getFileName()
        f = FileUtil.toFileObject(ff);
        assert f != null;
    }
    
    private final String getFileName() {
        return fileName;
    }
    

    /**
     * overload this test in your TestCase see <code>IDESettingsTest</code>
     */
    public void testPropertyNames() throws Exception {
        assertPropertyNames(new String[] {"just_cause_fail"
        });
    }
    
    public void testPreferencesNodePath() throws Exception {
        assertPreferencesNodePath("just_cause_fail");
    }
    
    
    private DefaultResult readSystemOption(boolean types) throws IOException, ClassNotFoundException {
        return SystemOptionsParser.parse(f, types);
    }

    public void assertPropertyNames(final String[] propertyNames) throws IOException, ClassNotFoundException {
        assertEquals(new TreeSet<>(Arrays.asList(propertyNames)).toString(),
                     new TreeSet<>(Arrays.asList(readSystemOption(false).getPropertyNames())).toString());
    }
    
    public void assertProperty(final String propertyName, final String expected) throws IOException, ClassNotFoundException {
        Result support = readSystemOption(false);
        
        List parsedPropNames = Arrays.asList(support.getPropertyNames());
        
        String parsedPropertyName = null;
        boolean isFakeName = !parsedPropNames.contains(propertyName);
        if (isFakeName) {
            assertTrue(propertyName+" (alias: "+parsedPropertyName + ") not found in: " + parsedPropNames,parsedPropNames.contains(parsedPropertyName));
        } else {
            parsedPropertyName = propertyName;
        }
        
        assertNotNull(parsedPropertyName);
        Class expectedClass = null;
        String actual = support.getProperty(parsedPropertyName);
        if (actual == null) {
            assertNull(expectedClass);
            assertEquals(expected, actual);
        } else {
            assertEquals(expected, actual);
        }
    }    
    
    public void assertPropertyType(final String propertyName, final String expected) throws IOException, ClassNotFoundException {
        Result support = readSystemOption(true);
        List parsedPropNames = Arrays.asList(support.getPropertyNames());        
        String parsedPropertyName = null;
        boolean isFakeName = !parsedPropNames.contains(propertyName);
        if (isFakeName) {
            assertTrue(propertyName+" (alias: "+parsedPropertyName + ") not found in: " + parsedPropNames,parsedPropNames.contains(parsedPropertyName));
        } else {
            parsedPropertyName = propertyName;
        }
        
        assertNotNull(parsedPropertyName);
        String actual = support.getProperty(parsedPropertyName);
        if (actual == null) {
            assertNull(expected);
        } else {
            Class<?> expectedClass = null;
            try {
                expectedClass = Class.forName(expected);
            } catch (ClassNotFoundException ex) {
            }
            if (expectedClass != null) {
                Class<?> cls = Class.forName(actual);
                assertTrue(expectedClass + " but : " + cls,expectedClass.isAssignableFrom(cls));
            } else {
                assertEquals(expected, actual);
            }
            assertEquals(expected, actual);
        }
    }
    
    public void assertPropertyTypeAndValue(String propertyName, String expectedType, String expectedValue) throws Exception {
        assertPropertyType(propertyName, expectedType);
        assertProperty(propertyName, expectedValue);
    }
    
    public void assertPreferencesNodePath(final String expectedInstanceName) throws IOException, ClassNotFoundException {
        DefaultResult support = readSystemOption(true);
        assertEquals(expectedInstanceName,"/"+support.getModuleName());//NOI18N
    }        
}
