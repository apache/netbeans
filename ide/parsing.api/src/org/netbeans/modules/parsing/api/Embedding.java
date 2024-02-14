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

package org.netbeans.modules.parsing.api;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.netbeans.api.editor.mimelookup.MimePath;


/**
 * Represents one block of code embedded in some other source. Performance 
 * is the only purpose of this class. You can obtain some basic information 
 * about embedded block of code before it is really created.
 *
 * Following example shows how to create compound Embedding from some snapshot:
 * 
 * <pre> 
 *         Embedding compoundEmbedding = Embedding.create (Arrays.asList (new Source[] {
 *             snapshot.create ("some prefix code", "text/x-java"),
 *             snapshot.create (10, 100, "text/x-java"),
 *             snapshot.create ("some postfix code", "text/x-java")
 *         })));
 * </pre>
 * 
 * @author Jan Jancura
 */
public final class Embedding {
    
    /**
     * Creates {@link Embedding} from a list of embeddings. All embeddings 
     * have to be created from one Smapshot. All embeddings must have the same 
     * mime type, but this mime type have to be different than current 
     * embedding mime types.
     * 
     * @param embeddings    A list of some embeddings created from one source.
     * @return              A new embedding compound from given pieces.
     * @throws IllegalArgumentException
     *                      if embeddings collection is empty, or
     *                      mime types of embeddings are not same.
     * @throws NullPointerException
     *                      embedding is null.
     */
    public static Embedding create (
        List<Embedding>        embeddings
    ) {
        if (embeddings.isEmpty ()) throw new IllegalArgumentException ();
        MimePath mimePath = null;
        Source source = null;
        StringBuilder sb = new StringBuilder ();
        List<int[]> currentToOriginal = new ArrayList<int[]> ();
        List<int[]> originalToCurrent = new ArrayList<int[]> ();
        int offset = 0;
        for (Embedding embedding : embeddings) {
            Snapshot snapshot = embedding.getSnapshot ();
            if (mimePath != null) {
                if (!mimePath.equals (embedding.mimePath)) {
                    throw new IllegalArgumentException ();
                }
                if (source != snapshot.getSource ()) {
                    throw new IllegalArgumentException ();
                }
            } else {
                mimePath = embedding.mimePath;
                source = snapshot.getSource ();
            }
            sb.append (snapshot.getText ());
            int[][] p = snapshot.currentToOriginal;
            for (int i = 0; i < p.length; i++) {
                if (currentToOriginal.isEmpty () ||
                    currentToOriginal.get (currentToOriginal.size () - 1) [1] != -1 ||
                    p [i] [1] != -1
                )
                    currentToOriginal.add (new int[] {p [i] [0] + offset, p [i] [1]});
                if (p [i] [1] >= 0) {
                    if (!originalToCurrent.isEmpty () &&
                        originalToCurrent.get (originalToCurrent.size () - 1) [1] >= 0
                    )
                        originalToCurrent.add (new int[] {
                            originalToCurrent.get (originalToCurrent.size () - 1) [0] +
                                p [i] [0] + offset -
                                originalToCurrent.get (originalToCurrent.size () - 1) [1],
                            -1
                        });
                    originalToCurrent.add (new int[] {p [i] [1], p [i] [0] + offset});
                } else 
                if (!originalToCurrent.isEmpty () &&
                    originalToCurrent.get (originalToCurrent.size () - 1) [1] >= 0
                ) {
                    originalToCurrent.add (new int[] {
                        originalToCurrent.get (originalToCurrent.size () - 1) [0] + 
                            p [i] [0] + offset - 
                            originalToCurrent.get (originalToCurrent.size () - 1) [1], 
                        -1
                    });
                }
            }
            offset +=snapshot.getText ().length ();
        }
        if (originalToCurrent.size() > 0 && originalToCurrent.get (originalToCurrent.size () - 1) [1] >= 0) {
            originalToCurrent.add (new int[] {
                originalToCurrent.get (originalToCurrent.size () - 1) [0] + 
                    sb.length () - 
                    originalToCurrent.get (originalToCurrent.size () - 1) [1], 
                -1
            });
        }
        originalToCurrent.sort(TMS_VCLV);
        Snapshot snapshot = Snapshot.create (
            sb,
            null,
            source,
            mimePath,
            currentToOriginal.toArray (new int [0] []),
            originalToCurrent.toArray (new int [0] [])
        );
        return new Embedding (
            snapshot, 
            mimePath
        );
    }
    
    private Snapshot        snapshot;
    private MimePath        mimePath;
                
    Embedding (
        Snapshot            snapshot,
        MimePath            mimePath
    ) {
        this.snapshot =     snapshot;
        this.mimePath =     mimePath;
    }
    
    /**
     * Returns {@link Snapshot} for embedded block of code.
     * 
     * @return              A {@link Snapshot} for embedded block of code..
     */
    public final Snapshot getSnapshot () {
        return snapshot;
    }
    
    /**
     * Returns mime type of embedded source.
     * 
     * @return              A mime type of embedded source.
     */
    public final String getMimeType () {
        return mimePath.getMimeType (mimePath.size () - 1);
    }
    
    /**
     * Returns <code>true</code> if this embedding contains given offset related
     * to top level source.
     * 
     * @param originalOffset
     *                      A offset in original source.
     * @return              <code>true</code> if this embedding contains given offset
     */
    public final boolean containsOriginalOffset (int originalOffset) {
	return snapshot.getEmbeddedOffset (originalOffset) >= 0;
    }

    @Override
    public String toString () {
        return "Embedding (" + getMimeType () + ", " + getSnapshot () + ")";
    }

    private static final Comparator<int[]> TMS_VCLV = new Comparator<int[]> () {

        public int compare (int[] o1, int[] o2) {
            return o1 [0] - o2 [0];
        }
    };
}




