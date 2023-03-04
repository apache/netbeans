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

package org.netbeans.modules.editor.guards;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.List;
import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.spi.editor.guards.support.AbstractGuardedSectionsProvider;

/** This stream is able to insert special guarded comments.
*/
final class GuardedWriter extends Writer {
    /** Encapsulated writer. */
    private Writer writer;

    private CharArrayWriter buffer;

    private final AbstractGuardedSectionsProvider gw;

    private boolean isClosed = false;

    private final List<GuardedSection> sections;

    
    /** Creates new GuardedWriter.
    * @param os Encapsulated output stream.
    * @param list The list of the guarded sections.
    */
    public GuardedWriter(AbstractGuardedSectionsProvider gw, OutputStream os, List<GuardedSection> list, Charset encoding) {
        if (encoding == null)
            writer = new OutputStreamWriter(os);
        else
            writer = new OutputStreamWriter(os, encoding);
        this.gw = gw;
        sections = list;
    }

    /** Writes chars to underlying writer */
    public void write(char[] cbuf, int off, int len) throws IOException {
        
        if (buffer == null) {
            buffer = new CharArrayWriter(10240);
        }
        
        buffer.write(cbuf, off, len);
    }

    /** Calls underlying writer flush */
    public void close() throws IOException {
        if (isClosed) {
            return;
        }
        isClosed = true;
        if (buffer != null) {
            char[] content = this.gw.writeSections(sections, buffer.toCharArray());
            writer.write(content);
        }
        writer.close();
    }

    /** Calls underlying writer flush */
    public void flush() throws IOException {
    }
    
}
