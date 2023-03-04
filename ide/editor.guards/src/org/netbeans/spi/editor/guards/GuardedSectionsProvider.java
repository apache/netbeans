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

package org.netbeans.spi.editor.guards;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import org.netbeans.spi.editor.guards.support.AbstractGuardedSectionsProvider;

/**
 * Subclasses should be able to read content containing guarded section marks 
 * and vice versa. You should subclass {@link AbstractGuardedSectionsProvider}.
 * Use this interface in case you load or save the {@link javax.swing.text.Document}.
 * @author Jan Pokorsky
 */
public interface GuardedSectionsProvider {
    
    /**
     * Creates a reader able to read persisted sections.
     * @param stream stream containing persisted sections
     * @param charset charset to decode read bytes, <code>null</code> implies
     *                  the system default charset.
     * @return the reader
     */
    Reader createGuardedReader(InputStream stream, Charset charset);

    /**
     * Creates a writer able to write persisted sections.
     * @param stream stream where the output should be written
     * @param charset charset used by the writer
     * @return the writer
     */
    Writer createGuardedWriter(OutputStream stream, Charset charset);
    
}
