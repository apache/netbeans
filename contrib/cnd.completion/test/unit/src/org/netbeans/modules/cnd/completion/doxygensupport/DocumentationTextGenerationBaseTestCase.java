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
package org.netbeans.modules.cnd.completion.doxygensupport;

import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.cnd.api.lexer.TokenItem;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.completion.cplusplus.hyperlink.HyperlinkBaseTestCase;
import org.netbeans.modules.cnd.spi.model.services.CsmDocProvider;
import org.openide.util.Lookup;

/**
 *
 *
 */
public class DocumentationTextGenerationBaseTestCase extends HyperlinkBaseTestCase {
    
    public DocumentationTextGenerationBaseTestCase(String testName) {
        super(testName);
    }
    
    void performTest(String source, int lineIndex, int colIndex, String expectedDoc) throws Exception {
        CsmDocProvider docProvider = Lookup.getDefault().lookup(CsmDocProvider.class);
        assertNotNull(docProvider);
        
        CsmCacheManager.enter();
        try {
            CsmOffsetable targetObject = findTargetObject(source, lineIndex, colIndex, new AtomicReference<TokenItem<TokenId>>(null));
            CharSequence documentation = docProvider.getDocumentation(targetObject, targetObject.getContainingFile());
        
            assertEquals(expectedDoc, documentation.toString());
        } finally {
            CsmCacheManager.leave();
        }
    }
}
