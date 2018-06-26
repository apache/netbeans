/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf.editor;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.netbeans.modules.javaee.project.api.ClientSideDevelopmentSupport.Pattern;
import org.netbeans.modules.javaee.project.spi.FrameworkServerURLMapping;
import org.netbeans.modules.web.jsf.editor.index.ResourcesMappingModel;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provides implementation of the {@code FrameworkServerURLMapping} class.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
@ServiceProvider(service = FrameworkServerURLMapping.class)
public final class JsfServerURLMapping implements FrameworkServerURLMapping {

    private static final String RESOURCE_DIR = "javax.faces.resource"; //NOI18N

    private static final String LIBRARY_PARAM = "ln";       //NOI18N
    private static final String CONTRACT_PARAM = "con";     //NOI18N
    private static final String RESOURCES = "resources";    //NOI18N
    private static final String CONTRACTS = "contracts";    //NOI18N

    @Override
    public FileObject convertURLtoFile(FileObject docRoot, Pattern mapping, String uri, String urlQuery) {
        switch (mapping.getType()) {
            case PREFIX:
                uri = uri.substring(mapping.getPattern().length());
                break;
            case SUFFIX:
                uri = uri.substring(0, uri.length() - mapping.getPattern().length());
        }

        if (uri.startsWith(RESOURCE_DIR + "/")) { //NOI18N
            String relPath = uri.substring(21);
            try {
                relPath = removeSessionIdFromUri(relPath);
                Map<String, String> pairs = splitQuery(urlQuery);
                String libraryValue = pairs.get(LIBRARY_PARAM);
                String contractValue = pairs.get(CONTRACT_PARAM);
                if (libraryValue != null) {
                    return docRoot.getFileObject(RESOURCES + "/" + libraryValue + "/" + relPath);
                } else if (contractValue != null) {
                    return docRoot.getFileObject(CONTRACTS + "/" + contractValue + "/" + relPath);
                } else {
                    return docRoot.getFileObject(RESOURCES + "/" + relPath);
                }
            } catch (UnsupportedEncodingException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }

    @Override
    public String convertFileToRelativeURL(FileObject file, String relPath) {
        JsfSupportImpl jsfSupport = JsfSupportImpl.findFor(file);
        if (jsfSupport == null) {
            return relPath;
        }
        String path = file.getPath();
        for (ResourcesMappingModel.Resource resource : jsfSupport.getIndex().getAllStaticResources()) {
            // another file
            if (!path.contains(resource.getLibrary()) || !path.endsWith(resource.getName())) {
                continue;
            }

            return getRelativePathForResource(file, resource, relPath);
        }
        return relPath;
    }

    private static String getRelativePathForResource(FileObject file, ResourcesMappingModel.Resource resource, String relPath) {
        // TODO handle contracts
        if (resource.getLibrary().isEmpty()) {
            return RESOURCE_DIR + "/" + resource.getName();
        } else {
            return RESOURCE_DIR + "/" + resource.getLibrary() + "/" + resource.getName();
        }
    }

    private static Map<String, String> splitQuery(String query) throws UnsupportedEncodingException {
        if (query == null) {
            return Collections.emptyMap();
        }

        Map<String, String> result = new LinkedHashMap<>();
        String[] pairs = query.split("&"); //NOI18N
        for (String pair : pairs) {
            int idx = pair.indexOf("="); //NOI18N
            if (idx != -1) {
                result.put(
                        URLDecoder.decode(pair.substring(0, idx), "UTF-8"),     //NOI18N
                        URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));   //NOI18N
            }
        }
        return result;
    }

    private String removeSessionIdFromUri(String relPath) {
        if (relPath.indexOf(";") != -1) { //NOI18N
            return relPath.substring(0, relPath.indexOf(";")); //NOI18N
        }
        return relPath;
    }

}
