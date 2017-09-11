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
package org.openide.text;

import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

import java.io.Writer;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.swing.text.*;


/** Indentation engine for formating text in documents.
* Provides mapping between MIME types and engines, so anybody
* can find appropriate type of engine for type of document.
*
* @author Jaroslav Tulach
 * @deprecated Please use Editor Indentation API instead, for details see
 *   <a href="@org-netbeans-modules-editor-indent@/overview-summary.html">Editor Indentation</a>.
*/
@Deprecated
public abstract class IndentEngine extends org.openide.ServiceType {
    private static final long serialVersionUID = -8548906260608507035L;

    /** hashtable mapping MIME type to engine */
    private static Map<String,IndentEngine> map = new HashMap<String,IndentEngine>(7);
    private static IndentEngine INSTANCE = null;

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /** Indents the current line. Should not effect any other
    * lines.
    * @param doc the document to work on
    * @param offset the offset of a character on the line
    * @return new offset of the original character
    */
    public abstract int indentLine(Document doc, int offset);

    /** Inserts new line at given position and indents the new line with
    * spaces.
    *
    * @param doc the document to work on
    * @param offset the offset of a character on the line
    * @return new offset to place cursor to
    */
    public abstract int indentNewLine(Document doc, int offset);

    /** Creates writer that formats text that is inserted into it.
    * The writer should not modify the document but use the
    * provided writer to write to. Usually the underlaying writer
    * will modify the document itself and optionally it can remember
    * the current position in document. That is why the newly created
    * writer should do no buffering.
    * <P>
    * The provided document and offset are only informational,
    * should not be modified but only used to find correct indentation
    * strategy.
    *
    * @param doc document
    * @param offset position to begin inserts at
    * @param writer writer to write to
    * @return new writer that will format written text and pass it
    *   into the writer
    */
    public abstract Writer createWriter(Document doc, int offset, Writer writer);

    /** Allow subclasses to decide whether they are suitable
     * for the given mime-type or not.
     * @param mime mime-type string
     * @return true if this engine is suitable for the given mime-type.
     */
    protected boolean acceptMimeType(String mime) {
        return false;
    }

    /**
     * @deprecated IndentEngine now is a ServiceType
     */
    @Deprecated
    public synchronized static void register(String mime, IndentEngine eng) {
        map.put(mime, eng);
    }

    /** Returns enumeration of all registered indentation engines.
     * @return enumeration of IndentEngine
     */
    public static Enumeration<? extends IndentEngine> indentEngines() {
        return Collections.enumeration(Lookup.getDefault().lookupAll(IndentEngine.class));
    }

    /** Finds engine associated with given mime type.
    * If no engine is associated returns default one.
    */
    public synchronized static IndentEngine find(String mime) {
        Enumeration<? extends IndentEngine> en = indentEngines();

        while (en.hasMoreElements()) {
            IndentEngine eng = en.nextElement();

            if (eng.acceptMimeType(mime)) {
                return eng;
            }
        }

        IndentEngine eng = map.get(mime);

        if (eng != null) {
            return eng;
        }

        return getDefault();
    }

    /** Finds engine associated with given document.
    * If no engine is associated returns default one.
    */
    public static IndentEngine find(Document doc) {
        Object o = doc.getProperty("indentEngine"); // NOI18N

        if (o instanceof IndentEngine) {
            return (IndentEngine) o;
        } else {
            o = doc.getProperty("mimeType"); // NOI18N

            String s = (o instanceof String) ? (String) o : "text/plain"; // NOI18N

            return find(s);
        }
    }

    /** Returns a simple indentation engine that does no formatting. */
    public static synchronized IndentEngine getDefault() {
        if (INSTANCE == null) {
            INSTANCE = new Default();
        }

        return INSTANCE;
    }

    /** Default indentation engine.
    */
    private static final class Default extends IndentEngine {
        private static final long serialVersionUID = 4493180326470838469L;

        Default() {
        }

        public int indentLine(Document doc, int offset) {
            return offset;
        }

        public int indentNewLine(Document doc, int offset) {
            try {
                doc.insertString(offset, "\n", null); // NOI18N
            } catch (BadLocationException ex) {
                // ignore
            }

            return offset + 1;
        }

        public Writer createWriter(Document doc, int offset, Writer writer) {
            return writer;
        }

        @Override
        protected boolean acceptMimeType(String mime) {
            return true;
        }

        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(Default.class);
        }
    }
}
