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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.spring.beans.completion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.spring.api.SpringUtilities;
import org.netbeans.modules.spring.api.beans.SpringConstants;
import org.netbeans.modules.spring.beans.index.SpringIndex;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.schema.completion.spi.CompletionContext;
import org.netbeans.modules.xml.schema.completion.spi.CompletionModelProvider;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.JarFileSystem;
import org.openide.util.Exceptions;

/**
 *
 * @author alexeybutenko
 */
@org.openide.util.lookup.ServiceProvider(service = CompletionModelProvider.class)
public class OpenTagCompletionProvider extends CompletionModelProvider {

    private static final Logger LOGGER = Logger.getLogger(OpenTagCompletionProvider.class.getSimpleName());
    private Map<String, String> declaredNamespaces;

    /**
     * Create CompletionModel from SpringBinaryIndexer.
     * @param context CompletionContext
     * @return list of created CompletionModel items
     */
    @Override
    public List<CompletionModel> getModels(CompletionContext context) {
        if (!context.getCompletionType().equals(CompletionContext.CompletionType.COMPLETION_TYPE_ELEMENT)) {
            return Collections.<CompletionModel>emptyList();
        }

        BaseDocument doc = context.getBaseDocument();
        String mime = (String) doc.getProperty(BaseDocument.MIME_TYPE_PROP);
        if (!SpringConstants.CONFIG_MIME_TYPE.equals(mime)) {
            return Collections.<CompletionModel>emptyList();
        }

        List<CompletionModel> models =  new ArrayList<CompletionModel>();
        FileObject primaryFile = context.getPrimaryFile();
        ClassPath cp = ClassPath.getClassPath(primaryFile, ClassPath.COMPILE);
        if (cp == null) {
            return Collections.<CompletionModel>emptyList();
        }
        FileObject resource = cp.findResource(SpringUtilities.SPRING_CLASS_NAME.replace('.', '/') + ".class"); //NOI18N
        if (resource != null) {
            FileObject ownerRoot = cp.findOwnerRoot(resource);
            String version = findVersion(ownerRoot);
            if (version == null) {
                LOGGER.log(Level.WARNING, "Unknown version of Spring jars: ownerRoot={0}", ownerRoot);
                return Collections.<CompletionModel>emptyList();
            }
            LOGGER.log(Level.FINE, "Spring jars version={0}", version); //NOI18N

            if (declaredNamespaces == null) {
                declaredNamespaces = context.getDeclaredNamespaces();
            }
            Map<String, FileObject> map = new SpringIndex(primaryFile).getAllSpringLibraryDescriptors();

            for (String namespace: map.keySet()) {
                if (!declaredNamespaces.containsValue(namespace)) {
                    FileObject file = map.get(namespace);
                    String fVersion = parseVersion(file);
                    if (version.startsWith(fVersion)) {
                        ModelSource source = Utilities.getModelSource(file, true);
                        SchemaModel model = SchemaModelFactory.getDefault().getModel(source);
                        CompletionModel completionModel = new OpenTagCompletionModel(generatePrefix(namespace), model);
                        models.add(completionModel);
                    }
                }
            }
            usedPrefixes.clear();
            return models;
        } else {
            return Collections.<CompletionModel>emptyList();
        }

    }

    private String parseVersion(FileObject file) {
        String version = file.getName();
        version = version.substring(version.lastIndexOf("-") + 1); //NOI18N
        return version;
    }

    private String findVersion(FileObject ownerRoot) {
        try {
            if (ownerRoot != null) { //NOI18N
                if (ownerRoot.getFileSystem() instanceof JarFileSystem) {
                    JarFileSystem jarFileSystem = (JarFileSystem) ownerRoot.getFileSystem();
                    return SpringUtilities.getImplementationVersion(jarFileSystem);
                }
            }
        } catch (FileStateInvalidException e) {
            Exceptions.printStackTrace(e);
        }
        return null;
    }

    private Set<String> usedPrefixes = new HashSet<String>();

    private String generatePrefix(String namespace) {
        String prefix = namespace.substring(namespace.lastIndexOf("/") + 1).toLowerCase();
        int i = 1;
        String newPrefix = prefix;
        while (usedPrefixes.contains(newPrefix)) {
            newPrefix = prefix + (i++);
        }
        usedPrefixes.add(newPrefix);
        return newPrefix;
    }

    public final class OpenTagCompletionModel extends CompletionModel {
        private final String prefix;
        private final SchemaModel model;

        public OpenTagCompletionModel(String prefix, SchemaModel model) {
            this.prefix = prefix;
            this.model = model;
            LOGGER.log(Level.FINE, "Created model: {0}:{1}", new Object[]{prefix, getTargetNamespace()});  //NOI18N
        }

        @Override
        public String getSuggestedPrefix() {
            return prefix;
        }

        @Override
        public String getTargetNamespace() {
            Schema schema = model.getSchema();
            if (schema != null) {
                return schema.getTargetNamespace();
            }
            return null;
        }

        @Override
        public SchemaModel getSchemaModel() {
            return model;
        }
    }
}
