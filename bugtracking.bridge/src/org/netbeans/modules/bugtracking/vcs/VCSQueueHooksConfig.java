/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.vcs;

import java.util.EnumMap;
import java.util.Map;
import java.util.prefs.Preferences;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.team.commons.LogUtils;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.util.NbPreferences;

/**
 *
 * @author Tomas Stupka
 */
public class VCSQueueHooksConfig {
    private final HookType type;

    static enum HookType {
        HG("hg");

        private String type;

        HookType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }  
    }
    
    private static Map<HookType, VCSQueueHooksConfig> instances = new EnumMap<HookType, VCSQueueHooksConfig>(HookType.class);

    private static final String HOOK_PREFIX               = "vcsqueuehook.";         // NOI18N
    private static final String HOOK_AFTER_REFRESH         = "_after_refresh";    // NOI18N
    private static final String HOOK_QFINISH_                = "_qfinish_hook_";      // NOI18N
    private static final String DELIMITER                 = "<=>###<=>";              // NOI18N
    
    private VCSQueueHooksConfig(HookType type) { 
        this.type = type;
    }

    static VCSQueueHooksConfig getInstance(HookType type) {
        VCSQueueHooksConfig instance = instances.get(type);
        if(instance == null) {
            instance = new VCSQueueHooksConfig(type);
            instances.put(type, instance);
        }
        return instance;
    }

    Preferences getPreferences() {
        return NbPreferences.forModule(VCSQueueHooksConfig.class);
    }

    boolean getAfterRefresh() {
        return getPreferences().getBoolean(HOOK_PREFIX + type + HOOK_AFTER_REFRESH, false);
    }

    void setAfterRefresh(boolean bl) {
        getPreferences().putBoolean(HOOK_PREFIX + type + HOOK_AFTER_REFRESH, bl);
    }
    
    void setFinishPatchAction (String patchId, FinishPatchOperation finishPatchAction) {
        getPreferences().put(HOOK_PREFIX + type + HOOK_QFINISH_ + patchId,  finishPatchAction.toString());
    }

    void clearFinishPatchAction (String patchId) {
        getPreferences().remove(HOOK_PREFIX + type + HOOK_QFINISH_ + patchId);
    }

    FinishPatchOperation popFinishPatchAction (String patchId, boolean clear) {
        String value = getPreferences().get(HOOK_PREFIX + type + HOOK_QFINISH_ + patchId, null);
        if (value == null) return null;
        String values[] = value.split(DELIMITER);
        if (clear) {
            getPreferences().remove(HOOK_PREFIX + type + HOOK_QFINISH_ + patchId);
        }
        return values.length < 5 ? null : new FinishPatchOperation(values[0], values[4].isEmpty() ? null : values[4], 
                "1".equals(values[1]) ? true : false, //NOI18N
                "1".equals(values[2]) ? true : false, //NOI18N
                "1".equals(values[3]) ? true : false); //NOI18N
    }

    static class FinishPatchOperation {
        private final String issueID;
        private final String msg;
        private final boolean close;
        private final boolean addInfo;
        private final boolean afterPush;
        public FinishPatchOperation (String issueID, String msg, boolean close, boolean addInfo, boolean afterPush) {
            this.issueID = issueID;
            this.msg = msg;
            this.close = close;
            this.addInfo = addInfo;
            this.afterPush = afterPush;
        }
        public String getIssueID() {
            return issueID;
        }
        public boolean isClose() {
            return close;
        }
        public boolean isAddInfo () {
            return addInfo;
        }
        public boolean isAfterPush () {
            return afterPush;
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
            sb.append(isClose() ? "1" : "0"); //NOI18N
            sb.append(DELIMITER);
            sb.append(isAddInfo() ? "1" : "0"); //NOI18N
            sb.append(DELIMITER);
            sb.append(isAfterPush() ? "1" : "0"); //NOI18N
            sb.append(DELIMITER);
            sb.append(message != null ? message.trim() : ""); //NOI18N
            return sb.toString();
        }
    }

}
