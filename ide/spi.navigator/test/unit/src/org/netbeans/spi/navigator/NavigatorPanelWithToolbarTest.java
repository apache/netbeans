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
package org.netbeans.spi.navigator;

import java.beans.PropertyChangeEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.navigator.LazyPanel;
import org.netbeans.modules.navigator.NavigatorController;
import org.netbeans.modules.navigator.NavigatorTC;
import org.netbeans.modules.navigator.UnitTestUtils;
import org.openide.util.ContextGlobalProvider;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;

/**
 *
 * @author jpeska
 */
public class NavigatorPanelWithToolbarTest extends NbTestCase {

    public NavigatorPanelWithToolbarTest(String testName) {
        super(testName);
    }

    public void testFix217212_ActivatePanel() throws Exception {
        InstanceContent ic = new InstanceContent();
        GlobalLookup4TestImpl nodesLkp = new GlobalLookup4TestImpl(ic);
        UnitTestUtils.prepareTest(new String[]{
                    "/META-INF/generated-layer.xml"},
                Lookups.singleton(nodesLkp));

        TestLookupHint hint = new TestLookupHint("annotation/tester");
        ic.add(hint);

        final NavigatorTC navTC = NavigatorTC.getInstance();
        Field field = NavigatorController.class.getDeclaredField("updateWhenNotShown");
        field.setAccessible(true);
        field.setBoolean(navTC.getController(), true);
        try {
            Mutex.EVENT.readAccess(new Mutex.ExceptionAction() {
                @Override
                public Object run() throws Exception {
                    navTC.getController().propertyChange(
                            new PropertyChangeEvent(navTC, TopComponent.Registry.PROP_TC_OPENED, null, navTC));
                    return null;
                }
            });
            waitForProviders(navTC);
            NavigatorPanel selPanel = navTC.getSelectedPanel();
            assertNotNull("Selected panel is null", selPanel);

            List<? extends NavigatorPanel> panels = navTC.getPanels();
            assertEquals(2, panels.size());

            NavigatorPanel lazyPanel1 = panels.get(0);
            Method method = LazyPanel.class.getDeclaredMethod("initialize");
            method.setAccessible(true);
            NavigatorPanel delegate1 = (NavigatorPanel) method.invoke(lazyPanel1);

            NavigatorPanel lazyPanel2 = panels.get(1);
            method = LazyPanel.class.getDeclaredMethod("initialize");
            method.setAccessible(true);
            NavigatorPanel delegate2 = (NavigatorPanel) method.invoke(lazyPanel2);

            System.out.println("selected panel before: " + selPanel.getDisplayName());

            //find not-selected panel
            final NavigatorPanel toActivate;
            final NavigatorPanel toActivateLazy;
            if (selPanel.equals(lazyPanel1)) {
                toActivate = delegate2;
                toActivateLazy = lazyPanel2;
            } else {
                toActivate = delegate1;
                toActivateLazy = lazyPanel1;

            }

            Mutex.EVENT.readAccess(new Mutex.ExceptionAction() {
                @Override
                public Object run() throws Exception {
                    NavigatorHandler.activatePanel(toActivate);
                    return null;
                }
            });

            assertTrue(selPanel != navTC.getSelectedPanel());
            assertTrue(toActivateLazy == navTC.getSelectedPanel());

            System.out.println("selected panel after: " + navTC.getSelectedPanel().getDisplayName());
        } finally {
            navTC.getController().propertyChange(
                    new PropertyChangeEvent(navTC, TopComponent.Registry.PROP_TC_CLOSED, null, navTC));
        }
    }

    @NavigatorPanel.Registration(mimeType = "annotation/tester", position = 100, displayName = "Panel Annotation 1")
    public static final class PanelAnnotated1 implements NavigatorPanel {

        @Override
        public String getDisplayName() {
            return "Panel Annotation 1";
        }

        @Override
        public String getDisplayHint() {
            return null;
        }

        @Override
        public JComponent getComponent() {
            return new JPanel();
        }

        @Override
        public void panelActivated(Lookup context) {
        }

        @Override
        public void panelDeactivated() {
        }

        @Override
        public Lookup getLookup() {
            return null;
        }
    }

    @NavigatorPanel.Registration(mimeType = "annotation/tester", position = 200, displayName = "Panel Annotation 2")
    public static final class PanelAnnotated2 implements NavigatorPanel {

        @Override
        public String getDisplayName() {
            return "Panel Annotation 2";
        }

        @Override
        public String getDisplayHint() {
            return null;
        }

        @Override
        public JComponent getComponent() {
            return new JPanel();
        }

        @Override
        public void panelActivated(Lookup context) {
        }

        @Override
        public void panelDeactivated() {
        }

        @Override
        public Lookup getLookup() {
            return null;
        }
    }

    private void waitForProviders(NavigatorTC navTC) throws NoSuchFieldException, SecurityException, InterruptedException, IllegalArgumentException, IllegalAccessException {
        Field field = NavigatorController.class.getDeclaredField("inUpdate");
        field.setAccessible(true);
        while (field.getBoolean(navTC.getController())) {
            Thread.sleep(100);
        }
    }

    private static final class TestLookupHint implements NavigatorLookupHint {

        private final String contentType;

        public TestLookupHint (String contentType) {
            this.contentType = contentType;
        }

        @Override
        public String getContentType () {
            return contentType;
        }

    }


    private static final class GlobalLookup4TestImpl extends AbstractLookup implements ContextGlobalProvider {

        public GlobalLookup4TestImpl (AbstractLookup.Content content) {
            super(content);
        }

        @Override
        public Lookup createGlobalContext() {
            return this;
        }
    }
}
