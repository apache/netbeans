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

package org.netbeans.modules.cnd.highlight.error;

import java.util.Collection;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo;

/**
 * Makes an error in a document that contain source code.
 * It can be called several times on a document:
 * for example, each time it finds a ";" and deletes it
 */
public interface ErrorMaker {
    
    /**
     * Performs initialization.
     * Is called before any other method.
     * @param document a document
     * @param csmFile the corresponding source file. 
     * I believe most error makers will just ignore it;
     * but some more smart one may need it
     */
    void init(BaseDocument document, CsmFile csmFile) throws Exception;
    
//    /**
//     * Is called after each iteration to determine
//     * whether to stop or proceed.
//     * @return true to proceed, false to stop
//     */
//    boolean hasMore();
//    
    
    /**
     * If it can make next change (e.g., if next ";" is found),
     * it makes the change and returns true.
     * In this case change() will be called once more.
     * 
     * If there are no more places to change,
     * does nothing and returns false -
     * this means that the cycle is over.
     * 
     * @return true if a change has been done, otherwise false.
     */
    boolean change() throws Exception;

    /**
     * Analyzes the errors that were reported after last change
     * Is called as soon as a change has been done 
     * and errors has been got from provider.
     * @param errors
     */
    void analyze(Collection<CsmErrorInfo> errors) throws Exception;
    
    /**
     * Notifies that the last change was undone.
     * One of the typical scenarios is to isolate changes one from another.
     * For example, we delete semicolon, see what error highlighting says in this respect,
     * then undo deletion, delete next semicolon, etc.
     * In this case error maker should be notified that its last change was undone
     */
    void undone() throws Exception;
    
}
