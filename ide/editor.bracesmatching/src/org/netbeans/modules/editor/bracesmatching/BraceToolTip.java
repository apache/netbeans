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
package org.netbeans.modules.editor.bracesmatching;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.text.JTextComponent;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author sdedic
 */
public class BraceToolTip extends JPanel {
    private JEditorPane previewPane;
    private int editorPaneWidth;
    
    public BraceToolTip(JComponent c, JEditorPane pane) {
        this.previewPane = pane;
        
        setLayout(new BorderLayout());
        add(c, BorderLayout.CENTER);
    }

    public BraceToolTip(JEditorPane editorPane, JEditorPane previewPane) {
        this.previewPane = previewPane;

        setLayout(new BorderLayout());
        add(previewPane, BorderLayout.CENTER);
        putClientProperty("tooltip-type", "fold-preview"); // Checked in NbToolTip

        addGlyphGutter(previewPane);

        editorPaneWidth = editorPane.getSize().width;

        Color foreColor = previewPane.getForeground();
        setBorder(new LineBorder(foreColor));
        setOpaque(true);
    }

    private void addGlyphGutter(JTextComponent jtx) {
        ClassLoader cls = Lookup.getDefault().lookup(ClassLoader.class);
        Class clazz;
        
        JComponent gutter = null;
        try {
            clazz = Class.forName("org.netbeans.editor.GlyphGutter", true, cls); // NOI18N
            // get the factory instance
            Object o = clazz.getDeclaredConstructor().newInstance();
            Method m = clazz.getDeclaredMethod("createSideBar", JTextComponent.class); // NOI18N
            gutter = (JComponent)m.invoke(o, jtx);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SecurityException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InstantiationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (gutter != null) {
            add(gutter, BorderLayout.WEST);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width, d.height);
    }
    
    
}
