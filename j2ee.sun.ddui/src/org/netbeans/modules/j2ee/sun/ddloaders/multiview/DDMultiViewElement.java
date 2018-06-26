/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.j2ee.sun.ddloaders.multiview;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ToolBarMultiViewElement;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor;
import org.openide.ErrorManager;


/**
 * @author pfiala
 */
public abstract class DDMultiViewElement extends ToolBarMultiViewElement {
    
    private static final long serialVersionUID = 20110810L;

    private SectionView view;
    protected ToolBarDesignEditor comp;
    protected SunDescriptorDataObject dataObject;

    public DDMultiViewElement(SunDescriptorDataObject dataObject) {
        super(dataObject);
        this.dataObject = dataObject;
        comp = new ToolBarDesignEditor();
        setVisualEditor(comp);
    }

    @Override
    public void componentShowing() {
        if (view == null) {
            view = createView();
            if (view instanceof SectionNodeView) {
                dataObject.getDDRoot().addPropertyChangeListener(new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        ((SectionNodeView) view).dataModelPropertyChange(evt.getSource(), evt.getPropertyName(),
                                evt.getOldValue(), evt.getNewValue());
                    }
                });
            }
        }
        comp.setContentView(view);
        if (view instanceof SectionNodeView) {
            ((SectionNodeView) view).refreshView();
        }
        view.checkValidity();
        super.componentShowing();
        Object lastActive = comp.getLastActive();
        final SectionNode node;
        final SectionNodeView sectionNodeView = ((SectionNodeView) view);
        if (lastActive instanceof SectionNode) {
            node = (SectionNode) lastActive;
        } else {
            node = sectionNodeView.getRootNode();
        }
        
        // TODO possible race condition -- occasionally nodes being opened by
        // this call do not exist at the time the call is made (possibly they have
        // been replaced by the background updates.)
        try {
            sectionNodeView.openPanel(node);
        } catch(IllegalArgumentException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        
        dataObject.checkParseable();
    }

    protected abstract SectionView createView();
    
    @Override
    public SectionView getSectionView() {
        return view;
    }
}
