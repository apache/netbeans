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
package org.netbeans.tax.io;

import java.io.*;

/**
 * This class can remember what was read from underlayng Reader.
 * <p>
 * It is not a good idea to plug under a buffered Reader because
 * it would remember whole buffer or miss whole buffer if above
 * buffer will match.
 *
 * @author  Petr Kuzel
 * @version
 */
public class RememberingReader extends Reader {

    private final Reader peer;
    private StringBuffer memory;

    /** Creates new RememberingReader */
    public RememberingReader (Reader peer) {
        this.peer = peer;
    }
    
    /**
     * All subsequent reads
     */
    public void startRemembering () {
        memory = new StringBuffer ();
    }
    
    public StringBuffer stopRemembering () {
        StringBuffer toret = memory;
        memory = null;
        return toret;
    }
    
    public void close () throws java.io.IOException {
        peer.close ();
    }
    
    public int read (char[] values, int off, int len) throws java.io.IOException {
        int toret = peer.read (values, off, len);
        if (memory != null && toret > 0) memory.append (values, off, toret);
        return toret;
    }
    
}
