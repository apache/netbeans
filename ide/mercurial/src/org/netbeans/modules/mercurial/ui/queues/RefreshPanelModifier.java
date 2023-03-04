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
