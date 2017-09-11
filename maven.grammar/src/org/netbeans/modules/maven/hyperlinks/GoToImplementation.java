/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
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
                        int line = Utilities.getLineOffset((BaseDocument) doc, currentPosition);
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
