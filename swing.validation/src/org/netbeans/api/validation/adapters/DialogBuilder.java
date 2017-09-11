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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
