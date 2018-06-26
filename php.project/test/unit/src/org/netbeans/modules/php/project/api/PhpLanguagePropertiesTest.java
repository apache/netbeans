/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
