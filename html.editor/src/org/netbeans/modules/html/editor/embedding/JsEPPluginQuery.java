/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor.embedding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.spi.embedding.JsEmbeddingProviderPlugin;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author marekfukala
 */
public class JsEPPluginQuery {

    private static JsEPPluginQuery DEFAULT;

    public static synchronized JsEPPluginQuery getDefault() {
        if (DEFAULT == null) {
            DEFAULT = new JsEPPluginQuery();
        }
        return DEFAULT;
    }
    private Lookup.Result<JsEmbeddingProviderPlugin> lookupResult;
    private Collection<? extends JsEmbeddingProviderPlugin> plugins;

    private JsEPPluginQuery() {
        Lookup lookup = MimeLookup.getLookup("text/html");
        lookupResult = lookup.lookupResult(JsEmbeddingProviderPlugin.class);
        lookupResult.addLookupListener(new LookupListener() {
            @Override
            public void resultChanged(LookupEvent ev) {
                refresh();
            }
        });

        refresh();
    }

    private void refresh() {
        Collection<? extends JsEmbeddingProviderPlugin> allInstances = lookupResult.allInstances();
        plugins = allInstances;
    }

    public Session createSession() {
        return new Session();
    }

    public class Session {

        private Collection<JsEmbeddingProviderPlugin> activePlugins;

        public Session() {
            activePlugins = new ArrayList<>();
        }

        public void startProcessing(HtmlParserResult parserResult, Snapshot snapshot, TokenSequence<HTMLTokenId> ts, List<Embedding> embeddings) {
            for (JsEmbeddingProviderPlugin jsep : plugins) {
                if(jsep.startProcessing(parserResult, snapshot, ts, embeddings)) {
                    activePlugins.add(jsep);
                }
            }
        }
        
        public boolean processToken() {
            for (JsEmbeddingProviderPlugin jsep : activePlugins) {
                if (jsep.processToken()) {
                    return true;
                }
            }
            return false;
        }

        public void endProcessing() {
            for (JsEmbeddingProviderPlugin jsep : activePlugins) {
                jsep.endProcessing();
            }
        }
    }
}
