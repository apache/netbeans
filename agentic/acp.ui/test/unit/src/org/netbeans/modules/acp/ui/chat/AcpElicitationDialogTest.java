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
package org.netbeans.modules.acp.ui.chat;

import com.agentclientprotocol.sdk.spec.AcpSchema.BooleanPropertySchema;
import com.agentclientprotocol.sdk.spec.AcpSchema.CreateElicitationRequest;
import com.agentclientprotocol.sdk.spec.AcpSchema.CreateElicitationResponse;
import com.agentclientprotocol.sdk.spec.AcpSchema.ElicitationPropertySchema;
import com.agentclientprotocol.sdk.spec.AcpSchema.ElicitationSchema;
import com.agentclientprotocol.sdk.spec.AcpSchema.EnumOption;
import com.agentclientprotocol.sdk.spec.AcpSchema.StringPropertySchema;
import java.awt.Component;
import java.awt.Dialog;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import junit.framework.TestCase;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.test.MockLookup;

public class AcpElicitationDialogTest extends TestCase {

    public void testAcceptsEditedTextBooleanAndEnumValues() {
        MockLookup.setInstances(displayer(dialog -> {
            assertEquals("Input", dialog.getTitle());
            JPanel form = (JPanel) ((JScrollPane) dialog.getMessage()).getViewport().getView();
            for (Component component : form.getComponents()) {
                if (component instanceof JTextField text) {
                    assertEquals("initial", text.getText());
                    text.setText("updated");
                } else if (component instanceof JCheckBox check) {
                    assertTrue(check.isSelected());
                    check.setSelected(false);
                } else if (component instanceof JComboBox<?> choices) {
                    assertEquals("basic", choices.getSelectedItem());
                    choices.setSelectedItem("full");
                }
            }
            return dialog.getOptions()[0];
        }));
        try {
            Map<String, ElicitationPropertySchema> fields = new LinkedHashMap<>();
            fields.put("name", new StringPropertySchema("string", "Name", null, "initial",
                    null, null, null, null, null, null, null));
            fields.put("enabled", new BooleanPropertySchema("boolean", "Enabled", null, true));
            fields.put("mode", StringPropertySchema.singleSelect("Mode",
                    List.of(new EnumOption("basic", "Basic"), new EnumOption("full", "Full"))));
            ElicitationSchema schema = new ElicitationSchema("object", fields, List.of(), "Input", null);

            CreateElicitationResponse response = AcpElicitationDialog.show(
                    CreateElicitationRequest.form("session", "Choose your options", schema));
            assertEquals(CreateElicitationResponse.accept(
                    Map.of("name", "updated", "enabled", false, "mode", "full")), response);
        } finally {
            MockLookup.setInstances();
        }
    }

    public void testDeclineAndCloseDoNotReturnFormValues() throws Exception {
        try {
            for (boolean close : new boolean[]{false, true}) {
                MockLookup.setInstances(displayer(dialog -> close ? NotifyDescriptor.CLOSED_OPTION : dialog.getOptions()[1]));
                SwingUtilities.invokeAndWait(() -> assertEquals(CreateElicitationResponse.decline(),
                        AcpElicitationDialog.show(CreateElicitationRequest.form("session", "Input",
                                new ElicitationSchema(Map.of(), List.of())))));
            }
        } finally {
            MockLookup.setInstances();
        }
    }

    private static DialogDisplayer displayer(Function<NotifyDescriptor, Object> choose) {
        return new DialogDisplayer() {
            @Override
            public Object notify(NotifyDescriptor descriptor) {
                assertTrue(SwingUtilities.isEventDispatchThread());
                return choose.apply(descriptor);
            }

            @Override
            public Dialog createDialog(DialogDescriptor descriptor) {
                throw new UnsupportedOperationException();
            }
        };
    }
}
