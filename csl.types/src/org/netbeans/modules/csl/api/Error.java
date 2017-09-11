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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.csl.api;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.filesystems.FileObject;

/**
 * This represents an error registered for the current java source,
 * possibly with associated fix proposals.
 *
 * @todo Add a getArgs() method etc. such that error messages can be parameterized; see javax.tools.DiagnosticMessage
 *
 * @author Tor Norbye
 */
public interface Error  {
    /**
     * Provide a short user-visible (and therefore localized) description of this error
     */
    @NonNull
    String getDisplayName();

    /**
     * Provide a full sentence description of this item, suitable for display in a tooltip
     * for example
     */
    @CheckForNull
    String getDescription();

    /**
     * Return a unique id/key for this error, such as "compiler.err.abstract.cant.be.instantiated".
     * This key is used for error hints providers.
     */
    @CheckForNull
    String getKey();
    
    ///** 
    // * Get the fixes associated with this error 
    // */
    //Collection<Fix> getFixes();
    //
    ///** 
    // * Register a fix proposal for this error 
    // */
    //void addFix(Fix fix);
    
    /**
     * Get the file object associated with this error, if any
     */
    @CheckForNull
    FileObject getFile();

    /**
     * Get the position of the error in the parsing input source (in other words,
     * this is the AST-based offset and may need translation to obtain the document
     * offset in documents with an embedding model.)
     */
    int getStartPosition();
    
    /**
     * Get the end offset of the error in the parsing input source (in other words,
     * this is the AST-based offset and may need translation to obtain the document
     * offset in documents with an embedding model.).
     * 
     * @return The end position, or -1 if unknown.
     */
    int getEndPosition();
    
    /**
     * Defines the way how an error annotation for this error behaves in the editor.
     * 
     * @return true if the error annotation should span over the whole line, false if 
     * the annotation is restricted exactly by the range defined by getStart/EndPostion() 
     */
    boolean isLineError();

    /**
     *  Get the severity of this error
     */
    @NonNull
    Severity getSeverity();
    
    /**
     * Return optional parameters for this message. The parameters may
     * provide the specific unknown symbol name for an unknown symbol error,
     * etc.
     */
    @CheckForNull
    Object[] getParameters();

    /**Error that may be used to show error badge in the projects tab.
     * @since 1.18
     */
    public interface Badging extends Error {
        /**Whether or not the error should be used to show error badge in the projects tab.
         *
         * @return true if this error should be used to show error badge in the projects tab.
         */
        public boolean showExplorerBadge();
    }
}
