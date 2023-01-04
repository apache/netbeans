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
package org.netbeans.modules.profiler.heapwalk.details.jdk.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.modules.profiler.ProfilerTopComponent;
import org.netbeans.modules.profiler.heapwalk.details.jdk.ui.ComponentBuilders.ComponentBuilder;
import org.netbeans.modules.profiler.heapwalk.details.spi.DetailsProvider;
import org.netbeans.modules.profiler.heapwalk.details.spi.DetailsUtils;
import org.netbeans.modules.profiler.heapwalk.model.BrowserUtils;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "ComponentDetailsProvider_NewWindow=Analyze in new window",
    "ComponentDetailsProvider_InvisibleComponentPrefix=[invisible]"
})
@ServiceProvider(service=DetailsProvider.class)
public final class ComponentDetailsProvider extends DetailsProvider.Basic {
    
    private static final String JLABEL_MASK = "javax.swing.JLabel+";                // NOI18N
    private static final String ABSTRACTBUTTON_MASK = "javax.swing.AbstractButton+";// NOI18N
    private static final String JTOOLTIP_MASK = "javax.swing.JToolTip+";            // NOI18N
    private static final String JFILECHOOSER_MASK = "javax.swing.JFileChooser+";    // NOI18N
    private static final String TABLECOLUMN_MASK = "javax.swing.table.TableColumn+";// NOI18N
    private static final String JPANEL_MASK = "javax.swing.JPanel+";                // NOI18N
    private static final String JPROGRESSBAR_MASK = "javax.swing.JProgressBar+";    // NOI18N
    
    private static final String JINTERNALFRAME_MASK = "javax.swing.JInternalFrame+";// NOI18N
    private static final String FRAME_MASK = "java.awt.Frame+";                     // NOI18N
    private static final String DIALOG_MASK = "java.awt.Dialog+";                   // NOI18N
    
    private static final String COMPONENT_MASK = "java.awt.Component+";             // NOI18N
    
    public ComponentDetailsProvider() {
        super(JLABEL_MASK, ABSTRACTBUTTON_MASK, JTOOLTIP_MASK, JFILECHOOSER_MASK,
              JINTERNALFRAME_MASK, TABLECOLUMN_MASK, JPANEL_MASK, JPROGRESSBAR_MASK,
              FRAME_MASK, DIALOG_MASK, COMPONENT_MASK);
    }
    
    public String getDetailsString(String className, Instance instance, Heap heap) {
        String string = null;
        
        if (JLABEL_MASK.equals(className) ||                                        // JLabel+
            ABSTRACTBUTTON_MASK.equals(className)) {                                // AbstractButton+
            string = DetailsUtils.getInstanceFieldString(
                    instance, "text", heap);                                        // NOI18N
        } else if (JTOOLTIP_MASK.equals(className)) {                               // JToolTip+
            string = DetailsUtils.getInstanceFieldString(
                    instance, "tipText", heap);                                     // NOI18N
        } else if (JFILECHOOSER_MASK.equals(className)) {                           // JFileChooser+
            string = DetailsUtils.getInstanceFieldString(
                    instance, "dialogTitle", heap);                                 // NOI18N
        } else if (JINTERNALFRAME_MASK.equals(className) ||                         // JInternalFrame+
                   FRAME_MASK.equals(className) ||                                  // Frame+
                   DIALOG_MASK.equals(className)) {                                 // Dialog+
            string = DetailsUtils.getInstanceFieldString(
                    instance, "title", heap);                                       // NOI18N
        } else if (TABLECOLUMN_MASK.equals(className)) {                            // TableColumn+
            string = DetailsUtils.getInstanceFieldString(
                    instance, "headerValue", heap);                                 // NOI18N
        } else if (JPROGRESSBAR_MASK.equals(className)) {                           // JProgressBar+
            boolean b = DetailsUtils.getBooleanFieldValue(
                    instance, "paintString", false);                                // NOI18N
            if (b) string = DetailsUtils.getInstanceFieldString(
                    instance, "progressString", heap);                              // NOI18N
        }
        
        if (string == null) {
            // Value for a generic Component
            string = getStringField(instance, "displayName", heap);
            if (string == null) string = getStringField(instance, "label", heap);
            if (string == null) string = getStringField(instance, "name", heap);
            // TODO: check tooltip

            if (string != null && string.trim().isEmpty()) string = null;
        }
        
        if (string != null) {
            // Mark invisible components
            boolean b = DetailsUtils.getBooleanFieldValue(
                    instance, "visible", false);                                    // NOI18N
            if (!b) string = Bundle.ComponentDetailsProvider_InvisibleComponentPrefix() + " " + string; // NOI18N
        }
        
        return string;
    }
    
    public View getDetailsView(String className, Instance instance, Heap heap) {
        return new ComponentView(instance, heap);
    }
    
    
    private static String getStringField(Instance instance, String field, Heap heap) {
        Object string = instance.getValueOfField(field);
        if (string instanceof Instance &&
            String.class.getName().equals(((Instance)string).getJavaClass().getName()))
            return DetailsUtils.getInstanceString((Instance)string, heap);
        return null;
    }
    
    
    private static class ComponentView extends Utils.View<ComponentBuilder> {
        
        private ComponentBuilder builder;
        private Component component;
        private Component hover;
        private final MouseHandler mouse;
        
