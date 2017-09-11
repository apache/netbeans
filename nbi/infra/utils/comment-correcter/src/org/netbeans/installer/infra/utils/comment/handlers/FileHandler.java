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
