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

package org.netbeans.modules.maven.execute.cmd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.maven.execution.ExecutionEvent;
import org.json.simple.JSONObject;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.windows.FoldHandle;
import org.openide.windows.IOFolding;
import org.openide.windows.IOPosition;
import org.openide.windows.InputOutput;

/**
 * a stub to be filled with parsed JSON values, vaguely related to ExecutionEventObject in maven codebase.
 * @author mkleint
 */

public class ExecutionEventObject {

    public final ExecutionEvent.Type type;

    public ExecutionEventObject(ExecutionEvent.Type type) {
        this.type = type;
    }


    public static class GAV {
        public final String groupId;
        public final String artifactId;
        public final String version;

        public GAV(@NonNull String groupId, @NonNull String artifactId, @NonNull String version) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
        }

        public String getId() {
            return groupId + ":" + artifactId + ":" + version;
        }

    }

    private static final List<ExecutionEvent.Type> mojo_types = Arrays.asList(new ExecutionEvent.Type[] {
        ExecutionEvent.Type.MojoStarted, ExecutionEvent.Type.MojoFailed, ExecutionEvent.Type.MojoSucceeded, ExecutionEvent.Type.MojoSkipped
    });
    private static final List<ExecutionEvent.Type> project_types = Arrays.asList(new ExecutionEvent.Type[] {
        ExecutionEvent.Type.ProjectStarted, ExecutionEvent.Type.ProjectFailed, ExecutionEvent.Type.ProjectSucceeded, ExecutionEvent.Type.ProjectSkipped
    });
    private static final List<ExecutionEvent.Type> session_types = Arrays.asList(new ExecutionEvent.Type[] {
        ExecutionEvent.Type.SessionStarted, ExecutionEvent.Type.SessionEnded
    });

    public static ExecutionEventObject create(JSONObject obj) {
        String s = (String) obj.get("type");
        ExecutionEvent.Type t = ExecutionEvent.Type.valueOf(s);
        if (mojo_types.contains(t)) {
            return ExecMojo.create(obj, t);
        }
        if (project_types.contains(t)) {
            return ExecProject.create(obj, t);
        }
        if (session_types.contains(t)) {
            return ExecSession.create(obj, t);
        }
        return new ExecutionEventObject(t);
    }

    public static class Tree {
        private final ExecutionEventObject startEvent;
        private ExecutionEventObject endEvent;
        private ExecutionEventObject.Tree parentNode;
        private final List<ExecutionEventObject.Tree> childrenNodes = new ArrayList<ExecutionEventObject.Tree>();
        private IOPosition.Position startOffset;
        private IOPosition.Position endOffset;
        private FoldHandle foldHandle;
        private FoldHandle innerOutputFoldHandle;

        public Tree(ExecutionEventObject current, ExecutionEventObject.Tree parent) {
            this.startEvent = current;
            this.parentNode = parent;
        }

        public @CheckForNull IOPosition.Position getStartOffset() {
            return startOffset;
        }

        public void setStartOffset(@NonNull IOPosition.Position startOffset) {
            this.startOffset = startOffset;
        }

        public @CheckForNull IOPosition.Position getEndOffset() {
            return endOffset;
        }

        public void setEndOffset(@NonNull IOPosition.Position endOffset) {
            this.endOffset = endOffset;
        }

        public void setEndEvent(ExecutionEventObject endEvent) {
            this.endEvent = endEvent;
            if (ExecutionEvent.Type.MojoStarted.equals(startEvent.type)) {
                ((ExecMojo)startEvent).setClasspathURLs(((ExecMojo)endEvent).getClasspathURLs());
            }
            assert endEvent != null && endEvent.getClass().equals(startEvent.getClass());
        }

        public @CheckForNull ExecutionEventObject getStartEvent() {
            return startEvent;
        }

        public @CheckForNull ExecutionEventObject getEndEvent() {
            return endEvent;
        }

        public @CheckForNull Tree getParentNode() {
            return parentNode;
        }

        public @NonNull List<Tree> getChildrenNodes() {
            return childrenNodes;
        }


        public ExecutionEventObject.Tree findParentNodeOfType(ExecutionEvent.Type startType) {
            if (parentNode == null) {
                return null;
            }
            ExecutionEventObject event = parentNode.getStartEvent();
            if (event == null) {
                return null;
            }
            if (startType.equals(event.type)) {
                return parentNode;
            }
            return parentNode.findParentNodeOfType(startType);
        }

        public void reassingParent(ExecutionEventObject.Tree parent) {
            this.parentNode = parent;
        }

        /**
         * Start fold for the curent tree.
         *
         * @param io InputOutput the output is written to.
         * @return true if the fold system is too broken to use
         */
        public boolean startFold(InputOutput io) {
            if (!IOFolding.isSupported(io)) {
                return false;
            }

            assert foldHandle == null;
            ExecutionEventObject.Tree parentProject = findParentNodeOfType(ExecutionEvent.Type.MojoStarted);
            if (parentProject != null) {
                //in forked environment..
                if (parentProject.foldHandle == null) {
                    return true;
                }
                this.foldHandle = parentProject.foldHandle.silentStartFold(true);
                return false;
            }

            parentProject = findParentNodeOfType(ExecutionEvent.Type.ProjectStarted);
            if (parentProject == null) {
                this.foldHandle = IOFolding.startFold(io, true);
                return false;
            } else {
                boolean broken = false;
                if (parentProject.foldHandle == null) {
                    broken = parentProject.startFold(io);
                }
                assert parentProject.foldHandle != null;
                this.foldHandle = parentProject.foldHandle.silentStartFold(true);
                return broken;
            }
        }

        /**
         * Finish fold for the current free. If no fold has been created, do
         * nothing. If a nested fold exists, finish it too.
         */
        public void finishFold() {
            if (foldHandle != null) {
                finishInnerOutputFold();
                if (!foldHandle.isFinished()) {
                    foldHandle.silentFinish();
                }
//                foldHandle = null;
            }
        }

        /**
         * Create a nested fold in the current tree fold. This is used e.g. for
         * stacktrace folds in the exec tree. If a nested fold already exists,
         * it will be finished before starting new fold.
         */
        public void startInnerOutputFold(InputOutput io) {
            if (!IOFolding.isSupported(io)) {
                return;
            }

            if (innerOutputFoldHandle != null && !innerOutputFoldHandle.isFinished()) {
                innerOutputFoldHandle.silentFinish();
            }
            if (foldHandle != null) {
                innerOutputFoldHandle = foldHandle.silentStartFold(true);
            } else {
                innerOutputFoldHandle = IOFolding.startFold(io, true);
            }
        }

        /**
         * Finish current nested fold created with {@link #startInnerOutputFold()}.
         */
        public void finishInnerOutputFold() {
            if (innerOutputFoldHandle != null) {
                if (!innerOutputFoldHandle.isFinished()) {
                    innerOutputFoldHandle.silentFinish();
                }
                innerOutputFoldHandle = null;
            }
        }

        /**
         * Check whether a nested fold exists.
         */
        public boolean hasInnerOutputFold() {
            return innerOutputFoldHandle != null;
        }

        public void collapseFold() {
            if (foldHandle != null) {
                foldHandle.setExpanded(false);
            }
        }

        public void expandFold() {
            if (foldHandle != null) {
                foldHandle.setExpanded(true);
            }
        }
    }

}
