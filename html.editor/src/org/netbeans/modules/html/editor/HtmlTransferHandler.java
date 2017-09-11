/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.InputEvent;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.TransferHandler;
import javax.swing.plaf.UIResource;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.html.editor.api.HtmlKit;

/* !!!!!!!!!!!!!!!!!!!!!
 *
 * fix for issue #43309
 *
 * !!!!!!!!!!!!!!!!!!!!!
 */
public class HtmlTransferHandler extends TransferHandler implements UIResource {
    
    public static void install(JTextComponent c) {
        TransferHandler origHandler = c.getTransferHandler();
        if (!(origHandler instanceof HtmlTransferHandler)) {
            c.setTransferHandler(new HtmlTransferHandler(c.getTransferHandler()));
        }
    }
    
    private final TransferHandler delegate;

    public HtmlTransferHandler(TransferHandler delegate) {
        this.delegate = delegate;
    }
    
    TransferHandler getDelegate() {
        return delegate;
    }
    
    @Override
    public boolean canImport(TransferSupport support) {
        if(support.getComponent() instanceof JEditorPane &&
                ((JEditorPane) support.getComponent()).getEditorKit() instanceof HtmlKit) {
            HtmlKit kit = (HtmlKit) ((JEditorPane) support.getComponent()).getEditorKit();
            APIAccessor.DEFAULT.setContentType(kit, "text/plain");
            try {
                return delegate.canImport(support);
            } finally {
                APIAccessor.DEFAULT.setContentType(kit, HtmlKit.HTML_MIME_TYPE);
            }
        } else {
            return delegate.canImport(support);
        }
    }
    
    @Override
    public boolean importData(TransferSupport support) {
        if(support.getComponent() instanceof JEditorPane &&
                ((JEditorPane) support.getComponent()).getEditorKit() instanceof HtmlKit) {
            HtmlKit kit = (HtmlKit) ((JEditorPane) support.getComponent()).getEditorKit();
            APIAccessor.DEFAULT.setContentType(kit, "text/plain");
            try {
                return delegate.importData(support);
            } finally {
                APIAccessor.DEFAULT.setContentType(kit, HtmlKit.HTML_MIME_TYPE);
            }
        } else {
            return delegate.importData(support);
        }
    }

    @Override
    public boolean canImport(JComponent comp, DataFlavor[] t) {
        if(comp instanceof JEditorPane &&
                ((JEditorPane) comp).getEditorKit() instanceof HtmlKit) {
            HtmlKit kit = (HtmlKit) ((JEditorPane) comp).getEditorKit();
            APIAccessor.DEFAULT.setContentType(kit, "text/plain");
            try {
                return delegate.canImport(comp, t);
            } finally {
                APIAccessor.DEFAULT.setContentType(kit, HtmlKit.HTML_MIME_TYPE);
            }
        } else {
            return delegate.canImport(comp, t);
        }
    }

    @Override
    public boolean importData(JComponent comp, Transferable t) {
        if(comp instanceof JEditorPane &&
                ((JEditorPane) comp).getEditorKit() instanceof HtmlKit) {
            HtmlKit kit = (HtmlKit) ((JEditorPane) comp).getEditorKit();
            APIAccessor.DEFAULT.setContentType(kit, "text/plain");
            try {
                return delegate.importData(comp, t);
            } finally {
                APIAccessor.DEFAULT.setContentType(kit, HtmlKit.HTML_MIME_TYPE);
            }
        } else {
            return delegate.importData(comp, t);
        }
    }
    
    @Override
    protected Transferable createTransferable(JComponent c) {
        try {
            java.lang.reflect.Method method = delegate.getClass().getDeclaredMethod(
                    "createTransferable", // NOI18N
                    new Class[]{javax.swing.JComponent.class});
            method.setAccessible(true);

            return (Transferable) method.invoke(delegate, new Object[]{c});
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (java.lang.reflect.InvocationTargetException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public void exportAsDrag(JComponent comp, InputEvent e, int action) {
        delegate.exportAsDrag(comp, e, action);
    }

    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {
        try {
            java.lang.reflect.Method method = delegate.getClass().getDeclaredMethod(
                    "exportDone", // NOI18N
                    new Class[]{javax.swing.JComponent.class, Transferable.class, int.class});
            method.setAccessible(true);
            method.invoke(delegate, new Object[]{source, data, new Integer(action)});
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (java.lang.reflect.InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void exportToClipboard(JComponent c, Clipboard clip, int action) throws IllegalStateException {
        delegate.exportToClipboard(c, clip, action);
    }

    @Override
    public int getSourceActions(JComponent c) {
        return delegate.getSourceActions(c);
    }

    @Override
    public Icon getVisualRepresentation(Transferable t) {
        return delegate.getVisualRepresentation(t);
    }
}
