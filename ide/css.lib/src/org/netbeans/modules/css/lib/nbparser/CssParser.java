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
package org.netbeans.modules.css.lib.nbparser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.css.lib.ExtCss3Lexer;
import org.netbeans.modules.css.lib.ExtCss3Parser;
import org.netbeans.modules.css.lib.api.CssParserResult;
import javax.swing.event.ChangeListener;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.css.lib.AbstractParseTreeNode;
import org.netbeans.modules.css.lib.NbParseTreeBuilder;
import org.netbeans.modules.css.lib.api.ProblemDescription;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.netbeans.modules.web.common.api.Constants;
import org.openide.filesystems.FileObject;
import org.openide.util.CharSequences;
import org.openide.util.NbBundle;

/**
 *
 * @author mfukala@netbeans.org
 */
@NbBundle.Messages({
    "too.large.snapshot=The source code is too large for CSS parsing.",
})
public class CssParser extends Parser {

    private static final Logger LOG = Logger.getLogger(CssParser.class.getSimpleName());

    private final AtomicBoolean cancelled = new AtomicBoolean();
    private final String topLevelSnapshotMimetype;

    private static final int MAX_SNAPSHOT_SIZE = 2 * 1024 * 1014; //2MB
    
    //cache
    private Snapshot snapshot;
    private AbstractParseTreeNode tree;
    private List<ProblemDescription> problems;

    public CssParser() {
        topLevelSnapshotMimetype = null;
    }

    /* test */ public CssParser(String topLevelSnapshotMimetype) {
        this.topLevelSnapshotMimetype = topLevelSnapshotMimetype;
    }

    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
        cancelled.set(false);
        if (snapshot == null) {
            return;
        }
        
        this.snapshot = snapshot;
        FileObject fo = snapshot.getSource().getFileObject();
        String fileName = fo == null ? "no file" : fo.getPath(); //NOI18N
        String mimeType = topLevelSnapshotMimetype != null ? topLevelSnapshotMimetype : (fo == null ? null : fo.getMIMEType());
        LOG.log(Level.FINE, "Parsing {0} ", fileName); //NOI18N
        long start = System.currentTimeMillis();
        try {
            boolean tooLargeSnapshot = snapshot.getText().length() > MAX_SNAPSHOT_SIZE;
            
            //parse just an empty string in case of an oversize snapshot
            CharSequence source = tooLargeSnapshot ? "" : snapshot.getText();
            
            ExtCss3Lexer lexer = new ExtCss3Lexer(source, mimeType);
            TokenStream tokenstream = new ProgressingTokenStream(
                10_000_000,
                new CommonTokenStream(lexer));
            NbParseTreeBuilder builder = new NbParseTreeBuilder(source);
            ExtCss3Parser parser = new ExtCss3Parser(tokenstream, builder, mimeType);

            if (cancelled.get()) {
                return;
            }

            try {
                parser.styleSheet();
            } catch (ProgressingFailedException pfe) {
                LOG.log(Level.INFO, "CSS/SASS/LESS document exceeded maximum reads: " + snapshot.getSource().getFileObject());
                this.tree = null;
                this.problems = Arrays.asList(new ProblemDescription(
                    0, snapshot.getText().length(), "Failed to parse CSS/SASS/LESS document", ProblemDescription.Keys.PARSING.name(), ProblemDescription.Type.FATAL
                ));
                return;
            }

            if (cancelled.get()) {
                return;
            }

            AbstractParseTreeNode tree_local = builder.getTree();
            List<ProblemDescription> problems_local = new ArrayList<>();
            //add lexer issues
            problems_local.addAll(lexer.getProblems());
            //add parser issues
            problems_local.addAll(builder.getProblems());

            filterProblemsInVirtualCode(snapshot, problems_local);
            filterTemplatingProblems(snapshot, problems_local);

            if(tooLargeSnapshot) {
                //add a problem description informing the user there's something 'wrong' with the file
                problems_local.add(new ProblemDescription(0, 0, 
                        Bundle.too_large_snapshot(), ProblemDescription.Keys.PARSING.name(), ProblemDescription.Type.WARNING ));
            }
            
            if (cancelled.get()) {
                return;
            }

            this.tree = tree_local;
            this.problems = problems_local;

        } catch (RecognitionException ex) {
            throw new ParseException(String.format("Error parsing %s snapshot.", snapshot), ex); //NOI18N
        } finally {
            long end = System.currentTimeMillis();
            LOG.log(Level.FINE, "Parsing of {0} took {1} ms.", new Object[]{fileName, (end - start)}); //NOI18N
        }

    }

    @Override
    public CssParserResult getResult(Task task) throws ParseException {
        return cancelled.get() || (tree == null) ? null : new CssParserResult(snapshot, tree, problems);
    }

    @Override
    public void cancel(CancelReason reason, SourceModificationEvent event) {
        if (CancelReason.SOURCE_MODIFICATION_EVENT == reason && event.sourceChanged()) {
            cancelled.set(true);
            tree = null;
            problems = null;
            snapshot = null;
        }
    }

    @Override
    public void addChangeListener(ChangeListener changeListener) {
        //no-op
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
        //no-op
    }

    private static void filterProblemsInVirtualCode(Snapshot snapshot, List<ProblemDescription> problems) {
        ListIterator<ProblemDescription> listIterator = problems.listIterator();
        while (listIterator.hasNext()) {
            ProblemDescription p = listIterator.next();
            int from = p.getFrom();
            int to = p.getTo();
            if (snapshot.getOriginalOffset(from) == -1 || snapshot.getOriginalOffset(to) == -1) {
                listIterator.remove();
            }
        }
    }

    //filtering out problems caused by templating languages
    private static void filterTemplatingProblems(Snapshot snapshot, List<ProblemDescription> problems) {
        MimePath mimePath = snapshot.getMimePath();
        if (mimePath.size() > 1) {
            //once the css code is embedde, we need to assume there might be sg. generated.
            //now also for plain html code as there are the html extenstions like Angular or Knockout,
            //which might contain expressions like <div style="width: {{model.getWidth()}}%">...</div>
            CharSequence text = snapshot.getText();
            ListIterator<ProblemDescription> listIterator = problems.listIterator();
            while (listIterator.hasNext()) {
                ProblemDescription p = listIterator.next();
                //XXX Idealy the filtering context should be dependent on the enclosing node
                //sg. like if there's a templating error in an declaration - search the whole
                //declaration for the templating mark. 
                //
                //Using some simplification - line context, though some nodes may span multiple
                //lines and the templating mark may not necessarily be at the line with the error.
                //
                //so find line bounds...

                //the "premature end of file" error has position pointing after the last char (=text.length())!
                if (p.getFrom() == text.length()) {
                    listIterator.remove(); //consider this as hidden error
                    continue;
                }

                int from, to;
                for (from = p.getFrom(); from > 0; from--) {
                    char c = text.charAt(from);
                    if (c == '\n') {
                        break;
                    }
                }
                for (to = p.getTo(); to < text.length(); to++) {
                    char c = text.charAt(to);
                    if (c == '\n') {
                        break;
                    }
                }
                //check if there's the templating mark (@@@) in the context
                CharSequence img = snapshot.getText().subSequence(from, to);
                if (CharSequences.indexOf(img, Constants.LANGUAGE_SNIPPET_SEPARATOR) != -1) {
                    listIterator.remove();
                }
            }
        }
    }

}
