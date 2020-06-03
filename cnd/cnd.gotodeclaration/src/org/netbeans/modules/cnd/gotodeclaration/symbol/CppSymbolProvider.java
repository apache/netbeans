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

package org.netbeans.modules.cnd.gotodeclaration.symbol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmDeclaration.Kind;
import org.netbeans.modules.cnd.api.model.CsmEnum;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.NameAcceptor;
import org.netbeans.modules.cnd.api.model.services.CsmVisibilityQuery;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.spi.jumpto.support.NameMatcher;
import org.netbeans.spi.jumpto.support.NameMatcherFactory;
import org.netbeans.spi.jumpto.symbol.SymbolProvider;
import org.netbeans.spi.jumpto.type.SearchType;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * SymbolProvider for C/C++ implementation
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.jumpto.symbol.SymbolProvider.class)
public class CppSymbolProvider implements SymbolProvider {

    private static final boolean TRACE = Boolean.getBoolean("cnd.gotosymbol.trace");
    private static final Logger LOG = TRACE ? Logger.getLogger("cnd.symbol.provider.trace") : null; // NOI18N
    private static final RequestProcessor RP = new RequestProcessor(CppSymbolProvider.class.getName(), 1);
    private static final Object resultLock = new Object();
    private final Object activeTaskLock = new Object();
    private WorkerTask activeTask;

    
    public CppSymbolProvider() {
        if (TRACE) {
            LOG.info("CppSymbolProvider created"); // NOI18N
        }
    }

