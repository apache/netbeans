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

package org.netbeans.modules.cnd.highlight.semantic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.test.ProjectBasedTestCase;

/**
 *
 */
public abstract class SemanticHighlightingTestBase  extends ProjectBasedTestCase {
    
    public SemanticHighlightingTestBase(String name) {
        super(name);
    }
    
    protected abstract Collection<? extends CsmOffsetable> getBlocks(FileImpl testFile, int offset);

    protected void performTest(String source) throws Exception {
        performTest(source, -1);
    }

    protected final void performTest(String testFileName, int line, int column) throws Exception {
        int offset = super.getOffset(getDataFile(testFileName), line, column);
        performTest(testFileName, offset);
    }
    
    protected final void performTest(String testFileName, int offset) throws Exception {
        CsmCacheManager.enter();
        try {
            FileImpl file = (FileImpl)getCsmFile(getDataFile(testFileName));
            Collection<? extends CsmOffsetable> out = getBlocks(file, offset);
            assertNotNull(out);
            List<? extends CsmOffsetable> sorted = new ArrayList<>(out);
            Collections.sort(sorted, new Comparator<CsmOffsetable>() {

                @Override
                public int compare(CsmOffsetable o1, CsmOffsetable o2) {
                    return o1.getStartOffset() - o2.getStartOffset();
                }
            });
            int i = 1;
            for (CsmOffsetable b : sorted) {
                ref( "Block " + (i++) + ": Position: " +  // NOI18N
                        file.getLineColumn(b.getStartOffset())[0] + ":" +file.getLineColumn(b.getStartOffset())[1] + "-" +
                        file.getLineColumn(b.getEndOffset())[0] + ":" + file.getLineColumn(b.getEndOffset())[1]// NOI18N
                );
            }
            compareReferenceFiles();
        } finally {
            CsmCacheManager.leave();
        }
    }

}
