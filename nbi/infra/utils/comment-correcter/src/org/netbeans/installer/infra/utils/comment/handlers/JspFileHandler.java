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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A {@link FileHandler} implementation capable of handling JSP documents.
 *
 * @author Kirill Sorokin
 */
public class JspFileHandler extends BlockFileHandler {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * Creates a new instance of {@link JspFileHandler}. The constuctor
     * simply falls back to the
     * {@link BlockFileHandler#BlockFileHandler(Pattern, String, String, String)}
     * passing in the parameters relevant to JSP files.
     */
    public JspFileHandler() {
        super(COMMENT_PATTERN,
                COMMENT_START,
                COMMENT_PREFIX,
                COMMENT_END);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean accept(final File file) {
        if (file == null) {
            throw new IllegalArgumentException(
                    "The 'file' parameter cannot be null."); // NOI18N
        }
        
        if (!file.isFile()) {
            return false;
        }
        
        return file.getName().endsWith(".jsp") || // NOI18N
                file.getName().endsWith(".jspx"); // NOI18N
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getCommentPosition() {
        if (contents == null) {
            throw new IllegalStateException(
                    "The contents cache has not been intialized."); // NOI18N
        }
        
        final Matcher matcher = COMMENT_POSITION_PATTERN.matcher(contents);
        if (matcher.find()) {
            return contents.indexOf(matcher.group(1));
        } else {
            return 0;
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    private static final Pattern COMMENT_POSITION_PATTERN = Pattern.compile(
            "\\A\\s*(?:<\\?xml.*?\\?>)?\\s*(.*)", // NOI18N
            Pattern.MULTILINE | Pattern.DOTALL);
    
    /**
     * The regular expression pattern which matches the initial comment.
     */
    private static final Pattern COMMENT_PATTERN = Pattern.compile(
            "\\A\\s*(?:<\\?xml.*?\\?>)?\\s*(<%--.*?--%>\\s*\\n)", // NOI18N
            Pattern.MULTILINE | Pattern.DOTALL);
    
    /**
     * The comment opening string.
     */
    private static final String COMMENT_START =
            "<%--" + Utils.NL; // NOI18N
    
    /**
     * The prefix which should be used for each line in the comment.
     */
    private static final String COMMENT_PREFIX =
            "  "; // NOI18N
    
    /**
     * The comment closing string.
     */
    private static final String COMMENT_END =
            "--%>" + Utils.NL; // NOI18N
}
