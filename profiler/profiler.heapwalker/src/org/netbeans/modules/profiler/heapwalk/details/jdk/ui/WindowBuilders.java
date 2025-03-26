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

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.beans.PropertyVetoException;
import java.util.List;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.ObjectArrayInstance;
import org.netbeans.modules.profiler.heapwalk.details.jdk.image.ImageBuilder;
import org.netbeans.modules.profiler.heapwalk.details.jdk.ui.BaseBuilders.IconBuilder;
import org.netbeans.modules.profiler.heapwalk.details.jdk.ui.ComponentBuilders.ComponentBuilder;
import org.netbeans.modules.profiler.heapwalk.details.jdk.ui.ComponentBuilders.ContainerBuilder;
import org.netbeans.modules.profiler.heapwalk.details.jdk.ui.ComponentBuilders.JComponentBuilder;
import org.netbeans.modules.profiler.heapwalk.details.spi.DetailsUtils;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Jiri Sedlacek
 */
final class WindowBuilders {
    
    // Make sure subclasses are listed before base class if using isSubclassOf
    static ComponentBuilder getBuilder(Instance instance, Heap heap) {
        if (DetailsUtils.isSubclassOf(instance, JRootPane.class.getName())) {
            return new JRootPaneBuilder(instance, heap);
        } else if (DetailsUtils.isSubclassOf(instance, JDesktopPane.class.getName())) {
            return new JDesktopPaneBuilder(instance, heap);
        } else if (DetailsUtils.isSubclassOf(instance, JLayeredPane.class.getName())) {
            return new JLayeredPaneBuilder(instance, heap);
        } else if (DetailsUtils.isSubclassOf(instance, Frame.class.getName())) {
            return new FrameBuilder(instance, heap);
        } else if (DetailsUtils.isSubclassOf(instance, Dialog.class.getName())) {
            return new DialogBuilder(instance, heap);
        } else if (DetailsUtils.isSubclassOf(instance, JInternalFrame.class.getName())) {
            return new JInternalFrameBuilder(instance, heap);
        }
        return null;
    }
    
    
    private static class JRootPaneBuilder extends JComponentBuilder<JRootPane> {
        
        private final int windowDecorationStyle;
        
        JRootPaneBuilder(Instance instance, Heap heap) {
            super(instance, heap);
            
            windowDecorationStyle = DetailsUtils.getIntFieldValue(instance, "windowDecorationStyle", 0);
        }
        
        static JRootPaneBuilder fromField(Instance instance, String field, Heap heap) {
            Object insets = instance.getValueOfField(field);
            if (!(insets instanceof Instance)) return null;
            return new JRootPaneBuilder((Instance)insets, heap);
        }
        
        protected void setupInstance(JRootPane instance) {
            super.setupInstance(instance);
            
            instance.setWindowDecorationStyle(windowDecorationStyle);
//            instance.setPreferredSize(instance.getSize());
        }
        
        protected JRootPane createInstanceImpl() {
            return new JRootPane();
        }
        
    }
    
    private static class JLayeredPaneBuilder extends JComponentBuilder<JLayeredPane> {
        
        JLayeredPaneBuilder(Instance instance, Heap heap) {
            super(instance, heap);
        }
        
        protected JLayeredPane createInstanceImpl() {
            return new JLayeredPane();
        }
        
    }
    
    private static class JDesktopPaneBuilder extends JLayeredPaneBuilder {
        
        JDesktopPaneBuilder(Instance instance, Heap heap) {
            super(instance, heap);
        }
        
        protected JLayeredPane createInstanceImpl() {
            return new JDesktopPane();
        }
        
    }
    
    private static class FrameBuilder extends ContainerBuilder<Frame> {
        
        private final String title;
        private final boolean undecorated;
        private final Image image;
        
        FrameBuilder(Instance instance, Heap heap) {
            super(instance, heap);
            
            title = Utils.getFieldString(instance, "title");
            undecorated = DetailsUtils.getBooleanFieldValue(instance, "undecorated", false);
            
            Image _image = null;
            Object icons = instance.getValueOfField("icons");
            if (icons instanceof Instance) {
                Instance i = (Instance)icons;
                if (DetailsUtils.getIntFieldValue(i, "size", 0) > 0) {
                    Object elementData = i.getValueOfField("elementData");
                    if (elementData instanceof ObjectArrayInstance) {
                        Object o = ((ObjectArrayInstance)elementData).getValues().get(0);
                        _image = o != null ? ImageBuilder.buildImage((Instance)o, heap) : null;
                    }
                }
            }
            image = _image;
        }
        
        protected void setupInstance(Frame instance) {
            super.setupInstance(instance);
            
            instance.setUndecorated(undecorated);
            if (image != null) instance.setIconImage(image);
        }
        
        protected Frame createInstanceImpl() {
            return new JFrame(title) {
                protected void frameInit() {}
                public void addNotify() {}
                public void remove(Component comp) {}
                public void setVisible(boolean b) {}
            };
        }
        
        protected Component createPresenterImpl(Frame instance) {
            if (instance.isUndecorated()) {
                JPanel presenter = new JPanel(null);
                presenter.setOpaque(true);
                Dimension dim = null;
                for (Component c : instance.getComponents()) {
                    presenter.add(c);
                    Dimension s = c.getSize();
                    if (dim == null) {
                        dim = s;
                    } else {
                        dim.width = Math.max(dim.width, s.width);
                        dim.height = Math.max(dim.height, s.height);
                    }
                }
                if (dim != null) presenter.setSize(dim);
                return presenter;
            } else {
                JInternalFrame presenter = new JInternalFrame(instance.getTitle());
                Image img = instance.getIconImage();
                if (img != null) presenter.setFrameIcon(ImageUtilities.image2Icon(img));
                for (Component c : instance.getComponents()) presenter.add(c);
                presenter.pack();
                return presenter;
            }
        }
        
    }
    
