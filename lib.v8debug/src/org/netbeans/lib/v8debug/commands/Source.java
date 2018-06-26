/* 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
