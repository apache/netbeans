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

import java.io.File;
import java.util.regex.Pattern;

/**
 * A {@link FileHandler} implementation capable of handling java-style properties
 * files.
 *
 * @author Kirill Sorokin
 */
public class PropertiesFileHandler extends LineFileHandler {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * Creates a new instance of {@link PropertiesFileHandler}. The constuctor 
     * simply falls back to the
     * {@link LineFileHandler#LineFileHandler(Pattern, Pattern, String)} passing in
     * the parameters relevant to properties files.
     */
    public PropertiesFileHandler() {
        super(COMMENT_PATTERN,
                IGNORE_PATTERN,
                COMMENT_PREFIX);
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
        
        return file.getName().endsWith(".properties"); // NOI18N
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    /**
     * The regular expression pattern which matches the line which is a comment.
     */
    private static final Pattern COMMENT_PATTERN = Pattern.compile(
            "^\\s*#.*"); // NOI18N
    
    /**
     * The regular expression pattern which matches the line which should be
     * ignored.
     */
    private static final Pattern IGNORE_PATTERN = Pattern.compile(
            "^$"); // NOI18N
    
    /**
     * The prefix which should be used for each line in the comment.
     */
    private static final String COMMENT_PREFIX =
            "# "; // NOI18N
}
