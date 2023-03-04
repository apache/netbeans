/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.parsing.spi.indexing.support;

import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.parsing.impl.indexing.FooPathRecognizer;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizer;

/**
 *
 * @author sdedic
 */
public final class EmbeddedPathRecognizer extends PathRecognizer {
    public static final String EMB_MIME = "text/x-emb"; //NOI18N
    public static final String EXT_EMB = "emb";            //NOI18N
    public static final String SRC_EMB = "emb-src";        //NOI18N

    @Override
    public Set<String> getSourcePathIds() {
        return Collections.singleton(SRC_EMB);
    }

    @Override
    public Set<String> getLibraryPathIds() {
        return Collections.<String>emptySet();
    }

    @Override
    public Set<String> getBinaryLibraryPathIds() {
        return Collections.<String>emptySet();
    }

    @Override
    public Set<String> getMimeTypes() {
        return Collections.singleton(EMB_MIME);
    }
    
}
