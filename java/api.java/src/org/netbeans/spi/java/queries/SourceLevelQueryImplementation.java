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
package org.netbeans.spi.java.queries;

import org.netbeans.api.java.queries.SourceLevelQuery;
import org.openide.filesystems.FileObject;

/**
 * Permits providers to return specification source level of Java source file.
 * @since org.netbeans.api.java/1 1.5
 * @deprecated use {@link SourceLevelQueryImplementation2}
 */
@Deprecated
public interface SourceLevelQueryImplementation {

    /**
     * Returns source level of the given Java file. For acceptable return values
     * see the documentation of <code>-source</code> command line switch of 
     * <code>javac</code> compiler .
     * @param javaFile Java source file in question
     * @return source level of the Java file, e.g. "1.3", "1.4" or "1.5", or
     *    null if it is not known. It is allowed to return source level synonyms
     *    e.g. "5" for "1.5". These synonyms are always normalized by
     * {@link SourceLevelQuery#getSourceLevel}.
     */
    public String getSourceLevel(FileObject javaFile);

}
