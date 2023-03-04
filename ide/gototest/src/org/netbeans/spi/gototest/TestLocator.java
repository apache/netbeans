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

package org.netbeans.spi.gototest;

import org.openide.filesystems.FileObject;

/**
 * Interface implemented by actions that can locate a test for a given source file,
 * or vice versa.
 *
 * @author Tor Norbye
 */
public interface TestLocator {
    enum FileType { 
        /** The file is a test class */
        TEST, 
        /** The file is a tested class */
        TESTED,
        /** The file is neither a test nor a tested file */
        NEITHER 
    }
    
    /** 
     * Return whether this TestLocator applies for the given file.
     * Only one TestLocator should apply for a given filetype.
     * 
     * @param fo The FileObject to be considered
     * @return True iff the given file object is applicable for this TestLocator
     */
    boolean appliesTo(FileObject fo);

    /**
     * This method determines whether the file-search mechanism should be synchronous
     * or asynchronous. If asynchronous, {@link #findOpposite(FileObject, int)} will be called
     * to compute the result directly. If it is false, {@link #findOpposite(FileObject, int, LocationListener)}
     * will be called instead.
     * 
     * @return True iff the search should be asynchronous, where {@link findOpposite(FileObject, int, LocationListener)}
     *   is called.
     */
    boolean asynchronous();

    /**
     * Compute the opposite file's location.
     * @param fo The FileObject to search for an opposite file for
     * @param caretOffset The caret offset in the current file, or -1 if unknown.
     *    Can be used to aid finding the opposite file. (For example, a TestLocator
     *    implementation can use it to determine the class around the caret offset
     *    and then use its own code index to locate a corresponding class based on
     *    class patterns rather than file names and locations.
     * @return The {@link Location} of the opposite file, or {@link Location#NONE} if
     *   no such file can be found
     */
    LocationResult findOpposite(FileObject fo, int caretOffset);
    
    /**
     * Compute the opposite file's location. This method will only be called
     * if {@link #asynchronous} is true. When the result is found, the implementation
     * of this method should call the given callback with the correct location
     * or error message.
     * @param fo The file object whose opposite file we want to find
     * @param caretOffset The caret offset in the current file, or -1 if unknown.
     *    Can be used to aid finding the opposite file. (For example, a TestLocator
     *    implementation can use it to determine the class around the caret offset
     *    and then use its own code index to locate a corresponding class based on
     *    class patterns rather than file names and locations.
     * @param callback The callback to call when the opposite file is found or an
     *   appropriate error message is known.
     */
    void findOpposite(FileObject fo, int caretOffset, LocationListener callback);

    /**
     * Decide what type of file is being edited (which will be used to enable either
     * the Go To Test action or the Go To Tested action). 
     * For a JUnit test for example, the {@link FileType#TEST} FileType should be returned
     * and as a consequence the "Go To Tested Class" action will be enabled.
     * This method will only be called for files that {@link #appliesTo} this FileObject.
     *
     * @param fo The FileObject currently being edited or selected
     * @return {@link FileType#TEST} for files that are tests,
     *    {@link FileType#TESTED} for files that aren't tests but (probably) have associated
     *    tests, and {@link FileType#NEITHER} for all other files where the Go To 
     *    action will be disabled.
     */
    FileType getFileType(FileObject fo);
    
    /**
     * Interface implemented by findOpposite callbacks. If findOpposite needs
     * to work asynchronously, it can return null instead of a location, and operate
     * on the callback instead.
     */
    public interface LocationListener {
        void foundLocation(FileObject fo, LocationResult location);
    }
    
    /**
     * A class to hold the location of a test or tested class; objects of this type
     * are returned from {@link TestLocator@findOpposite}.
     */
    public static class LocationResult {
        private FileObject file;
        private int offset;
        private String error;

        /**
         * Construct a Location from a given file and offset.
         * @param file The FileObject of the opposite file.
         * @param offset The offset in the file, or -1 if the offset
         *   is unknown.
         */
        public LocationResult(FileObject file, int offset) {
            this.file = file;
            this.offset = offset;
        }
        
        /**
         * Construct an invalid location with a given error message.
         * @param error The error message
         */
        public LocationResult(String error) {
            this.error = error;
        }

        /**
         * Get the FileObject associated with this location
         * @return The FileObject for this location, or null if 
         *   this is an invalid location. In that case, consult
         *   {@link #getErrorMessage} for more information.
         */
        public FileObject getFileObject() {
            return file;
        }

        /**
         * Get the offset associated with this location, if any.
         * @return The offset for this location, or -1 if the offset
         *   is not known.
         */
        public int getOffset() {
            return offset;
        }
        
        /**
         * Return the error message for the failure to find a given file
         * location.
         * @return The localized error message
         */
        public String getErrorMessage() {
            return error;
        }
    }
}
