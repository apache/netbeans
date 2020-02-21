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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.utils.CndUtils;

/**
 * A class that tracks states of the preprocessor conditionals within file
 */
public final class FilePreprocessorConditionState {
    public static final int ERROR_DIRECTIVE_MARKER = Integer.MAX_VALUE;
    public static final int PRAGMA_ONCE_DIRECTIVE_MARKER = Integer.MAX_VALUE - 1;
    private static final int[] ALL_INCLUDED = new int[0];
    public static final FilePreprocessorConditionState PARSING = new FilePreprocessorConditionState("PARSING", new int[]{0, Integer.MAX_VALUE}); // NOI18N
    
    /** a SORTED array of blocks [start-end] for which conditionals were evaluated to false */
    private final int[] offsets;

    /** for debugging purposes */
    private final transient CharSequence fileName;

    // for builder only
    private FilePreprocessorConditionState(CharSequence fileName, int[] offsets) {
        this.offsets = (offsets != null && offsets.length == 0) ? ALL_INCLUDED : offsets;
        this.fileName = fileName;
    }
    
    public FilePreprocessorConditionState(RepositoryDataInput input) throws IOException {
        int size = input.readInt();
        if (size > 0) {
            offsets = new int[size];
            for (int i = 0; i < size; i++) {
                offsets[i] = input.readInt();
            }
        } else {
            offsets = ALL_INCLUDED;
        }
        fileName = null;
    }

