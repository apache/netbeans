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

package org.netbeans.api.project;

import java.awt.Image;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import javax.swing.Icon;
import javax.swing.event.ChangeListener;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.projectapi.nb.NbProjectManagerAccessor;
import org.netbeans.spi.project.ProjectIconAnnotator;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.test.MockLookup;
import org.openide.util.test.MockPropertyChangeListener;

/**
 * Test {@link ProjectUtils}.
 * @author Jesse Glick
 */
public class ProjectUtilsTest extends NbTestCase {

    static {
        MockLookup.setInstances(TestUtil.testProjectFactory());
    }
    
    public ProjectUtilsTest(String name) {
        super(name);
    }
    
    public void testAnnotateIcon() throws IOException {
        class ProjectIconAnnotatorImpl implements ProjectIconAnnotator {
            final Image icon1 = ImageUtilities.loadImage("org/netbeans/api/project/resources/icon-1.png");
            final ChangeSupport pcs = new ChangeSupport(this);
            boolean enabled = true;
            public @Override Image annotateIcon(Project p, Image original, boolean openedNode) {
                return enabled ? icon1 : original;
            }
            public @Override void addChangeListener(ChangeListener listener) {
                pcs.addChangeListener(listener);
            }
            public @Override void removeChangeListener(ChangeListener listener) {
                pcs.removeChangeListener(listener);
            }
            void disable() {
                enabled = false;
                pcs.fireChange();
            }
        }
        FileObject goodproject = TestUtil.makeScratchDir(this).createFolder("good");
        goodproject.createFolder("testproject");
        ProjectIconAnnotatorImpl pia = new ProjectIconAnnotatorImpl();
        MockLookup.setInstances(TestUtil.testProjectFactory(), pia);
        NbProjectManagerAccessor.reset();
        Project p = ProjectManager.getDefault().findProject(goodproject);
        ProjectInformation pi = ProjectUtils.getInformation(p);
        Icon icon = pi.getIcon();
        assertEquals("Annotated image height should be 8", icon.getIconHeight(), 8);
        assertEquals("Annotated image width should be 8", icon.getIconWidth(), 8);
        MockPropertyChangeListener listener = new MockPropertyChangeListener();
        pi.addPropertyChangeListener(listener);
        pia.disable();
        listener.assertEvents(ProjectInformation.PROP_ICON);
        MockLookup.setInstances(TestUtil.testProjectFactory(), new ProjectIconAnnotatorImpl());
        listener.assertEvents(ProjectInformation.PROP_ICON);
        MockLookup.setInstances();
        listener.assertEvents(ProjectInformation.PROP_ICON);
        Reference<?> piRef = new WeakReference<Object>(pi);
        lookupResult = Lookup.getDefault().lookupResult(ProjectIconAnnotator.class);
        pi = null;
        assertGC("can collect proxy ProjectInformation's", piRef);
    }
    private static Object lookupResult;

}
