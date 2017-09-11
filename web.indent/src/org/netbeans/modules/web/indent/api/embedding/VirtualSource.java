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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.indent.api.embedding;

import javax.swing.text.Document;

/**
 * Virtual source generated for a language from a document. The purpose of virtual
 * source is to extract individual parts of given language from a document
 * and amend such a source to be syntactically correct as much as possible.
 * 
 * @since org.netbeans.modules.css.editor/1 1.3
 */
public interface VirtualSource {

    /**
     * Returns text for given start and end offset from virtual source.
     * @param startOffset start offset
     * @param endOffset end offset
     * @return text lying within given range or null if there is none
     */
    String getSource(int startOffset, int endOffset);

    /**
     * Factory creating virtual source of given mime from a document.
     */
    public interface Factory {

        /**
         * Create virtual source of specified MIME type from given document.
         * @param doc document to extract virtual source from
         * @param mimeOfInterest MIME type which should be extracted from document
         * @return instance of virtual source or null factory does not know
         *  how to extract requested MIME type from given document
         */
        VirtualSource createVirtualSource(Document doc, String mimeOfInterest);
        
    }
}
