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
package org.netbeans.modules.maven.hyperlinks;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.editor.AnnotationDesc;
import org.netbeans.editor.Annotations;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.netbeans.modules.maven.embedder.MavenEmbedder.ModelDescription;
import org.netbeans.modules.maven.spi.nodes.NodeUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

@EditorActionRegistration(
        name = "goto-declaration",
        mimeType = "text/x-maven-pom+xml"      
)
public final class GoToImplementation extends BaseAction {

    public GoToImplementation() {
        super();
        putValue(NAME, "goto-declaration"); //NOI18N
        putValue("supported-annotation-types", new String[] {
            "org-netbeans-modules-editor-annotations-implements"
        });
        setEnabled(true);
    }

    @Override
    public void actionPerformed(ActionEvent e, final JTextComponent c) {
        final Document doc = c.getDocument();

         if (doc instanceof BaseDocument) {
            final int currentPosition = c.getCaretPosition();
            final Annotations annotations = ((BaseDocument) doc).getAnnotations();
            final Boolean[] parent = new Boolean[] {Boolean.FALSE};
             doc.render(new Runnable() {
                 @Override
                public void run() {
                    try {
                        int line = LineDocumentUtils.getLineIndex((BaseDocument) doc, currentPosition);
                        AnnotationDesc desc = annotations.getActiveAnnotation(line);
                        if (desc == null) {
                            return;
                        }
                        if ("org-netbeans-modules-editor-annotations-implements".equals(desc.getAnnotationType())) {
                            parent[0] = Boolean.TRUE;
                        }
                    } catch (BadLocationException ex) {
                         Exceptions.printStackTrace(ex);
                     } 
                }
             });
             if (parent[0] == Boolean.TRUE) {
                FileObject fo = NbEditorUtilities.getFileObject(c.getDocument());
                if (fo.isValid() && "pom.xml".equals(fo.getNameExt())) {
                    try {
                        Project prj = ProjectManager.getDefault().findProject(fo.getParent());
                        NbMavenProject nbprj = prj != null ? prj.getLookup().lookup(NbMavenProject.class) : null;
                        if(nbprj != null) { // pom in a non maven project? see also issue #256717  
                            List<ModelDescription> desc = MavenEmbedder.getModelDescriptors(nbprj.getMavenProject());
                            if (desc != null) {
                                ModelDescription d = desc.get(1);
                                File f = d.getLocation();
                                if (f != null) {
                                    //in sources
                                    FileObject fobj = FileUtil.toFileObject(f);
                                    if (fobj != null) {
                                        //for files in local repo, fake read-only state..
                                        NodeUtils.openPomFile(fobj);
                                    }
                                } 
                            } else {
                                //no preexisting inheritance information

                            }
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (IllegalArgumentException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
         
        

    }

    
    
   
}
