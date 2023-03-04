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
package org.netbeans.modules.php.dbgp.packets;

import java.util.Locale;
import org.w3c.dom.Node;

/**
 * @author ads
 *
 */
public class Stack extends Input {
    private static final String INPUT = "input"; // NOI18N
    private static final String CMDEND = "cmdend"; // NOI18N
    private static final String CMDBEGIN = "cmdbegin"; // NOI18N
    private static final String WHERE = "where"; // NOI18N

    public enum Type {
        FILE,
        EVAL,
        QUEST;

        @Override
        public String toString() {
            if (this != QUEST) {
                return super.toString().toLowerCase(Locale.US);
            } else {
                return "?"; // NOI18N
            }
        }

        public static Type forString(String str) {
            Type[] types = Type.values();
            for (Type type : types) {
                if (type.toString().equals(str)) {
                    return type;
                }
            }
            return null;
        }

    }

    Stack(Node node) {
        super(node);
    }

    public String getCurrentCommandName() {
        return getAttribute(WHERE);
    }

    public Position getCurrentInstructionStart() {
        return getPosition(CMDBEGIN);
    }

    public Position getCurrentInstructionEnd() {
        return getPosition(CMDEND);
    }

    public Input getInput() {
        Node node = getChild(INPUT);
        if (node == null) {
            return null;
        }
        return new Input(node);
    }

    private Position getPosition(String attrName) {
        String value = getAttribute(attrName);
        if (value == null) {
            return null;
        }
        String[] values = value.split(":"); //NOI18N
        assert values.length == 2;
        return new Position(values[0], values[1]);
    }

    public static final class Position {
        private int myLine;
        private int myOffset;

        private Position(String line, String offset) {
            try {
                myLine = Integer.parseInt(line);
                myOffset = Integer.parseInt(offset);
            } catch (NumberFormatException e) {
                myLine = -1;
                myOffset = -1;
            }
        }

        public int getLine() {
            return myLine;
        }

        public int getOffset() {
            return myOffset;
        }

    }

}
