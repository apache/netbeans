/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.web.core.syntax;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.modules.web.core.syntax.gsf.JspParserResult;
import org.netbeans.modules.web.core.syntax.parser.JspSyntaxElement;

/**
 * The purpose of this ParserResultTask is to create java embeddings in places where
 * they are not created directly by the LanguageHierarchy<JspTokenId>.embedding().
 *
 * Notes: 1) the code doesn't translate snapshot offsets to the document offsets,
 *           the jsp syntax is expected to be on the top of the embedding hierarchy
 *
 * @author Marek Fukala
 */
public final class JspSourceTask extends ParserResultTask<JspParserResult> {

    private boolean cancelled;

    //map of jsp syntax element kind -> element name -> attribute name
    private static final Map<JspSyntaxElement.Kind, Map<String, Set<String>>> EMBEDDINGS =
            new HashMap<JspSyntaxElement.Kind, Map<String, Set<String>>>();

    static {
        Map<String, Set<String>> tags = new HashMap<String, Set<String>>();
        tags.put("jsp:useBean",
                new HashSet<String>(Arrays.asList(new String[]{"class", "type", "id"}))); //NOI18N
        
        Map<String, Set<String>> dirs = new HashMap<String, Set<String>>();
        dirs.put("page",
                new HashSet<String>(Arrays.asList(new String[]{"extends", "import"}))); //NOI18N
        dirs.put("tag",
                new HashSet<String>(Collections.singletonList("import"))); //NOI18N
        dirs.put("attribute",
                new HashSet<String>(Collections.singletonList("type"))); //NOI18N

        EMBEDDINGS.put(JspSyntaxElement.Kind.OPENTAG, tags);
        EMBEDDINGS.put(JspSyntaxElement.Kind.DIRECTIVE, dirs);

    }

    public static class Factory extends TaskFactory {

        @Override
        public Collection<? extends SchedulerTask> create(Snapshot snapshot) {
            String mimeType = snapshot.getMimeType();

            if(JspKit.JSP_MIME_TYPE.equals(mimeType) ||
                    JspKit.TAG_MIME_TYPE.equals(mimeType)) { //NOI18N
                return Collections.singletonList(new JspSourceTask());
            } else {
                return Collections.emptyList();
            }
        }
    }

    @Override
    public int getPriority() {
        return 90; //higher priority than the JSP EmbeddingProvider (==100)
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public void cancel() {
        cancelled = true;
    }

    @Override
    public void run(JspParserResult result, SchedulerEvent event) {
        if(cancelled) {
            cancelled = false;
            return ;
        }

        final Collection<JspSyntaxElement.Attribute> whereToCreateTheEmbeddings = new LinkedList<JspSyntaxElement.Attribute>();
        List<JspSyntaxElement> elements = result.elements();
        for(JspSyntaxElement e : elements) {
            Map<String, Set<String>> embs = EMBEDDINGS.get(e.kind());
            if(embs != null) {
                JspSyntaxElement.AttributedTagLikeElement element = (JspSyntaxElement.AttributedTagLikeElement)e;
                String elementName = element.name();
                Set<String> attrs = embs.get(elementName);
                if(attrs != null) {
                    for(JspSyntaxElement.Attribute a : element.attributes()) {
                        if(attrs.contains(a.getName())) {
                            //create the embedding
                            whereToCreateTheEmbeddings.add(a);
                        }
                    }
                }
            }
        }


        //create the embeddings
        final BaseDocument doc = (BaseDocument)result.getSnapshot().getSource().getDocument(false);
        if(doc != null) {
            doc.runAtomicAsUser(new Runnable() {

                @Override
                public void run() {

                    //having the task cancel check inside the render task
                    //should ensure that the offsets from the parser result
                    //will never become unsynchronized with the document offset.
                    //once the document is modified, this task if running is cancelled
                    //and new parsing-task loop runs. Inside the render task the
                    //document cannot be modified.
                    if (cancelled) {
                        cancelled = false;
                        return;
                    }

                    TokenHierarchy<BaseDocument> th = TokenHierarchy.get(doc);
                    TokenSequence<JspTokenId> ts = th.tokenSequence(JspTokenId.language());
                    if(ts == null) {
                        return ;
                    }
                    for(JspSyntaxElement.Attribute attr : whereToCreateTheEmbeddings) {
                        int diff = ts.move(attr.getValueOffset());
                        if(diff == 0) {
                            if(ts.moveNext()) {
                                Token<JspTokenId> t = ts.token();
                                if(t.id() == JspTokenId.ATTR_VALUE) {
                                    //create the embedding
                                    ts.createEmbedding(JavaTokenId.language(), 1, 1);
                                }
                            }
                        }
                    }

                }

            });
        }
    }

}

