/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
