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
package org.netbeans.lib.profiler.ui.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FocusTraversalPolicy;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.lib.profiler.ui.swing.GenericToolbar;
import org.openide.util.Lookup;

/**
 *
 * @author Jiri Sedlacek
 */
public abstract class ProfilerToolbar {
    
    public static ProfilerToolbar create(boolean showSeparator) {
        Provider provider = Lookup.getDefault().lookup(Provider.class);
        return provider != null ? provider.create(showSeparator) :
                                  new Impl(showSeparator);
    }
    
    
    public abstract JComponent getComponent();
    
    
    public Component add(ProfilerToolbar toolbar) { return toolbar.getComponent(); }
    
    public Component add(ProfilerToolbar toolbar, int index) { return toolbar.getComponent(); }
    
    public void remove(ProfilerToolbar toolbar) {}
    
    
    public abstract Component add(Action action);
    
    public abstract Component add(Component component);
    
    public abstract Component add(Component component, int index);
    
    public abstract void addSeparator();
    
    public abstract void addSpace(int width);
    
    public abstract void addFiller();
    
    public abstract void remove(Component component);
    
    public abstract void remove(int index);
    
    public abstract int getComponentCount();
    
    
    protected ProfilerToolbar() {}
    
    
    public abstract static class Provider {
        
        public abstract ProfilerToolbar create(boolean showSeparator);
        
    }
    
    public static class Impl extends ProfilerToolbar {
        
        protected final JComponent component;
        protected final JToolBar toolbar;
        
        protected Impl(boolean showSeparator) {
            toolbar = new GenericToolbar();
            if (UIUtils.isWindowsModernLookAndFeel())
                toolbar.setBorder(BorderFactory.createEmptyBorder(2, 2, 1, 2));
            else if (!UIUtils.isNimbusLookAndFeel() && !UIUtils.isAquaLookAndFeel())
                toolbar.setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 2));
            toolbar.setBorderPainted(false);
            toolbar.setRollover(true);
            toolbar.setFloatable(false);
            
            toolbar.setFocusTraversalPolicyProvider(true);
            toolbar.setFocusTraversalPolicy(new SimpleFocusTraversalPolicy());
            
            if (showSeparator) {
                component = new JPanel(new BorderLayout(0, 0));
                component.setOpaque(false);
                component.add(toolbar, BorderLayout.CENTER);
                component.add(UIUtils.createHorizontalLine(toolbar.getBackground()),
                        BorderLayout.SOUTH);
            } else {
                component = toolbar;
            }
        }
        
        @Override
        public JComponent getComponent() {
            return component;
        }
        
        @Override
        public Component add(ProfilerToolbar toolbar) {
            return add(toolbar, getComponentCount());
        }
    
        @Override
        public Component add(ProfilerToolbar toolbar, int index) {
            JToolBar implToolbar = ((Impl)toolbar).toolbar;
            implToolbar.setBorder(BorderFactory.createEmptyBorder());
            implToolbar.setOpaque(false);
            implToolbar.putClientProperty("Toolbar.noGTKBorder", Boolean.TRUE); // NOI18N
            return add(implToolbar, index);
        }
        
        @Override
        public void remove(ProfilerToolbar toolbar) {
            remove(((Impl)toolbar).toolbar);
        }

        @Override
        public Component add(Action action) {
            Component c = toolbar.add(action);
            tweakComponent(c);
            toolbar.repaint();
            return c;
        }

        @Override
        public Component add(Component component) {
            Component c = toolbar.add(component);
            tweakComponent(c);
            toolbar.repaint();
            return c;
        }
        
        @Override
        public Component add(Component component, int index) {
            Component c = toolbar.add(component, index);
            tweakComponent(c);
            toolbar.repaint();
            return c;
        }

        @Override
        public void addSeparator() {
            toolbar.addSeparator();
            toolbar.repaint();
        }
        
        @Override
        public void addSpace(int width) {
            toolbar.addSeparator(new Dimension(width, 0));
            toolbar.repaint();
        }
        
        @Override
        public void addFiller() {
            Dimension minDim = new Dimension(0, 0);
            Dimension maxDim = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
            toolbar.add(new Box.Filler(minDim, minDim, maxDim));
            toolbar.repaint();
        }
        
        @Override
        public void remove(Component component) {
            toolbar.remove(component);
            toolbar.repaint();
        }
        
        @Override
        public void remove(int index) {
            toolbar.remove(index);
            toolbar.repaint();
        }
        
        @Override
        public int getComponentCount() {
            return toolbar.getComponentCount();
        }
        
        
        private void tweakComponent(Component c) {
            if (c instanceof JComponent) ((JComponent)c).setOpaque(false);
        }
        
    }
    
    
    public static class SimpleFocusTraversalPolicy extends FocusTraversalPolicy {
        
        public Component getComponentAfter(Container aContainer, Component aComponent) {
            List<Component> l = components(topContainer(aContainer));
            int i = l.indexOf(aComponent);
            return i == -1 || i == l.size() - 1 ? null : l.get(i + 1);
        }

        public Component getComponentBefore(Container aContainer, Component aComponent) {
            List<Component> l = components(topContainer(aContainer));
            int i = l.indexOf(aComponent);
            return i == -1 || i == 0 ? null : l.get(i - 1);
        }

        public Component getFirstComponent(Container aContainer) {
            List<Component> l = components(topContainer(aContainer));
            return l.isEmpty() ? null : l.get(0);
        }

        public Component getLastComponent(Container aContainer) {
            List<Component> l = components(topContainer(aContainer));
            return l.isEmpty() ? null : l.get(l.size() - 1);
        }

        public Component getDefaultComponent(Container aContainer) {
            return getFirstComponent(aContainer);
        }

        protected Container topContainer(Container aContainer) {
            while (aContainer.getParent() instanceof JToolBar)
                aContainer = aContainer.getParent();
            return aContainer;
        }

        protected List<Component> components(Container aContainer) {
            List<Component> l = new ArrayList<>();

            for (int i = 0; i < aContainer.getComponentCount(); i++) {
                Component c = aContainer.getComponent(i);
                if (c instanceof JToolBar || c instanceof JPanel)
                    l.addAll(components((Container)c));
                else if (focusable(c)) l.add(c);
            }

            return l;
        }
        
        protected boolean focusable(Component c) {
            return c.isVisible() && c.isEnabled() && c.isFocusable();
        }
        
    }
    
}
