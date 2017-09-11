/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.spi.editor.fold;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldTemplate;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.spi.editor.mimelookup.MimeLocation;

/**
 * Callback interface, which is called to extract description for a fold.
 * The Reader will be called to produce a preview text to be displayed in the
 * folded view. The reader will be called under document lock, although the fold
 * hierarchy will not be locked. You may query the fold properties, but may not
 * traverse the fold hierarchy.
 * <p/>
 * An instance of ContentReader is used repeatedly, i.e. if the same FoldTemplate
 * is assigned to multiple folds. It is advised that the implementation of ContentReader
 * is stateless.
 * 
 * @author sdedic
 * @since 1.35
 */
public interface ContentReader {
    /**
     * Acquires text for fold content.
     * The method is executed under <i>read lock</i> on the Document. However, the Fold Hierarchy
     * is not locked. Accessing fold offsets should be safe, but relationships with other Folds
     * (parents, children, root) are not guarded.
     * <p/>
     * If the ContentReader cannot extract the contents (i.e. it does not want to handle the fold), 
     * {@code null} may be returned. If more ContentReaders are registered, some other instance might
     * handle the fold properly. If not, the placeholder will be retained and presented in the fold 
     * text.
     * 
     * @param d the document to read from
     * @param f fold, whose contents should be read
     * @param ft the original fold template, if available
     * @return content to be displayed in the folded view, or {@code null} if unwilling to retrieve the content.
     */
    public CharSequence read(Document d, Fold f, FoldTemplate ft) throws BadLocationException;

    /**
     * Factory, which produces ContentReader instance(s) appropriate for the fold type.
     * The returned instance may be used to read contents for all folds of the given type, in 
     * different documents (of the same mime type).
     */
    @MimeLocation(subfolderName = "FoldManager")
    public interface Factory {
        /**
         * @param ft the fold type
         * @return ContentReader instance or {@code null}, if content should not be presented in the fold preview.
         */
        public ContentReader    createReader(FoldType ft);
    }
}
