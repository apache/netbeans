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
package org.netbeans.api.lsp;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.lsp.HyperlinkLocationAccessor;
import org.netbeans.spi.lsp.HyperlinkLocationProvider;
import org.netbeans.spi.lsp.HyperlinkTypeDefLocationProvider;
import org.openide.filesystems.FileObject;

/**
 * Represents the target location of a hyperlink. Location is a range inside a
 * file object, such as a line inside a text file.
 *
 * @author Dusan Balek
 * @since 1.0
 */
public final class HyperlinkLocation {

    static {
        HyperlinkLocationAccessor.setDefault(new HyperlinkLocationAccessor() {
            @Override
            public HyperlinkLocation createHyperlinkLocation(FileObject fileObject, int startOffset, int endOffset) {
                return new HyperlinkLocation(fileObject, startOffset, endOffset);
            }
        });
    }

    private final FileObject fileObject;
    private final int startOffset;
    private final int endOffset;

    private HyperlinkLocation(@NonNull FileObject fileObject, int startOffset, int endOffset) {
        this.fileObject = fileObject;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    /**
     * Target file object of this hyperlink.
     *
     * @return file object
     *
     * @since 1.0
     */
    @NonNull
    public FileObject getFileObject() {
        return fileObject;
    }

    /**
     * The start offset of a hyperlink's target range.
     *
     * @return offset
     *
     * @since 1.0
     */
    public int getStartOffset() {
        return startOffset;
    }

    /**
     * The end offset of a hyperlink's target range.
     *
     * @return offset
     *
     * @since 1.0
     */
    public int getEndOffset() {
        return endOffset;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.fileObject);
        hash = 29 * hash + this.startOffset;
        hash = 29 * hash + this.endOffset;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final HyperlinkLocation other = (HyperlinkLocation) obj;
        if (this.startOffset != other.startOffset) {
            return false;
        }
        if (this.endOffset != other.endOffset) {
            return false;
        }
        if (!Objects.equals(this.fileObject, other.fileObject)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "HyperlinkLocation{" + "fileObject=" + fileObject + ", startOffset=" + startOffset + ", endOffset=" + endOffset + '}';
    }

    /**
     * Resolves a hyperlink at the given document offset and returns its target
     * location(s). Example usage can be illustrated by:
     * {@snippet file="org/netbeans/api/lsp/HyperlinkLocationTest.java" region="testHyperlinkResolve"}
     *
     * @param doc document on which to operate.
     * @param offset offset within document
     * @return target location(s)
     *
     * @since 1.0
     */
    @NonNull
    public static CompletableFuture<List<HyperlinkLocation>> resolve(@NonNull final Document doc, final int offset) {
        MimePath mimePath = MimePath.parse(DocumentUtilities.getMimeType(doc));
        CompletableFuture<HyperlinkLocation>[] futures = MimeLookup.getLookup(mimePath).lookupAll(HyperlinkLocationProvider.class).stream()
                .map(provider -> provider.getHyperlinkLocation(doc, offset)).toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(futures).thenApply(value -> {
            List<HyperlinkLocation> locations = new ArrayList<>(futures.length);
            for (CompletableFuture<HyperlinkLocation> future : futures) {
                HyperlinkLocation location = future.getNow(null);
                if (location != null && location.getFileObject() != null) {
                    locations.add(location);
                }
            }
            return locations;
        });
    }

    /**
     * Resolves a hyperlink at the given document offset and returns its target
     * type definition location(s). Example usage can be illustrated by:
     * {@snippet file="org/netbeans/api/lsp/HyperlinkLocationTest.java" region="testHyperlinkTypeDefResolve"}
     *
     * @param doc document on which to operate.
     * @param offset offset within document
     * @return target type definition location(s)
     *
     * @since 1.1
     */
    @NonNull
    public static CompletableFuture<List<HyperlinkLocation>> resolveTypeDefinition(@NonNull final Document doc, final int offset) {
        MimePath mimePath = MimePath.parse(DocumentUtilities.getMimeType(doc));
        CompletableFuture<HyperlinkLocation>[] futures = MimeLookup.getLookup(mimePath).lookupAll(HyperlinkTypeDefLocationProvider.class).stream()
                .map(provider -> provider.getHyperlinkTypeDefLocation(doc, offset)).toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(futures).thenApply(value -> {
            List<HyperlinkLocation> locations = new ArrayList<>(futures.length);
            for (CompletableFuture<HyperlinkLocation> future : futures) {
                HyperlinkLocation location = future.getNow(null);
                if (location != null) {
                    locations.add(location);
                }
            }
            return locations;
        });
    }
}
