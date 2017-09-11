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
 * ReusablePropertyModel.java
 *
 * Created on February 6, 2003, 5:12 PM
 */
package org.openide.explorer.propertysheet;

import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;

import java.beans.PropertyEditor;

import javax.swing.SwingUtilities;
import org.openide.util.Exceptions;


/** A reconfigurable property model for use by the rendering
 *  infrastructure, to avoid allocating memory while painting.
 *  Contains two static fields, PROPERTY and NODE which
 *  set the node and property this model acts as an interface to.<P>
 *  This class is <i>not thread safe</i>.  It assumes that it will
 *  only be called from the AWT thread, since it is used in painting
 *  infrastructure.  If property misrendering occurs, run NetBeans
 *  with the argument <code>-J-Dnetbeans.reusable.strictthreads=true</code>
 *  and exceptions will be thrown if any method is called from off the
 *  AWT thread.
 *
 * @author  Tim Boudreau
 */
class ReusablePropertyModel implements ExPropertyModel {
    static final boolean DEBUG = Boolean.getBoolean("netbeans.reusable.strictthreads");
    private transient Property PROPERTY = null;
    private final ReusablePropertyEnv env;

    /** Creates a new instance of ReusablePropertyModel */
    public ReusablePropertyModel(ReusablePropertyEnv env) {
        this.env = env;
        env.setReusablePropertyModel(this);
    }

    void clear() {
        PROPERTY = null;
    }

    /** Does nothing - if a property changes, the sheet will get notification
     *  and the model will be reconfigured with the new value and re-rendered */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
    }

    /** Does nothing - if a property changes, the sheet will get notification
     *  and the model will be reconfigured with the new value and re-rendered */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
    }

    public PropertyEditor getPropertyEditor() {
        Node.Property p = getProperty();

        // #52179: PropUtils.isExternallyEdited(p) - don't affect just 
        // externally edited properties or their current changes will be lost 
        // due to the firing PropertyChangeEvents to theirs UI counterpart
        return PropUtils.getPropertyEditor(p, !PropUtils.isExternallyEdited(p));
    }

    public Class getPropertyEditorClass() {
        if (DEBUG) {
            checkThread();
        }

        return getPropertyEditor().getClass();
    }

    public Class getPropertyType() {
        if (DEBUG) {
            checkThread();
        }

        return null == getProperty() ? Object.class : getProperty().getValueType();
    }

    public Object getValue() throws java.lang.reflect.InvocationTargetException {
        if (DEBUG) {
            checkThread();
        }

        try {
            return getProperty().getValue();
        } catch (IllegalAccessException iae) {
            Exceptions.printStackTrace(iae);
        }

        return null;
    }

    public void setValue(Object v) throws java.lang.reflect.InvocationTargetException {
        if (DEBUG) {
            checkThread();
        }

        try {
            getProperty().setValue(v);
        } catch (IllegalAccessException iae) {
            Exceptions.printStackTrace(iae);
        }
    }

    public Object[] getBeans() {
        if (DEBUG) {
            checkThread();
        }

        if (env.getNode() instanceof ProxyNode) {
            return ((ProxyNode) env.getNode()).getOriginalNodes();
        } else {
            return new Object[] { env.getNode() };
        }
    }

    public java.beans.FeatureDescriptor getFeatureDescriptor() {
        if (DEBUG) {
            checkThread();
        }

        return getProperty();
    }

    /** Ensure we're really running on the AWT thread, otherwise bad things can
     *  happen.  */
    static void checkThread() {
        if (SwingUtilities.isEventDispatchThread() == false) {
            throw new IllegalStateException("Reusable property model accessed from off the AWT thread.");
        }
    }

    public Node.Property getProperty() {
        return PROPERTY;
    }

    public void setProperty(Node.Property PROPERTY) {
        this.PROPERTY = PROPERTY;
    }
}
