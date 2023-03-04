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
package org.netbeans.modules.profiler.heapwalk.details.jdk.ui;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.ObjectArrayInstance;
import org.netbeans.modules.profiler.heapwalk.details.jdk.ui.BaseBuilders.ColorBuilder;
import org.netbeans.modules.profiler.heapwalk.details.jdk.ui.BaseBuilders.IconBuilder;
import org.netbeans.modules.profiler.heapwalk.details.jdk.ui.BorderBuilders.BorderBuilder;
import org.netbeans.modules.profiler.heapwalk.details.jdk.ui.ComponentBuilders.ComponentBuilder;
import org.netbeans.modules.profiler.heapwalk.details.jdk.ui.ComponentBuilders.ContainerBuilder;
import org.netbeans.modules.profiler.heapwalk.details.jdk.ui.ComponentBuilders.JComponentBuilder;
import org.netbeans.modules.profiler.heapwalk.details.jdk.ui.Utils.InstanceBuilder;
import org.netbeans.modules.profiler.heapwalk.details.spi.DetailsUtils;

/**
 *
 * @author Jiri Sedlacek
 */
final class PaneBuilders {
    
    // Make sure subclasses are listed before base class if using isSubclassOf
    static ComponentBuilder getBuilder(Instance instance, Heap heap) {
        if (DetailsUtils.isSubclassOf(instance, JViewport.class.getName())) {
            return new JViewportBuilder(instance, heap);
        } else if (DetailsUtils.isSubclassOf(instance, JScrollPane.class.getName())) {
            return new JScrollPaneBuilder(instance, heap);
        } else if (DetailsUtils.isSubclassOf(instance, JSplitPane.class.getName())) {
            return new JSplitPaneBuilder(instance, heap);
        } else if (DetailsUtils.isSubclassOf(instance, BasicSplitPaneDivider.class.getName())) {
            return new BasicSplitPaneDividerBuilder(instance, heap);
        } else if (DetailsUtils.isSubclassOf(instance, JTabbedPane.class.getName())) {
            return new JTabbedPaneBuilder(instance, heap);
        }
        return null;
    }
    
    
    private static class JViewportBuilder extends JComponentBuilder<JViewport> {
        
        JViewportBuilder(Instance instance, Heap heap) {
            super(instance, heap);
        }
        
        protected JViewport createInstanceImpl() {
            return new JViewport();
        }
        
    }
    
    private static class JScrollPaneBuilder extends JComponentBuilder<JScrollPane> {
        
        private final BorderBuilder viewportBorder;
        
        JScrollPaneBuilder(Instance instance, Heap heap) {
            super(instance, heap);
            
            viewportBorder = BorderBuilders.fromField(instance, "viewportBorder", false, heap);

        }
        
        protected void setupInstance(JScrollPane instance) {
            super.setupInstance(instance);
            
            if (viewportBorder != null) {
                Border b = viewportBorder.createInstance();
                if (b != null) instance.setViewportBorder(b);
            }
        }
        
        protected JScrollPane createInstanceImpl() {
            return new JScrollPane();
        }
        
    }
    
    private static class JSplitPaneBuilder extends JComponentBuilder<JSplitPane> {
        
        private final int orientation;
        
        JSplitPaneBuilder(Instance instance, Heap heap) {
            super(instance, heap);
            
            orientation = DetailsUtils.getIntFieldValue(instance, "orientation", JSplitPane.HORIZONTAL_SPLIT);

        }
        
        protected JSplitPane createInstanceImpl() {
            return new JSplitPane(orientation);
        }
        
    }
    
    private static class BasicSplitPaneDividerBuilder extends ContainerBuilder<BasicSplitPaneDivider> {
        
        private final int orientation;
//        private final int dividerSize;
        private final BorderBuilder border;
        
        BasicSplitPaneDividerBuilder(Instance instance, Heap heap) {
            super(instance, heap, false);
            
            orientation = DetailsUtils.getIntFieldValue(instance, "orientation", JSplitPane.HORIZONTAL_SPLIT);
//            dividerSize = DetailsUtils.getIntFieldValue(instance, "dividerSize", 0);
            border = BorderBuilders.fromField(instance, "border", false, heap);
        }
        
        protected void setupInstance(BasicSplitPaneDivider instance) {
            super.setupInstance(instance);
            
//            instance.setDividerSize(dividerSize);
            if (border != null) {
                Border b = border.createInstance();
                if (b != null) instance.setBorder(b);
            }
        }
        
        protected BasicSplitPaneDivider createInstanceImpl() {
            final JSplitPane split = new JSplitPane(orientation);
            BasicSplitPaneUI ui = split.getUI() instanceof BasicSplitPaneUI ?
                    (BasicSplitPaneUI)split.getUI() : new BasicSplitPaneUI() {
                        { installUI(split); }
                    };
            return new BasicSplitPaneDivider(ui);
        }
        
    }
    
    
    private static class PageImpl {
        final String title;
        final ColorBuilder background;
        final ColorBuilder foreground;
        final IconBuilder icon;
        final long component;
        final boolean enabled;
        
