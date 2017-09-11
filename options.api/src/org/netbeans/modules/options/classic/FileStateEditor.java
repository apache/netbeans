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

package org.netbeans.modules.options.classic;

import org.netbeans.beaninfo.editors.ListImageEditor;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.explorer.propertysheet.PropertyEnv;

import java.lang.reflect.InvocationTargetException;

import org.netbeans.modules.options.classic.SettingChildren.FileStateProperty;

/**
 *
 * @author  Vitezslav Stejskal
 */
class FileStateEditor extends ListImageEditor {

    private String action_define = null;
    private String action_revert = null;
    private String action_delete = null;
    
    private Node.Property<?> prop = null;

    /** Creates new FileStatePropertyEditor */
    public FileStateEditor () {
        super ();
        
        action_define = NbBundle.getMessage (FileStateEditor.class, "LBL_action_define");
        action_revert = NbBundle.getMessage (FileStateEditor.class, "LBL_action_revert");
        action_delete = NbBundle.getMessage (FileStateEditor.class, "LBL_action_delete");
    }

    @Override
    public void attachEnv (PropertyEnv env) {
        super.attachEnv (env);
        
        try {
            prop = (Node.Property)env.getFeatureDescriptor ();
        } catch (ClassCastException cce) {
            ClassCastException cce2 = new ClassCastException("Expected a Node.Property but got a " + env.getFeatureDescriptor() + " descriptor " + env.getFeatureDescriptor().getClass().getName());
            throw cce2;
        }
    }
    
    @Override
    public String getAsText () {
        return null;
    }

    @Override
    public void setAsText (String str) throws java.lang.IllegalArgumentException {        
        try {
            Integer value = null;
            if (action_define.equals (str)) {
                value  = Integer.valueOf (SettingChildren.FileStateProperty.ACTION_DEFINE);
            }
            if (action_revert.equals (str)) {
                value = Integer.valueOf (FileStateProperty.ACTION_REVERT);
            }
            if (action_delete.equals (str)) {
                value = Integer.valueOf (FileStateProperty.ACTION_DELETE);                
            }
            if (value != null) {
                doSetValue(prop, value);
                super.setValue(value);                
            }
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
    }

    private <T> void doSetValue(Node.Property<T> prop, Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        prop.setValue(prop.getValueType().cast(value));
    }
    
    @Override
    public String[] getTags () {
        Integer val = (Integer) getValue ();

        if (SettingChildren.PROP_LAYER_MODULES.equals (prop.getName ())) {
            return new String [] {
                action_revert
            };
        }
        if (val != null &&
            val.intValue () == FileStateManager.FSTATE_IGNORED) {
            return new String [] {
                action_define,
                action_revert,
                action_delete
            };
        }
        return new String [] {
            action_define
        };
    }
}
