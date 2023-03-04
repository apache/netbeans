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

package org.netbeans.lib.v8debug.commands;

import org.netbeans.lib.v8debug.PropertyBoolean;
import org.netbeans.lib.v8debug.PropertyLong;
import org.netbeans.lib.v8debug.V8Arguments;
import org.netbeans.lib.v8debug.V8Body;
import org.netbeans.lib.v8debug.V8Command;
import org.netbeans.lib.v8debug.V8Request;

/**
 *
 * @author Martin Entlicher
 */
public final class ChangeLive {
    
    private ChangeLive() {}
    
    public static V8Request createRequest(long sequence, long scriptId, String newSource) {
        return createRequest(sequence, scriptId, newSource, null);
    }
    
    public static V8Request createRequest(long sequence, long scriptId, String newSource, Boolean previewOnly) {
        return new V8Request(sequence, V8Command.Changelive, new Arguments(scriptId, newSource, previewOnly));
    }
    
    public static final class Arguments extends V8Arguments {
        
        private final long scriptId;
        private final String newSource;
        private final PropertyBoolean previewOnly;
        
        public Arguments(long scriptId, String newSource, Boolean previewOnly) {
            this.scriptId = scriptId;
            this.newSource = newSource;
            this.previewOnly = new PropertyBoolean(previewOnly);
        }

        public long getScriptId() {
            return scriptId;
        }

        public PropertyBoolean isPreviewOnly() {
            return previewOnly;
        }

        public String getNewSource() {
            return newSource;
        }
    }
    
    public static final class ResponseBody extends V8Body {
        
        private final ChangeLog changeLog;
        private final Result result;
        private final PropertyBoolean stepInRecommended;
        
        public ResponseBody(ChangeLog changeLog, Result result, Boolean stepInRecommended) {
            this.changeLog = changeLog;
            this.result = result;
            this.stepInRecommended = new PropertyBoolean(stepInRecommended);
        }

        public ChangeLog getChangeLog() {
            return changeLog;
        }

        public Result getResult() {
            return result;
        }

        public PropertyBoolean getStepInRecommended() {
            return stepInRecommended;
        }
        
    }
    
    public static final class Result {
        
        private final ChangeTree changeTree;
        private final TextualDiff diff;
        private final boolean updated;
        private final PropertyBoolean stackModified;
        private final PropertyBoolean stackUpdateNeedsStepIn;
        private final String createdScriptName;
        
        public Result(ChangeTree changeTree, TextualDiff diff, boolean updated,
                      Boolean stackModified, Boolean stackUpdateNeedsStepIn,
                      String createdScriptName) {
            this.changeTree = changeTree;
            this.diff = diff;
            this.updated = updated;
            this.stackModified = new PropertyBoolean(stackModified);
            this.stackUpdateNeedsStepIn = new PropertyBoolean(stackUpdateNeedsStepIn);
            this.createdScriptName = createdScriptName;
        }

        public ChangeTree getChangeTree() {
            return changeTree;
        }

        public TextualDiff getDiff() {
            return diff;
        }

        public boolean isUpdated() {
            return updated;
        }
        
        public PropertyBoolean getStackModified() {
            return stackModified;
        }

        public PropertyBoolean getStackUpdateNeedsStepIn() {
            return stackUpdateNeedsStepIn;
        }

        public String getCreatedScriptName() {
            return createdScriptName;
        }

        public static final class ChangeTree {
            
            private final String name;
            private final Positions positions;
            private final Positions newPositions;
            private final FunctionStatus status;
            private final String statusExplanation;
            private final ChangeTree[] children;
            private final ChangeTree[] newChildren;
            
            public static enum FunctionStatus {
                Unchanged,
                SourceChanged,
                Changed,
                Damaged;
                
                public static FunctionStatus fromString(String statusName) {
                    statusName = Character.toUpperCase(statusName.charAt(0)) + statusName.substring(1);
                    int i;
                    while ((i = statusName.indexOf(' ')) > 0) {
                        statusName = statusName.substring(0, i) +
                                     Character.toUpperCase(statusName.charAt(i+1)) +
                                     statusName.substring(i+2);
                    }
                    return FunctionStatus.valueOf(statusName);
                }

                @Override
                public String toString() {
                    String statusName = super.toString();
                    statusName = Character.toLowerCase(statusName.charAt(0)) + statusName.substring(1);
                    for (int i = 0; i < statusName.length(); i++) {
                        if (Character.isUpperCase(statusName.charAt(i))) {
                            statusName = statusName.substring(0, i) + " " +
                                         Character.toLowerCase(statusName.charAt(i)) +
                                         statusName.substring(i+1);
                            i++;
                        }
                    }
                    return statusName;
                }
                
            }
            
            public ChangeTree(String name,
                              Positions positions, Positions newPositions,
                              FunctionStatus status, String statusExplanation,
                              ChangeTree[] children, ChangeTree[] newChildren) {
                this.name = name;
                this.positions = positions;
                this.newPositions = newPositions;
                this.status = status;
                this.statusExplanation = statusExplanation;
                this.children = children;
                this.newChildren = newChildren;
            }

