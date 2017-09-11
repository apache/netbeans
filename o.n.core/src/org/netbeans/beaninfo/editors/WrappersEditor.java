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

package org.netbeans.beaninfo.editors;

import java.beans.*;
import java.text.MessageFormat;
import org.netbeans.core.UIExceptions;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.NbBundle;

/**
 * Abstract class represents Editor for Wrappers of 8 known primitive types
 * (Byte, Short, Integer, Long, Boolean, Float, Double, Character)
 *
 * @author  Josef Kozak
 */
public abstract class WrappersEditor implements ExPropertyEditor {

    protected PropertyEditor pe = null;
    
    public WrappersEditor(Class type) {
        super();
        pe = PropertyEditorManager.findEditor(type);
    }
    
    public void setValue(Object newValue) throws IllegalArgumentException {
        pe.setValue(newValue);
    }
    
    public Object getValue() {
	return pe.getValue();
    }        
    
    public boolean isPaintable() {
	return pe.isPaintable();
    }

    public void paintValue(java.awt.Graphics gfx, java.awt.Rectangle box) {
        pe.paintValue(gfx, box);
    }        
    
    public String getAsText () {
        if ( pe.getValue() == null )
            return "null";              // NOI18N
        return pe.getAsText();
    }

    public void setAsText(String text) throws IllegalArgumentException {
        if ( "null".equals( text ) )    // NOI18N
            return;
        try {
            pe.setAsText(text);
        } catch (Exception e) {
            //Reasonable to assume any exceptions from core/jdk editors are legit
            IllegalArgumentException iae = new IllegalArgumentException (e.getMessage());
            String msg = e.getLocalizedMessage();
            if (msg == null || e.getMessage().equals(msg)) {
                msg = MessageFormat.format(
                NbBundle.getMessage(
                    WrappersEditor.class, "FMT_EXC_GENERIC_BAD_VALUE"), text); //NOI18N
            }
            UIExceptions.annotateUser(iae, iae.getMessage(), msg, e,
                                     new java.util.Date());
            throw iae;
        }
    }
    
    public String[] getTags() {
	return pe.getTags();
    }
    
    public java.awt.Component getCustomEditor() {
	return pe.getCustomEditor();
    }

    public boolean supportsCustomEditor() {
	return pe.supportsCustomEditor();
    }
  
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        pe.addPropertyChangeListener(listener);
    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        pe.removePropertyChangeListener(listener);
    }    
    
    public void attachEnv(PropertyEnv env) {
        //Delegate if the primitive editor is an ExPropertyEditor -
        //boolean and int editors will be
        if (pe instanceof ExPropertyEditor) {
            ((ExPropertyEditor) pe).attachEnv (env);
        }
    }
}
