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

package org.netbeans.installer.infra.utils.comment.handlers;

import org.netbeans.installer.infra.utils.comment.utils.Utils;
import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * The base class for handling file types in which line-based comments are used,
 * such as java-style properties files or shell scripts.
 *
 * @author Kirill Sorokin
 */
public abstract class LineFileHandler implements FileHandler {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * The regular expression pattern which matches the line which is a comment.
     */
    protected final Pattern commentPattern;
    
    /**
     * The regular expression pattern which matches the line which should be
     * ignored. This is used to skip the non-whitespace lines that can preceed the
     * initial comment such as <code>#!/bin/sh</code> in shell scripts.
     */
    protected final Pattern ignorePattern;
    
    /**
     * The prefix which should be used for each line in the comment. If there should
     * be no prefix - an empty line should be used (which is very unlikely in this
     * case).
     */
    protected final String commentPrefix;
    
    /**
     * The cached file contents.
     */
    protected String contents;
    
    // constructor //////////////////////////////////////////////////////////////////
    /**
     * The constructor which should be called by the extending classes. It merely
     * sets the class fields, performnig some basic validation.
     *
     * @param commentPattern The regular expression pattern which matches the line
     *      which is a comment.
     * @param ignorePattern The regular expression pattern which matches the line
     *      which should be ignored.
     * @param commentPrefix The prefix which should be used for each line in the
     *      comment.
     * @throws java.lang.IllegalArgumentException if the parameters validation
     *      fails.
     */
    protected LineFileHandler(
            final Pattern commentPattern,
            final Pattern ignorePattern,
            final String commentPrefix) {
        if (commentPattern == null) {
            throw new IllegalArgumentException(
                    "The 'commentPattern' parameter cannot be null."); // NOI18N
        }
        this.commentPattern = commentPattern;
        
        if (ignorePattern == null) {
            throw new IllegalArgumentException(
                    "The 'ignorePattern' parameter cannot be null."); // NOI18N
        }
        this.ignorePattern = ignorePattern;
        
        if (commentPrefix == null) {
            throw new IllegalArgumentException(
                    "The 'commentPrefix' parameter cannot be null."); // NOI18N
        }
        this.commentPrefix = commentPrefix;
    }
    
    // public ///////////////////////////////////////////////////////////////////////
    /**
     * {@inheritDoc}
     */
    public final void load(final File file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException(
                    "The 'file' parameter cannot be null."); // NOI18N
        }
        
        contents = Utils.readFile(file);
    }
    
    /**
     * {@inheritDoc}
     */
    public final void save(final File file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException(
                    "The 'file' parameter cannot be null."); // NOI18N
        }
        if (contents == null) {
            throw new IllegalStateException(
                    "The contents cache has not been intialized."); // NOI18N
        }
        
        Utils.writeFile(file, contents);
    }
    
    /**
     * {@inheritDoc}
     */
    public final String getCurrentComment() {
        if (contents == null) {
            throw new IllegalStateException(
                    "The contents cache has not been intialized."); // NOI18N
        }
        
        final StringBuilder builder = new StringBuilder();
        final String[] lines = contents.split(Utils.NL_PATTERN);
        
        int i = 0;
        
        // skip the leading whitespace and ignored lines
        for (; i < lines.length; i++) {
            if (!lines[i].trim().equals("") &&
                    !ignorePattern.matcher(lines[i]).matches()) {
                break;
            }
        }
        
        // read the comment
        for (; i < lines.length; i++) {
            if (commentPattern.matcher(lines[i]).matches()) {
                builder.append(lines[i]).append(Utils.NL);
            } else {
                break;
            }
        }
        
        return builder.length() > 0 ? builder.toString() : null;
    }
    
    /**
     * {@inheritDoc}
     */
    public final String getCorrectComment(final String text, final int lineLength) {
        return commentPrefix + Utils.NL +
                Utils.reformat(text, commentPrefix, lineLength) +
                commentPrefix + Utils.NL;
    }
    
    /**
     * {@inheritDoc}
     */
    public final void insertComment(final String text, final int lineLength) {
        if (text == null) {
            throw new IllegalArgumentException(
                    "The 'text' parameter cannot be null."); // NOI18N
        }
        if (lineLength <= 0) {
            throw new IllegalArgumentException(
                    "The 'lineLength' parameter must be positive."); // NOI18N
        }
        if (contents == null) {
            throw new IllegalStateException(
                    "The contents cache has not been intialized."); // NOI18N
        }
        
        final StringBuilder builder = new StringBuilder();
        final String[] lines = contents.split(Utils.NL_PATTERN);
        final String comment = getCorrectComment(text, lineLength);
        
        int i = 0;
        
        // transfer the existing leading whitespace and ignored lines
        for (; i < lines.length; i++) {
            if (!lines[i].trim().equals("") &&
                    !ignorePattern.matcher(lines[i]).matches()) {
                break;
            } else {
                builder.append(lines[i]).append(Utils.NL);
            }
        }
        
        // transfer the comment and an empty line
        builder.append(comment).append(Utils.NL);
        
        // transfer the rest of file
        for (; i < lines.length; i++) {
            builder.append(lines[i]).append(Utils.NL);
        }
        
        contents = builder.toString();
    }
    
    /**
     * {@inheritDoc}
     */
    public final void updateComment(final String text, final int lineLength) {
        if (text == null) {
            throw new IllegalArgumentException(
                    "The 'text' parameter cannot be null."); // NOI18N
        }
        if (lineLength <= 0) {
            throw new IllegalArgumentException(
                    "The 'lineLength' parameter must be positive."); // NOI18N
        }
        if (contents == null) {
            throw new IllegalStateException(
                    "The contents cache has not been intialized."); // NOI18N
        }
        
        final String currentComment = getCurrentComment();
        
        if (currentComment == null) {
            insertComment(text, lineLength);
            return;
        }
        
        final String correctComment = getCorrectComment(text, lineLength);
        
        // we don't need to update anything if the current initial comment is the
        // same as the correct one
        if (currentComment.equals(correctComment)) {
            return;
        }
        
        final StringBuilder builder = new StringBuilder();
        final String[] lines = contents.split(Utils.NL_PATTERN);
        
        int i = 0;
        
        // skip the leading whitespace and the ignored lines
        for (; i < lines.length; i++) {
            final String trimmed = lines[i].trim();
            
            if (!trimmed.equals("") &&
                    !ignorePattern.matcher(lines[i]).matches()) {
                break;
            }
            
            builder.append(lines[i]).append(Utils.NL);
        }
        
        // skip the comment
        for (; i < lines.length; i++) {
            if (!commentPattern.matcher(lines[i]).matches()) {
                break;
            }
        }
        
        // skip the empty lines after the comment
        for (; i < lines.length; i++) {
            if (!lines[i].trim().equals("")) {
                break;
            }
        }
        
        // output the correct comment and an empty line
        builder.append(correctComment).append(Utils.NL);
        
        // transfer the rest of the file
        for (; i < lines.length; i++) {
            builder.append(lines[i]).append(Utils.NL);
        }
        
        contents = builder.toString();
    }
}
