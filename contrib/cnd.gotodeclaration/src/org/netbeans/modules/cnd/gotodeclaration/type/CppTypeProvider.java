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
package org.netbeans.modules.cnd.gotodeclaration.type;

import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.api.model.*;

import org.netbeans.api.project.Project;
import static org.netbeans.modules.cnd.api.model.CsmDeclaration.Kind.CLASS;
import static org.netbeans.modules.cnd.api.model.CsmDeclaration.Kind.ENUM;
import static org.netbeans.modules.cnd.api.model.CsmDeclaration.Kind.STRUCT;
import static org.netbeans.modules.cnd.api.model.CsmDeclaration.Kind.TYPEDEF;
import static org.netbeans.modules.cnd.api.model.CsmDeclaration.Kind.UNION;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.support.CsmClassifierResolver;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmVisibilityQuery;
import org.netbeans.spi.jumpto.support.NameMatcher;
import org.netbeans.spi.jumpto.support.NameMatcherFactory;

import org.netbeans.spi.jumpto.type.SearchType;
import org.netbeans.spi.jumpto.type.TypeDescriptor;
import org.netbeans.spi.jumpto.type.TypeProvider;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Implementation of provider for "Jump to Type" for C/C++
 *
 */
@org.openide.util.lookup.ServiceProvider(service = org.netbeans.spi.jumpto.type.TypeProvider.class)
public class CppTypeProvider implements TypeProvider {

    private static final boolean PROCESS_LIBRARIES = true; // Boolean.getBoolean("cnd.type.provider.libraries");
    private static final boolean TRACE = Boolean.getBoolean("cnd.type.provider.trace"); // NOI18N
    private static final Logger LOG = TRACE ? Logger.getLogger("cnd.type.provider.trace") : null; // NOI18N
    private static final RequestProcessor RP = new RequestProcessor(CppTypeProvider.class.getName(), 1);
    private static final Object resultLock = new Object();
    private final Object activeTaskLock = new Object();
    private WorkerTask activeTask;

    public CppTypeProvider() {
        if (TRACE) {
            LOG.info("CppTypeProvider created"); // NOI18N
        }
    }

    @Override
    public String name() {
        return "C/C++"; // NOI18N
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(CppTypeProvider.class, "TYPE_PROVIDER_DISPLAY_NAME"); // NOI18N
    }

    @Override
    public void computeTypeNames(final Context context, final Result res) {
        // GoToTypeAction-RP
        if (TRACE) {
            LOG.log(Level.INFO, "computeTypeNames request at {0} ms.", System.currentTimeMillis()); // NOI18N
        }

        WorkerTask task;
        synchronized (activeTaskLock) {
            task = activeTask;
            if (task != null && !sameContext(task.context, context)) {
                task.cancel();
                task = null;
                activeTask = null;
            }

            if (task == null) {
                task = new WorkerTask(context);
                activeTask = task;
                RP.submit(task);
            }
        }

        while (!task.isDone()) {
            try {
                task.get(200, TimeUnit.MILLISECONDS);
            } catch (TimeoutException ex) {
                //if (task.hasResult()) {
                    break;
                //}
            } catch (InterruptedException ex) {
                task.cancel();
                // clean flag
                Thread.interrupted();
                if (TRACE) {
                    LOG.log(Level.INFO, "InterruptedException"); // NOI18N
                }                
                break;
            } catch (CancellationException ex) {
                if (TRACE) {
                    LOG.log(Level.INFO, "CancellationException"); // NOI18N
                }                
                break;
            } catch (ExecutionException ex) {
                if (!(ex.getCause() instanceof CancellationException)) {
                    Exceptions.printStackTrace(ex);
                }
                break;
            }
        }

        if (!task.isDone()) {
            res.pendingResult();
            if (TRACE) {
                LOG.log(Level.INFO, "Results are not fully available yet at {0} ms.", System.currentTimeMillis()); // NOI18N
            }
        }

        res.addResult(task.getResult());
    }

    @Override
    public void cancel() {
        if (TRACE) {
            LOG.info("cancel request"); // NOI18N
        }
        WorkerTask task;
        synchronized (activeTaskLock) {
            task = activeTask;
            activeTask = null;
        }
        if (task != null) {
            task.cancel();
        }
    }

    @Override
    public void cleanup() {
        // GoToTypeAction-RP
        if (TRACE) {
            LOG.info("cleanup request"); // NOI18N
        }
        cancel();
    }

    private static class WorkerTask extends FutureTask<Void> {

        private final Set<TypeDescriptor> result;
        private final Context context;
        private final AtomicBoolean cancelled;

        public WorkerTask(Context context) {
            this(context, new HashSet<TypeDescriptor>(), new AtomicBoolean(false));
        }
        
        private WorkerTask(Context context, Set<TypeDescriptor> result, AtomicBoolean cancelled) {
            super(new Worker(context, result, cancelled), null);
            this.context = context;
            this.result = result;
            this.cancelled = cancelled;
        }

        private List<TypeDescriptor> getResult() {
            synchronized (resultLock) {
                return new ArrayList<TypeDescriptor>(result);
            }
        }

        private boolean hasResult() {
            synchronized (resultLock) {
                return !result.isEmpty();
            }
        }
        
        public void cancel() {
            cancelled.set(true);
        }
    }

    private static class Worker implements Runnable {

        private final Context context;
        private final Set<TypeDescriptor> result;
        private long startTime;
        private final AtomicBoolean cancelled;

