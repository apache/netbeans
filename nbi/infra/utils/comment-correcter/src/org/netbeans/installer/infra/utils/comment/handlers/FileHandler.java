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
import java.io.IOException;

/**
 * The interface that all file handlers, i.e. classes handling comment correction 
 * for concrete file types, should implement. 
 * 
 * <p>
 * It requires the implementing classes to provide functionality of loading/saving a 
 * file, extracting the current initial comment, iserting a new one and updating 
 * (replacing) the current.
 * 
 * @author Kirill Sorokin
 */
public interface FileHandler {
    /**
     * Checks whether the given ile can be processed by this file handler.
     * 
     * @param file The file for which to run the compatibility check.
     * @return <code>true</code> if the current file handler is capable of handling 
     *      the file, <code>false</code> otherwise.
     */
    boolean accept(final File file);
    
    /**
     * Loads the file into file handler's cache.
     * 
     * @param file The file to load.
     * @throws java.io.IOException if an I/O error occurs.
     * @throws java.lang.IllegalArgumentException if the parameter validation fails.
     */
    void load(final File file) throws IOException;
    
    /**
     * Saves the cached file contents to the given file on disk.
     * 
     * @param file The file to which the cache should be saved.
     * @throws java.io.IOException if an I/O error occurs.
     * @throws java.lang.IllegalArgumentException if the parameter validation fails.
     * @throws java.lang.IllegalStateException if the file contents cache 
     *      is <code>null</code>.
     */
    void save(final File file) throws IOException;
    
    /**
     * Extracts the current initial comment from the cached file contents.
     * 
     * @return Teh current initial comment or <code>null</code> if the initial 
     *      comment does not exist.
     * @throws java.lang.IllegalStateException if the file handler does not have 
     *      anything loaded.
     */
    String getCurrentComment();
    
    /**
     * Constructs the correct initial comment.
     * 
     * @param text The text of the initial comment.
     * @param lineLength The desired line length for the comment.
     * @return The correct, formatted, initial comment for this type of file.
     */
    String getCorrectComment(final String text, final int lineLength);
    
    /**
     * Inserts the initial comment to the cached file contents. If an intiial 
     * comment already exists in the file it is prepended by the new one.
     * 
     * @param text The text of the new initial comment.
     * @param lineLength The desired line length for the comment.
     * @throws java.lang.IllegalArgumentException if the parameters validation 
     *      fails.
     * @throws java.lang.IllegalStateException if the file handler does not have 
     *      anything loaded.
     */
    void insertComment(final String text, final int lineLength);
    
    /**
     * Updates the current initial comment in the cached file contents. If there is
     * no initia comment, then this method falls back to 
     * {@link #insertComment(String, int)}.
     * 
     * @param text The text of the new initial comment.
     * @param lineLength The desired line length for the comment.
     * @throws java.lang.IllegalArgumentException if the parameters validation 
     *      fails.
     * @throws java.lang.IllegalStateException if the file handler does not have 
     *      anything loaded.
     */
    void updateComment(final String text, final int lineLength);
}
