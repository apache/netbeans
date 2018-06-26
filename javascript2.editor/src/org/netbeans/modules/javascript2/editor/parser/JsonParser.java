/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.parser;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.javascript2.editor.Utils;
import org.netbeans.modules.javascript2.json.api.JsonOptionsQuery;
import org.netbeans.modules.javascript2.json.parser.JsonLexer;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.spi.lexer.MutableTextInput;
import org.netbeans.spi.lexer.TokenHierarchyControl;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.NbDocument;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 * @author Petr Hejl
 */
public class JsonParser extends SanitizingParser<JsonParserResult> implements PropertyChangeListener {

    private static final RequestProcessor LEXER_RP = new RequestProcessor(JsonParser.class);
    private final AtomicReference<Pair<FileObject,JsonOptionsQuery.Result>> options;
    private final Boolean enforcedAllowComments;

    public JsonParser() {
        this(null);
    }

    /*test*/
    JsonParser(Boolean enforcedAllowComments) {
        super(JsTokenId.jsonLanguage());
        this.options = new AtomicReference<>();
        this.enforcedAllowComments = enforcedAllowComments;
    }

    @Override
    protected String getDefaultScriptName() {
        return "json.json"; // NOI18N
    }

    @Override
    protected JsonParserResult parseSource(SanitizingParser.Context ctx, JsErrorManager errorManager) throws Exception {
        final Snapshot snapshot = ctx.getSnapshot();
        final String text = ctx.getSource();
        final FileObject fo = snapshot.getSource().getFileObject();
        final boolean allowComments;
        if (enforcedAllowComments != null) {
            allowComments = enforcedAllowComments;
        } else if (fo != null) {
            JsonOptionsQuery.Result opts = Optional.ofNullable(options.get())
                    .map((p) -> p.second())
                    .orElse(null);
            if (opts == null) {
                opts = JsonOptionsQuery.getOptions(fo);
                if (options.compareAndSet(null, Pair.<FileObject,JsonOptionsQuery.Result>of(fo, opts))) {
                    opts.addPropertyChangeListener(WeakListeners.propertyChange(this, opts));
                }
            }
            allowComments = opts.isCommentSupported();
        } else {
            allowComments = false;
        }
        final JsonLexer lex = new JsonLexer(new ANTLRInputStream(text), allowComments);
        lex.removeErrorListeners(); //Remove default console log listener
        lex.addErrorListener(errorManager);
        final CommonTokenStream tokens = new CommonTokenStream(lex);
        org.netbeans.modules.javascript2.json.parser.JsonParser parser =
                new org.netbeans.modules.javascript2.json.parser.JsonParser(tokens);
        parser.removeErrorListeners();  //Remove default console log listener
        parser.addErrorListener(errorManager);
        return new JsonParserResult(
                snapshot,
                parser.json());
    }

    @NonNull
    @Override
    protected JsonParserResult createErrorResult(Snapshot snapshot) {
        return new JsonParserResult(snapshot, null);
    }

    @Override
    protected String getMimeType() {
        return JsTokenId.JSON_MIME_TYPE;
    }

    @Override
    protected Sanitize getSanitizeStrategy() {
        return Sanitize.NEVER;
    }

    @Override
    public void propertyChange(@NonNull final PropertyChangeEvent evt) {
        if (JsonOptionsQuery.Result.PROP_COMMENT_SUPPORTED.equals(evt.getPropertyName())) {
            //Refresh parser
            fireChange();
            //Refresh lexer.
            //Is there any better way?
            final Optional<FileObject> maybeFile = Optional.ofNullable(options.get())
                    .map((p) -> p.first());
            maybeFile.map((fo) -> {
                        try {
                            return DataObject.find(fo).getLookup().lookup(EditorCookie.class);
                        } catch (DataObjectNotFoundException e)  {
                            return null;
                        }
                    })
                    .map((ec) -> ec.getDocument())
                    .ifPresent((doc) -> {
                        Optional.ofNullable((MutableTextInput) doc.getProperty(MutableTextInput.class))
                                .ifPresent((mti) -> {
                                    final TokenHierarchyControl control = mti.tokenHierarchyControl();
                                    //For sure render in worker to prevent deadlocks
                                    LEXER_RP.execute(()->NbDocument.runAtomic(doc,()->{control.rebuild();}));
                                });
                    });
            //Refresh tasklist
            Utils.refreshTaskListIndex(maybeFile.orElse(null));
        }
    }

}