        private Worker(Context context, Set<TypeDescriptor> result, AtomicBoolean cancelled) {
            if (TRACE) {
                LOG.log(Level.INFO, "New Worker for searching \"{0}\", {1} in {2} created.", // NOI18N
                        new Object[]{context.getText(), context.getSearchType().name(), context.getProject()});
            }
            this.context = context;
            this.result = result;
            this.cancelled = cancelled;
        }

        @Override
        public void run() {
            if (TRACE) {
                startTime = System.currentTimeMillis();
            }
            try {
                Project project = context.getProject();
                String text = context.getText();
                SearchType type = context.getSearchType();
                CsmCacheManager.enter();
                try {
                    CsmSelect.CsmFilter filter = CsmSelect.CLASSIFIER_KIND_FILTER;
                    NameMatcher matcher = NameMatcherFactory.createNameMatcher(text, type);
                    if (project == null) {
                        Collection<CsmProject> csmProjects = CsmModelAccessor.getModel().projects();
                        if (!csmProjects.isEmpty()) {
                            for (CsmProject csmProject : csmProjects) {
                                checkCancelled();
                                processProject(csmProject, filter, matcher);
                            }
                            if (PROCESS_LIBRARIES) {
                                for (CsmProject csmProject : csmProjects) {
                                    checkCancelled();
                                    Set<CsmProject> processedLibs = new HashSet<CsmProject>();
                                    processProjectLibs(csmProject, filter, processedLibs, matcher);
                                }
                            }
                        }
                    } else {
                        CsmProject csmProject = CsmModelAccessor.getModel().getProject(project);
                        processProject(csmProject, filter, matcher);
                        if (PROCESS_LIBRARIES) {
                            processProjectLibs(csmProject, filter, new HashSet<CsmProject>(), matcher);
                        }
                    }
                } finally {
                    CsmCacheManager.leave();
                }
            } catch (CancellationException ex) {
                if (TRACE) {
                    LOG.log(Level.INFO, "Worker for searching \"{0}\", {1} in {2} cancelled [after {3} ms.].", // NOI18N
                            new Object[]{context.getText(), context.getSearchType().name(), context.getProject(), (System.currentTimeMillis() - startTime)});
                }
            } finally {
                if (TRACE) {
                    LOG.log(Level.INFO, "Worker for searching \"{0}\", {1} in {2} done [in {3} ms.].", // NOI18N
                            new Object[]{context.getText(), context.getSearchType().name(), context.getProject(), (System.currentTimeMillis() - startTime)});
                }
            }
        }

        private void processProjectLibs(CsmProject project, CsmSelect.CsmFilter filter, Set<CsmProject> processedLibs, NameMatcher matcher) {
            for (CsmProject lib : project.getLibraries()) {
                checkCancelled();
                if (lib.isArtificial()) {
                    if (!processedLibs.contains(lib)) {
                        processedLibs.add(lib);
                        processProject(lib, filter, matcher);
                    }
                }
            }
        }

        private void processProject(CsmProject project, CsmSelect.CsmFilter filter, NameMatcher matcher) {
            checkCancelled();
            processNamespace(project.getGlobalNamespace(), filter, matcher);
        }

        private void processNamespace(CsmNamespace nsp, CsmSelect.CsmFilter filter, NameMatcher matcher) {
            checkCancelled();
            for (Iterator<CsmOffsetableDeclaration> iter = CsmSelect.getDeclarations(nsp, filter); iter.hasNext();) {
                checkCancelled();
                CsmDeclaration declaration = iter.next();
                processDeclaration(declaration, matcher);
            }
            for (CsmNamespace child : nsp.getNestedNamespaces()) {
                checkCancelled();
                processNamespace(child, filter, matcher);
            }
        }

        private void processDeclaration(CsmDeclaration decl, NameMatcher matcher) {
            checkCancelled();
            switch (decl.getKind()) {
                case CLASS:
                case UNION:
                case STRUCT:
                    CsmClass cls = (CsmClass) decl;
                    if (!CsmClassifierResolver.getDefault().isForwardClass(cls)) {
                        if (matcher.accept(decl.getName().toString())) {
                            if (CsmVisibilityQuery.isVisible(cls)) {
                                addResult(cls);
                            }
                        }
                        checkCancelled();
                        for (CsmMember member : cls.getMembers()) {
                            checkCancelled();
                            if (matcher.accept(member.getName().toString())) {
                                processDeclaration(member, matcher);
                            }
                        }

                    }
                    break;
                case ENUM:
                case TYPEDEF:
                    if (matcher.accept(decl.getName().toString())) {
                        if (CsmVisibilityQuery.isVisible(decl)) {
                            addResult((CsmClassifier) decl);
                        }
                    }
                    break;
            }
        }

        private void checkCancelled() throws CancellationException {
            if (cancelled.get()) {
                throw new CancellationException();
            }
        }

        private void addResult(CsmClassifier classifier) {
            TypeDescriptor typeDescriptor = createTypeDescriptor(classifier);
            synchronized (resultLock) {
                result.add(typeDescriptor);
            }
        }
    }

    private static TypeDescriptor createTypeDescriptor(CsmClassifier classifier) {
        CppTypeDescriptor descriptor = new CppTypeDescriptor(classifier);
        return TRACE ? new TracingTypeDescriptor(descriptor) : descriptor;
    }

    private static boolean sameContext(Context context1, Context context2) {
        if (context1 == null) {
            return context2 == null;
        }
        if (context2 == null) {
            return false;
        }
        if (!context1.getSearchType().equals(context2.getSearchType())) {
            return false;
        }
        if (!context1.getText().equals(context2.getText())) {
            return false;
        }
        return true;
    }
}
