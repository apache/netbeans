/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.text.Document;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 *
 * @author marekfukala
 */
public class Utils {

    /**
     * Saves the given document to its underlying {@link FileObject} if the document
     * is not opened in the nb editor, more formally if EditorCookie.getOpenedPanes() == null.
     * 
     * @param document
     * @throws IOException 
     */
    public static void saveDocumentIfNotOpened(Document document) throws IOException {

        Object o = document.getProperty(Document.StreamDescriptionProperty);
        if (o == null || !(o instanceof DataObject)) {
            return;
        }
        DataObject dobj = (DataObject) o;
        EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);
        if (ec != null && ec.getOpenedPanes() == null) {
            //file not open in any editor
            SaveCookie save = dobj.getLookup().lookup(SaveCookie.class);
            if (save != null) {
                save.save();
            }
        }
    }
    
    /**
     * Gets a {@link Document} instance for the given {@link FileObject}.
     * 
     * Formally it does EditorCookie.openDocument().
     * 
     * @param file
     * @return 
     */
    public static Document getDocument(FileObject file) {
        try {
            DataObject d = DataObject.find(file);
            EditorCookie ec = (EditorCookie) d.getLookup().lookup(EditorCookie.class);

            if (ec == null) {
                return null;
            }
            return ec.openDocument();
        } catch (IOException e) {
            return null;
        }
    }
    
        /**
     * Creates a new {@link Model} instance for the given {@link Source}.
     * 
     * @since 1.3
     * @param source
     * @return
     * @throws ParseException 
     */
    public static Model createCssSourceModel(Source source) throws ParseException {
        final AtomicReference<Model> model_ref = new AtomicReference<>();
        ParserManager.parse(Collections.singleton(source), new UserTask() {

            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                ResultIterator ri = WebUtils.getResultIterator(resultIterator, "text/css");
                if(ri != null) {
                    CssParserResult result = (CssParserResult)ri.getParserResult();
                    model_ref.set(Model.getModel(result));
                }
                
            }
        });
        return model_ref.get();
    }
    
    public static boolean isAttributeValueQuoted(CharSequence value) {
        if (value == null) {
            return false;
        }
        if (value.length() < 2) {
            return false;
        } else {
            return ((value.charAt(0) == '\'' || value.charAt(0) == '"')
                    && (value.charAt(value.length() - 1) == '\'' || value.charAt(value.length() - 1) == '"'));
        }
    }
}
