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
     * <p>
     * The method is designed to be called from the filesystem layer as follows:
     * <pre>{@code
     * &lt;file name="my-custom-foldmanager.instance">
     * &lt;attr name="instanceCreate" methodvalue="org.netbeans.api.editor.fold.FoldUtilities.createUserFoldManager"/>
     * &lt;attr name="tokenid" stringvalue="comment"/>
     * &lt;/file>
     * }</pre>
     *
     * @param params the configuration parameters.
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
     * <p>
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
     * <p>
     * Finally, if a suitable content is found, and it contains the 'terminator' pattern,
     * the content is only returned up to (excluding) the terminator.
     * <p>
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
     * A variant of {@code defaultReader} usable from FS layer.
     * See {@code defaultReader} documentation for parameter explanation. The 
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
