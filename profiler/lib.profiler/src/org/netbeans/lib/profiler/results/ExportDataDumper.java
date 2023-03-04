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

package org.netbeans.lib.profiler.results;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;


/**
 * An instance of this class is passed to code that generates text representation of profiling
 * results for export. It is intended that the text generating code periodically checks the size
 * of the StringBuffer it uses for storage, and if it's above some critical value, dumps it using
 * the code below. If there is an error during this process, it is not returned immediately to avoid
 * making text generator code too complex - instead the caller can eventually retrieve the error
 * using the getCaughtException() method.
 *
 * @author Misha Dmitriev
 * @author Petr Cyhelsky
 */
public class ExportDataDumper {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    public static final int BUFFER_SIZE = 32000; //roughly 32 kB buffer

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    OutputStreamWriter osw;
    BufferedOutputStream bos;
    IOException caughtEx;
    int numExceptions=0;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public ExportDataDumper(FileOutputStream fw) {
        bos = new BufferedOutputStream(fw, BUFFER_SIZE);
        osw = new OutputStreamWriter(bos, StandardCharsets.UTF_8);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public IOException getCaughtException() {
        return caughtEx;
    }

    public int getNumExceptions() {
        return numExceptions;
    }
    
    public void dumpByte(byte b) {
        if (caughtEx != null) {
            return;
        }

        try {
            bos.write(b);
        } catch (IOException ex) {
            caughtEx = ex;
            System.out.println(b);
            numExceptions++;
            System.err.println(ex.getMessage());
        }
    }

    public void dumpData(CharSequence s) {
        if (caughtEx != null) {
            return;
        }

        try {
            if (s!=null) osw.append(s);
        } catch (IOException ex) {
            caughtEx = ex;
            System.out.println(s);
            numExceptions++;
            System.err.println(ex.getMessage());
        }
    }

    public void close() {
        try {
            osw.close();
            bos.close();
        } catch (IOException ex) {
            caughtEx = ex;
            System.err.println(ex.getMessage());
        }
    }

    public void dumpDataAndClose(StringBuffer s) {
        dumpData(s);
        close();
    }

    public BufferedOutputStream getOutputStream() {
        return bos;
    }
}
