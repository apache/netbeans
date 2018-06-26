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
package org.netbeans.modules.ide.ergonomics.newproject;

import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.net.URL;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.ide.ergonomics.fod.FeatureInfo;
import org.netbeans.modules.ide.ergonomics.fod.FeatureManager;
import org.netbeans.modules.ide.ergonomics.fod.FoDLayersProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

public class FeatureOnDemandWizardIteratorTest extends NbTestCase {
    public FeatureOnDemandWizardIteratorTest(String s) {
        super(s);
    }

    @Override
    protected int timeOut() {
        return 10000;
    }

    @Override
    protected void setUp() throws Exception {
        URL fakeLayer = FeatureOnDemandWizardIteratorTest.class.getResource("smpl.xml");
        URL fakeProps = FeatureOnDemandWizardIteratorTest.class.getResource("smpl.properties");
        FeatureInfo info = FeatureInfo.create("fake", fakeLayer, fakeProps);
        Lookup lkp = Lookups.singleton(info);
        FeatureManager.assignFeatureTypesLookup(lkp);
        FoDLayersProvider.getInstance().refreshForce();
    }
    
    public void testIteratorCreatesUI() throws Exception {
        assertFalse("No EDT", EventQueue.isDispatchThread());
        
        FileObject fo = FileUtil.getConfigFile("smpl.tmp");
        assertNotNull("layer file found", fo);
        final FeatureOnDemandWizardIterator it = new FeatureOnDemandWizardIterator(fo);
        fo.setAttribute("instantiatingIterator", it);
        
        final TemplateWizard tw = new TemplateWizard();
        tw.setTemplate(DataObject.find(fo));
        
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                assertNotNull("Panel found", it.current());
                assertComponent("Our component found", it.current().getComponent(), tw);
            }
        });
    }
    static void assertComponent(String msg, Component component, TemplateWizard tw) {
        Object c = tw.getMessage();
        assertTrue("Is component: " + c, c instanceof Component);
        if (contains(component, ((Component)c))) {
            return;
        }
        fail(msg);
    }
    private static boolean contains(Component seachFor, Component where) {
        if (seachFor == where) {
            return true;
        }
        if (where instanceof Container) {
            Container c = (Container)where;
            for (Component a : c.getComponents()) {
                if (contains(seachFor, a)) {
                    return true;
                }
            }
        }
        return false;
    }
}
