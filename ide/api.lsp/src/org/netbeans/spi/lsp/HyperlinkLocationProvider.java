/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.spi.lsp;

import org.netbeans.api.lsp.HyperlinkLocation;
import java.util.concurrent.CompletableFuture;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.lsp.HyperlinkLocationAccessor;
import org.netbeans.spi.editor.mimelookup.MimeLocation;
import org.openide.filesystems.FileObject;

/**
 * Interface for resolving hyperlink locations. Implementations of the interface
 * should be registered in MimeLookup.
 * <pre>
 *
 *  {@code @MimeRegistration(mimeType = "text/foo", service = HyperlinkLocationProvider.class)
 *   public class FooHyperlinkLocationProvider implements HyperlinkLocationProvider {
 *     ...
 *   }
 *  }
 * </pre>
 *
 * @author Dusan Balek
 * @since 1.0
 */
@MimeLocation(subfolderName = "HyperlinkLocationProviders")
public interface HyperlinkLocationProvider {

    /**
     * Resolves a hyperlink at the given document offset and returns its
     * target location.
     *
     * @param doc document on which to operate.
     * @param offset offset within document
     * @return target location
     *
     * @since 1.0
     */
    CompletableFuture<HyperlinkLocation> getHyperlinkLocation(@NonNull Document doc, int offset);

    /**
     * Creates {@link HyperlinkLocation} instances.
     *
     * @param fileObject target file object of the hyperlink
     * @param startOffset start offset of the hyperlink's target range
     * @param endOffset end offset of the hyperlink's target range
     * @return new created instance
     *
     * @since 1.0
     */
    public static HyperlinkLocation createHyperlinkLocation(@NonNull FileObject fileObject, int startOffset, int endOffset) {
        return HyperlinkLocationAccessor.getDefault().createHyperlinkLocation(fileObject, startOffset, endOffset);
    }
}
