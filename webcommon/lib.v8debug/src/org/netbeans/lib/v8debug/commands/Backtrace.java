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
import org.netbeans.lib.v8debug.V8Frame;
import org.netbeans.lib.v8debug.V8Request;

/**
 *
 * @author Martin Entlicher
 */
public final class Backtrace {

    private Backtrace() {
    }
    
    public static V8Request createRequest(long sequence, Long fromFrame, Long toFrame, Boolean bottom, Boolean inlineRefs) {
        return new V8Request(sequence, V8Command.Backtrace, new Arguments(fromFrame, toFrame, bottom, inlineRefs));
    }

    public static final class Arguments extends V8Arguments {

        private final PropertyLong fromFrame;
        private final PropertyLong toFrame;
        private final PropertyBoolean bottom;
        private final PropertyBoolean inlineRefs;

        public Arguments(Long fromFrame, Long toFrame, Boolean bottom, Boolean inlineRefs) {
            this.fromFrame = new PropertyLong(fromFrame);
            this.toFrame = new PropertyLong(toFrame);
            this.bottom = new PropertyBoolean(bottom);
            this.inlineRefs = new PropertyBoolean(inlineRefs);
        }

        public PropertyLong getFromFrame() {
            return fromFrame;
        }

        public PropertyLong getToFrame() {
            return toFrame;
        }

        public PropertyBoolean isBottom() {
            return bottom;
        }

        public PropertyBoolean isInlineRefs() {
            return inlineRefs;
        }
    }

    public static final class ResponseBody extends V8Body {

        private final long fromFrame;
        private final long toFrame;
        private final long totalFrames;
        private final V8Frame[] frames;

        public ResponseBody(long fromFrame, long toFrame, long totalFrames, V8Frame[] frames) {
            this.fromFrame = fromFrame;
            this.toFrame = toFrame;
            this.totalFrames = totalFrames;
            this.frames = frames;
        }

        public long getFromFrame() {
            return fromFrame;
        }

        public long getToFrame() {
            return toFrame;
        }

        public long getTotalFrames() {
            return totalFrames;
        }

        public V8Frame[] getFrames() {
            return frames;
        }

    }
}
