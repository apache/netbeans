/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
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