    @Override
    public String name() {
        return "C/C++"; //NOI18N
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "CPP_Provider_Display_Name");
    }

    // synchronized is just in case here - it shouldn't be called async
    @Override
    public synchronized void computeSymbolNames(Context context, Result res) {
        if (TRACE) {
            LOG.log(Level.INFO, "computeSymbolNames {0}", toString(context)); // NOI18N
        }
        CsmSelect.NameAcceptor nameAcceptor = createNameAcceptor(context.getText(), context.getSearchType());
        if (nameAcceptor == null) {
            if (CndUtils.isDebugMode()) {
                Logger log = Logger.getLogger("org.netbeans.modules.cnd.gotodeclaration"); // NOI18N
                log.log(Level.SEVERE, "Can not create matcher for ''{0}'' search type {1}", new Object[]{context.getText(), context.getSearchType()}); //NOI18N
            }
            return;
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
                task = new WorkerTask(context, nameAcceptor);
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
                LOG.log(Level.INFO, "Results are not fully available yet at {0} ms.", System.currentTimeMillis()-task.startTime); // NOI18N
            }
        } else {
            if (TRACE) {
                LOG.log(Level.INFO, "Results are fully available at {0} ms.", System.currentTimeMillis()-task.startTime); // NOI18N
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

    private String toString(Context context) {
        return String.format("Context: prj=%s type=%s text=%s", context.getProject(), context.getSearchType(), context.getText()); //NOI18N
    }
    
    public static CsmSelect.NameAcceptor createNameAcceptor(final String text, final SearchType searchType) {
        final NameMatcher nameMatcher = NameMatcherFactory.createNameMatcher(text, searchType);
        return new NameAcceptorImpl(nameMatcher);
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

    private static final class NameAcceptorImpl implements NameAcceptor {

        private final NameMatcher nameMatcher;

        public NameAcceptorImpl(NameMatcher nameMatcher) {
            this.nameMatcher = nameMatcher;
        }

        @Override
        public boolean accept(CharSequence name) {
            return nameMatcher.accept(name.toString());
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 17 * hash + this.nameMatcher.hashCode();
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final NameAcceptorImpl other = (NameAcceptorImpl) obj;
            return this.nameMatcher.equals(other.nameMatcher);
        }
    }

    private static class WorkerTask extends FutureTask<Void> {

        private final Set<CppSymbolDescriptor> result;
        private final Context context;
        private final AtomicBoolean cancelled;
        private long startTime;

        public WorkerTask(Context context, CsmSelect.NameAcceptor nameAcceptor) {
            this(context, nameAcceptor, new HashSet<CppSymbolDescriptor>(), new AtomicBoolean(false));
        }
        
        private WorkerTask(Context context, CsmSelect.NameAcceptor nameAcceptor, Set<CppSymbolDescriptor> result, AtomicBoolean cancelled) {
            super(new Worker(context, nameAcceptor, result, cancelled), null);
            this.context = context;
            this.result = result;
            this.cancelled = cancelled;
            if (TRACE) {
                startTime = System.currentTimeMillis();
            }
        }

        private List<CppSymbolDescriptor> getResult() {
            synchronized (resultLock) {
                return new ArrayList<CppSymbolDescriptor>(result);
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
        private final Set<CppSymbolDescriptor> result;
        private long startTime;
        private final AtomicBoolean cancelled;
        private final CsmSelect.NameAcceptor nameAcceptor;

        private Worker(Context context, CsmSelect.NameAcceptor nameAcceptor, Set<CppSymbolDescriptor> result, AtomicBoolean cancelled) {
            if (TRACE) {
                LOG.log(Level.INFO, "New Worker for searching \"{0}\", {1} in {2} created.", // NOI18N
                        new Object[]{context.getText(), context.getSearchType().name(), context.getProject()});
            }
            this.context = context;
            this.result = result;
            this.cancelled = cancelled;
            this.nameAcceptor = nameAcceptor;
        }

        @Override
        public void run() {
            if (TRACE) {
                startTime = System.currentTimeMillis();
            }
            try {
                CsmCacheManager.enter();
                try {
                    collect(context);
                } finally {
                    CsmCacheManager.leave();
                }
                if (TRACE) {
                    LOG.log(Level.INFO, "Worker for searching \"{0}\", {1} in {2} cancelled [after {3} ms.].", // NOI18N
                            new Object[]{context.getText(), context.getSearchType().name(), context.getProject(), (System.currentTimeMillis() - startTime)});
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

        private void collect(Context context) {
            if (context.getProject() == null) {
                Set<CsmProject> libs = new HashSet<CsmProject>();
                for (CsmProject csmProject : CsmModelAccessor.getModel().projects()) {
                    checkCancelled();
                    collectSymbols(csmProject);
                    collectLibs(csmProject, libs);
                }
                for (CsmProject csmProject : libs) {
                    checkCancelled();
                    collectSymbols(csmProject);
                }
            } else {
                NativeProject nativeProject = context.getProject().getLookup().lookup(NativeProject.class);
                if (nativeProject != null) {
                    CsmProject csmProject = CsmModelAccessor.getModel().getProject(nativeProject);
                    if (csmProject != null) {
                        collectSymbols(csmProject);
                    }
                }
            }
        }

        private void collectLibs(CsmProject project, Collection<CsmProject> libs) {
            for( CsmProject lib : project.getLibraries()) {
                if (! libs.contains(lib)) {
                    libs.add(lib);
                    collectLibs(lib, libs);
                }
            }
        }

        private void collectSymbols(CsmProject csmProject) {

            // process project namespaces
            collectSymbols(csmProject.getGlobalNamespace());

            CsmSelect.CsmFilter nameFilter = CsmSelect.getFilterBuilder().createNameFilter(nameAcceptor);

            // process project files
            for(CsmFile csmFile : csmProject.getAllFiles()) {
                checkCancelled();
                // macros
                Iterator<CsmMacro> macros = CsmSelect.getMacros(csmFile, nameFilter);
                while (macros.hasNext()) {
                    checkCancelled();
                    CsmMacro macro = macros.next();
                    if (nameAcceptor.accept(macro.getName())) {
                        if(CsmVisibilityQuery.isVisible(macro)) {
                            addResult(new CppSymbolDescriptor(macro));
                        }
                    }
                }
                checkCancelled();
                // static functions
                Iterator<CsmFunction> funcs = CsmSelect.getStaticFunctions(csmFile, nameFilter);
                while (funcs.hasNext()) {
                    checkCancelled();
                    CsmFunction func = funcs.next();
                    if (nameAcceptor.accept(func.getName())) {
                        if (CsmKindUtilities.isFunctionDefinition(func)) { // which is unlikely, but just in case
                            if(CsmVisibilityQuery.isVisible(func)) {
                                addResult(new CppSymbolDescriptor(func));
                            }
                        } else {
                            // static functions definitions are not returned by Select;
                            // neither do they reside in namespace
                            CsmFunctionDefinition definition = func.getDefinition();
                            if (definition != null ) {
                                if(CsmVisibilityQuery.isVisible(definition)) {
                                    addResult(new CppSymbolDescriptor(definition));
                                }
                            }
                        }
                    }
                }
                checkCancelled();
                CsmSelect.CsmFilter definitions = CsmSelect.getFilterBuilder().createCompoundFilter(nameFilter,  CsmSelect.getFilterBuilder().createKindFilter(Kind.FUNCTION_DEFINITION));
                Iterator<CsmOffsetableDeclaration> declarations = CsmSelect.getDeclarations(csmFile, definitions);
                while (declarations.hasNext()) {
                    checkCancelled();
                    CsmOffsetableDeclaration decl = declarations.next();
                    if (nameAcceptor.accept(decl.getName())) {
                        if (CsmKindUtilities.isFunctionDefinition(decl) && ((CsmFunction)decl).isStatic()) {
                            CsmFunction func = (CsmFunction) decl;
                            if (func.equals(func.getDeclaration()) && CsmKindUtilities.isFile(func.getScope())) {
                                if(CsmVisibilityQuery.isVisible(func)) {
                                    addResult(new CppSymbolDescriptor(func));
                                }
                            }
                        }
                    }
                }
                checkCancelled();
                // static variables
                Iterator<CsmVariable> vars = CsmSelect.getStaticVariables(csmFile, nameFilter);
                while (vars.hasNext()) {
                    checkCancelled();
                    CsmVariable var = vars.next();
                    if (nameAcceptor.accept(var.getName())) {
                        if(CsmVisibilityQuery.isVisible(var)) {
                            addResult(new CppSymbolDescriptor(var));
                        }
                    }
                }
            }
        }

        private void collectSymbols(CsmNamespace namespace) {

            // we can filter out "simple" (non-class) namespace elements via CsmSelect;
            // later we have to instantiate classes and enums to check their *members* as well

            CsmSelect.CsmFilter nameFilter = CsmSelect.getFilterBuilder().createNameFilter(nameAcceptor);

            CsmSelect.CsmFilter simpleKindFilter = CsmSelect.getFilterBuilder().createKindFilter(
                    CsmDeclaration.Kind.FUNCTION, CsmDeclaration.Kind.FUNCTION_DEFINITION, 
                    CsmDeclaration.Kind.FUNCTION_FRIEND, CsmDeclaration.Kind.FUNCTION_FRIEND_DEFINITION,
                    CsmDeclaration.Kind.VARIABLE, CsmDeclaration.Kind.TYPEDEF);

            CsmSelect.CsmFilter simpleNameAndKindFilter = CsmSelect.getFilterBuilder().createCompoundFilter(nameFilter, simpleKindFilter);

            Iterator<? extends CsmOffsetableDeclaration> declarations = CsmSelect.getDeclarations(namespace, simpleNameAndKindFilter);
            while (declarations.hasNext()) {
                checkCancelled();
                CsmOffsetableDeclaration decl = declarations.next();
                if (nameAcceptor.accept(decl.getName())) {
                    addDeclarationItself(decl);
                }
            }

            // instantiate classes and enums to check them and their members as well
            CsmSelect.CsmFilter kindFilter = CsmSelect.getFilterBuilder().createKindFilter(
                    CsmDeclaration.Kind.CLASS, CsmDeclaration.Kind.ENUM, CsmDeclaration.Kind.STRUCT);
            CsmSelect.CsmFilter classesOrMembers = CsmSelect.getFilterBuilder().createOrFilter(kindFilter, nameFilter);
            declarations = CsmSelect.getDeclarations(namespace, kindFilter);
            while (declarations.hasNext()) {
                checkCancelled();
                addDeclarationIfNeed(declarations.next(), classesOrMembers, nameFilter);
            }

            // process nested namespaces
            for (CsmNamespace child : namespace.getNestedNamespaces()) {
                checkCancelled();
                collectSymbols(child);
            }
        }

        /**
         * Is called for classes, enums and their members.
         * Checks name, if it suites, adds result to symbols collection.
         * Does the same recursively (with members/enumerators)
         */
        private void addDeclarationIfNeed(CsmOffsetableDeclaration decl, CsmSelect.CsmFilter classesOrMembers, CsmSelect.CsmFilter nameFilter) {
            if (nameAcceptor.accept(decl.getName())) {
                addDeclarationItself(decl);
            }
            if (CsmKindUtilities.isClass(decl)) {
                CsmClass cls = (CsmClass) decl;
                final Iterator<CsmMember> classMembers = CsmSelect.getClassMembers(cls, classesOrMembers);
                while(classMembers.hasNext()) {
                    addDeclarationIfNeed(classMembers.next(), classesOrMembers, nameFilter);
                }
            } else if (CsmKindUtilities.isEnum(decl)) {
                CsmEnum en = (CsmEnum) decl;
                Iterator<CsmEnumerator> enumerators = CsmSelect.getEnumerators(en, nameFilter);
                while(enumerators.hasNext()) {
                    CsmEnumerator enumerator = enumerators.next();
                    if (nameAcceptor.accept(enumerator.getName())) {
                        if(CsmVisibilityQuery.isVisible(enumerator)) {
                            addResult(new CppSymbolDescriptor(enumerator));
                        }
                    }
                }
            }
        }

        private void addDeclarationItself(CsmOffsetableDeclaration decl) {
            if (CsmKindUtilities.isFunction(decl)) {
                // do not add declarations if their definitions exist
                if (CsmKindUtilities.isFunctionDefinition(decl)) {
                    if(CsmVisibilityQuery.isVisible(decl)) {
                        addResult(new CppSymbolDescriptor(decl));
                    }
                } else {
                    boolean added = false;
                    CsmFunctionDefinition definition = ((CsmFunction) decl).getDefinition();
                    if (definition != null) {
                        if(CsmVisibilityQuery.isVisible(definition)) {
                            added = true;
                            if (definition != decl) {
                                addResult(new CppSymbolDescriptor(decl, definition));
                            } else {
                                addResult(new CppSymbolDescriptor(definition));
                            }
                        }
                    }
                    if (!added) {
                        if(CsmVisibilityQuery.isVisible(decl)) {
                            addResult(new CppSymbolDescriptor(decl));
                        }
                    }
                }
            } else {
                if(CsmVisibilityQuery.isVisible(decl)) {
                    addResult(new CppSymbolDescriptor(decl));
                }
            }
        }

        private void checkCancelled() throws CancellationException {
            if (cancelled.get()) {
                throw new CancellationException();
            }
        }

        private void addResult(CppSymbolDescriptor symbolDescriptor) {
            synchronized (resultLock) {
                result.add(symbolDescriptor);
            }
        }
    }
}
