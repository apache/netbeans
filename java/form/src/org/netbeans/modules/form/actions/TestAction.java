/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.form.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import javax.swing.*;
import java.awt.*;

import java.awt.event.WindowAdapter;
import java.util.HashMap;
import java.util.Map;
import javax.swing.plaf.synth.SynthLookAndFeel;
import org.openide.ErrorManager;
import org.openide.util.HelpCtx;
import org.openide.nodes.*;
import org.openide.util.actions.*;

import org.netbeans.modules.form.*;
import org.netbeans.modules.form.palette.PaletteItem;
import org.netbeans.modules.form.palette.PaletteUtils;
import org.netbeans.modules.form.project.ClassPathUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Preview design action.
 *
 * @author Tomas Pavek, Jan Stola
 */
public class TestAction extends CallableSystemAction implements Runnable {
    private static String name;
    /**
     * Maps path of form file to laf-classname -> preview map.
     * It is used to keep at most one preview per laf per file.
     */
    private Map<String,Map<String,Frame>> previews = new HashMap<String,Map<String,Frame>>();

    public TestAction() {
    }

    @Override
    public boolean isEnabled() {
        FormDesigner designer = FormDesigner.getSelectedDesigner();
        return designer != null && designer.getTopDesignComponent() != null;
    }

    /**
     * Forces re-evaluation of enabled state.
     */
    public void updateEnabled() {
        firePropertyChange("enabled", null, null); // NOI18N
    }
    
    @Override
    protected boolean asynchronous() {
        return false;
    }

    /** Human presentable name of the action. This should be
     * presented as an item in a menu.
     * @return the name of the action
     */
    @Override
    public String getName() {
        if (name == null)
            name = org.openide.util.NbBundle.getBundle(TestAction.class)
                     .getString("ACT_TestMode"); // NOI18N
        return name;
    }

