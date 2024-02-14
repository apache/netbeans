/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.editor.indent.spi.support;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.spi.editor.typinghooks.TypedTextInterceptor;

/**
 * This class contains factory methods for typing interceptor implementations
 * that can be used for automated text indenting.
 *
 * <p>The interceptors provided by this class are implementations of Typing Hooks SPI
 * interfaces that can be registered in <code>MimeLookup</code>. Typically there are
 * two factory methods for each interceptor implementation. One factory method creates
 * the implementated interceptor and is suitable for direct use from java code. The
 * other factory method creates a factory object that can be registred in an XML layer
 * as an <code>.instance</code> file.
 *
 * @author Vita Stejskal
 * @since 1.11
 */
public final class AutomatedIndenting {

    /**
     * Creates <code>TypedTextInterceptor</code> that automatically
     * indents a line depending on text typed on that line.
     *
     * <p>The text patterns recognized by the intercetor are defined in form
     * of regular expressions passed to this method. The interceptor will match
     * all text before the caret on the line where a user is typing (including
     * the last typed character) against the regular expression patterns. If the text
     * matches at least one pattern the interceptor will reindent the line by
     * calling {@link Indent#reindent(int)} method.
     *
     * @param linePatterns The regular expressions that will be used for matching
     *   text typed on a line. Any matching pattern will trigger the line reindentation.
     *
     * @return The interceptor that checks text typed on a line and reindents the line
     *   if it matches any of the <code>linePatterns</code>.
     *
     * @since 1.11
     */
    public static TypedTextInterceptor createHotCharsIndenter(Pattern... linePatterns) {
        return new RegExBasedIndenter(linePatterns);
    }

    /**
     * This is a version of {@link #createHotCharsIndenter(java.util.regex.Pattern[])} method suitable
     * for XML layers registration.
     *
     * <div class="nonnormative">
     * <p>Here is an example of an XML layer registration done
     * for <code>text/x-java</code> mime type. The registered interceptor will indent
     * any line that contains whitespace followed by 'else'. The ending 'e' character is
     * the last character typed on the line.
     *
     * <pre>
     * &lt;folder name="Editors"&gt;
     *  &lt;folder name="text"&gt;
     *   &lt;folder name="x-java"&gt;
     *    &lt;file name="org-something-AutoIndenter.instance"&gt;
     *     &lt;attr name="instanceOf" stringvalue="org.netbeans.spi.editor.typinghooks.TypedTextInterceptor$Factory"/&gt;
     *     &lt;attr name="instanceCreate"
     *              methodvalue="org.netbeans.modules.editor.indent.spi.support.AutomatedIndenting.createHotCharsIndenter"/&gt;
     *     &lt;attr name="regex1" stringvalue="\s*else"/&gt;
     *    &lt;/file&gt;
     *   &lt;/folder&gt;
     *  &lt;/folder&gt;
     * &lt;/folder&gt;
     * </pre>
     * </div>
     *
     * @param fileAttributes The map of <code>FileObject</code> attributes. This method
     *   will recognize any attributes, which name starts with <code>regex</code> and will
     *   try to interpret their value as a regular expression. These regular expressions
     *   will then be used as <code>linePatterns</code> when calling <code>createHotCharsIndenter(Pattern...)</code> method.
     * 
     * @return The interceptor factory that will provide a regular expressions based
     *   automated indenter returned from the {@link #createHotCharsIndenter(java.util.regex.Pattern[])} method.
     *   The list of line patterns will be recovered from the <code>fileAttributes</code>.
     * 
     * @since 1.11
     */
    public static TypedTextInterceptor.Factory createHotCharsIndenter(Map<Object, Object> fileAttributes) {
        final ArrayList<Pattern> linePatterns = new ArrayList<Pattern>();

        for(Object key : fileAttributes.keySet()) {
            if (key.toString().startsWith("regex")) { //NOI18N
                Object value = fileAttributes.get(key);
                try {
                    Pattern pattern = Pattern.compile(value.toString());
                    linePatterns.add(pattern);
                } catch (PatternSyntaxException pse) {
                    LOG.log(Level.WARNING, null, pse);
                }
            }
        }

        return new TypedTextInterceptor.Factory() {
            public TypedTextInterceptor createTypedTextInterceptor(MimePath mimePath) {
                return createHotCharsIndenter(linePatterns.toArray(new Pattern [0]));
            }
        };
    }

    // ------------------------------------------------------------------------
    // private
    // ------------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(AutomatedIndenting.class.getName());
    
    private static final class RegExBasedIndenter implements TypedTextInterceptor {

        private final Pattern [] linePatterns;

        public RegExBasedIndenter(Pattern... linePatterns) {
            this.linePatterns = linePatterns;
        }

        public boolean beforeInsert(Context context) {
            // no-op
            return false;
        }

        public void insert(MutableContext context) {
            // no-op
        }

        public void afterInsert(Context context) {
            int textLen = context.getText().length();
            if (textLen > 0) {
                CharSequence lineText;
                final int lineStartOffset;
                final int lineEndOffset;

                try {
                    Element lineElement = DocumentUtilities.getParagraphElement(context.getDocument(), context.getOffset());
                    lineText = DocumentUtilities.getText(context.getDocument(),
                        lineElement.getStartOffset(),
                        context.getOffset() - lineElement.getStartOffset() + textLen);
                    lineStartOffset = lineElement.getStartOffset();
                    lineEndOffset = Math.max(lineStartOffset, lineElement.getEndOffset() - 1); // without EOL
                } catch (Exception e) {
                    LOG.log(Level.INFO, null, e);
                    return;
                }

                for(Pattern p : linePatterns) {
                    if (p.matcher(lineText).matches()) {
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine("The line '" + lineText + "' matches '" + p.pattern() //NOI18N
                                + "' -> calling Indent.reindent(" + lineStartOffset + ", " + lineEndOffset + ")"); //NOI18N
                        }

                        final Indent indenter = Indent.get(context.getDocument());
                        indenter.lock();
                        try {
                            runAtomicAsUser(context.getDocument(), new Runnable() {
                                public void run() {
                                    try {
                                        indenter.reindent(lineStartOffset, lineEndOffset);
                                    } catch (BadLocationException ble) {
                                        LOG.log(Level.INFO, null, ble);
                                    }
                                }
                            });
                        } finally {
                            indenter.unlock();
                        }
                        break;
                    }
                }
            }
        }

        public void cancelled(Context context) {
            // no-op
        }

        private static void runAtomicAsUser(Document doc, Runnable run) {
            try {
                Method runAtomicAsUserMethod = doc.getClass().getMethod("runAtomicAsUser", Runnable.class); //NOI18N
                runAtomicAsUserMethod.invoke(doc, run);
            } catch (Exception e) {
                LOG.log(Level.INFO, null, e);
            }
        }
    } // End of RegExBasedIndenter class
}
