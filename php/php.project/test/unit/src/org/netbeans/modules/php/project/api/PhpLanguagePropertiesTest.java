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
package org.netbeans.modules.php.project.api;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.modules.php.project.util.PhpTestCase;
import org.netbeans.modules.php.project.util.TestUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


public class PhpLanguagePropertiesTest extends PhpTestCase {

    public PhpLanguagePropertiesTest(String name) {
        super(name);
    }

    public void testDefaultProperties() {
        PhpLanguageProperties defaultProps = PhpLanguageProperties.getDefault();
        PhpLanguageProperties nullProps = PhpLanguageProperties.forFileObject(null);
        assertSame(defaultProps, nullProps);
    }

    public void testProjectProperties() throws Exception {
        PhpProject project = TestUtils.createPhpProject(getWorkDir());
        FileObject a = FileUtil.createData(project.getProjectDirectory(), "a.php");
        FileObject b = FileUtil.createData(project.getProjectDirectory(), "b.php");

        PhpLanguageProperties projectProps = project.getLookup().lookup(PhpLanguageProperties.class);
        assertSame(projectProps, PhpLanguageProperties.forFileObject(a));
        assertSame(projectProps, PhpLanguageProperties.forFileObject(b));
    }

    public void testDefaultPropertyChanges() throws Exception {
        final PropertyChangeListenerImpl listener = new PropertyChangeListenerImpl();
        final PhpLanguageProperties defaultProps = PhpLanguageProperties.getDefault();

        attachListenerAndChangeProperty(defaultProps, listener);

        assertFalse(listener.latch.await(500, TimeUnit.MILLISECONDS));
        assertTrue(listener.events.isEmpty());
        assertEquals(true, defaultProps.areShortTagsEnabled());
        assertEquals(true, defaultProps.areAspTagsEnabled());
        assertEquals(PhpVersion.getDefault(), defaultProps.getPhpVersion());
    }

    public void testProjectPropertyChanges() throws Exception {
        final PropertyChangeListenerImpl listener = new PropertyChangeListenerImpl();
        PhpProject project = TestUtils.createPhpProject(getWorkDir());
        final PhpLanguageProperties projectProps = PhpLanguageProperties.forFileObject(project.getProjectDirectory());

        attachListenerAndChangeProperty(project, projectProps, listener);

        assertTrue(listener.latch.await(500, TimeUnit.MILLISECONDS));
        assertEquals(3, listener.events.size());
        assertTrue(listener.events.contains(PhpLanguageProperties.PROP_SHORT_TAGS));
        assertTrue(listener.events.contains(PhpLanguageProperties.PROP_ASP_TAGS));
        assertTrue(listener.events.contains(PhpLanguageProperties.PROP_PHP_VERSION));

        assertEquals(true, projectProps.areShortTagsEnabled());
        assertEquals(true, projectProps.areAspTagsEnabled());
        assertEquals(PhpVersion.PHP_54, projectProps.getPhpVersion());
    }

    private void attachListenerAndChangeProperty(PhpLanguageProperties properties, PropertyChangeListenerImpl listener) throws Exception {
        attachListenerAndChangeProperty(TestUtils.createPhpProject(getWorkDir()), properties, listener);
    }

    /**
     * Set properties:
     * - short tags to FALSE,
     * - ASP tags to FALSE,
     * - PhpVersion to PHP_5.
     * Attach listener and change:
     * - short tags to TRUE,
     * - ASP tags to TRUE,
     * - PhpVersion to PHP_54.
     */
    private void attachListenerAndChangeProperty(PhpProject project, PhpLanguageProperties properties, PropertyChangeListenerImpl listener) throws Exception {
        PhpProjectProperties phpProjectProperties = new PhpProjectProperties(project);
        phpProjectProperties.setShortTags(Boolean.FALSE.toString());
        phpProjectProperties.setAspTags(Boolean.FALSE.toString());
        phpProjectProperties.setPhpVersion(PhpVersion.PHP_5.name());
        phpProjectProperties.save();

        properties.addPropertyChangeListener(listener);
        assertTrue(listener.events.isEmpty());

        phpProjectProperties.setShortTags(Boolean.TRUE.toString());
        phpProjectProperties.setAspTags(Boolean.TRUE.toString());
        phpProjectProperties.setPhpVersion(PhpVersion.PHP_54.name());
        phpProjectProperties.save();
    }

    //~ Inner classes

    private static final class PropertyChangeListenerImpl implements PropertyChangeListener {

        final List<String> events = new CopyOnWriteArrayList<>();
        final CountDownLatch latch = new CountDownLatch(3);


        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            events.add(evt.getPropertyName());
            latch.countDown();
        }

    }

}