    /** Help context where to find more about the action.
     * @return the help context for this action
     */
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("gui.testing"); // NOI18N
    }

    /** @return resource for the action icon */
    @Override
    protected String iconResource() {
        return "org/netbeans/modules/form/resources/test_form.png"; // NOI18N
    }

    @Override
    public void performAction() {
        if (FormDesigner.getSelectedDesigner() != null) {
            selectedLaf = null;
            if (java.awt.EventQueue.isDispatchThread())
                run();
            else
                java.awt.EventQueue.invokeLater(this);
        }
    }

    @Override
    public void run() {
        FormDesigner designer = FormDesigner.getSelectedDesigner();
        RADVisualComponent topComp = designer != null ? designer.getTopDesignComponent() : null;
        if (topComp == null) {
            return;
        }

        RADVisualComponent parent = topComp.getParentContainer();
        while (parent != null) {
            topComp = parent;
            parent = topComp.getParentContainer();
        }

        RADVisualFormContainer formContainer =
            topComp instanceof RADVisualFormContainer ?
                (RADVisualFormContainer) topComp : null;
        
        createPreview(topComp, formContainer);
    }

    /**
     * Creates preview of some {@code RADVisualComponent}.
     * 
     * @param componentToPreview component to preview.
     * @param formContainer corresponding {@code RADVisualFormContainer} (can be {@code null}).
     * @return preview of {@code componentToPreview}.
     */
    public Frame createPreview(RADVisualComponent componentToPreview,
            RADVisualFormContainer formContainer) {
        try {
            FormModel formModel = componentToPreview.getFormModel();
            if (selectedLaf == null) {
                selectedLaf = UIManager.getLookAndFeel().getClass();
            }

            // Dispose the previous preview (if it exists)
            FileObject formFile = FormEditor.getFormDataObject(formModel).getFormFile();
            Map<String,Frame> map = previews.get(formFile.getPath());
            if (map == null) {
                map = new HashMap<String,Frame>();
                previews.put(formFile.getPath(), map);
            }
            Frame previousFrame = map.get(selectedLaf.getName());
            if (previousFrame != null) {
                previousFrame.dispose();
            }

            // create a copy of form
            final ClassLoader classLoader = ClassPathUtils.getProjectClassLoader(formFile);
            final FormLAF.PreviewInfo previewInfo = FormLAF.initPreviewLaf(selectedLaf, classLoader);
            final Frame frame = (Frame) FormDesigner.createFormView(componentToPreview, previewInfo);
            frame.setEnabled(true); // Issue 178457
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    frame.dispose();
                }
            });
            map.put(selectedLaf.getName(), frame);

            // set title
            String title = frame.getTitle();
            if (title == null || "".equals(title)) { // NOI18N
                title = componentToPreview == formModel.getTopRADComponent() ?
                        formModel.getName() : componentToPreview.getName();
                frame.setTitle(java.text.MessageFormat.format(
                    org.openide.util.NbBundle.getBundle(TestAction.class)
                                               .getString("FMT_TestingForm"), // NOI18N
                    new Object[] { title }
                ));
            }

            // prepare close operation
            if (frame instanceof JFrame) {
                ((JFrame)frame).setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                HelpCtx.setHelpIDString(((JFrame)frame).getRootPane(),
                                        "gui.modes"); // NOI18N
            }
            else {
                frame.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent evt) {
                        frame.dispose();
                    }
                });
            }
 
            // set size
            boolean shouldPack = false;
            if (formContainer != null
                && formContainer.getFormSizePolicy()
                                     == RADVisualFormContainer.GEN_BOUNDS
                && formContainer.getGenerateSize())
            {
                Dimension size = formContainer.getFormSize();
                if (frame.isUndecorated()) { // will be shown as decorated anyway
                    Dimension diffSize = RADVisualFormContainer.getDecoratedWindowContentDimensionDiff();
                    size = new Dimension(size.width + diffSize.width, size.height + diffSize.height);
                }
                frame.setSize(size);
            }
            else {
                shouldPack = true;
            }
            frame.setUndecorated(false);
            frame.setFocusableWindowState(true);
            frame.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);

            // Issue 66594 and 12084
            final boolean pack = shouldPack;
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (pack) {
                        try {
                            FormLAF.setUsePreviewDefaults(classLoader, previewInfo);
                            frame.pack();
                        } finally {
                            FormLAF.setUsePreviewDefaults(null, null);
                        }
                    }
                    frame.setBounds(org.openide.util.Utilities.findCenterBounds(frame.getSize()));
                    frame.setVisible(true);
                }
            });
            return frame;
        }
        catch (Exception ex) {
            org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
        }
        return null;
    }

    @Override
    public JMenuItem getMenuPresenter() {
        return getPopupPresenter();
    }

    @Override
    public JMenuItem getPopupPresenter() {
        if (FormLAF.noLafSwitching()) {
            JMenuItem previewItem = new JMenuItem(getName());
            previewItem.addActionListener(this);
            return previewItem;
        }
        JMenu layoutMenu = new LAFMenu(getName());
        layoutMenu.setEnabled(isEnabled());
        HelpCtx.setHelpIDString(layoutMenu, SelectLayoutAction.class.getName());
        return layoutMenu;
    }

    // -------

