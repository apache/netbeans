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

package org.netbeans.modules.java.platform.wizard;

import java.awt.Component;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.metal.MetalLookAndFeel;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.platform.InstallerRegistry;
import org.netbeans.modules.java.platform.InstallerRegistryAccessor;
import org.netbeans.spi.java.platform.CustomPlatformInstall;
import org.netbeans.spi.java.platform.GeneralPlatformInstall;
import org.netbeans.spi.java.platform.PlatformInstall;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.WizardDescriptor;

/**
 *
 * @author Tomas Zezula
 */
public class PlatformInstallIteratorTest extends NbTestCase {
    
    public PlatformInstallIteratorTest(String testName) {
        super(testName);
    }

    protected @Override boolean runInEQ() {
        return true;
    }
    
    public void testSinglePlatformInstall () throws IOException, UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        InstallerRegistry regs = InstallerRegistryAccessor.prepareForUnitTest(new GeneralPlatformInstall[] {
            new FileBasedPlatformInstall ("FileBased1", Collections.<WizardDescriptor.Panel<WizardDescriptor>>singletonList(
                new Panel ("FileBased1_panel1")
            ))
        });
        PlatformInstallIterator iterator = PlatformInstallIterator.create();
        WizardDescriptor wd = new WizardDescriptor (iterator);
        iterator.initialize(wd);
        assertEquals("Invalid state", 1, iterator.getPanelIndex());
        WizardDescriptor.Panel panel = iterator.current();
        assertTrue ("Invalid panel",panel instanceof LocationChooser.Panel);
        ((JFileChooser)panel.getComponent()).setSelectedFile(this.getWorkDir());    //Select some folder
        assertTrue ("LocationChooser is not valid after folder was selected",panel.isValid());
        assertTrue ("Should have next panel",iterator.hasNext());
        assertFalse ("Should not have previous panel", iterator.hasPrevious());
        iterator.nextPanel();
        assertEquals("Invalid state", 2, iterator.getPanelIndex());
        panel = iterator.current();
        assertEquals("Invalid panel","FileBased1_panel1",panel.getComponent().getName());
        assertFalse ("Should not have next panel",iterator.hasNext());
        assertTrue ("Should have previous panel", iterator.hasPrevious());
    }
    
    public void testSingleCustomInstall () throws IOException {
        InstallerRegistry regs = InstallerRegistryAccessor.prepareForUnitTest(new GeneralPlatformInstall[] {
            new OtherPlatformInstall ("Custom1", Collections.<WizardDescriptor.Panel<WizardDescriptor>>singletonList(
                new Panel ("Custom1_panel1")
            ))
        });
        PlatformInstallIterator iterator = PlatformInstallIterator.create();
        WizardDescriptor wd = new WizardDescriptor (iterator);
        iterator.initialize(wd);
        assertEquals("Invalid state", 3, iterator.getPanelIndex());
        WizardDescriptor.Panel panel = iterator.current();
        assertEquals("Invalid panel","Custom1_panel1",panel.getComponent().getName());
        assertFalse ("Should not have next panel",iterator.hasNext());
        assertFalse ("Should not have previous panel", iterator.hasPrevious());
    }
    
    public void testMultipleGenralPlatformInstalls () throws IOException, UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        GeneralPlatformInstall[] installers = new GeneralPlatformInstall[] {
            new FileBasedPlatformInstall ("FileBased1", Collections.<WizardDescriptor.Panel<WizardDescriptor>>singletonList(
                new Panel ("FileBased1_panel1")
            )),
            new OtherPlatformInstall ("Custom1", Collections.<WizardDescriptor.Panel<WizardDescriptor>>singletonList(
                new Panel ("Custom1_panel1")
            ))
        };
        InstallerRegistry regs = InstallerRegistryAccessor.prepareForUnitTest (installers);
        PlatformInstallIterator iterator = PlatformInstallIterator.create();        
        WizardDescriptor wd = new WizardDescriptor (iterator);
        iterator.initialize(wd);
        assertEquals("Invalid state", 0, iterator.getPanelIndex());
        WizardDescriptor.Panel panel = iterator.current();
        assertTrue ("Invalid panel",panel instanceof SelectorPanel.Panel);
        assertTrue ("Installer was not found",((SelectorPanel)panel.getComponent()).selectInstaller(installers[0]));
        assertTrue ("SelectorPanel should be valid",panel.isValid());
        assertTrue ("Should have next panel",iterator.hasNext());
        assertFalse ("Should not have previous panel", iterator.hasPrevious());
        iterator.nextPanel ();
        assertEquals("Invalid state", 1, iterator.getPanelIndex());
        panel = iterator.current();
        assertTrue ("Invalid panel",panel instanceof LocationChooser.Panel);
        assertTrue ("Should have previous panel", iterator.hasPrevious());
        iterator.previousPanel();
        assertEquals("Invalid state", 0, iterator.getPanelIndex());
        panel = iterator.current();
        assertTrue ("Invalid panel",panel instanceof SelectorPanel.Panel);
        assertTrue ("Installer was not found",((SelectorPanel)panel.getComponent()).selectInstaller(installers[1]));
        assertTrue ("SelectorPanel should be valid",panel.isValid());
        assertTrue ("Should have next panel",iterator.hasNext());
        assertFalse ("Should not have previous panel", iterator.hasPrevious());
        iterator.nextPanel ();
        assertEquals("Invalid state", 3, iterator.getPanelIndex());
        panel = iterator.current();
        assertEquals("Invalid panel","Custom1_panel1",panel.getComponent().getName());
        assertFalse ("Should not have next panel",iterator.hasNext());
        assertTrue ("Should have previous panel", iterator.hasPrevious());
    }
    
    public void testMultipleFileBasedPlatformInstalls () throws IOException, UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        GeneralPlatformInstall[] installers = new GeneralPlatformInstall[] {
            new FileBasedPlatformInstall ("FileBased1", Collections.<WizardDescriptor.Panel<WizardDescriptor>>singletonList(
                new Panel ("FileBased1_panel1")
            )),
            new FileBasedPlatformInstall ("FileBased2", Collections.<WizardDescriptor.Panel<WizardDescriptor>>singletonList(
                new Panel ("FileBased2_panel2")
            )),
        };
        InstallerRegistry regs = InstallerRegistryAccessor.prepareForUnitTest (installers);
        PlatformInstallIterator iterator = PlatformInstallIterator.create();        
        WizardDescriptor wd = new WizardDescriptor (iterator);
        iterator.initialize(wd);
        assertEquals("Invalid state", 0, iterator.getPanelIndex());
        WizardDescriptor.Panel panel = iterator.current();
        assertTrue ("Invalid panel",panel instanceof SelectorPanel.Panel);
        assertTrue ("Installer was not found",((SelectorPanel)panel.getComponent()).selectInstaller(installers[0]));
        iterator.nextPanel ();
        assertEquals("Invalid state", 1, iterator.getPanelIndex());
        panel = iterator.current();
        assertTrue ("Invalid panel",panel instanceof LocationChooser.Panel);
        PlatformInstall platformInstall = ((LocationChooser.Panel)panel).getPlatformInstall();
        assertEquals ("Invalid PlatformInstall",installers[0],platformInstall);
        iterator.previousPanel();
        assertEquals("Invalid state", 0, iterator.getPanelIndex());
        panel = iterator.current();
        assertTrue ("Invalid panel",panel instanceof SelectorPanel.Panel);
        assertTrue ("Installer was not found",((SelectorPanel)panel.getComponent()).selectInstaller(installers[1]));
        iterator.nextPanel ();
        assertEquals("Invalid state", 1, iterator.getPanelIndex());
        panel = iterator.current();
        assertTrue ("Invalid panel",panel instanceof LocationChooser.Panel);
        platformInstall = ((LocationChooser.Panel)panel).getPlatformInstall();
        assertEquals ("Invalid PlatformInstall",installers[1],platformInstall);
    }
    
    public void testIteratorWithMorePanels () throws IOException {
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
        panels.add(new Panel("Custom1_panel1"));
        panels.add(new Panel("Custom1_panel2"));
        InstallerRegistry regs = InstallerRegistryAccessor.prepareForUnitTest(new GeneralPlatformInstall[] {
            new OtherPlatformInstall("Custom1", panels)
        });
        PlatformInstallIterator iterator = PlatformInstallIterator.create();
        WizardDescriptor wd = new WizardDescriptor (iterator);
        iterator.initialize(wd);
        assertEquals("Invalid state", 3, iterator.getPanelIndex());
        WizardDescriptor.Panel panel = iterator.current();
        assertEquals("Invalid panel","Custom1_panel1",panel.getComponent().getName());
        assertTrue ("Should have next panel",iterator.hasNext());
        assertFalse ("Should not have previous panel", iterator.hasPrevious());
        iterator.nextPanel();
        panel = iterator.current();
        assertEquals("Invalid panel","Custom1_panel2",panel.getComponent().getName());
        assertFalse ("Should not have next panel",iterator.hasNext());
        assertTrue ("Should have previous panel", iterator.hasPrevious());
    }
    
    private static class FileBasedPlatformInstall extends PlatformInstall {
        
        private String name;
        private WizardDescriptor.InstantiatingIterator<WizardDescriptor> iterator;
        
        public FileBasedPlatformInstall (String name, List<WizardDescriptor.Panel<WizardDescriptor>> panels) {
            this.name = name;
            this.iterator = new Iterator (panels);
        }

        public WizardDescriptor.InstantiatingIterator<WizardDescriptor> createIterator(FileObject baseFolder) {
            return this.iterator;
        }

        public boolean accept(FileObject baseFolder) {
            return true;
        }

        public String getDisplayName() {
            return this.name;
        }                        
    }
    
    private static class OtherPlatformInstall extends CustomPlatformInstall {
        
        private String name;
        private WizardDescriptor.InstantiatingIterator<WizardDescriptor> iterator;
        
        public OtherPlatformInstall (String name, List<WizardDescriptor.Panel<WizardDescriptor>> panels) {
            this.name = name;
            this.iterator = new Iterator (panels);
        }

        public String getDisplayName() {
            return this.name;
        }

        public WizardDescriptor.InstantiatingIterator<WizardDescriptor> createIterator () {
            return this.iterator;
        }
                        
    }
    
    private static class Iterator implements WizardDescriptor.InstantiatingIterator<WizardDescriptor> {
        
        private List<WizardDescriptor.Panel<WizardDescriptor>> panels;
        private int index;
        
        public Iterator (List<WizardDescriptor.Panel<WizardDescriptor>> panels) {
            this.panels = panels;
        }
        
        public void removeChangeListener(ChangeListener l) {
        }

        public void addChangeListener(ChangeListener l) {
        }

        public void uninitialize(WizardDescriptor wizard) {
        }

        public void initialize(WizardDescriptor wizard) {
            this.index = 0;
        }

        public void previousPanel() {
            this.index--;
        }

        public void nextPanel() {
            this.index++;
        }

        public String name() {
            return "Test";      //NOI18N
        }

        public Set instantiate() throws IOException {
            return Collections.EMPTY_SET;
        }

        public boolean hasPrevious() {
            return this.index > 0;
        }

        public boolean hasNext() {
            return this.index < (this.panels.size() - 1);
        }

        public WizardDescriptor.Panel<WizardDescriptor> current() {
            return this.panels.get(this.index);
        }
        
    }
    
    private static class Panel implements WizardDescriptor.Panel<WizardDescriptor> {
        
        private JPanel p;
        private String name;
        
        public Panel (String name) {
            this.name = name;
        }
        
        public void removeChangeListener(ChangeListener l) {
        }

        public void addChangeListener(ChangeListener l) {
        }

        public void storeSettings(WizardDescriptor wiz) {
        }

        public void readSettings(WizardDescriptor qiz) {
        }

        public boolean isValid() {
            return true;
        }

        public HelpCtx getHelp() {
            return HelpCtx.DEFAULT_HELP;
        }

        public Component getComponent() {
            if (this.p == null) {
                p = new JPanel ();
                p.setName(this.name);
            }
            return p;
        }
        
    }
    
}
