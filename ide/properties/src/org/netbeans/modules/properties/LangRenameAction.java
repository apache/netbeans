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


package org.netbeans.modules.properties;


import java.text.MessageFormat;

import org.openide.actions.RenameAction;
import org.openide.nodes.Node;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;


/**
 * Renames a <code>PropertiesLocaleNode</code> node,
 * i.e. one locale (=locale suffix of one properties file)
 * belonging to certain bundle of properties files.
 *
 * @author   Petr Jiricka
 * @see Node#setName
 */
public class LangRenameAction extends RenameAction {

    /** Generated serial version UID. */
    static final long serialVersionUID =-6548687347804513177L;
    

    /** Performs action. Overrides superclass method. */
    protected void performAction (Node[] activatedNodes) {
        Node n = activatedNodes[0]; // we supposed that one node is activated
        Node.Cookie cake = n.getCookie(PropertiesLocaleNode.class);
        PropertiesLocaleNode pln = (PropertiesLocaleNode)cake;

        String lang = Util.getLocaleSuffix(pln.getFileEntry());
        if (lang.length() > 0)
            if (lang.charAt(0) == PropertiesDataLoader.PRB_SEPARATOR_CHAR)
                lang = lang.substring(1);

        NotifyDescriptor.InputLine dlg = new NotifyDescriptor.InputLine(
		NbBundle.getMessage(LangRenameAction.class,
				    "LBL_RenameLabel"),			//NOI18N
		NbBundle.getMessage(LangRenameAction.class,
				    "LBL_RenameTitle"));		//NOI18N
        
        dlg.setInputText(lang);
        if (NotifyDescriptor.OK_OPTION.equals(DialogDisplayer.getDefault().notify(dlg))) {
            try {
                pln.setName(Util.assembleName (pln.getFileEntry().getDataObject().getPrimaryFile().getName(), dlg.getInputText()));
            }
            catch (IllegalArgumentException e) {
                // catch & report badly formatted names
                NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                    MessageFormat.format(
                        NbBundle.getBundle("org.openide.actions.Bundle").getString("MSG_BadFormat"),
                        new Object[] {pln.getName()}),
                    NotifyDescriptor.ERROR_MESSAGE);
                        
                DialogDisplayer.getDefault().notify(msg);
            }
        }
    }
}
