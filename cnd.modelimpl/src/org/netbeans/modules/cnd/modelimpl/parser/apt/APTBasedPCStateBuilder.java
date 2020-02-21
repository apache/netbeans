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
package org.netbeans.modules.cnd.modelimpl.parser.apt;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.modelimpl.accessors.CsmCorePackageAccessor;
import org.netbeans.modules.cnd.modelimpl.csm.core.FilePreprocessorConditionState;
import org.netbeans.modules.cnd.modelimpl.parser.apt.APTParseFileWalker;

/**
 *
 */
public final class APTBasedPCStateBuilder implements APTParseFileWalker.EvalCallback {
    private final SortedSet<int[]> blocks = new TreeSet<>(COMPARATOR);
    private final CharSequence name;

    public APTBasedPCStateBuilder(CharSequence name) {
        this.name = name;
    }

    /*package*/
    final APTBasedPCStateBuilder addBlockImpl(int startDeadBlock, int endDeadBlock) {
        assert endDeadBlock >= startDeadBlock : "incorrect offsets " + startDeadBlock + " and " + endDeadBlock; // NOI18N
        if (endDeadBlock > startDeadBlock) {
            blocks.add(new int[]{startDeadBlock, endDeadBlock});
        }
        return this;
    }

    private void addDeadBlock(APT startNode, APT endNode) {
        if (startNode != null && endNode != null) {
            int startDeadBlock = startNode.getEndOffset();
            int endDeadBlock = endNode.getOffset() - 1;
            addBlockImpl(startDeadBlock, endDeadBlock);
        }
    }

    /**
     * Implements APTParseFileWalker.EvalCallback -
     * adds offset of dead branch to offsets array
     */
    @Override
    public void onErrorDirective(APT apt) {
        // on error directive we add special dead block
        addBlockImpl(FilePreprocessorConditionState.ERROR_DIRECTIVE_MARKER - apt.getToken().getOffset(), FilePreprocessorConditionState.ERROR_DIRECTIVE_MARKER);
    }

    /**
     * Implements APTParseFileWalker.EvalCallback - adds offset of dead
     * branch to offsets array
     */
    @Override
    public void onPragmaOnceDirective(APT apt) {
        // on pragma once directive we add dead block from pragma till the end
        addBlockImpl(apt.getToken().getOffset(), FilePreprocessorConditionState.PRAGMA_ONCE_DIRECTIVE_MARKER);
    }

    /**
     * Implements APTParseFileWalker.EvalCallback -
     * adds offset of dead branch to offsets array
     */
    @Override
    public void onEval(APT apt, boolean result) {
        if (result) {
            // if condition was evaluated as 'true' check if we
            // need to mark siblings as dead blocks
            APT start = apt.getNextSibling();
            while (start != null) {
                APT end = start.getNextSibling();
                if (end != null) {
                    switch (end.getType()) {
                        case APT.Type.ELIF:
                        case APT.Type.ELSE:
                            addDeadBlock(start, end);
                            // continue
                            start = end;
                            break;
                        case APT.Type.ENDIF:
                            addDeadBlock(start, end);
                            // stop
                            start = null;
                            break;
                        default:
                            // stop
                            start = null;
                            break;
                    }
                } else {
                    break;
                }
            }
        } else {
            // if condition was evaluated as 'false' mark it as dead block
            APT end = apt.getNextSibling();
            if (end != null) {
                switch (end.getType()) {
                    case APT.Type.ELIF:
                    case APT.Type.ELSE:
                    case APT.Type.ENDIF:
                        addDeadBlock(apt, end);
                        break;
                }
            }
        }
    }

    public FilePreprocessorConditionState build() {
        int size = 0;
        for (int[] deadInterval : blocks) {
            size++;
            if (deadInterval[1] == FilePreprocessorConditionState.ERROR_DIRECTIVE_MARKER) {
                break;
            }
        }
        int[] offsets = new int[size * 2];
        int index = 0;
        for (int[] deadInterval : blocks) {
            offsets[index++] = deadInterval[0];
            offsets[index++] = deadInterval[1];
            if (deadInterval[1] == FilePreprocessorConditionState.ERROR_DIRECTIVE_MARKER) {
                break;
            }
        }
        return FilePreprocessorConditionState.build(this.name, offsets);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (name != null) {
            sb.append(name);
        }
        sb.append("["); //NOI18N
        int i = 0;
        for (int[] deadInterval : blocks) {
            if (i++ > 0) {
                sb.append("]["); //NOI18N
            }
            if (deadInterval[1] == FilePreprocessorConditionState.ERROR_DIRECTIVE_MARKER) {
                sb.append(FilePreprocessorConditionState.ERROR_DIRECTIVE_MARKER - deadInterval[0]);
                sb.append("#error"); //NOI18N
            } else if (deadInterval[1] == FilePreprocessorConditionState.PRAGMA_ONCE_DIRECTIVE_MARKER) {
                sb.append(deadInterval[0]);
                sb.append("#pragma once"); //NOI18N
            } else {
                sb.append(deadInterval[0]);
                sb.append("-"); //NOI18N
                sb.append(deadInterval[1]);
            }
        }
        sb.append("]"); //NOI18N
        return sb.toString();
    }
    private static final Comparator<int[]> COMPARATOR = new Comparator<int[]>() {
        @Override
        public int compare(int[] segment1, int[] segment2) {
            return segment1[0] - segment2[0];
        }
    };
    
}
