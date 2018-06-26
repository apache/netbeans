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
package org.netbeans.modules.websvc.api.support;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;
import org.openide.util.Lookup;

/**
 * Provides a facility for obtaining the Invoke Operation feature.
 * for both JAX-WS and JAX-RPC web service.
 */
public interface InvokeOperationCookie {

    /** Adds a method definition to the the implementation class, possibly to SEI.
     *
     * @param sourceNodeLookup source node lookup
     * @param targetComponent target text component where the code should be generated
     */
    void invokeOperation(Lookup sourceNodeLookup, JTextComponent targetComponent);

    /** provides JPanel for dialog descriptor to choose web service clients.
     *
     * @return ClientSelectionPanel panel
     */
    ClientSelectionPanel getDialogDescriptorPanel();

    /** Abstract JPanel for Client selection.
     *
     */
    public abstract static class ClientSelectionPanel extends JPanel {
        /** Property to fire when the selection is valid, invalid. */
        public static final String PROPERTY_SELECTION_VALID =
                ClientSelectionPanel.class.getName() + ".SELECTION_VALID"; //NOI18N

        private boolean selectionValid;

        /** Set Node selection valid or invalid.
         *
         * @param selectionValid true node selection is valid fasle if not
         */
        protected final void setSelectionValid(boolean selectionValid) {
            boolean wasSelectionValid = this.selectionValid;
            if (wasSelectionValid != selectionValid) {
                this.selectionValid = selectionValid;
                firePropertyChange(PROPERTY_SELECTION_VALID, wasSelectionValid, selectionValid);
            }
        }

        /** Get lookup context of selected client node.
         *
         * @return lookup of selected client node
         */
        public abstract Lookup getSelectedClient();

    }
    /** Enumeration for target source type.
     */
    public enum TargetSourceType {
        /** Target source is java class. */
        JAVA,
        /** Target source is JSP. */
        JSP,
        /** Target source is unknown. */
        UNKNOWN;
    }
}
