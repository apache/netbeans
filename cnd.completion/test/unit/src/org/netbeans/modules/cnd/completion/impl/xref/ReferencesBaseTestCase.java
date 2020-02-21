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

package org.netbeans.modules.cnd.completion.impl.xref;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.cnd.api.lexer.CndTokenUtilities;
import org.netbeans.cnd.api.lexer.CndAbstractTokenProcessor;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.util.CsmTracer;
import org.netbeans.modules.cnd.modelimpl.test.ProjectBasedTestCase;

/**
 *
 *
 */
public class ReferencesBaseTestCase extends ProjectBasedTestCase {

    public ReferencesBaseTestCase(String testName) {
        super(testName);
    }

    protected final void performTest(String source) throws Exception {
        File testSourceFile = getDataFile(source);
        CsmFile csmFile = getCsmFile(testSourceFile);
        final BaseDocument doc = getBaseDocument(testSourceFile);
        log("creating list of references:");
        final MyTP tp = new MyTP(csmFile, doc);
        doc.render(new Runnable() {
            @Override
            public void run() {
                CndTokenUtilities.processTokens(tp, doc, 0, doc.getLength());
            }
        });
        log("end of references list");
        log("start resolving referenced objects");
        CsmCacheManager.enter();
        try {
            for (ReferenceImpl ref : tp.references) {
                CsmObject owner = ref.getOwner();
                ref(ref.toString());
                ref("--OWNER:\n    " + CsmTracer.toString(owner));
                CsmObject out = ref.getReferencedObject();
                ref("--RESOLVED TO:\n    " + CsmTracer.toString(out));
                ref("==============================================================");
            }
        } finally {
            CsmCacheManager.leave();
        }
        log("end of resolving referenced objects");
        compareReferenceFiles();
    }

    protected static boolean supportReference(TokenId tokenID) {
        assert tokenID != null;
        if(tokenID instanceof CppTokenId) {
            switch ((CppTokenId)tokenID) {
                case IDENTIFIER:
                case PREPROCESSOR_IDENTIFIER:
                case PREPROCESSOR_USER_INCLUDE:
                case PREPROCESSOR_SYS_INCLUDE:
                case PREPROCESSOR_INCLUDE:
                case PREPROCESSOR_INCLUDE_NEXT:
                    return true;
            }
        }
        return false;
    }

    private final class MyTP extends CndAbstractTokenProcessor<Token<TokenId>> {
        final List<ReferenceImpl> references = new ArrayList<ReferenceImpl>();
        private final CsmFile csmFile;
        private final BaseDocument doc;

        MyTP(CsmFile csmFile, BaseDocument doc) {
            this.csmFile = csmFile;
            this.doc = doc;
        }

        @Override
        public boolean token(Token<TokenId> token, int tokenOffset) {
            if (token.id() == CppTokenId.PREPROCESSOR_DIRECTIVE) {
                return true;
            }
            if (supportReference(token.id())) {
                ReferenceImpl ref = ReferencesSupport.createReferenceImpl(csmFile, doc, tokenOffset, CndTokenUtilities.createTokenItem(token, tokenOffset), null);
                assertNotNull("reference must not be null for valid token " + token, ref);
                references.add(ref);
                log(ref.toString());
            }
            return false;
        }
    }
}
