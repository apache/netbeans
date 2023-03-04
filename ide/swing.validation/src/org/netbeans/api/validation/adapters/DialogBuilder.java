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
package org.netbeans.api.validation.adapters;

import java.awt.Component;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import org.netbeans.validation.api.ui.swing.SwingValidationGroup;
import org.netbeans.validation.api.ui.swing.ValidationPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Tim Boudreau
 */
public final class DialogBuilder {

    private String token;
    private String title;
    private boolean modal = true;
    private Object[] options;
    private Object defaultOption;
    private DialogType dialogType = DialogType.PLAIN;
    private Object message;
    private Object[] additionalOptions;
    private HelpCtx ctx;
    private ButtonSet optionType;
    private ActionListener al;
    private Object[] closingOptions;
    private SwingValidationGroup validationGroup;

    public DialogBuilder(Class<?> clazz) {
        this(clazz.getName());
    }

    public DialogBuilder(String token) {
        this.token = token;
    }

    public DialogBuilder setModal(boolean modal) {
        this.modal = modal;
        return this;
    }

    public DialogBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public DialogBuilder setOptions(Object... options) {
        this.options = options;
        if (optionType != null) {
            throw new IllegalStateException("Call setOptions, " + //NOI18N
                    "or setOptionType, not both." + //NOI18N
                    "They are mutually exclusive"); //NOI18N
        }
        return this;
    }

    public DialogBuilder setDefaultButton(Object defaultOption) {
        this.defaultOption = defaultOption;
        return this;
    }

    public DialogBuilder setDialogType(DialogType type) {
        this.dialogType = type;
        return this;
    }

    public DialogBuilder setContent(Object message) {
        this.message = message;
        return this;
    }

    public DialogBuilder setHelpContext(HelpCtx ctx) {
        this.ctx = ctx;
        return this;
    }

    public DialogBuilder setAdditionalButtons(Object[] additionalOptions) {
        this.additionalOptions = additionalOptions;
        return this;
    }

    public DialogBuilder setButtonSet(ButtonSet optionType) {
        this.optionType = optionType;
        if (options != null) {
            throw new IllegalStateException("Use setButtons or " + //NOI18N
                    "setButtonSet, not both"); //NOI18N
        }
        return this;
    }

    public DialogBuilder setActionListener(ActionListener al) {
        this.al = al;
        return this;
    }

    public DialogBuilder setClosingOptions(Object... options) {
        this.closingOptions = options;
        return this;
    }

    public DialogBuilder setValidationGroup(SwingValidationGroup group) {
        this.validationGroup = group;
        return this;
    }

    public boolean showDialog(Object okResult) {
        return okResult.equals(showDialog());
    }

    public Object showDialog() {
        DialogDescriptor des = createDialogDescriptor();
        Object dlgResult = DialogDisplayer.getDefault().notify(des);
        if (DialogDescriptor.YES_OPTION.equals(dlgResult) ||
                DialogDescriptor.OK_OPTION.equals(dlgResult) ||
                (defaultOption != null && defaultOption.equals(dlgResult)) || NbBundle.getMessage(DialogBuilder.class,
                "BTN_CLOSE").equals(dlgResult)) { //NOI18N
        }
        return dlgResult;
    }

    private ValidationPanel createValidationPanel(Object msg, DialogDescriptor des) {
        ValidationPanel result;
        result = validationGroup == null ? new ValidationPanel() : new ValidationPanel(validationGroup);
        result.setDelegateValidationUI(new DialogDescriptorAdapter(des));
        if (msg instanceof Component) {
            result.setInnerComponent((Component) msg);
        } else {
            result.setInnerComponent(new JLabel(msg.toString()));
        }
        return result;
    }

    private DialogDescriptor createDialogDescriptor() {
        if (message == null) {
            throw new IllegalStateException("Message not set"); //NOI18N
        }
        Object msg = message;
        final DialogDescriptor dlg = new DialogDescriptor(msg, title);
        if (validationGroup != null) {
            msg = createValidationPanel(msg, dlg);
            dlg.setMessage(msg);
        }

        if (msg instanceof ValidationPanel) {
            DialogDescriptorAdapter adap = new DialogDescriptorAdapter(dlg);
            ((ValidationPanel) msg).setDelegateValidationUI(adap);
        }

        dlg.setModal(modal);
        dlg.setHelpCtx(this.ctx == null ? HelpCtx.DEFAULT_HELP : this.ctx);
        if (additionalOptions != null) {
            dlg.setAdditionalOptions(options);
        }
        if (optionType != null) {
            if (optionType != ButtonSet.CLOSE) {
                dlg.setOptionType(optionType.getDialogDisplayerConstant());
            } else {
                dlg.setOptions(new Object[]{NbBundle.getMessage(DialogBuilder.class,
                            "BTN_CLOSE")}); //NOI18N
            }
        } else if (options == null) {
            dlg.setOptionType(ButtonSet.OK_CANCEL.getDialogDisplayerConstant());
        } else if (options != null) {
            dlg.setOptions(options);
        }
        if (al != null) {
            dlg.setButtonListener(al);
        }
        if (closingOptions != null) {
            dlg.setClosingOptions(options);
        }
        dlg.setMessageType(dialogType.getDialogDisplayerConstant());
        return dlg;
    }

    public enum DialogType {

        WARNING, INFO, QUESTION, PLAIN, ERROR;

        int getDialogDisplayerConstant() {
            switch (this) {
                case WARNING:
                    return DialogDescriptor.WARNING_MESSAGE;
                case INFO:
                    return DialogDescriptor.INFORMATION_MESSAGE;
                case QUESTION:
                    return DialogDescriptor.QUESTION_MESSAGE;
                case PLAIN:
                    return DialogDescriptor.PLAIN_MESSAGE;
                case ERROR:
                    return DialogDescriptor.ERROR_MESSAGE;
                default:
                    throw new AssertionError();
            }
        }
    }

    public enum ButtonSet {

        OK_CANCEL,
        YES_NO,
        YES_NO_CANCEL,
        CLOSE;

        private int getDialogDisplayerConstant() {
            switch (this) {
                case OK_CANCEL:
                    return DialogDescriptor.OK_CANCEL_OPTION;
                case YES_NO:
                    return DialogDescriptor.YES_NO_OPTION;
                case YES_NO_CANCEL:
                    return DialogDescriptor.YES_NO_CANCEL_OPTION;
                case CLOSE:
                    return -1;
                default:
                    throw new AssertionError();
            }
        }
    }
}
