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
package org.netbeans.api.editor.fold;

import java.util.Map;
import org.netbeans.modules.editor.fold.CustomFoldManager;
import org.netbeans.spi.editor.fold.ContentReader;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldManagerFactory;
import org.netbeans.modules.editor.fold.JavadocReader;

/**
 * This utility class collects APIs to create default implementations
 * of various pieces of infrastructure.
 * 
 * @author sdedic
 * @since 1.35
 */
public final class FoldingSupport {
    private FoldingSupport() {}

    /**
     * Creates a user-defined fold manager, that processes specific token type.
     * The manager tries to find start/end markers within the token text. If found,
     * a Fold is created. The manager only looks in tokens, whose {@code primaryCategory}
     * starts with a String, which is stored under 'tokenId' key in the params map.
     * <p/>
     * The method is designed to be called from the filesystem layer as follows:
     * <code><pre>
     * &lt;file name="my-custom-foldmanager.instance">
     * &lt;attr name="instanceCreate" methodvalue="org.netbeans.api.editor.fold.FoldUtilities.createUserFoldManager"/>
     * &lt;attr name="tokenid" stringvalue="comment"/>
     * &lt;/file>
     * </pre></code>
     *
     * @param map the configuration parameters.
     * @return FoldManagerFactory instance
     */
    public static FoldManagerFactory userFoldManagerFactory(Map params) {
        final String s = (String) params.get("tokenId");
        return new FoldManagerFactory() {
            @Override
            public FoldManager createFoldManager() {
                return userFoldManager(s);
            }
        };
    }
    
    /**
     * Creates a user-defined fold manager, that processes specific token type.
     * The manager tries to find start/end markers within the token text. If found,
     * a Fold is created. The manager only looks in tokens, whose {@code primaryCategory}
     * starts with tokenId string.
     * <p/>
     * {@code Null} value of 'tokenId' means the default "comment" will be used.
     *
     * @param tokenId filter for prefix of the token's primaryCategory.
     * @return FoldManager instance
     */
    public static FoldManager userFoldManager(String tokenId) {
        if (tokenId != null) {
            return new CustomFoldManager(tokenId);
        } else {
            return new CustomFoldManager();
        }
    }

    /**
     * Creates a default implementation of {@link ContentReader}.
     * 
     * The default implementation is modeled to work with Javadoc-like comments. It will
     * ignore markers at line start (first non-whitespace) - 'start' parameter. If the reader
     * encounters the 'stop' regex pattern, it stops scanning and returns {@code null}. Typically
     * some tags are placed at the end of the doc comment, and they are not informative enough
     * to put them into folded preview.
     * <p/>
     * Finally, if a suitable content is found, and it contains the 'terminator' pattern,
     * the content is only returned up to (excluding) the terminator.
     * <p/>
     * Javadoc (PHPdoc) reader can be constructed as <code>defaultReader("*", "\\.", "@");</code>
     *
     * @param start character sequence, which will be ignored at the beginning of the line. Can be {@code null}
     * @param terminator pattern, which marks the end of the summary line/sentence. Can be {@code null}
     * @param stop content search stop at the 'stop' pattern. Can be {@code null}
     */
    public static ContentReader contentReader(String start, String terminator, String stop, String prefix) {
        return new JavadocReader(start, terminator, stop, prefix);
    }

    /**
     * A variant of {@link #defaultReader} usable from FS layer.
     * See {@link #defaultReader} documentation for parameter explanation. The 
     * method expects those parameters as keys in the Map (values are all Strings).
     * An additional entry (key: 'type') is required in the map, it identifies the
     * FoldType for which the reader should work.
     * 
     * @param m configuration parameters for the reader factory.
     * @see #contentReader
     */
    public static ContentReader.Factory contentReaderFactory(Map m) {
        final String start = (String) m.get("start"); // NOI18N
        final String terminator = (String) m.get("terminator"); // NOI18N
        final String stop = (String) m.get("stop"); // NOI18N
        final String foldType = (String) m.get("type"); // NOI18N
        final String prefix = (String)m.get("prefix"); // NOI18N
        return new ContentReader.Factory() {
            @Override
            public ContentReader createReader(FoldType ft) {
                if (foldType != null && foldType.equals(ft.code()) || (foldType == null && ft.isKindOf(FoldType.DOCUMENTATION))) {
                    return contentReader(start, terminator, stop, prefix);
                } else {
                    return null;
                }
            }
        };
    }

}
