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

package org.netbeans.modules.java.source.parsing;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizer;

/**
 *
 * @author Tomas Zezula
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.parsing.spi.indexing.PathRecognizer.class, position=100)
public class JavaPathRecognizer extends PathRecognizer {

    private static final Set<String> MIME_TYPES = Collections.singleton(JavacParser.MIME_TYPE);

    private static final Set<String> SOURCES = Collections.singleton(ClassPath.SOURCE);

    private static final Set<String> BINARIES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(new String[] {
        ClassPath.BOOT,
        ClassPath.COMPILE
    })));

    @Override
    public Set<String> getSourcePathIds() {
        return SOURCES;
    }

    @Override
    public Set<String> getBinaryLibraryPathIds() {
        return BINARIES;
    }

    @Override
    public Set<String> getLibraryPathIds() {
        return null;
    }

    @Override
    public Set<String> getMimeTypes() {
        return MIME_TYPES;
    }

}
