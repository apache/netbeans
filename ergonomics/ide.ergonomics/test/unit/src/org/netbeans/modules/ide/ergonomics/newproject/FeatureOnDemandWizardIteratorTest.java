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