        PageImpl(String title, ColorBuilder background, ColorBuilder foreground,
                 IconBuilder icon, long component, boolean enabled) {
            this.title = title;
            this.background = background;
            this.foreground = foreground;
            this.icon = icon;
            this.component = component;
            this.enabled = enabled;
        }
    }
    
    private static class PageImplBuilder extends InstanceBuilder<List<PageImpl>> {
        
        private final List<PageImpl> pages;
        
        PageImplBuilder(Instance instance, Heap heap) {
            super(instance, heap);
            
            pages = new ArrayList(1);
            
            Object _elementData = instance.getValueOfField("elementData");
            if (_elementData instanceof ObjectArrayInstance) {
                int size = DetailsUtils.getIntFieldValue(instance, "size", Integer.MIN_VALUE); // ArrayList, JDK 7+
                if (size == Integer.MIN_VALUE) size = DetailsUtils.getIntFieldValue(instance, "elementCount", 0); // Vector, JDK 6-
                
                if (size > 0) { // TODO: should read up to 'size' elements
                    ObjectArrayInstance elementData = (ObjectArrayInstance)_elementData;
                    for (Object _page : elementData.getValues()) {
                        if (_page instanceof Instance) {
                            Instance page = (Instance)_page;
                            Object comp = page.getValueOfField("component");
                            pages.add(new PageImpl(
                                            Utils.getFieldString(page, "title"),
                                            ColorBuilder.fromField(page, "background", heap),
                                            ColorBuilder.fromField(page, "foreground", heap),
                                            IconBuilder.fromField(page, "icon", heap),
                                            comp instanceof Instance ? ((Instance)comp).getInstanceId() : -1,
                                            DetailsUtils.getBooleanFieldValue(page, "enabled", true)
                                      ));
                        }
                    }
                }
            }
        }
        
        static PageImplBuilder fromField(Instance instance, String field, Heap heap) {
            Object pages = instance.getValueOfField(field);
            if (!(pages instanceof Instance)) return null;
            return new PageImplBuilder((Instance)pages, heap);
        }
        
        protected List<PageImpl> createInstanceImpl() {
            return pages;
        }
        
    }
    
    private static class JTabbedPaneBuilder extends JComponentBuilder<JTabbedPane> {
        
        private final int tabPlacement;
        private final int tabLayoutPolicy;
        private final PageImplBuilder pages;
        private final long visCompId;
        private final InstanceBuilder<Component> visComp;
        private int selComp = -1;
        
        JTabbedPaneBuilder(Instance instance, Heap heap) {
            super(instance, heap, false);
            
            tabPlacement = DetailsUtils.getIntFieldValue(instance, "tabPlacement", JTabbedPane.TOP);
            tabLayoutPolicy = DetailsUtils.getIntFieldValue(instance, "tabLayoutPolicy", JTabbedPane.WRAP_TAB_LAYOUT);
            pages = PageImplBuilder.fromField(instance, "pages", heap);
            
            Object _visComp = instance.getValueOfField("visComp");
            if (_visComp instanceof Instance) {
                Instance visCompI = (Instance)_visComp;
                visCompId = visCompI.getInstanceId();
                visComp = ComponentBuilders.getBuilder(visCompI, heap);
            } else {
                visCompId = Long.MIN_VALUE;
                visComp = null;
            }
        }
        
        protected void setupInstance(JTabbedPane instance) {
            super.setupInstance(instance);
            
            if (pages != null) {
                List<PageImpl> pageImpls = pages.createInstance();
                for (PageImpl page : pageImpls) {
                    int index = instance.getTabCount();
                    Component comp = null;
                    if (selComp == -1 && visComp != null && visCompId == page.component) {
//                        comp = new JPanel(null) { public boolean isOpaque() { return false; } };
                        comp = visComp.createInstance();
                        selComp = index;
                    }
                    instance.addTab(page.title, page.icon == null ? null : page.icon.createInstance(), comp);
                    if (page.background != null) {
                        Color background = page.background.createInstance();
                        if (background != null) instance.setBackgroundAt(index, background);
                    }
                    if (page.foreground != null) {
                        Color foreground = page.foreground.createInstance();
                        if (foreground != null) instance.setForegroundAt(index, foreground);
                    }
                    instance.setEnabledAt(index, page.enabled);
                }
            }
            
            if (selComp != -1) {
                instance.setSelectedIndex(selComp);
//                instance.add(visComp.createInstance());
                selComp = -1; // Cleanup for eventual Builder reuse
            }
        }
        
        protected JTabbedPane createInstanceImpl() {
            return new JTabbedPane(tabPlacement, tabLayoutPolicy) {
//                public Component add(Component component) {
//                    addImpl(component, null, getComponentCount());
//                    return component;
//                }
//                protected void processContainerEvent(ContainerEvent e) {}
            };
        }
        
    }
    
}
