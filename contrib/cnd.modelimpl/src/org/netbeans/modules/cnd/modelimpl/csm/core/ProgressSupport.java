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
