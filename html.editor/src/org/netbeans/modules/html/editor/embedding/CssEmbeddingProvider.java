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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor.embedding;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;

/**
 *
 * @author marekfukala
 */
@EmbeddingProvider.Registration(
        mimeType="text/html",
        targetMimeType="text/css"
)
public class CssEmbeddingProvider extends EmbeddingProvider {

    private static final Logger LOG = Logger.getLogger(CssEmbeddingProvider.class.getSimpleName());
    private static final long MAX_SNAPSHOT_SIZE = 4 * 1024 * 1024; //4MB
    private static final String HTML_MIME_TYPE = "text/html"; // NOI18N

    private String sourceMimeType;
    private Translator translator;

    public CssEmbeddingProvider() {
        this.sourceMimeType = HTML_MIME_TYPE;
        this.translator = new CssHtmlTranslator();
    }

    public static interface Translator {

        public List<Embedding> getEmbeddings(Snapshot snapshot);
    
    }

    @Override
    public List<Embedding> getEmbeddings(Snapshot snapshot) {
        if (sourceMimeType.equals(snapshot.getMimeType())) {
            int slen = snapshot.getText().length();
            LOG.fine(String.format("CssEmbeddingProvider.create(snapshot): mimetype: %s, size: %s", snapshot.getMimeType(), slen)); //NOI18N
            if(slen > MAX_SNAPSHOT_SIZE) {
                LOG.fine(String.format("Size %s > maximum (%s) => providing no css embedding", slen, MAX_SNAPSHOT_SIZE)); //NOI18N
                return Collections.<Embedding>emptyList();
            }
            List<Embedding> embeddings = translator.getEmbeddings(snapshot);
            if(embeddings.isEmpty()) {
                return Collections.emptyList();
            } else {
                return Collections.singletonList(Embedding.create(embeddings));
            }
        } else {
            LOG.log(Level.WARNING, "Unexpected snapshot type: ''{0}''; expecting ''{1}''", new Object[]{snapshot.getMimeType(), sourceMimeType}); //NOI18N
            return Collections.<Embedding>emptyList();
        }
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE; //todo specify reasonable number
    }

    @Override
    public void cancel() {
        //ignore //todo resolve cancel operation
    }
}
