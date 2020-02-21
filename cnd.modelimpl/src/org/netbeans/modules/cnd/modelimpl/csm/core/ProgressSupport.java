/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.util.Collection;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.modelimpl.util.WeakList;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.openide.util.Lookup;

/**
 *
 */
public class ProgressSupport {

    private static final ProgressSupport instance = new ProgressSupport();
    private final WeakList<CsmProgressListener> progressListeners = new WeakList<>();
    /** Creates a new instance of ProgressSupport */
    private ProgressSupport() {
    }

    /*package-local*/ static ProgressSupport instance() {
        return instance;
    }

    /*package-local*/ void addProgressListener(CsmProgressListener listener) {
        progressListeners.add(listener);
    }

    /*package-local*/ void removeProgressListener(CsmProgressListener listener) {
        progressListeners.remove(listener);
    }

    private Iterable<? extends CsmProgressListener> getProgressListeners() {
        Collection<? extends CsmProgressListener> services = Lookup.getDefault().lookupAll(CsmProgressListener.class);
        return (services.isEmpty()) ? progressListeners : progressListeners.join(services);
    }

    /*package-local*/ void fireFileInvalidated(FileImpl file) {
        if (TraceFlags.TRACE_PARSER_QUEUE || TraceFlags.TRACE_PARSER_PROGRESS) {
            System.err.println("fireFileInvalidated " + file.getAbsolutePath());
        }
        for (CsmProgressListener listener : getProgressListeners()) {
            try { // have to do this to not allow a listener to crush code model threads
                listener.fileInvalidated(file);
            } catch (Throwable e) {
                DiagnosticExceptoins.register(e);
            }
        }
    }

    /*package-local*/ void fireFileAddedToParse(FileImpl file) {
        if (TraceFlags.TRACE_PARSER_QUEUE || TraceFlags.TRACE_PARSER_PROGRESS) {
            System.err.println("fireFileAddedToParse " + file.getAbsolutePath());
        }
        for (CsmProgressListener listener : getProgressListeners()) {
            try { // have to do this to not allow a listener to crush code model threads
                listener.fileAddedToParse(file);
            } catch (Throwable e) {
                DiagnosticExceptoins.register(e);
            }
        }
    }

    /*package-local*/ void fireFileParsingStarted(FileImpl file) {
        if (TraceFlags.TRACE_PARSER_QUEUE || TraceFlags.TRACE_PARSER_PROGRESS) {
            System.err.println("fireFileParsingStarted " + file.getAbsolutePath());
        }
        for (CsmProgressListener listener : getProgressListeners()) {
            try { // have to do this to not allow a listener to crush code model threads
                listener.fileParsingStarted(file);
            } catch (Throwable e) {
                DiagnosticExceptoins.register(e);
            }
        }
    }

    /*package-local*/ void fireFileParsingFinished(FileImpl file) {
        if (TraceFlags.TRACE_PARSER_QUEUE || TraceFlags.TRACE_PARSER_PROGRESS) {
            System.err.println("fireFileParsingFinished " + file.getAbsolutePath());
        }
        for (CsmProgressListener listener : getProgressListeners()) {
            try { // have to do this to not allow a listener to crush code model threads
                listener.fileParsingFinished(file);
            } catch (Throwable e) {
                DiagnosticExceptoins.register(e);
            }
        }
    }

    /*package-local*/ void fireFileRemoved(FileImpl file) {
        if (TraceFlags.TRACE_PARSER_QUEUE || TraceFlags.TRACE_PARSER_PROGRESS) {
            System.err.println("fireFileRemoved " + file.getAbsolutePath());
        }
        for (CsmProgressListener listener : getProgressListeners()) {
            try { // have to do this to not allow a listener to crush code model threads
                listener.fileRemoved(file);;
            } catch (Throwable e) {
                DiagnosticExceptoins.register(e);
            }
        }
    }
    /*package-local*/ void fireProjectParsingStarted(ProjectBase project) {
        if (TraceFlags.TRACE_PARSER_QUEUE || TraceFlags.TRACE_PARSER_PROGRESS) {
            System.err.println("fireProjectParsingStarted " + project.getName());
        }
        for (CsmProgressListener listener : getProgressListeners()) {
            try { // have to do this to not allow a listener to crush code model threads
                listener.projectParsingStarted(project);
            } catch (Throwable e) {
                DiagnosticExceptoins.register(e);
            }
        }
    }

    /*package-local*/ void fireProjectParsingFinished(ProjectBase project) {
        if (TraceFlags.TRACE_PARSER_QUEUE || TraceFlags.TRACE_PARSER_PROGRESS) {
            System.err.println("fireProjectParsingFinished " + project.getName());
        }
        for (CsmProgressListener listener : getProgressListeners()) {
            try { // have to do this to not allow a listener to crush code model threads
                listener.projectParsingFinished(project);
            } catch (Throwable e) {
                DiagnosticExceptoins.register(e);
            }
        }
    }

    /*package-local*/ void fireProjectLoaded(ProjectBase project) {
        if (TraceFlags.TRACE_PARSER_QUEUE || TraceFlags.TRACE_PARSER_PROGRESS) {
            System.err.println("fireProjectLoaded " + project.getName());
        }
        for (CsmProgressListener listener : getProgressListeners()) {
            try { // have to do this to not allow a listener to crush code model threads
                listener.projectLoaded(project);
            } catch (Throwable e) {
                DiagnosticExceptoins.register(e);
            }
        }
    }

    /*package-local*/ void fireIdle() {
        for (CsmProgressListener listener : getProgressListeners()) {
            try { // have to do this to not allow a listener to crush code model threads
                listener.parserIdle();
            } catch (Throwable e) {
                DiagnosticExceptoins.register(e);
            }
        }
    }

    /*package-local*/ void fireProjectFilesCounted(ProjectBase project, int cnt) {
        if (TraceFlags.TRACE_PARSER_QUEUE || TraceFlags.TRACE_PARSER_PROGRESS) {
            System.err.println("fireProjectFilesCounted " + project.getName() + ' ' + cnt);
        }
        for (CsmProgressListener listener : getProgressListeners()) {
            try { // have to do this to not allow a listener to crush code model threads
                listener.projectFilesCounted(project, cnt);
            } catch (Throwable e) {
                DiagnosticExceptoins.register(e);
            }
        }
    }
}
