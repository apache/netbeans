/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.api.common.singlesourcefile;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.extexecution.print.ConvertedLine;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Arunava Sinha
 */
class DebugSingleSourceOutputConvertor implements LineConvertor {

    private final FileObject fileObject;
    private final DebugProcess debuggerProcess;

    DebugSingleSourceOutputConvertor(FileObject fileObj, DebugProcess debuggerProcess) {
        this.fileObject = fileObj;
        this.debuggerProcess = debuggerProcess;
    }
    private static final String PATTERN_STRING = "Listening for transport dt_socket at address: ";  //NOI18N
    private static final Pattern DEBUGGER_PATTERN = Pattern.compile(PATTERN_STRING + "[0-9]*");  //NOI18N

    @Override
    public List<ConvertedLine> convert(String line) {
        ConvertedLine line1 = ConvertedLine.forText(line, null);

        Matcher m = DEBUGGER_PATTERN.matcher(line);

        if (m.matches()) {
            String port = line.split(PATTERN_STRING)[1].trim();

            AttachDebuggerProcess attachDebugger = new AttachDebuggerProcess();
            try {
                attachDebugger.attach("test", "dt_socket", "localhost", port, fileObject);  //NOI18N
            } catch (Exception ex) {
                if (debuggerProcess.isAlive()) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return Collections.singletonList(line1);
    }

}
