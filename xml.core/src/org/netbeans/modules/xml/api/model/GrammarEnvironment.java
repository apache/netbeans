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

package org.netbeans.modules.xml.api.model;

import java.util.Enumeration;
import org.openide.filesystems.FileObject;
import org.xml.sax.InputSource;

/**
 * Grammar environment provides grammar factory with a context.
 * All returned object must be treated as read-only. Grammar
 * should do the best it can do in passed (possibly partial)
 * environment.
 *
 * @author  Petr Kuzel
 */
public final class GrammarEnvironment {

    private final FileObject fileObject;
    private final InputSource inputSource;
    private final Enumeration documentChildren;
        
    /** 
     * Creates a new instance of GrammarEnvironment.
     *
     * @param documentChildren Enumeration of document level DOM nodes.
     * @param inputSource Supported document input source.
     * @param fileObject  Supported document fileObject or <code<null</code>.
     */
    public GrammarEnvironment(Enumeration documentChildren, InputSource inputSource, FileObject fileObject) {
        if (inputSource == null) throw new NullPointerException();
        if (documentChildren == null) throw new NullPointerException();
        this.inputSource = inputSource;
        this.fileObject = fileObject;
        this.documentChildren = documentChildren;
    }
    
    /**
     * There is always input source of supported document reflecting
     * current in-memory state.
     *
     * @return InputSource
     */
    public InputSource getInputSource() {
        return inputSource;
    }
    
    /**
     * If supported document exists in a form of FileObject it can
     * be retrieved. Note that data provided from file object
     * input stream may be different than current in-memory state.
     *
     * @return FileObject or null.
     */
    public FileObject getFileObject() {
        return fileObject;
    }

    /**
     * Preparsed document children for fast decision based on
     * document structure.
     *
     * @return Enumeration of DOM Nodes representing document
     * children. Enumeration elements are valid only during
     * {@link GrammarQueryManager#enabled} method invocation.
     */
    public Enumeration getDocumentChildren() {
        return documentChildren;
    }
}