            public String getName() {
                return name;
            }

            public Positions getPositions() {
                return positions;
            }

            public Positions getNewPositions() {
                return newPositions;
            }

            public FunctionStatus getStatus() {
                return status;
            }

            public String getStatusExplanation() {
                return statusExplanation;
            }

            public ChangeTree[] getChildren() {
                return children;
            }

            public ChangeTree[] getNewChildren() {
                return newChildren;
            }
            
            public static final class Positions {

                private final long startPosition;
                private final long endPosition;

                public Positions(long startPosition, long endPosition) {
                    this.startPosition = startPosition;
                    this.endPosition = endPosition;
                }

                public long getStartPosition() {
                    return startPosition;
                }

                public long getEndPosition() {
                    return endPosition;
                }
            }

        }
        
        public static final class TextualDiff {
            
            private final long oldLength;
            private final long newLength;
            private final long[] chunks;
            
            public TextualDiff(long oldLength, long newLength, long[] chunks) {
                this.oldLength = oldLength;
                this.newLength = newLength;
                this.chunks = chunks;
            }

            public long getOldLength() {
                return oldLength;
            }

            public long getNewLength() {
                return newLength;
            }

            public long[] getChunks() {
                return chunks;
            }
        }
    }
    
    public static final class ChangeLog {
        
        private final BreakpointUpdate[] breakpointsUpdate;
        private final String[] namesLinkedToOldScript;
        private final String[] droppedFrames;
        private final FunctionPatched functionPatched;
        private final PositionPatched[] patchedPositions;
        
        public ChangeLog(BreakpointUpdate[] breakpointsUpdate,
                         String[] namesLinkedToOldScript,
                         String[] droppedFrames,
                         FunctionPatched functionPatched,
                         PositionPatched[] patchedPositions) {
            this.breakpointsUpdate = breakpointsUpdate;
            this.namesLinkedToOldScript = namesLinkedToOldScript;
            this.droppedFrames = droppedFrames;
            this.functionPatched = functionPatched;
            this.patchedPositions = patchedPositions;
        }

        public BreakpointUpdate[] getBreakpointsUpdate() {
            return breakpointsUpdate;
        }

        public String[] getNamesLinkedToOldScript() {
            return namesLinkedToOldScript;
        }

        public String[] getDroppedFrames() {
            return droppedFrames;
        }

        public FunctionPatched getFunctionPatched() {
            return functionPatched;
        }

        public PositionPatched[] getPatchedPositions() {
            return patchedPositions;
        }
        
        public static final class BreakpointUpdate {
            
            public static enum Type {
                CopiedToOld,
                PositionChanged;
                
                public static Type fromString(String typeName) {
                    int i = 0, i2;
                    StringBuilder typeEnumStr = new StringBuilder();
                    while (i < typeName.length()) {
                        typeEnumStr.append(Character.toUpperCase(typeName.charAt(i)));
                        i2 = typeName.indexOf('_', i);
                        if (i2 < 0) {
                            i2 = typeName.length();
                        }
                        typeEnumStr.append(typeName.substring(i+1, i2));
                        i = i2 + 1;
                    }
                    return Type.valueOf(typeEnumStr.toString());
                }
            }
            
            private final Type type;
            private final long id;
            private final PropertyLong newId;
            private final Position oldPositions;
            private final Position newPositions;
            
            public BreakpointUpdate(Type type, long id, PropertyLong newId,
                                    Position oldPositions,
                                    Position newPositions) {
                this.type = type;
                this.id = id;
                this.newId = newId;
                this.oldPositions = oldPositions;
                this.newPositions = newPositions;
            }

            public Type getType() {
                return type;
            }

            public long getId() {
                return id;
            }

            public PropertyLong getNewId() {
                return newId;
            }

            public Position getOldPositions() {
                return oldPositions;
            }

            public Position getNewPositions() {
                return newPositions;
            }
            
            public static final class Position {
                
                private final long position;
                private final long line;
                private final long column;
                
                public Position(long position, long line, long column) {
                    this.position = position;
                    this.line = line;
                    this.column = column;
                }

                public long getPosition() {
                    return position;
                }

                public long getLine() {
                    return line;
                }

                public long getColumn() {
                    return column;
                }
            }
        }
        
        public static final class FunctionPatched {
            
            private final String function;
            private final PropertyBoolean functionInfoNotFound;

            public FunctionPatched(String function, PropertyBoolean functionInfoNotFound) {
                this.function = function;
                this.functionInfoNotFound = functionInfoNotFound;
            }

            public String getFunction() {
                return function;
            }

            public PropertyBoolean getFunctionInfoNotFound() {
                return functionInfoNotFound;
            }
            
        }

        public static final class PositionPatched {
            
            private final String name;
            private final PropertyBoolean infoNotFound;

            public PositionPatched(String name, PropertyBoolean infoNotFound) {
                this.name = name;
                this.infoNotFound = infoNotFound;
            }

            public String getName() {
                return name;
            }

            public PropertyBoolean getInfoNotFound() {
                return infoNotFound;
            }
            
        }
    }
    
}
