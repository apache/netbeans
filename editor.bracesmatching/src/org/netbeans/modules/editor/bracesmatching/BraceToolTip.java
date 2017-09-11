/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
            Object o = clazz.newInstance();
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