    public void write(RepositoryDataOutput output) throws IOException {
        int size = offsets.length;
        output.writeInt(size);
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                output.writeInt(offsets[i]);
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FilePreprocessorConditionState other = (FilePreprocessorConditionState) obj;
        if (!Arrays.equals(this.offsets, other.offsets)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5 + Arrays.hashCode(this.offsets);
        return hash;
    }

    @Override
    public String toString() {
        return toStringBrief(this);
    }

    /*package*/ static String toStringBrief(FilePreprocessorConditionState state) {
        if (state == FilePreprocessorConditionState.PARSING) {
            return FilePreprocessorConditionState.PARSING.fileName.toString();
        }
        if (state == null) {
            return "null"; // NOI18N
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");//NOI18N
        for (int i = 0; i < state.offsets.length; i+=2) {
            if (i > 0) {
                sb.append("][");//NOI18N
            }
            if (state.offsets[i+1] == ERROR_DIRECTIVE_MARKER) {
                sb.append(ERROR_DIRECTIVE_MARKER-state.offsets[i]);
                sb.append("#error");//NOI18N
            } else if (state.offsets[i + 1] == PRAGMA_ONCE_DIRECTIVE_MARKER) {
                sb.append(state.offsets[i]);
                sb.append("#pragma once");//NOI18N
            } else {
                sb.append(state.offsets[i]);
                sb.append("-");//NOI18N
                sb.append(state.offsets[i+1]);
            }
        }
        sb.append("]");//NOI18N
        return sb.toString();
    }
    
    public int getActiveCoverage(int startContext, int endContext) {
        assert endContext >= startContext;
        int coverage = getCoverage(offsets, startContext, endContext);
        assert(coverage >= 0 || !isInActiveBlock(startContext, endContext));
        return coverage;
    }

    public boolean isInActiveBlock(int startContext, int endContext) {
        if (offsets.length == 0 || startContext == 0) {
            return true;
        }
        // TODO: improve speed, if needed, offsets are ordered
        for (int i = 0; i < offsets.length; i += 2) {
            int start = offsets[i];
            int end = offsets[i + 1];
            if (start <= startContext && startContext <= end) {
                return false;
            }
            if (start <= endContext && endContext <= end) {
                return false;
            }
        }
        return true;
    }

    /**
     * check if this state can be used to replace another (it is better or equal to another)
     * @param other
     * @return
     */
    public final boolean isBetterOrEqual(FilePreprocessorConditionState other) {
        if (other == null) {
            return false;
        }
        if (this.offsets.length == 0) {
            // can replace all
            return true;
        }
        if (other.offsets.length == 0) {
            // this is not empty, so it can not replace empty
            return false;
        }
        // check if all my blocks are inactive in terms of other (but not equal to them)
        for (int i = 0; i < offsets.length; i += 2) {
            int start = offsets[i];
            int end = offsets[i + 1];
            boolean active = true;
            for (int j = 0; j < other.offsets.length; j += 2) {
                int secondStart = other.offsets[j];
                int secondEnd = other.offsets[j + 1];
                if (secondStart <= start && end <= secondEnd) {
                    // not in active
                    active = false;
                } else if (start < secondStart && secondEnd < end) {
                    // our dead block is bigger
                    return false;
                }
                if (!active || (end < secondStart)) {
                    // we can stop, because blocks are sorted
                    break;
                }
            }
            // our block is active => we can't be the best
            if (active) {
                return false;
            }
        }
        return true;
    }

    public final List<CsmOffsetable> createBlocksForFile(CsmFile file) {
        List<CsmOffsetable> blocks = new ArrayList<>();
        for (int i = 0; i < offsets.length; i+=2) {
            blocks.add(org.netbeans.modules.cnd.modelimpl.csm.core.Utils.createOffsetable(file, offsets[i], offsets[i+1]));
        }
        return blocks;
    }

    public boolean isFromErrorDirective() {
        if (this.offsets.length == 0 || this == PARSING) {
            return false;
        }
        return offsets[offsets.length-1] == ERROR_DIRECTIVE_MARKER;
    }
    
    public static FilePreprocessorConditionState build(CharSequence name, int[] offsets) {
        // TODO: copy offsets?
        FilePreprocessorConditionState pcState = new FilePreprocessorConditionState(name, offsets);
        if (CndUtils.isDebugMode()) {
            checkConsistency(pcState);
        }
        return pcState;
    }

    static int[] getDeadBlocks(FilePreprocessorConditionState pcState) {
        // TODO: copy offsets?
        return pcState.offsets;
    }
    
    /**
     * Calculates covered area from start to end if there are excluded blocks.
     * NB! startContext and endContext shouldn't be at the border of a block
     * or inside any block. If they are, coverage is -1.
     * 
     * @param excluded - sorted blocks in format: [startOffset, endOffset], [startOffset, endOffset]...
     * @param start - start offset of area we are interested in
     * @param end - end offset of area we are interested in
     * @return size of covered area
     */
    static int getCoverage(int excluded[], int startContext, int endContext) {
        if (excluded.length == 0 || startContext == 0) {
            return endContext - startContext;
        }
        int index = Arrays.binarySearch(excluded, startContext);
        if (index >= 0) {
            // startContext is at the border of some block
            return -1;
        }
        index = -index - 1;
        if ((index & 1) != 0) {
            // startContext is inside some block
            return -1;
        }
        int coverage = endContext - startContext;
        for (int i = index; i < excluded.length; i += 2) {
            int start = excluded[i];
            int end = excluded[i + 1];
            if (endContext < start) {
                break;
            }
            if (start <= endContext && endContext <= end) {
                // check that endContext is not inside some block or at its border
                return -1;
            }
            coverage -= (end - start);
        }
        return coverage;
    }
    
    private static void checkConsistency(FilePreprocessorConditionState pcState) {
        // check consistency for ordering and absence of intersections
        if (pcState.offsets != null) {            
            for (int i = 0; i < pcState.offsets.length; i++) {
                if (i + 1 < pcState.offsets.length) {
                    if (!(pcState.offsets[i] < pcState.offsets[i + 1])) {
                        CndUtils.assertTrue(false, "inconsistent state " + pcState);  // NOI18N
                    }
                }
            }
        }
    }

    boolean isAllIncluded() {
        return this.offsets == ALL_INCLUDED;
    }
}
