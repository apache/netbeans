/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.csl.api;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.csl.api.CodeCompletionHandler.QueryType;
import org.netbeans.modules.csl.spi.ParserResult;

/**
 * This class provides context regarding a code completion request. The infrastructure
 * will subclass this class and pass an instance to you.
 * 
 * @author Tor Norbye
 */
public abstract class CodeCompletionContext {

    /** 
     * The caret offset where we want completion 
     * @return The caret offset where we want cmpletion
     */
    public abstract int getCaretOffset();

//    /** 
//     * The compilation info for this file 
//     * @return The compilation info for this file
//     */
//    @NonNull
//    public abstract CompilationInfo getInfo();
    
    public abstract ParserResult getParserResult ();

    /** 
     * The prefix computed for this caret offset (as determined by your own {@link #getPrefix()} method 
     * @return The prefix computed for this caret offset
     */
    @NonNull
    public abstract String getPrefix();

    /**
     * The type of query to perform -- normal code completion for a popup list, or documentation
     * completion for a single item, or tooltip computation, etc.
     * @return The type of query to perform
     */
    @NonNull
    public abstract QueryType getQueryType();

    /**
     * Whether the search should match prefixes or whole identifiers.
     * @return If <code>true</code> the search should match <code>getPrefix</code> against
     *   the beginnig of identifiers. If <code>false</code> the search should match
     *   <code>getPrefix</code> against the whole identifier.
     */
    public abstract boolean isPrefixMatch();
    
    /** Whether the search should be case sensitive.
     * @return Whether the search should be case sensitive
     * @todo This should be merged with the NameKind which already passes this information
     */
    public abstract boolean isCaseSensitive();
}
