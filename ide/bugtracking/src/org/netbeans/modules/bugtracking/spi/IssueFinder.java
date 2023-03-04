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

package org.netbeans.modules.bugtracking.spi;

/**
 * Recognizes references to issues in text as in the editor or in 
 * Versioning commit messages. Such references are then hyperlinked to 
 * easily access the issue in the IDE.
 * <p>
 * Note that an implementation of this interface is not mandatory for a 
 * NetBeans bugtracking plugin. 
 * </p>
 * 
 * @author Tomas Stupka
 * @author Marian Petras
 * @since 1.85
 */
public interface IssueFinder {

    /**
     * Finds boundaries of one or more references to issues in the given text.
     * The returned array must not be {@code null} and must contain even number
     * of numbers. An empty array is a valid return value. The first number in
     * the array is an index of the beginning of a reference string,
     * the second number is an index of the first character after the reference
     * string. Next numbers express boundaries of other found references, if
     * any.
     * <p>
     * The reference substrings (given by indexes returned by this method)
     * may contain any text as long as method {@link #getIssueId} is able to
     * extract issue identifiers from them. E.g. it is correct that method
     * {@code getIssueSpans()}, when given text &quot;fixed the first bug&quot;,
     * returns array {@code [6, 19]} (boundaries of substring
     * {@code &quot;the first bug&quot;}) if method {@link #getIssueId} can
     * deduce that substring {@code &quot;the first bug&quot;} refers to bug
     * #1. In other words, only (boundaries of) substrings that method
     * {@link #getIssueId} is able to transform the actual issue identifier,
     * should be returned by this method.
     * <p>
     * <b>Please note</b> that this method might be called in EDT and should avoid any 
     * excessive computation.
     * 
     * @param  text  text to be searched for references
     * @return  non-{@code null} array of boundaries of hyperlink references
     *          in the given text
     * @since 1.85
     */
    public int[] getIssueSpans(CharSequence text);

    /**
     * Transforms the given text to an issue identifier.
     * The format of the returned value is specific for the type of issue
     * tracker - it may but may not be a number.
     * <p>
     * <b>Please note</b> that this method might be called in EDT and should avoid any 
     * excessive computation.
     * 
     * @param  issueHyperlinkText  text that refers to a bug/issue
     * @return  unique identifier of the bug/issue
     * @since 1.85
     */
    public String getIssueId(String issueHyperlinkText);

}
