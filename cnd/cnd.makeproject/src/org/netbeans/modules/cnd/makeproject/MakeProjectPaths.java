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

package org.netbeans.modules.cnd.makeproject;

import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizer;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service=org.netbeans.modules.parsing.spi.indexing.PathRecognizer.class)
public class MakeProjectPaths extends PathRecognizer {

    public static final String SOURCES = "org.netbeans.modules.cnd.makeproject/SOURCES"; //NOI18N

    // -----------------------------------------------------------------------
    // PathRecognizer implementation
    // -----------------------------------------------------------------------

    @Override
    public Set<String> getSourcePathIds() {
        return Collections.<String>singleton(SOURCES);
    }

    @Override
    public Set<String> getLibraryPathIds() {
        return null;
    }

    @Override
    public Set<String> getBinaryLibraryPathIds() {
        return null;
    }

    @Override
    public Set<String> getMimeTypes() {
        return MIMENames.CND_TEXT_MIME_TYPES;
    }
}
