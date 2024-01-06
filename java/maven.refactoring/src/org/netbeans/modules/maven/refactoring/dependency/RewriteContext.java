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
package org.netbeans.modules.maven.refactoring.dependency;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.lsp.TextEdit;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMModelFactory;
import org.netbeans.modules.project.dependency.Dependency;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author sdedic
 */
public class RewriteContext {
    private final Project project;
    private final FileObject pomFile;

    private POMModel mutableModel;
    private Document documentCopy;
    private DocumentChangesConverter converter;

    public RewriteContext(Project project) {
        this.project = project;
        this.pomFile = project.getLookup().lookup(FileObject.class);
    }

    public FileObject getPomFile() {
        return pomFile;
    }
    
    public POMModel getWriteModel() throws IOException {
        if (mutableModel == null) {
            createModel();
        }
        return mutableModel;
    }
    
    public List<TextEdit> createEdits() {
        return converter.makeTextEdits();
    }
    
    public String mavenScope(Dependency d) {
        if (d.getScope() == null) {
            return "compile";
        }
        String ms = MavenDependencyModifierImpl.scope2Maven.get(d.getScope().name());
        if (ms == null) {
            return "compile";
        } else {
            return ms;
        }
    }

    private void createModel() throws IOException {
        NbMavenProject mp = project.getLookup().lookup(NbMavenProject.class);
        FileObject pomFile = project.getLookup().lookup(FileObject.class);
        EditorCookie cake = pomFile.getLookup().lookup(EditorCookie.class);
        Document d = cake.openDocument();
        
        // create a copy of the document
        Document copy = LineDocumentUtils.createDocument("text/pom+xml");
        try {
            copy.insertString(0, d.getText(0, d.getLength()), null);
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
        copy.putProperty(Document.StreamDescriptionProperty, d.getProperty(Document.StreamDescriptionProperty));
        documentCopy = copy;
        converter = new DocumentChangesConverter(documentCopy);
        copy.addDocumentListener(converter);
        
        List<Object> lkpContents = new ArrayList<>();
        lkpContents.add(pomFile); 
        lkpContents.add(copy);
        File f = FileUtil.toFile(pomFile);
        if (f != null) {
            lkpContents.add(f);
        }
        
        ModelSource ms = new ModelSource(Lookups.fixed(lkpContents.toArray()), true);
        mutableModel = POMModelFactory.getDefault().getModel(ms);
    }
}
