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

package org.netbeans.modules.debugger.jpda.truffle.ast;

/**
 * A representation of Truffle Node class.
 */
public final class TruffleNode {

    private final String className;
    private final String description;
    private final String sourceURI;
    private final int l1;
    private final int c1;
    private final int l2;
    private final int c2;
    private final String tags;
    private final TruffleNode[] ch;
    private boolean current;

    public TruffleNode(String className, String description, String sourceURI, int l1, int c1, int l2, int c2, String tags, int numCh) {
        this.className = className;
        this.description = description;
        this.sourceURI = sourceURI;
        this.l1 = l1;
        this.c1 = c1;
        this.l2 = l2;
        this.c2 = c2;
        this.tags = tags;
        this.ch = new TruffleNode[numCh];
    }

    private void setChild(int i, TruffleNode node) {
        ch[i] = node;
    }

    public String getClassName() {
        return className;
    }

    public String getClassSimpleName() {
        int index = className.lastIndexOf('.');
        if (index > 0) {
            return className.substring(index + 1);
        } else {
            return className;
        }
    }

    public String getTags() {
        return tags;
    }

    public String getDescription() {
        return description;
    }

    public String getSourceURI() {
        return sourceURI;
    }

    public int getStartLine() {
        return l1;
    }

    public int getStartColumn() {
        return c1;
    }

    public int getEndLine() {
        return l2;
    }

    public int getEndColumn() {
        return c2;
    }

    public TruffleNode[] getChildren() {
        return ch;
    }

    /** This node is currently being executed. */
    public boolean isCurrent() {
        return current;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private TruffleNode node;
        private int currentLine;

        private Builder() {}

        public Builder nodes(String nodes) {
            StringLineReader slr = new StringLineReader(nodes);
            node = parseNode(slr);
            return this;
        }

        public TruffleNode build() {
            if (currentLine > 0) {
                markCurrent(node, currentLine);
            }
            return node;
        }

        /** Mark all node paths which are currently being executed as current. */
        private boolean markCurrent(TruffleNode node, int currentLine) {
            if (node.getChildren().length == 0) {
                if (node.getStartLine() <= currentLine && currentLine <= node.getEndLine()) {
                    node.current = true;
                    return true;
                } else {
                    return false;
                }
            } else {
                boolean isSomeCurrent = false;
                for (TruffleNode ch : node.getChildren()) {
                    if (markCurrent(ch, currentLine)) {
                        isSomeCurrent = true;
                    }
                }
                node.current = isSomeCurrent;
                return isSomeCurrent;
            }
        }

        private TruffleNode parseNode(StringLineReader slr) {
            String className = slr.nextLine();
            String description = slr.nextLine();
            String sourceURI;
            int l1, c1, l2, c2;
            String ss = slr.nextLine();
            if (ss.isEmpty()) {
                sourceURI = null;
                l1 = c1 = l2 = c2 = -1;
            } else {
                sourceURI = ss;
                ss = slr.nextLine();
                int i1 = 0;
                int i2 = ss.indexOf(':');
                l1 = Integer.parseInt(ss.substring(i1, i2));
                i1 = i2 + 1;
                i2 = ss.indexOf('-');
                c1 = Integer.parseInt(ss.substring(i1, i2));
                i1 = i2 + 1;
                i2 = ss.indexOf(':', i1);
                l2 = Integer.parseInt(ss.substring(i1, i2));
                i1 = i2 + 1;
                i2 = ss.length();
                c2 = Integer.parseInt(ss.substring(i1, i2));
            }
            String tags = slr.nextLine();
            int numCh = Integer.parseInt(slr.nextLine());
            TruffleNode node = new TruffleNode(className, description, sourceURI, l1, c1, l2, c2, tags, numCh);
            for (int i = 0; i < numCh; i++) {
                node.setChild(i, parseNode(slr));
            }
            return node;
        }

        public Builder currentLine(int line) {
            this.currentLine = line;
            return this;
        }

        private static class StringLineReader {

            private final String lines;
            private int i = 0;

            private StringLineReader(String lines) {
                this.lines = lines;
            }

            String nextLine() {
                int i2 = lines.indexOf('\n', i);
                if (i2 < i) {
                    return null;
                }
                String line = lines.substring(i, i2);
                i = i2 + 1;
                return line;
            }
        }
    }
}
