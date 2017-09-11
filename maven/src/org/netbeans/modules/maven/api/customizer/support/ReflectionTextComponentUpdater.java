/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.api.customizer.support;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.JLabel;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.maven.api.customizer.ModelHandle;
import org.netbeans.modules.maven.api.customizer.ModelHandle2;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.pom.POMComponent;
import org.netbeans.modules.maven.model.pom.POMModel;

/**
 *
 * @author mkleint
 */
public final class ReflectionTextComponentUpdater extends TextComponentUpdater {
    private final Object model;
    private final Object defaults;
    private final Method modelgetter;
    private Method defgetter;
    private Method modelsetter;
    private ModelHandle handle;
    private ModelHandle2 handle2;
    private String initialValue2;
    private Operation operation;
    /** Creates a new instance of ReflectionTextComponentUpdater */
    public ReflectionTextComponentUpdater(String getter, String setter, Object model, Object defaults, JTextComponent field, JLabel label, ModelHandle handle) 
                        throws NoSuchMethodException {
        this(getter, setter, model, defaults, field, label);
        this.handle = handle;
        
    }
    
    public ReflectionTextComponentUpdater(String getter, Object model, Object defaults, JTextComponent field, JLabel label, ModelHandle2 handle, Operation operation) 
                        throws NoSuchMethodException {
        this(getter, null, model, defaults, field, label);
        assert handle != null;
        assert operation != null;
        this.operation = operation;
        this.handle2 = handle;
        initialValue2 = getValue();
    }
    
    
    private ReflectionTextComponentUpdater(String getter, String setter, Object model, Object defaults, JTextComponent field, JLabel label) 
                        throws NoSuchMethodException {
        super(field, label);
        this.model = model;
        this.defaults = defaults;
        modelgetter = model.getClass().getMethod(getter, new Class[0]);
        if (setter != null) { 
        modelsetter = model.getClass().getMethod(setter, new Class[] {String.class});
        }
        if (defaults != null) {
            defgetter = defaults.getClass().getMethod(getter, new Class[0]);
        }
    }
    
    
    @Override
    public String getValue() {
        if (operation.isValueSet) {
            return operation.getNewValue();
        }
        try {
            return (String)modelgetter.invoke(model, new Object[0]);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    @Override
    public String getDefaultValue() {
        if (defgetter == null) {
            return null;
        }
        try {
            return (String)defgetter.invoke(defaults, new Object[0]);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    
    @Override
    public void setValue(String value) {
        try {
            if (handle != null) {
                modelsetter.invoke(model, new Object[] { value });
                if (model instanceof POMComponent) {
                    handle.markAsModified(((POMComponent)model).getModel());
                } else {
                    handle.markAsModified(model);
                }
            }
            if (handle2 != null) {
                operation.setNewValue(value);
                if (value != null && value.equals(initialValue2)) {
                    handle2.removePOMModification(operation);
                } else {
                    //TODO ideally only add if not added before..
                    handle2.addPOMModification(operation);
                }
            }
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }
    }
    
    
    public static abstract class Operation implements ModelOperation<POMModel> {

        boolean isValueSet = false; 
        private String newValue;
        
        public final void setNewValue(String value) {
            newValue = value;
            isValueSet = true;
        }
        
        public final String getNewValue() {
            return newValue;
        }
        
        
    }
    
}
