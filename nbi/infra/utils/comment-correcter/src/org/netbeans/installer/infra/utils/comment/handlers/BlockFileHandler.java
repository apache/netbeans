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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The base class for ahndling file types in which block comments are used, such as
 * java source files or xml documents.
 *
 * @author Kirill Sorokin
 */
public abstract class BlockFileHandler implements FileHandler {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * The regular expression pattern which matches the initial comment.
     */
    protected final Pattern commentPattern;
    
    /**
     * The comment opening string.
     */
    protected final String commentStart;
    
    /**
     * The prefix which should be used for each line in the comment. If there should
     * be no prefix - an empty line should be used.
     */
    protected final String commentPrefix;
    
    /**
     * The comment closing string.
     */
    protected final String commentEnd;
    
    /**
     * The cached file contents.
     */
    protected String contents;
    
    // constructor //////////////////////////////////////////////////////////////////
    /**
     * The constructor which should be called by the extending classes. It merely
     * sets the class fields, performnig some basic validation.
     *
     * @param commentPattern The regular expression pattern which matches the
     *      initial comment.
     * @param commentStart The comment opening string.
     * @param commentPrefix The prefix which should be used for each line in the
     *      comment.
     * @param commentEnd The comment closing string.
     * @throws java.lang.IllegalArgumentException if the parameters validation
     *      fails.
     */
    protected BlockFileHandler(
            final Pattern commentPattern,
            final String commentStart,
            final String commentPrefix,
            final String commentEnd) {
        if (commentPattern == null) {
            throw new IllegalArgumentException(
                    "The 'commentPattern' parameter cannot be null."); // NOI18N
        }
        this.commentPattern = commentPattern;
        
        if (commentStart == null) {
            throw new IllegalArgumentException(
                    "The 'commentStart' parameter cannot be null."); // NOI18N
        }
        this.commentStart = commentStart;
        
        if (commentPrefix == null) {
            throw new IllegalArgumentException(
                    "The 'commentPrefix' parameter cannot be null."); // NOI18N
        }
        this.commentPrefix = commentPrefix;
        
        if (commentEnd == null) {
            throw new IllegalArgumentException(
                    "The 'commentEnd' parameter cannot be null."); // NOI18N
        }
        this.commentEnd = commentEnd;
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
        
        final Matcher matcher = commentPattern.matcher(contents);
        
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public final String getCorrectComment(final String text, final int lineLength) {
        return commentStart +
                Utils.reformat(text, commentPrefix, lineLength) +
                commentEnd + Utils.NL;
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
        
        final String comment = getCorrectComment(text, lineLength);
        final int position = getCommentPosition();
        
        final String prefix = contents.substring(0, position);
        final String suffix = contents.substring(position);
        
        contents = prefix + comment + suffix;
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
        
        final String correctComment = 
                getCorrectComment(text, lineLength);
        
        final String prefix =
                contents.substring(0, contents.indexOf(currentComment));
        final String suffix =
                contents.substring(prefix.length() + currentComment.length());
        
        contents = prefix + correctComment + suffix;
    }
    
    // protected ////////////////////////////////////////////////////////////////////
    /**
     * Calculates the proper initial comment position. This is used in the
     * <code>insertComment</code> method to devise the position at which to insert
     * the new comment. Extending classes may want to override this method to
     * provide initial comment position that is correct for their file type (e.g. in
     * xml documents the comment should appear only after the
     * <code>&lt;?xml ... ?&gt;</code> tag.
     *
     * @return The proper position for the initial comment.
     */
    protected int getCommentPosition() {
        if (contents == null) {
            throw new IllegalStateException(
                    "The contents cache has not been intialized."); // NOI18N
        }
        
        return 0;
    }
}
