/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.lib.nbjavac.services;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javadoc.main.Messager;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.tools.JavaFileObject;

/**
 *
 * @author Tomas Zezula
 */
public final class NBMessager extends Messager {

    private static final String ERR_NOT_IN_PROFILE = "not.in.profile";  //NOI18N

    private final Map<URI,Collection<Symbol.ClassSymbol>> notInProfiles =
            new HashMap<>();
    
    private NBMessager(
            final Context context,
            final String programName,
            final PrintWriter errWriter,
            final PrintWriter warnWriter,
            final PrintWriter noticeWriter) {
        super(context, programName, errWriter, warnWriter, noticeWriter);
    }

    public static NBMessager instance(Context context) {
        final Log log = Log.instance(context);
        if (!(log instanceof NBMessager)) {
            throw new InternalError("No NBMessager instance!"); //NOI18N
        }
        return (NBMessager) log;
    }
    
    public static void preRegister(Context context,
                                   final String programName,
                                   final PrintWriter errWriter,
                                   final PrintWriter warnWriter,
                                   final PrintWriter noticeWriter) {
        context.put(logKey, new Context.Factory<Log>() {
            @Override
            public Log make(Context c) {
                return new NBMessager(
                    c,
                    programName,
                    errWriter,
                    warnWriter,
                    noticeWriter);
            }
        });
    }

    @Override
    public void error(
            final DiagnosticPosition pos,
            final String key,
            final Object ... args) {
        if (ERR_NOT_IN_PROFILE.equals(key)) {
            final JavaFileObject currentFile = currentSourceFile();
            if (currentFile != null) {
                final URI uri = currentFile.toUri();
                Symbol.ClassSymbol type = (Symbol.ClassSymbol) args[0];
                Collection<Symbol.ClassSymbol> types = notInProfiles.get(uri);
                if (types == null) {
                    types = new ArrayList<>();
                    notInProfiles.put(uri,types);
                }
                types.add(type);
            }
        }
        super.error(pos, key, args);
    }

    Collection<? extends Symbol.ClassSymbol> removeNotInProfile(final URI uri) {
        return uri == null ? null : notInProfiles.remove(uri);
    }

    @Override
    protected int getDefaultMaxWarnings() {
        return Integer.MAX_VALUE;
    }

    @Override
    protected int getDefaultMaxErrors() {
        return Integer.MAX_VALUE;
    }

}
