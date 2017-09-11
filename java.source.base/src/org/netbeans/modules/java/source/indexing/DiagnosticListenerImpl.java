/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010-2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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

class DiagnosticListenerImpl implements DiagnosticListener<JavaFileObject> {

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
