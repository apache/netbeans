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

import org.netbeans.lib.v8debug.PropertyLong;
import org.netbeans.lib.v8debug.V8Arguments;
import org.netbeans.lib.v8debug.V8Body;
import org.netbeans.lib.v8debug.V8Command;
import org.netbeans.lib.v8debug.V8Request;

/**
 *
 * @author Martin Entlicher
 */
public final class Source {
    
    private Source() {}
    
    public static V8Request createRequest(long sequence, Long frame, Long fromLine, Long toLine) {
        return new V8Request(sequence, V8Command.Source, new Arguments(frame, fromLine, toLine));
    }
    
    public static final class Arguments extends V8Arguments {
        
        private final PropertyLong frame;
        private final PropertyLong fromLine;
        private final PropertyLong toLine;
        
        public Arguments(Long frame, Long fromLine, Long toLine) {
            this.frame = new PropertyLong(frame);
            this.fromLine = new PropertyLong(fromLine);
            this.toLine = new PropertyLong(toLine);
        }

        public PropertyLong getFrame() {
            return frame;
        }

        public PropertyLong getFromLine() {
            return fromLine;
        }

        public PropertyLong getToLine() {
            return toLine;
        }
    }
    
    public static final class ResponseBody extends V8Body {
        
        private final String source;
        private final long fromLine;
        private final long toLine;
        private final long fromPosition;
        private final long toPosition;
        private final long totalLines;
        
        public ResponseBody(String source, long fromLine, long toLine,
                            long fromPosition, long toPosition, long totalLines) {
            this.source = source;
            this.fromLine = fromLine;
            this.toLine = toLine;
            this.fromPosition = fromPosition;
            this.toPosition = toPosition;
            this.totalLines = totalLines;
        }

        public String getSource() {
            return source;
        }

        public long getFromLine() {
            return fromLine;
        }

        public long getToLine() {
            return toLine;
        }

        public long getFromPosition() {
            return fromPosition;
        }

        public long getToPosition() {
            return toPosition;
        }

        public long getTotalLines() {
            return totalLines;
        }
    }
}
