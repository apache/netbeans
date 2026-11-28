/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.acp.ui;

import com.agentclientprotocol.sdk.spec.AcpSchema;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import junit.framework.TestCase;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.test.MockLookup;

public class AcpPermissionDialogTest extends TestCase {

    public void testChangingFromAllowToRejectClearsRememberedPermission() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            MockLookup.setInstances(new DialogDisplayer() {
                @Override
                public Object notify(NotifyDescriptor descriptor) {
                    Component panel = (Component) descriptor.getMessage();
                    JCheckBox remember = findComponent(panel, JCheckBox.class);
                    assertTrue(remember.isEnabled());
                    remember.setSelected(true);
                    findComponent(panel, JRadioButton.class, "Reject once").setSelected(true);
                    assertFalse(remember.isEnabled());
                    assertFalse(remember.isSelected());
                    return NotifyDescriptor.OK_OPTION;
                }

                @Override
                public Dialog createDialog(DialogDescriptor descriptor) {
                    throw new UnsupportedOperationException();
                }
            });
            try {
                var tool = new AcpSchema.ToolCallUpdate("call", "Edit file", "edit_file",
                        AcpSchema.ToolKind.EDIT, null, null, null, null, null, null);
                var request = new AcpSchema.RequestPermissionRequest("session", tool, List.of(
                        new AcpSchema.PermissionOption("allow", "Allow once", AcpSchema.PermissionOptionKind.ALLOW_ONCE),
                        new AcpSchema.PermissionOption("reject", "Reject once", AcpSchema.PermissionOptionKind.REJECT_ONCE)));
                var result = AcpPermissionDialog.showPermissionRequest(request);
                assertEquals(new AcpSchema.PermissionSelected("reject"), result.response().outcome());
                assertNull(result.allowToolForSession());
            } finally {
                MockLookup.setInstances();
            }
        });
    }

    private static <T extends Component> T findComponent(Component component, Class<T> type) {
        return findComponent(component, type, null);
    }

    private static <T extends Component> T findComponent(Component component, Class<T> type,
            String radioLabel) {
        if (type.isInstance(component)
                && (radioLabel == null || radioLabel.equals(((JRadioButton) component).getText()))) {
            return type.cast(component);
        }
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                T found = findComponent(child, type, radioLabel);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
