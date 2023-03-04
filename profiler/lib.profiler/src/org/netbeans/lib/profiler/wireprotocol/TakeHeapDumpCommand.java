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

package org.netbeans.lib.profiler.wireprotocol;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


/**
 * This command sent by client is the request to the TA to take a heap dump
 * @author Tomas Hurka
 */
public class TakeHeapDumpCommand extends Command {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private String outputFile; //Dumps the heap to the outputFile file

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public TakeHeapDumpCommand(String name) {
        super(TAKE_HEAP_DUMP);
        outputFile = name;
    }

    // Custom serialization support
    TakeHeapDumpCommand() {
        super(TAKE_HEAP_DUMP);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public String getOutputFile() {
        return outputFile;
    }

    // For debugging
    public String toString() {
        return super.toString() + ", outputFile: " + outputFile; // NOI18N
    }

    void readObject(ObjectInputStream in) throws IOException {
        outputFile = in.readUTF();
    }

    void writeObject(ObjectOutputStream out) throws IOException {
        out.writeUTF(outputFile);
    }
}
