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
/*
 * NodePropertyModel.java
 *
 * Created on April 22, 2003, 5:09 PM
 */
package org.openide.explorer.propertysheet;

import org.openide.nodes.Node;

import java.beans.FeatureDescriptor;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyEditor;

import java.lang.reflect.InvocationTargetException;


/** Implementation of the <code>PropertyModel</code> interface keeping
 * a <code>Node.Property</code>.  Refactored from PropertyPanel.SimpleModel
 * as part of the property sheet rewrite.  */
class NodePropertyModel implements ExPropertyModel {
    //This class was originally PropertyPanel.SimpleModel up to
    //PropertyPanel 1.123

    /** Property to work with.  */
    private Node.Property prop;

    /** Array of beans(nodes) to which belong the property.  */
    private Object[] beans;

    /** Property change support.  */
    private PropertyChangeSupport sup = new PropertyChangeSupport(this);
    String beanName = null;

    /** Construct simple model instance.
     * @param property proeprty to work with
     * @param beans array of beans(nodes) to which belong the property
     */
    public NodePropertyModel(Node.Property property, Object[] beans) {
        this.prop = property;
        this.beans = beans;
    }

    String getBeanName() {
        if (beans != null) {
            if ((beans.length == 1) && beans[0] instanceof Node.Property) {
                return ((Node.Property) beans[0]).getDisplayName();
            }
        }

        return null;
    }

    /** Implements <code>PropertyModel</code> interface.  */
    public Object getValue() throws InvocationTargetException {
        try {
            return prop.getValue();
        } catch (IllegalAccessException iae) {
            throw annotateException(iae);
        } catch (InvocationTargetException ite) {
            throw annotateException(ite);
        } catch (ProxyNode.DifferentValuesException dve) {
            return null;
        }
    }

    /** Implements <code>PropertyModel</code> interface.  */
    public void setValue(Object v) throws InvocationTargetException {
        try {
            prop.setValue(v);
            sup.firePropertyChange(PropertyModel.PROP_VALUE, null, null);
        } catch (IllegalAccessException iae) {
            throw annotateException(iae);
        } catch (IllegalArgumentException iaae) {
            throw annotateException(iaae);
        } catch (InvocationTargetException ite) {
            throw annotateException(ite);
        }
    }

    /** Annotates specified exception. Helper method.
     * @param exception original exception to annotate
     * @return <code>IvocationTargetException</code> which annotates the
     *       original exception
     */
    private InvocationTargetException annotateException(Exception exception) {
        if (exception instanceof InvocationTargetException) {
            return (InvocationTargetException) exception;
        } else {
            return new InvocationTargetException(exception);
        }
    }

    /** Implements <code>PropertyModel</code> interface.  */
    public Class getPropertyType() {
        return prop.getValueType();
    }

    /** Implements <code>PropertyModel</code> interface.  */
    public Class getPropertyEditorClass() {
        Object ed = prop.getPropertyEditor();

        if (ed != null) {
            return ed.getClass();
        }

        return null;
    }

    /** Mainly a hack to avoid gratuitous calls to fetch property editors.
     *  @since 1.123.2.1 - branch propsheet_issue_29447
     */
    public PropertyEditor getPropertyEditor() {
        return PropUtils.getPropertyEditor(prop);
    }

    /** Implements <code>PropertyModel</code> interface.  */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        sup.addPropertyChangeListener(l);
    }

    /** Implements <code>PropertyModel</code> interface.  */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        sup.removePropertyChangeListener(l);
    }

    /** Implements <code>ExPropertyModel</code> interface.  */
    public Object[] getBeans() {
        return beans;
    }

    /** Implements <code>ExPropertyModel</code> interface.  */
    public FeatureDescriptor getFeatureDescriptor() {
        return prop;
    }

    void fireValueChanged() {
        sup.firePropertyChange(PropertyModel.PROP_VALUE, null, null);
    }

    /** Package private method to return the property, so error handling
     *  can use the display name in the dialog for the user if the user
     *  enters an invalid value  */
    Node.Property getProperty() {
        return prop;
    }
}
