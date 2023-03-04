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
            method.invoke(delegate, new Object[]{source, data, Integer.valueOf(action)});
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
