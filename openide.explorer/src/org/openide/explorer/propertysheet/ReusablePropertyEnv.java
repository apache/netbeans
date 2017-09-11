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
package org.openide.explorer.propertysheet;

import java.beans.*;


/** A subclass of PropertyEnv that can be reused by the rendering infrastructure.
 *  All methods for attaching listeners are no-ops:  A renderer will only be
 *  momentarily attached to a given property, and property changes will result
 *  the property being rerendered (and the ReusablePropertyEnv being
 *  reconfigured correctly).<P>
 *  This class is <i>not thread safe</i>.  It assumes that it will
 *  only be called from the AWT thread, since it is used in painting
 *  infrastructure.  If property misrendering occurs, run NetBeans
 *  with the argument <code>-J-Dnetbeans.reusable.strictthreads=true</code>
 *  and exceptions will be thrown if it is called from off the
 *  AWT thread.
 *  <P>Note, the use of this class may be non-obvious at first - the value of
 *  <code>NODE</code> is set in the rendering loop, by the SheetTable instance,
 *  which knows about the nodes (other classes in the package should only
 *  be interested in the properties they represnt).  The instance is actually
 *  used in <code>PropertyEditorBridgeEditor.setPropertyEditor()</code>, but
 *  must rely on the table to configure it.
 * @author  Tim Boudreau
 */
final class ReusablePropertyEnv extends PropertyEnv {
    private Object NODE = null;
    private ReusablePropertyModel mdl;

    /** Creates a new instance of ReusablePropertyEnv */
    public ReusablePropertyEnv() {
        mdl = new ReusablePropertyModel(this);
    }

    public ReusablePropertyModel getReusablePropertyModel() {
        return mdl;
    }

    void clear() {
        NODE = null;

        if (mdl != null) {
            mdl.clear();
        }
    }

    void setReusablePropertyModel(ReusablePropertyModel mdl) {
        if (mdl == null) {
            throw new NullPointerException();
        }
        this.mdl = mdl;
    }

    /** Uses the <code>NODE</code> field to supply the beans - if it is an instance
     *  of ProxyNode (multi-selection), returns the nodes that ProxyNode represents. */
    public Object[] getBeans() {
        if (ReusablePropertyModel.DEBUG) {
            ReusablePropertyModel.checkThread();
        }

        if (getNode() instanceof ProxyNode) {
            return ((ProxyNode) getNode()).getOriginalNodes();
        } else if (getNode() instanceof Object[]) {
            return (Object[]) getNode();
        } else {
            return new Object[] { getNode() };
        }
    }

    public FeatureDescriptor getFeatureDescriptor() {
        return mdl.getProperty();
    }

    public void addVetoableChangeListener(VetoableChangeListener l) {
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
    }

    public void removeVetoableChangeListener(VetoableChangeListener l) {
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
    }

    public boolean isEditable() {
        boolean result;

        if (mdl.getProperty() != null) {
            result = mdl.getProperty().canWrite();
        } else {
            result = true;
        }

        return result;
    }

    public void reset() {
        setEditable(true);
        setState(STATE_NEEDS_VALIDATION);
    }

    public Object getNode() {
        return NODE;
    }

    public void setNode(Object NODE) {
        this.NODE = NODE;
    }
}
