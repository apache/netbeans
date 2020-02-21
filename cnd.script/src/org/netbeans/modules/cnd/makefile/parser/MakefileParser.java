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
package org.netbeans.modules.cnd.makefile.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.event.ChangeListener;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.cnd.api.makefile.MakefileElement;
import org.netbeans.modules.cnd.api.makefile.MakefileInclude;
import org.netbeans.modules.cnd.api.makefile.MakefileMacro;
import org.netbeans.modules.cnd.api.makefile.MakefileRule;
import org.netbeans.modules.cnd.makefile.MakefileApiAccessor;
import org.netbeans.modules.cnd.api.script.MakefileTokenId;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.openide.filesystems.FileObject;

/**
 */
public class MakefileParser extends Parser {

    private final AtomicBoolean cancelled;
    private MakefileParseResult result;

    public MakefileParser() {
        cancelled = new AtomicBoolean();
    }

    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
        result = null;
        cancelled.set(false);
        result = parse(snapshot, cancelled);
    }

    @Override
    public MakefileParseResult getResult(Task task) throws ParseException {
        return result;
    }

    @Deprecated
    @Override
    public void cancel() {
        cancelled.set(true);
    }

    @Override
    public void cancel(CancelReason reason, SourceModificationEvent event) {
        cancelled.set(true);
    }
    
    @Override
    public void addChangeListener(ChangeListener changeListener) {
        // nothing to listen to => no reason to register listeners
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
        // nothing to listen to => no reason to register listeners
    }


    private static MakefileParseResult parse(Snapshot snapshot, AtomicBoolean cancelled) {
        TokenHierarchy<?> tokenHierarchy = snapshot.getTokenHierarchy();
        if (tokenHierarchy == null) {
            return null;
        }
        TokenSequence<MakefileTokenId> tokenSequence = tokenHierarchy.tokenSequence(MakefileTokenId.language());

        if (tokenSequence == null) {
            return null;
        }

        FileObject fobj = snapshot.getSource().getFileObject();

        List<MakefileElement> makefileElements = new ArrayList<MakefileElement>(1);

        int startIndex = 0;
        while (tokenSequence.moveNext() && !cancelled.get()) {
            Token<MakefileTokenId> token = tokenSequence.token();

            switch (token.id()) {
                case DEFINE:
                    makefileElements.add(createDefine(fobj, tokenSequence));
                    startIndex = tokenSequence.index() + 1;
                    break;

                case EQUALS:
                case COLON_EQUALS:
                case PLUS_EQUALS:
                    tokenSequence.moveIndex(startIndex);
                    makefileElements.add(createMacro(fobj, tokenSequence));
                    startIndex = tokenSequence.index() + 1;
                    break;

                case COLON:
                    tokenSequence.moveIndex(startIndex);
                    makefileElements.add(createRule(fobj, tokenSequence));
                    startIndex = tokenSequence.index() + 1;
                    break;

                case INCLUDE:
                    makefileElements.add(createInclude(fobj, tokenSequence));
                    startIndex = tokenSequence.index() + 1;
                    break;

                case COMMENT:
                case NEW_LINE:
                    startIndex = tokenSequence.index() + 1;
            }
        }

        return cancelled.get()? null : new MakefileParseResult(snapshot, makefileElements);
    }

    private static MakefileMacro createDefine(FileObject fobj, TokenSequence<MakefileTokenId> tokenSequence) {
        StringBuilder nameBuilder = new StringBuilder();
        int startOffset = tokenSequence.offset();
        NAME_LOOP: while (tokenSequence.moveNext()) {
            Token<MakefileTokenId> token = tokenSequence.token();
            switch (token.id()) {
                case COMMENT:
                case NEW_LINE:
                    break NAME_LOOP;
                default:
                    nameBuilder.append(token.text());
            }
        }

        StringBuilder valueBuilder = new StringBuilder();
        VALUE_LOOP: while (tokenSequence.moveNext()) {
            Token<MakefileTokenId> token = tokenSequence.token();
            switch (token.id()) {
                case ENDEF:
                    break VALUE_LOOP;
                default:
                    valueBuilder.append(token.text());
            }
        }

        int endOffset = tokenSequence.offset();

        return MakefileApiAccessor.getInstance().newMakefileMacro(fobj, startOffset, endOffset, nameBuilder.toString().trim(), valueBuilder.toString().trim());
    }

    private static MakefileMacro createMacro(FileObject fobj, TokenSequence<MakefileTokenId> tokenSequence) {
        StringBuilder nameBuilder = new StringBuilder();
        int startOffset = -1;
        NAME_LOOP: while (tokenSequence.moveNext()) {
            Token<MakefileTokenId> token = tokenSequence.token();
            if (startOffset == -1) {
                startOffset = tokenSequence.offset();
            }
            switch (token.id()) {
                case EQUALS:
                case COLON_EQUALS:
                case PLUS_EQUALS:
                    break NAME_LOOP;
                default:
                    nameBuilder.append(token.text());
            }
        }

        StringBuilder valueBuilder = new StringBuilder();
        VALUE_LOOP: while (tokenSequence.moveNext()) {
            Token<MakefileTokenId> token = tokenSequence.token();
            switch (token.id()) {
                case COMMENT:
                case NEW_LINE:
                    break VALUE_LOOP;
                default:
                    valueBuilder.append(token.text());
            }
        }
        int endOffset = tokenSequence.offset();

        return MakefileApiAccessor.getInstance().newMakefileMacro(fobj, startOffset, endOffset, nameBuilder.toString().trim(), valueBuilder.toString());
    }

    private static MakefileRule createRule(FileObject fobj, TokenSequence<MakefileTokenId> tokenSequence) {
        StringBuilder nameBuilder = new StringBuilder();
        int startOffset = -1;

        List<String> targets = new ArrayList<String>(1);
        TARGETS_LOOP: while (tokenSequence.moveNext()) {
            Token<MakefileTokenId> token = tokenSequence.token();
            if (startOffset == -1) {
                startOffset = tokenSequence.offset();
            }
            switch (token.id()) {
                case COLON:
                    if (0 < nameBuilder.length()) {
                        targets.add(nameBuilder.toString());
                        nameBuilder.setLength(0);
                    }
                    break TARGETS_LOOP;
                case WHITESPACE:
                case ESCAPED_NEW_LINE:
                    if (0 < nameBuilder.length()) {
                        targets.add(nameBuilder.toString());
                        nameBuilder.setLength(0);
                    }
                    break;
                default:
                    nameBuilder.append(token.text());
            }
        }

        List<String> prereqs = new ArrayList<String>(1);
        PREREQS_LOOP: while (tokenSequence.moveNext()) {
            Token<MakefileTokenId> token = tokenSequence.token();
            switch (token.id()) {
                case WHITESPACE:
                case ESCAPED_NEW_LINE:
                    if (0 < nameBuilder.length()) {
                        prereqs.add(nameBuilder.toString());
                        nameBuilder.setLength(0);
                    }
                    break;
                case COMMENT:
                case NEW_LINE:
                    if (0 < nameBuilder.length()) {
                        prereqs.add(nameBuilder.toString());
                        nameBuilder.setLength(0);
                    }
                    break PREREQS_LOOP;
                default:
                    nameBuilder.append(token.text());
            }
        }
        int endOffset = tokenSequence.offset();

        return MakefileApiAccessor.getInstance().newMakefileRule(fobj, startOffset, endOffset, targets, prereqs);
    }

    private static MakefileInclude createInclude(FileObject fobj, TokenSequence<MakefileTokenId> tokenSequence) {
        StringBuilder nameBuilder = new StringBuilder();
        int startOffset = tokenSequence.offset();

        List<String> fileNames = new ArrayList<String>(1);
        NAMES_LOOP: while (tokenSequence.moveNext()) {
            Token<MakefileTokenId> token = tokenSequence.token();
            switch (token.id()) {
                case WHITESPACE:
                case ESCAPED_NEW_LINE:
                    if (0 < nameBuilder.length()) {
                        fileNames.add(nameBuilder.toString());
                        nameBuilder.setLength(0);
                    }
                    break;

                case COMMENT:
                case NEW_LINE:
                    if (0 < nameBuilder.length()) {
                        fileNames.add(nameBuilder.toString());
                        nameBuilder.setLength(0);
                    }
                    break NAMES_LOOP;

                default:
                    nameBuilder.append(token.text());
            }
        }

        return MakefileApiAccessor.getInstance().newMakefileInclude(fobj, startOffset, startOffset, fileNames);
    }
}
