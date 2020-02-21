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

package org.netbeans.modules.cnd.classview.model;

import java.util.Collection;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.classview.NameCache;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.openide.nodes.*;

/**
 * Misc static utilitiy functions
 */
public class CVUtil {
    private static final boolean showParamNames = CndUtils.getBoolean("cnd.classview.show-param-names", true); // NOI18N
    
    public static CharSequence getSignature(CsmFunction fun) {
	return NameCache.getManager().getString(CsmUtilities.getSignature(fun, showParamNames));
    }
        
    public static CharSequence getNamespaceDisplayName(CsmNamespace ns){
        CharSequence displayName = ns.getName();
        if (displayName.length() == 0) {
            Collection<CsmNamespaceDefinition> definitions = ns.getDefinitions();
            CharSequence fileName = null;
            if (definitions.size() == 1) {
                CsmNamespaceDefinition def = definitions.iterator().next();
                CsmScope file = def.getScope();
                if (file instanceof CsmFile) {
                    fileName = ((CsmFile)file).getName();
                }
            }
            displayName = ns.getQualifiedName();
            int scope = CharSequenceUtils.lastIndexOf(displayName, "::"); // NOI18N
            if (scope != -1) {
                displayName = displayName.subSequence(scope + 2, displayName.length());
            }
            int start = CharSequenceUtils.indexOf(displayName, '<');
            int end = CharSequenceUtils.indexOf(displayName, '>');
            if (start >=0 && end >= 0 && start < end && fileName != null) {
                CharSequence name = displayName.subSequence(start+1, end);
                displayName = name+" ("+fileName+")"; // NOI18N
            } else {
                if (CharSequenceUtils.indexOf(displayName, '<') >=0 || CharSequenceUtils.indexOf(displayName, '>') >=0) { // NOI18N
                    displayName = displayName.toString().replace('<', ' ').replace('>', ' '); // NOI18N
                }
            }
        }
        return  NameCache.getManager().getString(displayName);
    }

    public static Node createLoadingNode() {
        BaseNode node = new LoadingNode();
        return node;
    }    
}
