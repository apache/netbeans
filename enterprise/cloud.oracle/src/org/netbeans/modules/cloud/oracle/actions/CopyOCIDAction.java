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
package org.netbeans.modules.cloud.oracle.actions;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.ExClipboard;

/**
 *
 * @author Tomas Hurka
 */
@ActionID(
        category = "Tools",
        id = "org.netbeans.modules.cloud.oracle.actions.CopyOCIDAction"
)
@ActionRegistration(displayName = "#CTL_CopyOCIDAction")
@ActionReferences(value = {
    @ActionReference(path = "Cloud/Oracle/Common/Actions", position = 300)
})
@NbBundle.Messages("CTL_CopyOCIDAction=Copy OCID")
public class CopyOCIDAction implements ActionListener {

    private final OCIItem context;

    public CopyOCIDAction(OCIItem context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        setClipboardContents(context.getKey().getValue());
    }

    @NbBundle.Messages("MSG_OCID_placed_in_clipboard=OCID was placed in clipboard")
    private void setClipboardContents(String content) {
        Clipboard clipboard = Lookup.getDefault().lookup(ExClipboard.class);
        if (clipboard == null) {
            clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        }
        if (clipboard != null) {
            clipboard.setContents(new StringSelection(content), null);
            StatusDisplayer.getDefault().setStatusText(Bundle.MSG_OCID_placed_in_clipboard());
        }
    }
}
