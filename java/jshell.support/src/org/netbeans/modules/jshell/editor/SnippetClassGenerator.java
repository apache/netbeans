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
package org.netbeans.modules.jshell.editor;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ImportTree;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.lib.nbjshell.JShellAccessor;
import jdk.jshell.Snippet;
import jdk.jshell.Snippet.SubKind;
import jdk.jshell.VarSnippet;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.Project;
import org.netbeans.api.templates.FileBuilder;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.netbeans.modules.java.hints.friendapi.SourceChangeUtils;
import org.netbeans.modules.jshell.support.ShellSession;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author sdedic
 */
public class SnippetClassGenerator implements Runnable {
    private final Project project;
    private final ShellSession shellSession;
    private final FileObject targetFolder;
    private final String    className;
    private FileObject javaFile;
    private StringBuilder executableContent = new StringBuilder();
    private StringBuilder declarativeConent = new StringBuilder();
    private List<Snippet> liveSnippets;
    
    /**
     * Snippets which have been processed already.
     */
    private Set<Snippet>  processed = new HashSet<>();
    private Set<Snippet>  declared = new HashSet<>();
    
    private Throwable error;

    public SnippetClassGenerator(Project project, ShellSession shellSession, FileObject targetFolder, String className) {
        this.project = project;
        this.shellSession = shellSession;
        this.targetFolder = targetFolder;
        this.className = className;
    }
    
    public Throwable getError() {
        return error;
    }
    
    @NbBundle.Messages({
        "EXC_UnexpectedTemplateContents=Unexpected plain class template contents",
        "EXC_ShellTemplateMissing=Unexpected plain class template contents"
    })
    private FileObject createJavaFile() throws IOException {
        FileObject template = FileUtil.getConfigFile("Templates/Classes/ShellClass.java"); // NOI18N
        if (template == null) {
            throw new IOException(Bundle.EXC_ShellTemplateMissing());
        }
        FileBuilder builder = new FileBuilder(template, targetFolder);
        builder.name(className);
        builder.param("executables", executableContent.toString());
        builder.param("declaratives", declarativeConent.toString());
        
        Collection<FileObject> l = builder.build();
        if (l.size() != 1) {
            throw new IOException(Bundle.EXC_UnexpectedTemplateContents());
        }
        return l.iterator().next();
    }

    /**
     * Copies all 'non-persistent' statements and expressions into
     * a method.
     */
    private void createStatementsText() {
        for (Snippet s : liveSnippets) {
            
            if (s.kind().isPersistent()) {
                // exclude temporaries, the unreferenced ones should be generated
                // as expression statements.
                if (s.subKind() != SubKind.TEMP_VAR_EXPRESSION_SUBKIND) {
                    continue;
                }
            }
            
            
            if (!processed.add(s)) {
                // already processed
                continue;
            }
            
            String text = s.source();
            
            if (s.subKind() == SubKind.TEMP_VAR_EXPRESSION_SUBKIND) {
                VarSnippet vs = (VarSnippet)s;
                if (!declared.contains(s)) {
                    // those tmp vars not used by persistent snippets are declared locally
                    executableContent.
                            append(vs.typeName()).append(" ");
                }
                executableContent.
                        append(vs.name()).
                        append(" = ");
            }
            executableContent.append(text);
            if (!text.endsWith(";") && !text.endsWith("}")) {   // NOI18N
                executableContent.append(";");  // NOI18N
            }
            executableContent.append("\n"); // NOI18N
        }
    }
    
    private boolean declarationsDependsOn(Snippet snip) {
        Deque<Snippet> candidates = new ArrayDeque<>();
        candidates.add(snip);
        while (!candidates.isEmpty()) {
            Snippet c = candidates.poll();
            Collection<Snippet> deps = JShellAccessor.getDependents(shellSession.ensureShell(), c);
            for (Snippet s : deps) {
                if (!s.kind().isPersistent()) {
                    continue;
                }
                if (s.kind() == Snippet.Kind.IMPORT) {
                    continue;
                }
                if (s.subKind() == SubKind.TEMP_VAR_EXPRESSION_SUBKIND) {
                    candidates.push(s);
                } else {
                    return true;
                }
            }
        }
        return false;
    }
    
