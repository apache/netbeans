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

package org.netbeans.modules.xml.multiview;

import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor;
import org.openide.loaders.DataObject;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ProxyLookup;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;

/**
 * An implementation of <code>MultiViewElement</code> that is specific to 
 * <code>XmlMultiViewDataObject</code>. 
 *
 * @see org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor
 *
 * Created on October 5, 2004, 1:35 PM
 * @author  mkuchtiak
 */
public abstract class ToolBarMultiViewElement extends AbstractMultiViewElement {
    private ToolBarDesignEditor editor;
    
    private PropertyChangeListener listener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            if (DataObject.PROP_MODIFIED.equals(evt.getPropertyName()) && editor != null) {
                Utils.runInAwtDispatchThread(new Runnable() {
                    public void run() {
                        if (null != callback){
                            callback.getTopComponent().setDisplayName(dObj.getEditorSupport().messageName());
                            callback.getTopComponent().setHtmlDisplayName(dObj.getEditorSupport().messageHtmlName());
                            callback.getTopComponent().setToolTipText(dObj.getEditorSupport().messageToolTip());
                        }
                    }
                });
            }
        }
    };
    
    public ToolBarMultiViewElement(final XmlMultiViewDataObject dObj) {
        super(dObj);
        dObj.addPropertyChangeListener(WeakListeners.propertyChange(listener, dObj));
    }
    
    protected void setVisualEditor(ToolBarDesignEditor editor) {
        this.editor=editor;
    }
    
    public CloseOperationState canCloseElement() {
        if (!editorValidate()) {
            return MultiViewFactory.createUnsafeCloseState(ToolBarDesignEditor.PROPERTY_FLUSH_DATA, null, null);
        } else {
            return super.canCloseElement();
        }
    }
    
    private boolean editorValidate() {
        try {
            editor.fireVetoableChange(ToolBarDesignEditor.PROPERTY_FLUSH_DATA, this, null);
            return true;
        } catch (PropertyVetoException e) {
            return false;
        }
    }
    
    public void componentActivated() {
        editor.componentActivated();
    }
    
    public void componentClosed() {
        editor.componentClosed();
    }
    
    public void componentDeactivated() {
        editor.componentDeactivated();
    }
    
    public void componentHidden() {
        editor.componentHidden();
        dObj.setActiveMultiViewElement(null);
    }
    
    public void componentOpened() {
        editor.componentOpened();
    }
    
    public void componentShowing() {
        if (editorValidate()) {
            editor.componentShowing();
            dObj.setActiveMultiViewElement(this);
        }
    }
    
    public org.openide.util.Lookup getLookup() {
        return new ProxyLookup(new org.openide.util.Lookup[] {
            dObj.getNodeDelegate().getLookup()
        });
    }
    
    public javax.swing.JComponent getToolbarRepresentation() {
        return editor.getStructureView();
    }
    
    public javax.swing.JComponent getVisualRepresentation() {
        return editor;
    }
    /** Enable to get the SectionView included in this MultiView Element
     */
    public abstract SectionView getSectionView();
    
}
