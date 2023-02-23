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
package org.netbeans.modules.java.hints;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class WrongPackageSuggestion extends AbstractHint {
    
    /** Creates a new instance of WrongPackageSuggestion */
    public WrongPackageSuggestion() {
        super( true, false, AbstractHint.HintSeverity.ERROR, "", "WrongPackageStatement");
    }
    
    public Set<Kind> getTreeKinds() {
        return EnumSet.of(Kind.COMPILATION_UNIT, Kind.PACKAGE);
    }

    public List<ErrorDescription> run(CompilationInfo info, TreePath treePath) {
        Tree t = treePath.getLeaf();
        
        CompilationUnitTree tree = null;
        switch (t.getKind()) {
            case COMPILATION_UNIT:
                tree = (CompilationUnitTree)t;
                break;
            case PACKAGE:
                tree = treePath.getCompilationUnit();
                break;
            default:
                return null;
        }
        
        StringBuffer packageNameBuffer = new StringBuffer();
        boolean hasPackageClause = tree.getPackageName() != null;
        
        if (hasPackageClause) {
            new ErrorAwareTreeScanner<Void, StringBuffer>() {
                @Override
                public Void visitIdentifier(IdentifierTree node, StringBuffer p) {
                    p.append(node.getName().toString());
                    return null;
                }
                
                @Override
                public Void visitMemberSelect(MemberSelectTree node, StringBuffer p) {
                    super.visitMemberSelect(node, p);
                    p.append('.');
                    p.append(node.getIdentifier().toString());
                    return null;
                }
                
            }.scan(tree.getPackageName(), packageNameBuffer);
        }
        else if (tree.getTypeDecls().isEmpty()){
            return null;
        }
        
        String packageName = packageNameBuffer.toString();
        
        ClassPath cp = info.getClasspathInfo().getClassPath(PathKind.SOURCE);
        
        if (cp == null || !cp.isResourceVisible(info.getFileObject())) {
            Logger.getLogger(WrongPackageSuggestion.class.getName()).log(Level.INFO, "source cp is either null or does not contain the compiled source cp={0}", cp); // NOI18N
            return null;
        }
        
        String packageLocation = cp.getResourceName(info.getFileObject().getParent(), '.', false);
        
        if ((isCaseSensitive() && packageName.equals(packageLocation)) || (!isCaseSensitive() && packageName.equalsIgnoreCase(packageLocation))) {
            return null;
        }
        
        long startPos;
        long endPos;
        
        if (hasPackageClause) {
            startPos = info.getTrees().getSourcePositions().getStartPosition(tree, tree.getPackageName());
            endPos   = info.getTrees().getSourcePositions().getEndPosition(tree, tree.getPackageName());
        } else {
            startPos = 0;
            endPos   = 1;
        }
        
        if (startPos == (-1) || endPos == (-1))
            return null;
        
        List<Fix> fixes = Arrays.<Fix>asList(new MoveToCorrectPlace(info.getFileObject(), cp, packageName), new CorrectPackageDeclarationFix(info.getFileObject(), packageLocation));
        String description = NbBundle.getMessage(WrongPackageSuggestion.class, "HINT_WrongPackage");
        
        return Collections.singletonList(ErrorDescriptionFactory.createErrorDescription(getSeverity().toEditorSeverity(), description, fixes, info.getFileObject(), (int) startPos, (int) endPos));
    }

    public void cancel() {
        // XXX implement me
    }
    
    public String getId() {
        return WrongPackageSuggestion.class.getName();
    }

    public String getDisplayName() {
        return NbBundle.getMessage(WrongPackageSuggestion.class, "DN_WrongPackage");
    }

    public String getDescription() {
        return NbBundle.getMessage(WrongPackageSuggestion.class, "DESC_WrongPackage");
    }
    
    public Preferences getPreferences() {
        return null;
    }
    
    public JComponent getCustomizer(Preferences node) {
        return null;
    }    
    

    private static boolean isCaseSensitive () {
        return ! new File ("a").equals (new File ("A"));    //NOI18N
    }
    
    static final class MoveToCorrectPlace implements Fix {
        
        private FileObject file;
        private ClassPath  cp;
        private String packageName;
        
        public MoveToCorrectPlace(FileObject file, ClassPath  cp, String packageName) {
            this.file = file;
            this.cp   = cp;
            this.packageName = packageName;
        }
        
        public String getText() {
            return NbBundle.getMessage(WrongPackageSuggestion.class, "FIX_WrongPackageMove");
        }
        
        public ChangeInfo implement() {
            try {
                String path = packageName.replace('.', '/');
                FileObject root = cp.findOwnerRoot(file);
                
                FileObject packFile = root.getFileObject(path);
                
                if (packFile != null && !packFile.isFolder()) {
                    NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(WrongPackageSuggestion.class, "ERR_CannotMoveAlreadyExists"), NotifyDescriptor.Message.ERROR_MESSAGE);
                    
                    DialogDisplayer.getDefault().notifyLater(nd);
                    
                    return null;
                }
                
                packFile = FileUtil.createFolder(root, packageName.replace('.', '/'));
                
                DataObject fileDO = DataObject.find(file);
                DataFolder folder = DataFolder.findFolder(packFile);
                
                fileDO.move(folder);
            } catch (IllegalArgumentException e) {
                Exceptions.attachLocalizedMessage(e, NbBundle.getMessage(WrongPackageSuggestion.class, "ERR_CannotMove"));
                ErrorManager.getDefault().notify(ErrorManager.USER, e);
                Logger.getLogger(WrongPackageSuggestion.class.getName()).log(Level.INFO, null, e);
            } catch (IOException e ) {
                Exceptions.attachLocalizedMessage(e, NbBundle.getMessage(WrongPackageSuggestion.class, "ERR_CannotMove"));
                ErrorManager.getDefault().notify(ErrorManager.USER, e);
                Logger.getLogger(WrongPackageSuggestion.class.getName()).log(Level.INFO, null, e);
            }
            
            return null;
        }
    }
    
    static final class CorrectPackageDeclarationFix implements Fix {
        
        private FileObject file;
        private String packageName;
        
        public CorrectPackageDeclarationFix(FileObject file, String packageName) {
            this.file = file;
            this.packageName = packageName;
        }
        
        public String getText() {
            return NbBundle.getMessage(WrongPackageSuggestion.class, "FIX_WrongPackageFix", packageName.length() == 0 ? 0 : 1, packageName);
        }
        
        public ChangeInfo implement() throws IOException {
            JavaSource js = JavaSource.forFileObject(file);
            
            js.runModificationTask(new Task<WorkingCopy>() {
                public void run(WorkingCopy copy) throws Exception {
                    copy.toPhase(Phase.PARSED);

                    CompilationUnitTree cut = copy.getCompilationUnit();

                    if (packageName.length() == 0) {
                        copy.rewrite(cut, copy.getTreeMaker().CompilationUnit(cut.getPackageAnnotations(), null, cut.getImports(), cut.getTypeDecls(), cut.getSourceFile()));
                    } else {
                        if (cut.getPackageName() == null) {
                            copy.rewrite(cut, copy.getTreeMaker().CompilationUnit(cut.getPackageAnnotations(), createForFQN(copy, packageName), cut.getImports(), cut.getTypeDecls(), cut.getSourceFile()));
                        } else {
                            copy.rewrite(cut.getPackageName(), createForFQN(copy, packageName));
                        }
                    }
                }
            }).commit();
            
            return null;
        }
        
        private ExpressionTree createForFQN(WorkingCopy copy, String fqn) {
            int dot = fqn.indexOf('.');
            
            if (dot == (-1)) {
                return copy.getTreeMaker().Identifier(fqn);
            } else {
                return copy.getTreeMaker().MemberSelect(createForFQN(copy, fqn.substring(0, dot)), fqn.substring(dot + 1));
            }
        }
    }
}
