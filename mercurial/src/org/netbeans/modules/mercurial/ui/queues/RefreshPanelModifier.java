/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.mercurial.ui.queues;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import org.netbeans.modules.versioning.util.common.VCSCommitOptions;
import org.netbeans.modules.versioning.util.common.VCSCommitPanelModifier;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
final class RefreshPanelModifier extends VCSCommitPanelModifier {

    private static Map<String, RefreshPanelModifier> instances = new HashMap<String, RefreshPanelModifier>(2);
    private final Map<BundleMessage, String> messages;
    
    private RefreshPanelModifier () {
        messages = new EnumMap<BundleMessage, String>(BundleMessage.class);
    }

    static RefreshPanelModifier getDefault (String budleSuffixKey) {
        RefreshPanelModifier instance = instances.get(budleSuffixKey);
        if (instance == null) {
            RefreshPanelModifier prov = new RefreshPanelModifier();
            ResourceBundle loc = NbBundle.getBundle(RefreshPanelModifier.class);
            prov.messages.put(BundleMessage.FILE_TABLE_HEADER_COMMIT, loc.getString("FILE_TABLE_HEADER_COMMIT")); //NOI18N
            prov.messages.put(BundleMessage.FILE_TABLE_HEADER_COMMIT_DESC, loc.getString("FILE_TABLE_HEADER_COMMIT_DESC")); //NOI18N
            prov.messages.put(BundleMessage.FILE_TABLE_HEADER_ACTION, loc.getString("FILE_TABLE_HEADER_ACTION")); //NOI18N
            prov.messages.put(BundleMessage.FILE_TABLE_HEADER_ACTION_DESC, loc.getString("FILE_TABLE_HEADER_ACTION_DESC")); //NOI18N
            prov.messages.put(BundleMessage.FILE_TABLE_INCLUDE_ACTION_NAME, loc.getString("IncludeOption.name")); //NOI18N
            prov.messages.put(BundleMessage.FILE_TABLE_EXCLUDE_ACTION_NAME, loc.getString("ExcludeOption.name")); //NOI18N
            prov.messages.put(BundleMessage.FILE_TABLE_ACCESSIBLE_NAME, loc.getString("FILE_TABLE_ACCESSIBLE_NAME")); //NOI18N
            prov.messages.put(BundleMessage.FILE_TABLE_ACCESSIBLE_DESCRIPTION, loc.getString("FILE_TABLE_ACCESSIBLE_DESCRIPTION")); //NOI18N
            prov.messages.put(BundleMessage.FILE_PANEL_TITLE, loc.getString("FILE_PANEL_TITLE")); //NOI18N
            prov.messages.put(BundleMessage.TABS_MAIN_NAME, loc.getString("TABS_MAIN_NAME")); //NOI18N
            prov.messages.put(BundleMessage.COMMIT_BUTTON_ACCESSIBLE_DESCRIPTION, loc.getString("COMMIT_BUTTON_ACCESSIBLE_DESCRIPTION." + budleSuffixKey)); //NOI18N
            prov.messages.put(BundleMessage.COMMIT_BUTTON_ACCESSIBLE_NAME, loc.getString("COMMIT_BUTTON_ACCESSIBLE_NAME." + budleSuffixKey)); //NOI18N
            prov.messages.put(BundleMessage.COMMIT_BUTTON_LABEL, loc.getString("COMMIT_BUTTON_LABEL." + budleSuffixKey)); //NOI18N
            prov.messages.put(BundleMessage.MESSAGE_FINISHING_FROM_DIFF, loc.getString("MESSAGE_FINISHING_FROM_DIFF." + budleSuffixKey)); //NOI18N
            prov.messages.put(BundleMessage.MESSAGE_FINISHING_FROM_DIFF_TITLE, loc.getString("MESSAGE_FINISHING_FROM_DIFF_TITLE." + budleSuffixKey)); //NOI18N
            prov.messages.put(BundleMessage.PANEL_ACCESSIBLE_NAME, loc.getString("PANEL_ACCESSIBLE_NAME." + budleSuffixKey)); //NOI18N
            prov.messages.put(BundleMessage.PANEL_ACCESSIBLE_DESCRIPTION, loc.getString("PANEL_ACCESSIBLE_DESCRIPTION." + budleSuffixKey)); //NOI18N
            instance = prov;
            instances.put(budleSuffixKey, prov);
        }
        return instance;
    }
    
    @Override
    public String getMessage (BundleMessage message) {
        String retval = messages.get(message);
        if (retval == null) {
            retval = message.toString();
        }
        return retval;
    }

    @Override
    public VCSCommitOptions getExcludedOption () {
        return QFileNode.EXCLUDE;
    }
}