    private void prepareDeclarations() {
        for (Snippet s : liveSnippets) {
            if (!s.kind().isPersistent()) {
                continue;
            }
            if (s.kind() == Snippet.Kind.IMPORT) {
                continue;
            }
            if (s.subKind() == SubKind.TEMP_VAR_EXPRESSION_SUBKIND &&
                !declarationsDependsOn(s)) {
                // include ONLY if the snippet is referenced from others
                continue;
            } 
            declared.add(s);
            String text;
            if (declarativeConent.length() > 0) {
                // force some newline
                declarativeConent.append("\n"); // NOI18N
            }
            if (s.subKind() == SubKind.TEMP_VAR_EXPRESSION_SUBKIND) {
                VarSnippet vs = (VarSnippet)s;
                text = vs.typeName() + " " + vs.name(); // NOI18N
                declarativeConent.append(text);
            } else {
                text = s.source();
                declarativeConent.append(text);
                processed.add(s);
            }
            if (!text.endsWith(";") && !text.endsWith("}")) {   // NOI18N
                declarativeConent.append(";");  // NOI18N
            }
            declarativeConent.append("\n"); // NOI18N
        }
    }
    
    private void prepareImports() {
    }
    
    private void copyImports() {
        List<ImportTree> imps = new ArrayList<>();
        for (Snippet s : shellSession.getSnippets(false, true)) {
            if (s.kind() != Snippet.Kind.IMPORT) {
                continue;
            }
            String importText = s.source();
            int ii = importText.indexOf("import");
            if (ii == -1) {
                continue;
            }
            String ident = importText.substring(ii + 6 /* length of import */).trim();
            if (ident.endsWith(";")) {
                ident = ident.substring(0, ident.length() - 1);
            }
            boolean stat = ident.startsWith("static");
            if (stat) {
                // do not import stuff from REPL classes:
                ident = ident.substring(6 /* length of static */).trim();

                if (ident.startsWith("REPL.$$")) {
                    continue;
                }
            }
            ExpressionTree qi = copy.getTreeMaker().QualIdent(ident);
            imps.add(copy.getTreeMaker().Import(qi, stat));
        }
        
        CompilationUnitTree t = copy.getCompilationUnit();
        for (ImportTree i : imps) {
            t = copy.getTreeMaker().addCompUnitImport(t, i);
        }
        copy.rewrite(copy.getCompilationUnit(), t);
    }
    
    private WorkingCopy copy;
    

    @Override
    public void run() {
        liveSnippets = shellSession.getSnippets(true, true);
        prepareDeclarations();
        createStatementsText();
        prepareImports();
        try {
            FileObject replaced = targetFolder.getFileObject(className, "java");
            if (replaced != null) {
                replaced.delete();
            }
            javaFile = createJavaFile();
            JavaSource src = JavaSource.forFileObject(javaFile);
            if (src == null) {
                return;
            }
            src.runModificationTask(wc ->  {
                wc.toPhase(JavaSource.Phase.RESOLVED);
               this.copy = wc;
               copyImports();
            }).commit();
            
            // reformat
            EditorCookie editor = javaFile.getLookup().lookup(EditorCookie.class);
            Document d = editor.openDocument();
            Reformat r = Reformat.get(d);
            r.lock();
            try {
                r.reformat(0, d.getLength());
            } finally {
                r.unlock();
            }
            // not organize those imports; must run a separate task, so the
            // analyzer sees the text:
            src.runModificationTask(wc ->  {
                wc.toPhase(JavaSource.Phase.RESOLVED);
               this.copy = wc;
                SourceChangeUtils.doOrganizeImports(copy, null, true);
            }).commit();
            editor.saveDocument();
        } catch (IOException ex) {
            error = ex;
        } catch (BadLocationException ex) {
            error = ex;
        }
    }
    
    public FileObject getJavaFile() {
        return javaFile;
    }
}
