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
package org.netbeans.spi.editor.bracesmatching;

import org.netbeans.spi.editor.mimelookup.MimeLocation;

/**
 * The factory for creating {@link BracesMatcher}s. Instances of this class
 * are supposed to be registered in MIME lookup under the mime type of documents
 * that they wish to provide matching services for.
 * 
 * @author Vita Stejskal
 */
@MimeLocation(subfolderName="BracesMatchers")
public interface BracesMatcherFactory {

    /**
     * Creates a matcher for searching a document for matching areas.
     * 
     * <p class="nonnormative">An example of <code>BracesMatcher</code> could be
     * a matcher that detects braces, brackets or parenthesis next to a caret
     * and finds their matching counterparts.
     * 
     * @param context The context to use for searching. It contains
     *   the position of a caret in a document and allows to report results.
     * 
     * @return A new matcher.
     */
    public BracesMatcher createMatcher(MatcherContext context);
    
}
