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

package org.netbeans.modules.project.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.lang.ref.WeakReference;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import org.netbeans.junit.NbTestCase;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Children;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public class TemplatesPanelGUITest extends NbTestCase implements TemplatesPanelGUI.Builder {
    public TemplatesPanelGUITest(String testName) {
        super(testName);
    }

    private static Object editor;
    public void testTemplatesPanel() throws Exception {
        final WeakReference[] refRef = new WeakReference[] { null };
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                TemplatesPanelGUI inst = new TemplatesPanelGUI(TemplatesPanelGUITest.this);

                inst.addNotify();
                editor = find(inst, JEditorPane.class, true);
                WeakReference<Object> ref = new WeakReference<Object>(inst);
                refRef[0] = ref;

                inst.removeNotify();
            }
        });
        assertGC("Panel does not hold ref", refRef[0]);
    }
    
    private static Component find(Component c, Class<?> clazz, boolean fail) {
        if (clazz.isInstance(c)) {
            return c;
        }
        if (c instanceof Container) {
            Container cont = (Container)c;
            for (Component p : cont.getComponents()) {
                Component r = find(p, clazz, false);
                if (r != null) {
                    return r;
                }
            }
        }
        if (fail) {
            fail("Not found " + clazz + " in children of " + c);
        }
        return null;
    }
    
    public Children createCategoriesChildren(DataFolder folder, String filterText) {
        return Children.LEAF;
    }

    public Children createTemplatesChildren(DataFolder folder, String filterText) {
        return Children.LEAF;
    }

    public String getCategoriesName() {
        return "";
    }

    public String getTemplatesName() {
        return "";
    }

    public void fireChange() {
    }

    @Override
    public void actionPerformed( ActionEvent e ) {
        throw new UnsupportedOperationException( "Not supported yet." );
    }
}
