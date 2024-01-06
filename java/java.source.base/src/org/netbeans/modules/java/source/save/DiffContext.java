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

package org.netbeans.modules.java.source.save;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.Trees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Position;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.PositionConverter;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.openide.filesystems.FileObject;

/**
 *
 * @author lahvac
 */
public class DiffContext {
    public final TokenSequence<JavaTokenId> tokenSequence;
    public final String origText;
    public final CodeStyle style;
    public final Context context;
    public final JCCompilationUnit origUnit;
    public final Trees trees;
    public final Document doc;
    public final PositionConverter positionConverter;
    public final FileObject file;
    public final Set<Tree> syntheticTrees;
    
    public final JCCompilationUnit mainUnit;
    public final String mainCode;
    
    public final int textLength;
    
    public final BlockSequences blockSequences;
    public final Map<JCTree, Integer> syntheticEndPositions = new HashMap<>();
    
    /**
     * Special flag; when creating new CUs from template, always include their initial comments
     */
    public final boolean forceInitialComment;
    
    public Map<Integer, Comment> usedComments = new HashMap<>();

    public DiffContext(CompilationInfo copy) {
        this(copy, new HashSet<Tree>());
    }

    public DiffContext(CompilationInfo copy, Set<Tree> syntheticTrees) {
        this.tokenSequence = copy.getTokenHierarchy().tokenSequence(JavaTokenId.language());
        this.mainCode = this.origText = copy.getText();
        this.style = getCodeStyle(copy);
        this.context = JavaSourceAccessor.getINSTANCE().getJavacTask(copy).getContext();
        this.mainUnit = this.origUnit = (JCCompilationUnit) copy.getCompilationUnit();
        this.trees = copy.getTrees();
        this.doc = copy.getSnapshot().getSource().getDocument(false); //TODO: true or false?
        this.positionConverter = copy.getPositionConverter();
        this.file = copy.getFileObject();
        this.syntheticTrees = syntheticTrees;
        
        this.textLength = copy.getSnapshot() == null ? Integer.MAX_VALUE : copy.getSnapshot().getOriginalOffset(copy.getSnapshot().getText().length());
        this.blockSequences = new BlockSequences(this.tokenSequence, doc, textLength);
        
        this.forceInitialComment = false;
    }

    /**
     * Note: the constructor is only used when creating new compilation units; a special flag will be set up, so an initial comment is not skipped.
     */
    public DiffContext(CompilationInfo copy, CompilationUnitTree cut, String code, PositionConverter positionConverter, FileObject file, Set<Tree> syntheticTrees, CompilationUnitTree mainUnit, String mainCode) {
        this.tokenSequence = TokenHierarchy.create(code, JavaTokenId.language()).tokenSequence(JavaTokenId.language());
        this.origText = code;
        this.style = getCodeStyle(copy);
        this.context = JavaSourceAccessor.getINSTANCE().getJavacTask(copy).getContext();
        this.origUnit = (JCCompilationUnit) cut;
        this.trees = copy.getTrees();
        this.doc = null;
        this.positionConverter = positionConverter;
        this.file = file;
        this.syntheticTrees = syntheticTrees;
        this.mainUnit = (JCCompilationUnit) mainUnit;
        this.mainCode = mainCode;
        
        this.textLength = copy.getSnapshot() == null ? Integer.MAX_VALUE : copy.getSnapshot().getOriginalOffset(copy.getSnapshot().getText().length());
        this.blockSequences = new BlockSequences(this.tokenSequence, doc, textLength);
        this.forceInitialComment = true;
    }

    public static final CodeStyle getCodeStyle(CompilationInfo info) {
        if (info != null) {
            try {
                Document doc = info.getDocument();
                if (doc != null) {
                    CodeStyle cs = (CodeStyle)doc.getProperty(CodeStyle.class);
                    return cs != null ? cs : CodeStyle.getDefault(doc);
                }
            } catch (IOException ioe) {
                // ignore
            }

            FileObject file = info.getFileObject();
            if (file != null) {
                return CodeStyle.getDefault(file);
            }
        }

        return CodeStyle.getDefault((Document)null);
    }

    public int getEndPosition(JCCompilationUnit unit, JCTree t) {
        int endPos = TreeInfo.getEndPos(t, unit.endPositions);

        if (endPos == Position.NOPOS && unit == origUnit) {
            endPos = syntheticEndPositions.getOrDefault(t, Position.NOPOS);
        }

        return endPos;
    }
}
