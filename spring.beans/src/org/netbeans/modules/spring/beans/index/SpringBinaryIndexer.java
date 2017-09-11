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

package org.netbeans.modules.spring.beans.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.parsing.spi.indexing.ConstrainedBinaryIndexer;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.netbeans.modules.spring.api.SpringUtilities;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
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
@ConstrainedBinaryIndexer.Registration(
    mimeType=SpringBinaryIndexer.XSD_MIME,
    requiredResource=SpringBinaryIndexer.REQUIRED_RESOURCE,
    indexerName=SpringBinaryIndexer.INDEXER_NAME,
    indexVersion=SpringBinaryIndexer.INDEX_VERSION
)
public class SpringBinaryIndexer extends ConstrainedBinaryIndexer {

   public static final Logger LOGGER = Logger.getLogger(SpringBinaryIndexer.class.getSimpleName());

    static final String INDEXER_NAME = "SpringBinary"; //NOI18N
    static final int INDEX_VERSION = 1;
    private static final String XSD_SUFFIX = ".xsd";    //NOI18N
    static final String XSD_MIME = "text/xsd+xml";  //NOI18N
    static final String REQUIRED_RESOURCE = "org/springframework";  //NOI18N
    static final String LIBRARY_MARK_KEY = "xsdSpringSchema";   //NOI18N
    static final String NAMESPACE_MARK_KEY = "namespace";   //NOI18N


    private String version;

    @Override
    protected void index(
        Map<String,? extends Iterable<? extends FileObject>> files,
        Context context) {
        LOGGER.log(Level.FINE, "indexing " + context.getRoot()); //NOI18N

        FileObject root = context.getRoot();
        if (root == null) {
            return;
        }
        assert root.getFileObject("org/springframework") != null;
        version = findVersion(root);
        if (version !=null) {
            processXsds(files, context);
        }
    }

    private void processXsds(
        Map<String,? extends Iterable<? extends FileObject>> files,
        Context context) {
        for (FileObject fileObject : findSpringLibraryDescriptors(files.get(XSD_MIME), XSD_SUFFIX)) {
            try {
                ModelSource modelSource = Utilities.getModelSource(fileObject, true);
                if (modelSource.getLookup().lookup(Document.class) != null) {
                    SchemaModel model = SchemaModelFactory.getDefault().getModel(modelSource);

                    Schema schema = model.getSchema();
                    String targetNamespace = schema.getTargetNamespace();
                    if (targetNamespace !=null) {
                        IndexingSupport sup = IndexingSupport.getInstance(context);
                        IndexDocument doc = sup.createDocument(fileObject);
                        doc.addPair(NAMESPACE_MARK_KEY, targetNamespace, true, true);
                        doc.addPair(LIBRARY_MARK_KEY, Boolean.TRUE.toString(), true, true);
                        sup.addDocument(doc);

                        LOGGER.log(Level.INFO, "The file " + fileObject + " indexed as a XSD (namespace=" + targetNamespace + ")"); //NOI18N

                    }
                }
            }catch(Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private Collection<FileObject> findSpringLibraryDescriptors(Iterable<? extends FileObject> res, String suffix) {
        assert res != null;
        Collection<FileObject> files = new ArrayList<FileObject>();
        for (FileObject file : res) {
            //XXX Version??? spring 2-5 has some xsd's with non 2.5 version
            String fileName = file.getNameExt().toLowerCase();
            if (fileName !=null && fileName.endsWith(suffix) && version.startsWith(findXsdVersion(file))) { //NOI18N
                //found library, create a new instance and cache it
                files.add(file);
            }
        }
        return files;
    }

    private String findXsdVersion(FileObject file) {
        String v = file.getName();
        v = v.substring(v.lastIndexOf("-")+1);
        return v;
    }

    private String findVersion(FileObject classpathRoot) {
        ClassPath cp = ClassPath.getClassPath(classpathRoot, ClassPath.COMPILE);
        if (cp == null) {
            return null;
        }
        String classRelativePath = SpringUtilities.SPRING_CLASS_NAME.replace('.', '/') + ".class"; //NOI18N
        try {
            FileObject resource = cp.findResource(classRelativePath);  //NOI18N
            if (resource==null) {
                return null;
            }
            FileObject ownerRoot = cp.findOwnerRoot(resource);

            if (ownerRoot !=null) { //NOI18N
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
}