        private JComponent glassPane;
        
        private final String className;
        private final int instanceNumber;
        
        private final boolean enableNewWindow;
        private final boolean enableInteraction;
        
        ComponentView(Instance instance, Heap heap) {
            this(instance, heap, null, true, false);
        }
        
        private ComponentView(Instance instance, Heap heap, ComponentBuilder builder,
                              boolean enableNewWindow, boolean enableInteraction) {
            super(instance, heap);
            
            this.builder = builder;
            this.enableNewWindow = enableNewWindow;
            this.enableInteraction = enableInteraction;
            
            if (enableNewWindow || enableInteraction) {
                mouse = new MouseHandler();
                addMouseListener(mouse);
            } else {
                mouse = null;
            }
            
            if (enableNewWindow) {
                className = instance.getJavaClass().getName();
                instanceNumber = instance.getInstanceNumber();
            } else {
                className = null;
                instanceNumber = -1;
            }
        }
        
        protected ComponentBuilder getBuilder(Instance instance, Heap heap) {
            if (builder == null)
                builder = ComponentBuilders.getBuilder(instance, heap);
            return builder;
        }
        
        protected Component getComponent(ComponentBuilder builder) {
            component = builder.createPresenter();
            if (component != null) component.setVisible(true);
            return component;
        }
        
        protected void setupGlassPane(JPanel glassPane) {
            this.glassPane = glassPane;
            if (mouse != null) {
                glassPane.addMouseListener(mouse);
                if (enableInteraction) {
                    glassPane.addMouseMotionListener(mouse);
                }
            }
        }
        
        private class MouseHandler extends MouseAdapter {

            @Override
            public void mousePressed(final MouseEvent e) {
                if (e.isPopupTrigger()) showPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) showPopup(e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                Component at = e == null ? null :
                        componentAt(component, e.getX(), e.getY());
                if (hover == at) return;
                hover = at;
                hoverChanged();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!enableInteraction) return;
                mouseMoved(null);
            }
            
        }
        
        
        private void hoverChanged() {
            String toolTipText;
            if (hover != null) {                
                JComponent jc = hover instanceof JComponent ? (JComponent)hover : null;
                Object cn = jc == null ? null : jc.getClientProperty("className");
                String name = cn == null ? "" : cn.toString();
                
                toolTipText = name.isEmpty() ? null : name;
            } else {
                toolTipText = null;
            }
            // ToolTipManager doesn't like changing the tooltip from mouseMoved().
            // This is a workaround to hide the tip when needed and prevent NPEs.
            if (toolTipText == null) ToolTipManager.sharedInstance().mousePressed(null);
            glassPane.setToolTipText(toolTipText);
            repaint();
        }
        
        public void paint(Graphics g) {
            super.paint(g);
            
            if (hover != null) {
                Rectangle b = SwingUtilities.convertRectangle(
                        hover.getParent(), hover.getBounds(), this);
                g.setColor(Color.RED);
                g.drawRect(b.x, b.y, b.width, b.height);
            }
        }
        
        static Component componentAt(Component comp, int x, int y) {
            if (!comp.contains(x, y)) return null;
            
            if (comp instanceof Container) {
                for (Component c : ((Container)comp).getComponents()) {
                    if (c != null && c.isVisible()) {
                        Component at = componentAt(c, x - c.getX(), y - c.getY());
                        if (at != null) return at;
                    }
                }
            }
                
            return comp;
        }
        
        private void showPopup(MouseEvent e) {
            if (!enableNewWindow || builder == null || component == null) return;
            
            JMenuItem test = new JMenuItem(Bundle.ComponentDetailsProvider_NewWindow()) {
                @Override
                protected void fireActionPerformed(ActionEvent e) {
                    openNewWindow();
                }
            };
            
            JPopupMenu popup = new JPopupMenu();
            popup.add(test);
            popup.show(e.getComponent(), e.getX(), e.getY());
        }
        
        private void openNewWindow() {
            Component c = new ComponentView(null, null, builder, false, true);
            ComponentTopComponent ctc =
                    new ComponentTopComponent(c, className, instanceNumber);
            ctc.open();
            ctc.requestActive();
        }
        
    }
    
    private static class ComponentTopComponent extends ProfilerTopComponent {
        
        private static final String HELP_CTX_KEY = "HeapWalker.ComponentPreview.HelpCtx"; // NOI18N
        private static final HelpCtx HELP_CTX = new HelpCtx(HELP_CTX_KEY);
        
        ComponentTopComponent(Component c, String className, int instanceNumber) {
            setName(BrowserUtils.getSimpleType(className) + " #" + instanceNumber);
//            setIcon(Icons.getImage(ProfilerIcons.HEAP_DUMP));
            setToolTipText("Preview of " + className + " #" + instanceNumber);
//            getAccessibleContext().setAccessibleDescription(org.netbeans.modules.profiler.heapwalk.ui.Bundle.HeapWalkerUI_ComponentDescr());
            
            setLayout(new BorderLayout());
            add(new JScrollPane(c), BorderLayout.CENTER);
        }
        
        public int getPersistenceType() {
            return TopComponent.PERSISTENCE_NEVER;
        }
        
        protected String preferredID() {
            return this.getClass().getName();
        }
        
        public HelpCtx getHelpCtx() {
            return HELP_CTX;
        }
        
    }
    
}
