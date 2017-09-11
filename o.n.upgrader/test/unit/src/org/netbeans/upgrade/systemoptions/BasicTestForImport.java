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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
        assertEquals(new TreeSet<String>(Arrays.asList(propertyNames)).toString(),
                new TreeSet<String>(Arrays.asList(readSystemOption(false).getPropertyNames())).toString());
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
            Class expectedClass = null;
            try {
                expectedClass = Class.forName(expected);
            } catch (ClassNotFoundException ex) {
            }
            if (expectedClass != null) {
                Class cls = Class.forName(actual);
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
