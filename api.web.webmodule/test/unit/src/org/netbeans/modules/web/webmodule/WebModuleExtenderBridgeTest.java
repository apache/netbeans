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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.webmodule;

import java.awt.Component;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.api.webmodule.ExtenderController;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.FrameworkConfigurationPanel;
import org.netbeans.modules.web.spi.webmodule.WebFrameworkProvider;
import org.netbeans.modules.web.spi.webmodule.WebModuleExtender;
import org.netbeans.modules.web.spi.webmodule.WebModuleFactory;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.test.MockChangeListener;

/**
 *
 * @author Andrei Badea
 */
@SuppressWarnings("deprecation")
public class WebModuleExtenderBridgeTest extends NbTestCase {

    private WebModule webModule;
    private ExtenderController controller;

    @Override
    protected void setUp() {
        webModule = WebModuleFactory.createWebModule(new SimpleWebModuleImpl());
        controller = ExtenderController.create();
    }

    public WebModuleExtenderBridgeTest(String testName) {
        super(testName);
    }

    public void testBasic() {

        PanelImpl panel = new PanelImpl();
        FrameworkImpl framework = new FrameworkImpl(panel);
        WebModuleExtender extender = framework.createWebModuleExtender(webModule, controller);
        MockChangeListener listener = new MockChangeListener();

        extender.update();
        extender.addChangeListener(listener);
        assertFalse(extender.isValid());
        assertEquals("Not valid", controller.getErrorMessage());

        assertTrue(extender.getComponent() instanceof JPanel);

        controller.getProperties().setProperty("prop", "foo");
        extender.update();
        assertTrue(extender.isValid());
        assertNull(controller.getErrorMessage());

        panel.forceInvalid(true);
        listener.assertEventCount(1);
        assertFalse(extender.isValid());
        assertEquals("Not valid", controller.getErrorMessage());

        panel.forceInvalid(false);
        listener.assertEventCount(1);
        assertTrue(extender.isValid());

        controller.getProperties().setProperty("prop", null);
        extender.update();
        assertFalse(extender.isValid());

        assertFalse(framework.extendCalled);
        extender.extend(webModule);
        assertTrue(framework.extendCalled);
    }

    public void testConfigurationPanelCanBeNullIssue121712() {
        FrameworkImpl framework = new FrameworkImpl(null);
        WebModuleExtender extender = framework.createWebModuleExtender(webModule, controller);
        extender.update(); // should not throw NPE
    }

    private static final class PanelImpl implements FrameworkConfigurationPanel {

        private final ChangeSupport changeSupport = new ChangeSupport(this);
        private WizardDescriptor wizard;
        private boolean forcedInvalid;

        public void enableComponents(boolean enable) {
        }

        public void addChangeListener(ChangeListener l) {
            changeSupport.addChangeListener(l);
        }

        public Component getComponent() {
            return new JPanel();
        }

        public HelpCtx getHelp() {
            return new HelpCtx("help me");
        }

        public boolean isValid() {
            boolean valid = "foo".equals(wizard.getProperty("prop")) && !forcedInvalid;
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, valid ? " " : "Not valid");
            return valid;
        }

        public void readSettings(Object settings) {
            wizard = (WizardDescriptor) settings;
        }

        public void removeChangeListener(ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }

        public void storeSettings(Object settings) {
        }

        void forceInvalid(boolean value) {
            forcedInvalid = value;
            changeSupport.fireChange();
        }
    }

    @SuppressWarnings("deprecation")
    private static final class FrameworkImpl extends WebFrameworkProvider {

        private final PanelImpl panel;
        private boolean extendCalled;

        public FrameworkImpl(PanelImpl panel) {
            super("name", "description");
            this.panel = panel;
        }

        @Override
        public FrameworkConfigurationPanel getConfigurationPanel(WebModule wm) {
            return panel;
        }

        @Override
        public Set extend(WebModule wm) {
            extendCalled = true;
            return new HashSet();
        }

        public File[] getConfigurationFiles(WebModule wm) {
            return new File[0];
        }

        public boolean isInWebModule(WebModule wm) {
            return false;
        }
    }
}
