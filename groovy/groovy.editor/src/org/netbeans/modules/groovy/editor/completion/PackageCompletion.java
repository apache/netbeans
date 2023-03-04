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

package org.netbeans.modules.groovy.editor.completion;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.groovy.editor.api.completion.CompletionItem;
import org.netbeans.modules.groovy.editor.api.completion.util.CompletionContext;

/**
 * Here we complete package-names like java.lan to java.lang ...
 * 
 * @author Martin Janicek
 */
public class PackageCompletion extends BaseCompletion {

    @Override
    public boolean complete(Map<Object, CompletionProposal> proposals, CompletionContext request, int anchor) {
        LOG.log(Level.FINEST, "-> completePackages"); // NOI18N

        // this can happen for ?. or similar constructs
        PackageCompletionRequest packageRequest = getPackageRequest(request);
        if (request.isBehindDot() && packageRequest.basePackage.length() <= 0) {
            return false;
        }

        LOG.log(Level.FINEST, "Token fullString = >{0}<", packageRequest.fullString);

        ClasspathInfo pathInfo = getClasspathInfoFromRequest(request);
        assert pathInfo != null : "Can not get ClasspathInfo";

        if (request.context.before1 != null
                && CharSequenceUtilities.textEquals(request.context.before1.text(), "*")
                && request.isBehindImportStatement()) {
            return false;
        }

        // try to find suitable packages ...

        Set<String> pkgSet = pathInfo.getClassIndex().getPackageNames(packageRequest.fullString, true, EnumSet.allOf(ClassIndex.SearchScope.class));

        for (String singlePackage : pkgSet) {
            LOG.log(Level.FINEST, "PKG set item: {0}", singlePackage);

            if (packageRequest.prefix.equals("")) {
                singlePackage = singlePackage.substring(packageRequest.fullString.length());
            } else if (!packageRequest.basePackage.equals("")) {
                singlePackage = singlePackage.substring(packageRequest.basePackage.length() + 1);
            }

            if (singlePackage.startsWith(packageRequest.prefix) && singlePackage.length() > 0) {
                CompletionItem.PackageItem item = new CompletionItem.PackageItem(singlePackage, anchor, request.getParserResult());

                if (request.isBehindImportStatement()) {
                    item.setSmart(true);
                }
                proposals.put("pack:" + singlePackage, item);
            }
        }

        return false;
    }
}
