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
