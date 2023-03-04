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

package org.netbeans.modules.parsing.impl.indexing;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizer;

/**
 *
 * @author sdedic
 */
public final class FooPathRecognizer extends PathRecognizer {

    public static final String FOO_EXT = "foo";    //NOI18N
    public static final String FOO_MIME = "text/x-foo";    //NOI18N
    public static final String FOO_SOURCES = "foo-src";    //NOI18N
    public static final String FOO_BINARY = "foo-bin";      //NOI18N
    public static final String FOO_PLATFORM = "foo-platform";      //NOI18N

    @Override
    public Set<String> getSourcePathIds() {
        return Collections.<String>singleton(FOO_SOURCES);
    }

    @Override
    public Set<String> getLibraryPathIds() {
        return Collections.<String>emptySet();
    }

    @Override
    public Set<String> getBinaryLibraryPathIds() {
        Set<String> s = new HashSet<>();
        s.add(FOO_PLATFORM);
        s.add(FOO_BINARY);
        return s;
    }

    @Override
    public Set<String> getMimeTypes() {
        return Collections.<String>singleton(FOO_MIME);
    }
    
}
