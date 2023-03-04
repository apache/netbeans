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

package org.netbeans.modules.php.project.environment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
final class UnixPhpEnvironment extends PhpEnvironment {
    private static final String PHP = "php"; // NOI18N

    UnixPhpEnvironment() {
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("DMI_HARDCODED_ABSOLUTE_FILENAME")
    @Override
    protected List<DocumentRoot> getDocumentRoots(String projectName) {
        List<DocumentRoot> roots = new ArrayList<>(2);

        // ~/public_html
        DocumentRoot userPublicHtml = getUserPublicHtmlDocumentRoot(projectName);
        if (userPublicHtml != null) {
            roots.add(userPublicHtml);
        }

        // /var/www
        File www = new File("/var/www"); // NOI18N
        File wwwLocalhost = new File(www, "localhost"); // NOI18N
        String documentRoot = null;
        boolean canWrite = false;
        if (wwwLocalhost.isDirectory()) {
            documentRoot = getFolderName(wwwLocalhost, projectName);
            canWrite = wwwLocalhost.canWrite();
        } else if (www.isDirectory()) {
            documentRoot = getFolderName(www, projectName);
            canWrite = www.canWrite();
        }
        if (documentRoot != null) {
            String url = getDefaultUrl(projectName);
            String hint = NbBundle.getMessage(SolarisPhpEnvironment.class, "TXT_HtDocs");
            roots.add(new DocumentRoot(documentRoot, url, hint, roots.isEmpty() && canWrite));
        }
        if (!roots.isEmpty()) {
            return roots;
        }
        return Collections.<DocumentRoot>emptyList();
    }

    @Override
    public List<String> getAllPhpInterpreters() {
        return getAllPhpInterpreters(PHP);
    }
}