    private static class DialogBuilder extends ContainerBuilder<Dialog> {
        
        private final String title;
        private final boolean undecorated;
        private final Image image;
        
        DialogBuilder(Instance instance, Heap heap) {
            super(instance, heap);
            
            title = Utils.getFieldString(instance, "title");
            undecorated = DetailsUtils.getBooleanFieldValue(instance, "undecorated", false);
            
            Image _image = null;
            Object icons = instance.getValueOfField("icons");
            if (icons == null) {
                Object parent = instance.getValueOfField("parent");
                while (parent instanceof Instance) {
                    icons = ((Instance)parent).getValueOfField("icons");
                    if (icons instanceof Instance) break;
                    parent = ((Instance)parent).getValueOfField("parent");
                }
            }
            if (icons instanceof Instance) {
                Instance i = (Instance)icons;
                if (DetailsUtils.getIntFieldValue(i, "size", 0) > 0) {
                    Object elementData = i.getValueOfField("elementData");
                    if (elementData instanceof ObjectArrayInstance) {
                        Object o = ((ObjectArrayInstance)elementData).getValues().get(0);
                        _image = o != null ? ImageBuilder.buildImage((Instance)o, heap) : null;
                    }
                }
            }
            image = _image;
        }
        
        protected void setupInstance(Dialog instance) {
            super.setupInstance(instance);
            
            instance.setUndecorated(undecorated);
            if (image != null) instance.setIconImage(image);
        }
        
        protected Dialog createInstanceImpl() {
            return new JDialog((Frame)null, title) {
                protected void dialogInit() {}
                public void addNotify() {}
                public void remove(Component comp) {}
                public void setVisible(boolean b) {}
            };
        }
        
        protected Component createPresenterImpl(Dialog instance) {
            if (instance.isUndecorated()) {
                JPanel presenter = new JPanel(null);
                presenter.setOpaque(true);
                Dimension dim = null;
                for (Component c : instance.getComponents()) {
                    presenter.add(c);
                    Dimension s = c.getSize();
                    if (dim == null) {
                        dim = s;
                    } else {
                        dim.width = Math.max(dim.width, s.width);
                        dim.height = Math.max(dim.height, s.height);
                    }
                }
                if (dim != null) presenter.setSize(dim);
                return presenter;
            } else {
                JInternalFrame presenter = new JInternalFrame(instance.getTitle());
                List<Image> images = instance.getIconImages();
                Image img = images.isEmpty() ? null : images.get(0);
                if (img != null) presenter.setFrameIcon(ImageUtilities.image2Icon(img));
                for (Component c : instance.getComponents()) presenter.add(c);
                presenter.pack();
                return presenter;
            }
        }
        
    }
    
    private static class JInternalFrameBuilder extends JComponentBuilder<JInternalFrame> {
        
        private final JRootPaneBuilder _rootPane;
        private final boolean closable;
        private final boolean _isClosed;
        private final boolean maximizable;
        private final boolean _isMaximum;
        private final boolean iconable;
        private final boolean _isIcon;
        private final boolean resizable;
        private final boolean _isSelected;
        private final IconBuilder frameIcon;
        private final String  title;
        
        JInternalFrameBuilder(Instance instance, Heap heap) {
            super(instance, heap, false);
            
            _rootPane = JRootPaneBuilder.fromField(instance, "rootPane", heap);
            closable = DetailsUtils.getBooleanFieldValue(instance, "closable", false);
            _isClosed = DetailsUtils.getBooleanFieldValue(instance, "isClosed", false);
            maximizable = DetailsUtils.getBooleanFieldValue(instance, "maximizable", false);
            _isMaximum = DetailsUtils.getBooleanFieldValue(instance, "isMaximum", false);
            iconable = DetailsUtils.getBooleanFieldValue(instance, "iconable", false);
            _isIcon = DetailsUtils.getBooleanFieldValue(instance, "isIcon", false);
            resizable = DetailsUtils.getBooleanFieldValue(instance, "resizable", false);
            _isSelected = DetailsUtils.getBooleanFieldValue(instance, "isSelected", false);
            frameIcon = IconBuilder.fromField(instance, "frameIcon", heap);
            title = Utils.getFieldString(instance, "title");
        }
        
        protected void setupInstance(JInternalFrame instance) {
            super.setupInstance(instance);
            
            if (frameIcon != null) instance.setFrameIcon(frameIcon.createInstance());
        }
        
        protected JInternalFrame createInstanceImpl() {
            JInternalFrame frame = new JInternalFrame(title, resizable, closable, maximizable, iconable) {
                protected JRootPane createRootPane() {
                    return _rootPane == null ? null : _rootPane.createInstance();
                }
                public void addNotify() {
                    try {
                        // Doesn't seem to work correctly
                        setClosed(_isClosed);
                        setMaximum(_isMaximum);
                        setIcon(_isIcon);
                        setSelected(_isSelected);
                    } catch (PropertyVetoException ex) {}
                }
            };
            return frame;
        }
        
    }
    
}
