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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import org.netbeans.modules.acp.ui.HtmlUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

@NbBundle.Messages({
    "TTL_Elicitation=Agent Input Request",
    "BTN_Accept=Accept",
    "BTN_Decline=Decline"
})
final class AcpElicitationDialog {

    private static final Logger LOG = Logger.getLogger(AcpElicitationDialog.class.getName());

    private AcpElicitationDialog() {
    }

    static CreateElicitationResponse show(CreateElicitationRequest request) {
        try {
            return Mutex.EVENT.readAccess(() -> showForm(request));
        } catch (RuntimeException ex) {
            LOG.log(Level.WARNING, "Elicitation dialog error", ex);
            return CreateElicitationResponse.decline();
        }
    }

    private static CreateElicitationResponse showForm(CreateElicitationRequest request) {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        if (request.message() != null && !request.message().isEmpty()) {
            form.add(new JLabel("<html><p style='width:350px'>"
                    + HtmlUtils.escapeHtml(request.message()) + "</p></html>"));
            form.add(Box.createVerticalStrut(8));
        }

        Map<String, Supplier<Object>> fields = new LinkedHashMap<>();
        ElicitationSchema schema = request.requestedSchema();
        if (schema != null && schema.properties() != null) {
            schema.properties().forEach((name, property) -> {
                fields.put(name, addField(form, name, property));
                form.add(Box.createVerticalStrut(4));
            });
        }

        String title = schema != null && schema.title() != null ? schema.title() : Bundle.TTL_Elicitation();
        String accept = Bundle.BTN_Accept();
        DialogDescriptor dialog = new DialogDescriptor(new JScrollPane(form), title,
                true, new Object[]{accept, Bundle.BTN_Decline()}, accept,
                DialogDescriptor.DEFAULT_ALIGN, null, null);
        if (!accept.equals(DialogDisplayer.getDefault().notify(dialog))) {
            return CreateElicitationResponse.decline();
        }
        Map<String, Object> content = new LinkedHashMap<>();
        fields.forEach((name, value) -> content.put(name, value.get()));
        return CreateElicitationResponse.accept(content);
    }

    private static Supplier<Object> addField(JPanel form, String name, ElicitationPropertySchema property) {
        if (property instanceof BooleanPropertySchema bool) {
            JCheckBox field = new JCheckBox(bool.title() != null ? bool.title() : name,
                    Boolean.TRUE.equals(bool.defaultValue()));
            field.putClientProperty("html.disable", Boolean.TRUE);
            form.add(field);
            return field::isSelected;
        }
        if (property instanceof StringPropertySchema string) {
            addLabel(form, string.title() != null ? string.title() : name);
            List<String> choices = string.enumValues();
            if ((choices == null || choices.isEmpty()) && string.oneOf() != null) {
                choices = string.oneOf().stream().map(EnumOption::constValue).toList();
            }
            if (choices != null && !choices.isEmpty()) {
                JComboBox<String> field = new JComboBox<>(choices.toArray(String[]::new));
                if (string.defaultValue() != null) {
                    field.setSelectedItem(string.defaultValue());
                }
                form.add(field);
                return field::getSelectedItem;
            }
            JTextField field = new JTextField(string.defaultValue(), 25);
            form.add(field);
            return field::getText;
        }
        addLabel(form, name);
        JTextField field = new JTextField(25);
        form.add(field);
        return field::getText;
    }

    private static void addLabel(JPanel form, String text) {
        JLabel label = new JLabel(text + ":");
        label.putClientProperty("html.disable", Boolean.TRUE);
        form.add(label);
    }
}
