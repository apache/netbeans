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

package org.netbeans.modules.java.source.indexing;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;

public class DiagnosticListenerImpl implements DiagnosticListener<JavaFileObject> {

    private static final Logger ERROR_LOG = Logger.getLogger(DiagnosticListenerImpl.class.getName() + "-errors");
    private final Map<URI, List<Diagnostic<? extends JavaFileObject>>> diagnostics = new HashMap<URI, List<Diagnostic<? extends JavaFileObject>>>();

    public void report(Diagnostic<? extends JavaFileObject> d) {
        assert logDiagnostic(d);

        JavaFileObject source = d.getSource();

        if (source != null) {
            List<Diagnostic<? extends JavaFileObject>> current = diagnostics.get(source.toUri());

            if (current == null) {
                diagnostics.put(source.toUri(), current = new LinkedList<Diagnostic<? extends JavaFileObject>>());
            }

            current.add(d);
        }
    }

    public List<Diagnostic<? extends JavaFileObject>> getDiagnostics(JavaFileObject file) {
        List<Diagnostic<? extends JavaFileObject>> result = diagnostics.remove(file.toUri());

        if (result == null) {
            result = Collections.emptyList();
        }

        return result;
    }

    public List<Diagnostic<? extends JavaFileObject>> peekDiagnostics(JavaFileObject file) {
        return diagnostics.getOrDefault(file.toUri(), Collections.emptyList());
    }

    public void cleanDiagnostics() {
        diagnostics.clear();
    }

    private static boolean logDiagnostic(Diagnostic<? extends JavaFileObject> d) {
        Level logLevel = findLogLevel(d);

        if (ERROR_LOG.isLoggable(logLevel)) {
            ERROR_LOG.log(logLevel, d.getSource().toUri().toASCIIString() + ":" + d.getCode() + ":" + d.getLineNumber() + ":" + d.getMessage(null), new Exception());
        }

        return true;
    }
    
    private static Level findLogLevel(Diagnostic<? extends JavaFileObject> d) {
        if (d.getKind() == Diagnostic.Kind.ERROR) {
            return Level.FINE;
        } else {
            return Level.FINER;
        }
    }

}
