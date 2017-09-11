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