//    private FormDesigner formDesigner;
//
//    public void setFormDesigner(FormDesigner designer) {
//        formDesigner = designer;
//        setEnabled(formDesigner != null && formDesigner.getTopDesignComponent() != null);
//    }
    
    // LAFMenu

    private Class selectedLaf;
    
    private class LAFMenu extends JMenu implements ActionListener {
        private boolean initialized = false;

        private LAFMenu(String name) {
            super(name);
        }

        @Override
        public JPopupMenu getPopupMenu() {
            JPopupMenu popup = super.getPopupMenu();
            JMenuItem mi;
            if (!initialized) {
                popup.removeAll();

                boolean isSynthLAF = UIManager.getLookAndFeel() instanceof SynthLookAndFeel;
                String lafName = UIManager.getLookAndFeel().getClass().getName();
                
                // Swing L&Fs
                UIManager.LookAndFeelInfo[] lafs = UIManager.getInstalledLookAndFeels();
                for (int i=0; i<lafs.length; i++) {
                    String className = lafs[i].getClassName();
                    if (isSynthLAF) {
                        try {
                            Class lafClass = Class.forName(className);
                            if (!lafName.equals(className) && SynthLookAndFeel.class.isAssignableFrom(lafClass)) {
                                continue; // 134848, 145807: Cannot use two different SynthLookAndFeels
                            }
                        } catch (ClassNotFoundException cnfex) {
                            // should not happen
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, cnfex);
                        }
                    }
                    mi = new JMenuItem(lafs[i].getName());
                    mi.putClientProperty("lafInfo", new LookAndFeelItem(lafs[i].getClassName())); // NOI18N
                    mi.addActionListener(this);
                    popup.add(mi);
                }

                // L&Fs from the Palette
                Node[] cats = PaletteUtils.getCategoryNodes(PaletteUtils.getPaletteNode(), false);
                for (int i=0; i<cats.length; i++) {
                    if ("LookAndFeels".equals(cats[i].getName())) { // NOI18N
                        final Node lafNode = cats[i];
                        Node[] items = PaletteUtils.getItemNodes(lafNode, true);
                        if (items.length != 0) {
                            popup.add(new JSeparator());
                        }
                        for (int j=0; j<items.length; j++) {
                            PaletteItem pitem = items[j].getLookup().lookup(PaletteItem.class);
                            boolean supported = false;
                            try {
                                Class<?> clazz = pitem.getComponentClass();
                                if ((clazz != null) && (LookAndFeel.class.isAssignableFrom(clazz))) {
                                    LookAndFeel laf = (LookAndFeel)clazz.getDeclaredConstructor().newInstance();
                                    supported = laf.isSupportedLookAndFeel();
                                    if (supported && isSynthLAF && !lafName.equals(pitem.getComponentClassName())
                                            && SynthLookAndFeel.class.isAssignableFrom(clazz)) {
                                        supported = false; // 134848, 145807: Cannot use two different SynthLookAndFeels
                                    }
                                }
                            } catch (Exception ex) {
                                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                            } catch (LinkageError ex) {
                                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                            }
                            if (supported) {
                                mi = new JMenuItem(items[j].getDisplayName());
                                mi.putClientProperty("lafInfo", new LookAndFeelItem(pitem)); // NOI18N
                                mi.addActionListener(this);
                                popup.add(mi);
                            }
                        }
                    }
                }

                initialized = true;
            }
            return popup;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Object o = e.getSource();
            if (o instanceof JComponent) {
                JComponent source = (JComponent)o;
                LookAndFeelItem item = (LookAndFeelItem)source.getClientProperty("lafInfo"); // NOI18N
                try {
                    selectedLaf = item.getLAFClass();
                    run();
                } catch (Exception ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
        }

    }

    /**
     * Information about one look and feel.
     */
    static class LookAndFeelItem {
        /** Name of the look and feel's class. */
        private String className;
        /** The corresponding PaletteItem, if exists. */
        private PaletteItem pitem;

        public LookAndFeelItem(String className) {
            this.className = className;
        }

        public LookAndFeelItem(PaletteItem pitem) {
            this.pitem = pitem;
            this.className = pitem.getComponentClassName();
        }

        public String getClassName() {
            return className;
        }

        public Class getLAFClass() throws ClassNotFoundException {
            Class clazz;
            if (pitem == null) {
                if (className == null) {
                    clazz = UIManager.getLookAndFeel().getClass();
                } else {
                    ClassLoader classLoader = Lookup.getDefault().lookup(ClassLoader.class);
                    clazz = Class.forName(className, true, classLoader);
                }
            } else {
                clazz = pitem.getComponentClass();
            }
            return clazz;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof LookAndFeelItem)) return false;
            LookAndFeelItem item = (LookAndFeelItem)obj;
            return (pitem == item.pitem) && ((pitem != null)
                || ((className == null) ? (item.className == null) : className.equals(item.className)));
        }

        @Override
        public int hashCode() {
            return (className == null) ? pitem.hashCode() : className.hashCode();
        }
        
    }

}
