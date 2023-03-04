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

package org.netbeans.modules.bugtracking.vcs;

import java.util.EnumMap;
import java.util.Map;
import java.util.prefs.Preferences;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.team.commons.LogUtils;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Tomas Stupka
 */
public class VCSHooksConfig {
    private final HookType type;

    static enum HookType {
        SVN("svn"),
        HG("hg"),
        GIT("git");

        private String type;

        HookType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }  
    }
    
    private static final Map<HookType, VCSHooksConfig> instances = new EnumMap<HookType, VCSHooksConfig>(HookType.class);

    private static final String HOOK_PREFIX               = "vcshook.";         // NOI18N
    
    private static final String HOOK_RESOLVE              = "_resolve";         // NOI18N
    private static final String HOOK_LINK                 = "_link";            // NOI18N
    private static final String HOOK_AFTER_COMMIT         = "_after_commit";    // NOI18N

    private static final String HOOK_ISSUE_INFO_TEMPLATE  = "_issue_format";    // NOI18N
    private static final String HOOK_REVISION_TEMPLATE    = "_comment_format";  // NOI18N
    

    private static final String HOOK_PUSH_                = "_push_hook_";      // NOI18N
    private static final String DELIMITER                 = "<=>";              // NOI18N
    
    private VCSHooksConfig(HookType type) { 
        this.type = type;
    }

    static VCSHooksConfig getInstance(HookType type) {
        VCSHooksConfig instance = instances.get(type);
        if(instance == null) {
            instance = new VCSHooksConfig(type);
            instances.put(type, instance);
        }
        return instance;
    }

    Preferences getPreferences() {
        return NbPreferences.forModule(VCSHooksConfig.class);
    }

    void setIssueInfoTemplate(Format format) {
        getPreferences().put(HOOK_PREFIX + type + HOOK_ISSUE_INFO_TEMPLATE, format.toString());
    }

    void setRevisionTemplate(Format format) {
        getPreferences().put(HOOK_PREFIX + type + HOOK_REVISION_TEMPLATE, format.toString());
    }

    void setResolve(boolean bl) {
        getPreferences().putBoolean(HOOK_PREFIX + type + HOOK_RESOLVE, bl);
    }

    void setLink(boolean bl) {
        getPreferences().putBoolean(HOOK_PREFIX + type + HOOK_LINK, bl);
    }

    boolean getResolve() {
        return getPreferences().getBoolean(HOOK_PREFIX + type + HOOK_RESOLVE, false);
    }

    boolean getLink() {
        return getPreferences().getBoolean(HOOK_PREFIX + type + HOOK_LINK, true);
    }

    boolean getAfterCommit() {
        return getPreferences().getBoolean(HOOK_PREFIX + type + HOOK_AFTER_COMMIT, false);
    }

    Format getRevisionTemplate() {
        return getFormat(getPreferences().get(HOOK_PREFIX + type + HOOK_REVISION_TEMPLATE, null), type == HookType.SVN ? getDefaultSvnRevisionTemplate() : getDefaultHgRevisionTemplate());
    }

    Format getIssueInfoTemplate() {
        return getFormat(getPreferences().get(HOOK_PREFIX + type + HOOK_ISSUE_INFO_TEMPLATE, null), getDefaultIssueInfoTemplate());
    }

    void setPushAction(String changeset, PushOperation pushAction) {
        getPreferences().put(HOOK_PREFIX + type + HOOK_PUSH_ + changeset,  pushAction.toString());
    }

    void setAfterCommit(boolean bl) {
        getPreferences().putBoolean(HOOK_PREFIX + type + HOOK_AFTER_COMMIT, bl);
    }
    
    PushOperation popPushAction(String changeset) {
        String value = getPreferences().get(HOOK_PREFIX + type + HOOK_PUSH_ + changeset, null);
        if(value == null) return null;
        String values[] = value.split(DELIMITER);
        getPreferences().remove(HOOK_PREFIX + type + HOOK_PUSH_ + changeset);
        return new PushOperation(values[0], !values[1].equals("") ? values[1] : null, values[2].equals("1") ? true : false); // NOI18N
    }

    Format getDefaultRevisionTemplate() {
        if(type == HookType.SVN) {
            return getDefaultSvnRevisionTemplate();
        }
        return getDefaultHgRevisionTemplate();
    }
    
    private static Format getDefaultHgRevisionTemplate() {
        return new Format(false, normalizeFormat(new String[] {
            NbBundle.getMessage(VCSHooksConfig.class, "LBL_Changeset"),         // NOI18N
            "{changeset}\n",                                                    // NOI18N
            NbBundle.getMessage(VCSHooksConfig.class, "LBL_Author"),            // NOI18N
            "{author}\n",                                                       // NOI18N
            NbBundle.getMessage(VCSHooksConfig.class, "LBL_Date"),              // NOI18N
            "{date}\n",                                                         // NOI18N
            NbBundle.getMessage(VCSHooksConfig.class, "LBL_Message"),           // NOI18N
            "{message}"                                                         // NOI18N
        }));
    }

    Format getDefaultIssueInfoTemplate() {
        return new Format(false, NbBundle.getMessage(VCSHooksConfig.class, "LBL_Issue") + "{id} - {summary}");  // NOI18N
    }

    private static Format getDefaultSvnRevisionTemplate() {
        return new Format(false, normalizeFormat(new String[] {
            NbBundle.getMessage(VCSHooksConfig.class, "LBL_Revision"),          // NOI18N
            "{revision}\n",                                                     // NOI18N
            NbBundle.getMessage(VCSHooksConfig.class, "LBL_Author"),            // NOI18N
            "{author}\n",                                                       // NOI18N
            NbBundle.getMessage(VCSHooksConfig.class, "LBL_Date"),              // NOI18N
            "{date}\n",                                                         // NOI18N
            NbBundle.getMessage(VCSHooksConfig.class, "LBL_Message"),           // NOI18N
            "{message}"                                                         // NOI18N
        }));
    }

    private Format getFormat(String value, Format defaultFormat) {
        Format format;
        if (value == null) {
            format = defaultFormat;
        } else {
            String[] values = value.split(DELIMITER);
            format = new Format(values[0].equals("1"), values.length > 1 ? values[1] : ""); //NOI18N
        }
        return format;
    }

    private static String normalizeFormat(String [] params) {
        int l = 0;
        for (int i = 0; i < params.length; i = i + 2) {
            if(l < params[i].length()) l = params[i].length();
        }
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < params.length; i++) {
            ret.append(params[i]);
            StringBuilder s = new StringBuilder();
            for (int j = 0; j < l - params[i].length() + 1; j++) s.append(" "); // NOI18N
            ret.append(s.toString());
            ret.append(params[++i]);
        }
        return ret.toString();
    }

    static class PushOperation {
        private final String issueID;
        private final String msg;
        private final boolean close;
        public PushOperation(String issueID, String msg, boolean close) {
            this.issueID = issueID;
            this.msg = msg;
            this.close = close;
        }
        public String getIssueID() {
            return issueID;
        }
        public boolean isClose() {
            return close;
        }
        public String getMsg() {
            return msg;
        }
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            String message = getMsg();
            sb.append(getIssueID());
            sb.append(DELIMITER);
            sb.append(message != null ? message.trim() : "");
            sb.append(DELIMITER);
            sb.append(isClose() ? "1" : "0");                                   // NOI18N
            return sb.toString();
        }
    }

    static class Format {
        private boolean above;
        private String format;
        public Format(boolean above, String format) {
            this.above = above;
            this.format = format;
        }
        public boolean isAbove() {
            return above;
        }
        public String getFormat() {
            return format;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(above ? "1" : "0");                                       // NOI18N
            sb.append(DELIMITER);
            sb.append(format);
            return sb.toString();
        }

    }

    static void logHookUsage(String vcs, Repository bugRepository) {
        Utils.logVCSActionEvent("COMMIT_HOOK_"+vcs); // NOI18N
    }
}
