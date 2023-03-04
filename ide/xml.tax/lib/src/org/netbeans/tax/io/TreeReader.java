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

import org.netbeans.tax.TreeDocumentRoot;
import org.netbeans.tax.TreeException;
import org.netbeans.tax.io.TreeStreamResult;
import org.netbeans.tax.io.TreeWriter;

/**
 * Access tree content using reader. The tree MUST NOT change until the
 * reader is closed. It is responsibility of caller.
 *
 * @author  Petr Kuzel
 * @version 0.9
 */
public class TreeReader extends Reader {

    private final StringReader reader;

    /**
     * Creates new TreeReader that is immediatelly ready for reading.
     */
    public TreeReader (TreeDocumentRoot doc) throws IOException {
        
        reader = new StringReader (Convertors.treeToString (doc));
        
    }
    
    public void close () throws IOException {
        if (reader == null) throw new IOException (Util.THIS.getString ("EXC_null_reader"));
        reader.close ();
    }
    
    public int read (char[] cbuf, int off, int len) throws IOException {
        if (reader == null) throw new IOException (Util.THIS.getString ("EXC_null_reader"));
        return reader.read (cbuf, off, len);
    }
}
