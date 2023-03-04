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

package org.netbeans.modules.ant.freeform;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.util.Mutex;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Test property evaluation.
 * @author Jesse Glick
 */
public class FreeformEvaluatorTest extends TestBase {
    
    public FreeformEvaluatorTest(String name) {
        super(name);
    }
    
    public void testPropertyEvaluation() throws Exception {
        PropertyEvaluator eval = simple.evaluator();
        assertEquals("right src.dir", "src", eval.getProperty("src.dir"));
    }

    @RandomlyFails
    public void testPropertyEvaluationChanges() throws Exception {
        FreeformProject simple2 = copyProject(simple);
        PropertyEvaluator eval = simple2.evaluator();
        assertEquals("right src.dir", "src", eval.getProperty("src.dir"));
        EditableProperties p = new EditableProperties();
        FileObject buildProperties = simple2.getProjectDirectory().getFileObject("build.properties");
        assertNotNull("have build.properties", buildProperties);
        InputStream is = buildProperties.getInputStream();
        try {
            p.load(is);
        } finally {
            is.close();
        }
        assertEquals("right original value", "src", p.getProperty("src.dir"));
        p.setProperty("src.dir", "somethingnew");
        TestPCL l = new TestPCL();
        eval.addPropertyChangeListener(l);
        final OutputStream os = buildProperties.getOutputStream();
        try {
            p.store(os);
        } finally {
            // close file under ProjectManager.readAccess so that events are fired synchronously
            ProjectManager.mutex().readAccess(new Mutex.ExceptionAction<Void>() {
                public Void run() throws Exception {
                    os.close();
                    return null;
                }
            });
        }
        assertEquals("got a change from properties file in src.dir", Collections.singleton("src.dir"), l.changed);
        l.reset();
        assertEquals("new value of src.dir", "somethingnew", eval.getProperty("src.dir"));
    }

    @RandomlyFails
    public void testChangesInPropertyFileLocation() throws Exception {
        // #48230: if some change in earlier properties causes location of a property file to change, reread it
        FreeformProject simple2 = copyProject(simple);
        // Replace <property name="build.properties">build.properties</property>
        // with <property-file>loc.properties</property-file> so that we can change it
        // without triggering a project.xml change (which always fires changes, so it is cheating).
        EditableProperties p = new EditableProperties();
        p.setProperty("build.properties", "build.properties");
        FileObject locProperties = simple2.getProjectDirectory().createData("loc.properties");
        OutputStream os = locProperties.getOutputStream();
        try {
            p.store(os);
        } finally {
            os.close();
        }
        Element data = simple2.getPrimaryConfigurationData();
        NodeList propertiesNL = data.getElementsByTagNameNS(FreeformProjectType.NS_GENERAL, "properties");
        assertEquals("one <properties>", 1, propertiesNL.getLength());
        Element properties = (Element) propertiesNL.item(0);
        properties.removeChild(properties.getFirstChild());
        Element propertyFile = properties.getOwnerDocument().createElementNS(FreeformProjectType.NS_GENERAL, "property-file");
        propertyFile.appendChild(properties.getOwnerDocument().createTextNode("loc.properties"));
        properties.insertBefore(propertyFile, properties.getFirstChild());
        simple2.putPrimaryConfigurationData(data);
        ProjectManager.getDefault().saveProject(simple2);
        // Now check baseline evaluation.
        PropertyEvaluator eval = simple2.evaluator();
        TestPCL l = new TestPCL();
        eval.addPropertyChangeListener(l);
        assertEquals("right src.dir", "src", eval.getProperty("src.dir"));
        // Make a build2.properties with a slight change.
        p = new EditableProperties();
        FileObject buildProperties = simple2.getProjectDirectory().getFileObject("build.properties");
        assertNotNull("have build.properties", buildProperties);
        InputStream is = buildProperties.getInputStream();
        try {
            p.load(is);
        } finally {
            is.close();
        }
        assertEquals("right original value", "src", p.getProperty("src.dir"));
        p.setProperty("src.dir", "somethingnew");
        FileObject buildProperties2 = simple2.getProjectDirectory().createData("build2.properties");
        os = buildProperties2.getOutputStream();
        try {
            p.store(os);
        } finally {
            os.close();
        }
        assertEquals("No changes fired yet", Collections.EMPTY_SET, l.changed);
        // Tell loc.properties to point to it.
        p = new EditableProperties();
        p.setProperty("build.properties", "build2.properties");
        locProperties = simple2.getProjectDirectory().getFileObject("loc.properties");
        os = locProperties.getOutputStream();
        try {
            p.store(os);
        } finally {
            // close file under ProjectManager.readAccess so that events are fired synchronously
            final OutputStream _os = os;
            ProjectManager.mutex().readAccess(new Mutex.ExceptionAction<Void>() {
                public Void run() throws Exception {
                    _os.close();
                    return null;
                }
            });
        }
        // Check that the change took.
        Set<String> exact = new HashSet<String>(Arrays.asList("src.dir", "build.properties"));
        // OK to just return null for the property name instead.
        assertTrue("got a change from properties file in src.dir: " + l.changed, l.changed.contains(null) || l.changed.equals(exact));
        l.reset();
        assertEquals("new value of src.dir", "somethingnew", eval.getProperty("src.dir"));
    }
    
}
