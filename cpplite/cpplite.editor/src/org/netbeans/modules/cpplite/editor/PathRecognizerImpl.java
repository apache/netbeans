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
package org.netbeans.modules.cpplite.editor;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.cpplite.editor.file.MIMETypes;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizer;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=PathRecognizer.class)
public class PathRecognizerImpl extends PathRecognizer{

    @Override
    public Set<String> getSourcePathIds() {
        return Collections.singleton(ClassPath.SOURCE);
    }

    @Override
    public Set<String> getLibraryPathIds() {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getBinaryLibraryPathIds() {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getMimeTypes() {
        return new HashSet<>(Arrays.asList(MIMETypes.C, MIMETypes.CPP, MIMETypes.H, MIMETypes.HPP));
    }

}
