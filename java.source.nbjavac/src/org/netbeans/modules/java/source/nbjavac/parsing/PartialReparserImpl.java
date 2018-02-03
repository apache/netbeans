/**
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
package org.netbeans.modules.java.source.nbjavac.parsing;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.parser.LazyDocCommentTable;
import com.sun.tools.javac.tree.EndPosTable;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.CouplingAbort;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Options;
import com.sun.tools.javac.util.Position.LineMapImpl;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.modules.*;
import org.netbeans.modules.java.source.parsing.CompilationInfoImpl;
import org.netbeans.modules.java.source.parsing.JavacFlowListener;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.java.source.parsing.JavacParser.PartialReparser;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
@ServiceProvider(service = PartialReparser.class, position = 100)
public class PartialReparserImpl implements PartialReparser {
    private static final Logger LOGGER = Logger.getLogger(PartialReparserImpl.class.getName());

    @Override
    public boolean reparseMethod (final CompilationInfoImpl ci,
            final Snapshot snapshot,
            final MethodTree orig,
            final String newBody) throws IOException {
        assert ci != null;
        final FileObject fo = ci.getFileObject();
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.log(Level.FINER, "Reparse method in: {0}", fo);          //NOI18N
        }
        final Phase currentPhase = ci.getPhase();
        if (Phase.PARSED.compareTo(currentPhase) > 0) {
            return false;
        }
        try {
            final CompilationUnitTree cu = ci.getCompilationUnit();
            if (cu == null || newBody == null) {
                return false;
            }
            final JavacTaskImpl task = ci.getJavacTask();
            if (Options.instance(task.getContext()).isSet(JavacParser.LOMBOK_DETECTED)) {
                return false;
            }
            PartialReparserService pr = PartialReparserService.instance(task.getContext());
            if (((JCTree.JCMethodDecl)orig).localEnv == null) {
                //We are seeing interface method or abstract or native method with body.
                //Don't do any optimalization of this broken code - has no attr env.
                return false;
            }
            final JavacTrees jt = JavacTrees.instance(task);
            final int origStartPos = (int) jt.getSourcePositions().getStartPosition(cu, orig.getBody());
            final int origEndPos = (int) jt.getSourcePositions().getEndPosition(cu, orig.getBody());
            if (origStartPos < 0) {
                LOGGER.log(Level.WARNING, "Javac returned startpos: {0} < 0", new Object[]{origStartPos});  //NOI18N
                return false;
            }
            if (origStartPos > origEndPos) {
                LOGGER.log(Level.WARNING, "Javac returned startpos: {0} > endpos: {1}", new Object[]{origStartPos, origEndPos});  //NOI18N
                return false;
            }
            final FindAnonymousVisitor fav = new FindAnonymousVisitor();
            fav.scan(orig.getBody(), null);
            if (fav.hasLocalClass) {
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.log(Level.FINER, "Skeep reparse method (old local classes): {0}", fo);   //NOI18N
                }
                return false;
            }
            final int firstInner = fav.firstInner;
            final int noInner = fav.noInner;
            final Context ctx = task.getContext();
            final TreeLoader treeLoader = TreeLoader.instance(ctx);
            if (treeLoader != null) {
                treeLoader.startPartialReparse();
            }
            try {
                final Log l = Log.instance(ctx);
                l.startPartialReparse();
                final JavaFileObject prevLogged = l.useSource(cu.getSourceFile());
                JCTree.JCBlock block;
                try {
                    DiagnosticListener dl = ci.getDiagnosticListener();
                    assert dl instanceof CompilationInfoImpl.DiagnosticListenerImpl;
                    ((CompilationInfoImpl.DiagnosticListenerImpl)dl).startPartialReparse(origStartPos, origEndPos);
                    long start = System.currentTimeMillis();
                    Map<JCTree,LazyDocCommentTable.Entry> docComments = new HashMap<>();
                    block = pr.reparseMethodBody(cu, orig, newBody + " ", firstInner, docComments);
                    LOGGER.log(Level.FINER, "Reparsed method in: {0}", fo);     //NOI18N
                    if (block == null) {
                        LOGGER.log(
                            Level.FINER,
                            "Skeep reparse method, invalid position, newBody: ",       //NOI18N
                            newBody);
                        return false;
                    }
                    final int newEndPos = (int) jt.getSourcePositions().getEndPosition(cu, block);
                    if (newEndPos != origStartPos + newBody.length()) {
                        return false;
                    }
                    fav.reset();
                    fav.scan(block, null);
                    final int newNoInner = fav.noInner;
                    if (fav.hasLocalClass || noInner != newNoInner) {
                        if (LOGGER.isLoggable(Level.FINER)) {
                            LOGGER.log(Level.FINER, "Skeep reparse method (new local classes): {0}", fo);   //NOI18N
                        }
                        return false;
                    }
                    ((LazyDocCommentTable) ((JCTree.JCCompilationUnit)cu).docComments).table.keySet().removeAll(fav.docOwners);
                    ((LazyDocCommentTable) ((JCTree.JCCompilationUnit)cu).docComments).table.putAll(docComments);
                    long end = System.currentTimeMillis();
                    if (fo != null) {
                        JavacParser.logTime (fo,Phase.PARSED,(end-start));
                    }
                    final int delta = newEndPos - origEndPos;
                    final EndPosTable endPos = ((JCTree.JCCompilationUnit)cu).endPositions;
                    final TranslatePositionsVisitor tpv = new TranslatePositionsVisitor(orig, endPos, delta);
                    tpv.scan(cu, null);
                    ((JCTree.JCMethodDecl)orig).body = block;
                    if (Phase.RESOLVED.compareTo(currentPhase)<=0) {
                        start = System.currentTimeMillis();
                        pr.reattrMethodBody(orig, block);
                        if (LOGGER.isLoggable(Level.FINER)) {
                            LOGGER.log(Level.FINER, "Resolved method in: {0}", fo);     //NOI18N
                        }
                        if (!((CompilationInfoImpl.DiagnosticListenerImpl)dl).hasPartialReparseErrors()) {
                            final JavacFlowListener fl = JavacFlowListener.instance(ctx);
                            if (fl != null && fl.hasFlowCompleted(fo)) {
                                if (LOGGER.isLoggable(Level.FINER)) {
                                    final List<? extends Diagnostic> diag = ci.getDiagnostics();
                                    if (!diag.isEmpty()) {
                                        LOGGER.log(Level.FINER, "Reflow with errors: {0} {1}", new Object[]{fo, diag});     //NOI18N
                                    }
                                }
                                TreePath tp = TreePath.getPath(cu, orig);       //todo: store treepath in changed method => improve speed
                                Tree t = tp.getParentPath().getLeaf();
                                pr.reflowMethodBody(cu, (ClassTree) t, orig);
                                if (LOGGER.isLoggable(Level.FINER)) {
                                    LOGGER.log(Level.FINER, "Reflowed method in: {0}", fo); //NOI18N
                                }
                            }
                        }
                        end = System.currentTimeMillis();
                        if (fo != null) {
                            JavacParser.logTime (fo, Phase.ELEMENTS_RESOLVED,0L);
                            JavacParser.logTime (fo,Phase.RESOLVED,(end-start));
                        }
                    }

                    //fix CompilationUnitTree.getLineMap:
                    long startM = System.currentTimeMillis();
                    char[] chars = snapshot.getText().toString().toCharArray();
                    ((LineMapImpl) cu.getLineMap()).build(chars, chars.length);
                    LOGGER.log(Level.FINER, "Rebuilding LineMap took: {0}", System.currentTimeMillis() - startM);

                    ((CompilationInfoImpl.DiagnosticListenerImpl)dl).endPartialReparse (delta);
                } finally {
                    l.endPartialReparse();
                    l.useSource(prevLogged);
                }
                ci.update(snapshot);
            } finally {
              if (treeLoader != null) {
                  treeLoader.endPartialReparse();
              }
            }
        } catch (CouplingAbort ca) {
            //Needs full reparse
            return false;
        } catch (Throwable t) {
            if (t instanceof ThreadDeath) {
                throw (ThreadDeath) t;
            }
            boolean a = false;
            assert a = true;
            if (a) {
                JavacParser.dumpSource(ci, t);
            }
            return false;
        }
        return true;
    }

}
