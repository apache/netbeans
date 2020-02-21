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

package org.netbeans.modules.cnd.mixeddev;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.services.CsmSymbolResolver;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeSession;
import org.netbeans.modules.cnd.debugger.common2.debugger.State;
import org.netbeans.modules.cnd.debugger.common2.debugger.StateListener;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineType;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineTypeManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.debugtarget.DebugTarget;
import org.netbeans.modules.cnd.debugger.common2.debugger.remote.Host;
import org.netbeans.modules.cnd.debugger.common2.utils.PsProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.DevelopmentHostConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.mixeddev.java.JNISupport;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

/**
 *
 */
public final class MixedDevUtils {
        
    public static final String DOT = "."; // NOI18N
    
    public static final String COMMA = ","; // NOI18N
    
    public static final String LPAREN = "("; // NOI18N
    
    public static final String RPAREN = ")"; // NOI18N
    
    public static final String SCOPE = "::"; // NOI18N
    
    public static final String POINTER = "*"; // NOI18N
    
    public static String stringize(Collection<? extends CharSequence> collection, CharSequence separator) {
        boolean first = true;
        StringBuilder result = new StringBuilder();
        for (CharSequence seq : collection) {
            if (!first) {
                result.append(separator);
            } else {
                first = false;
            }
            result.append(seq);
        }
        return result.toString();
    }
    
    public static String repeat(String pattern, int times) {
        StringBuilder sb = new StringBuilder();
        while (times-- > 0) {
            sb.append(pattern);
        }
        return sb.toString();
    }
    
    public static <K, V> Map<K, V> createMapping(Pair<K, V> ... pairs) {
        Map<K, V> mapping = new HashMap<K, V>();
        for (Pair<K, V> pair : pairs) {
            mapping.put(pair.first(), pair.second());
        }
        return Collections.unmodifiableMap(mapping);
    }    
    
    public static interface Converter<F, T> {

        T convert(F from);

    }

    public static <F, T> T[] transform(F[] from, Converter<F, T> converter, Class<T> toClass) {
        T[] to = (T[]) Array.newInstance(toClass, from.length);
        for (int i = 0; i < from.length; i++) {
            to[i] = converter.convert(from[i]);
        }
        return to;
    }

    public static <F, T> List<T> transform(List<F> from, Converter<F, T> converter) {
        List<T> to = new ArrayList<T>(from.size());
        for (F f : from) {
            to.add(converter.convert(f));
        }
        return to;
    }    
    
    public static <T> List<T> toList(Iterable<T> iterable) {
        List<T> result = new ArrayList<>();
        for (T t : iterable) {
            result.add(t);
        }
        return result;
    }
    
    public static Iterable<NativeProject> findNativeProjects() {
        final Project[] projects = OpenProjects.getDefault().getOpenProjects();
        return new Iterable<NativeProject>() {
            @Override
            public Iterator<NativeProject> iterator() {
                return new Iterator<NativeProject>() {
                    
                    private int i = 0;
                    
                    private NativeProject nextProject = findNext();

                    @Override
                    public boolean hasNext() {
                        return nextProject != null;
                    }

                    @Override
                    public NativeProject next() {
                        NativeProject current = nextProject;
                        nextProject = findNext();
                        return current;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException("Not supported."); // NOI18N
                    }
                    
                    private NativeProject findNext() {
                        NativeProject nativeProject = null;
                        while (nativeProject == null && i < projects.length) {
                            nativeProject = projects[i].getLookup().lookup(NativeProject.class);
                            ++i;
                        }
                        return nativeProject;
                    }
                };
            }
        };
    }
    
    public static CsmOffsetable findCppSymbol(String cppNames[]) {
        if (cppNames != null) {
            for (NativeProject nativeProject : findNativeProjects()) {
                for (String cppName : cppNames) {
                    Collection<CsmOffsetable> candidates = CsmSymbolResolver.resolveSymbol(nativeProject, cppName);
                    if (!candidates.isEmpty()) {
                        return candidates.iterator().next();
                    }
                }
            }
        }
        return null;
    }

    public static MakeConfiguration findCppFunctionMakeConfiguration(String cppNames[]) {
        if (cppNames != null) {
            for (NativeProject nativeProject : findNativeProjects()) {
                for (String cppName : cppNames) {
                    Collection<CsmOffsetable> candidates = CsmSymbolResolver.resolveGlobalFunction(nativeProject, cppName);
                    if (!candidates.isEmpty()) {
                        Lookup.Provider project = nativeProject.getProject();
                        if (project instanceof Project) {
                            ConfigurationDescriptorProvider provider = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
                            if (provider != null && provider.gotDescriptor()) {
                                MakeConfigurationDescriptor descriptor = provider.getConfigurationDescriptor();
                                if (descriptor != null) {
                                    return descriptor.getActiveConfiguration();
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
    
    public static Session  attachToJavaProcess(String processFilter, String javaClassName, 
            String javaMethodName, final boolean isAutoStart){
        PsProvider.PsData psData = PsProvider.getDefault(Host.getLocal()).getData(false);
        if (psData == null) {
            NativeDebuggerManager.warning(NbBundle.getMessage(MixedDevUtils.class,"MSG_PS_Failed")); // NOI18N
            return null;
        }
        final Vector<Vector<String>> processes = psData.processes(
            // Pattern.compile(".*java.+:" + properties.get("conn_port") + ".*") // NOI18N
            Pattern.compile(processFilter) // NOI18N
        );
        if (processes.size() != 1) {
            NativeDebuggerManager.warning(NbBundle.getMessage(MixedDevUtils.class, "MSG_ProcessDetectionError", processFilter)); // NOI18N
            return null;
        }
        final String funcName = MethodMapper.getNativeName(
              //  "" + properties.get("javaClass") + "." + properties.get("javaMethod")); // NOI18N
                  "" + javaClassName + "." + javaMethodName); // NOI18N
        final String stringPid = processes.firstElement().get(psData.pidColumnIdx());
        final long longPid = Long.parseLong(stringPid);
        Session ret = recognizeSessionByPid(longPid);
        if (ret == null) {
            MakeConfiguration cppSymbolMakeConfiguration = MixedDevUtils.findCppFunctionMakeConfiguration(new String[]{funcName});
            final DebugTarget target = new DebugTarget();
            setDefaultEngine(target, ExecutionEnvironmentFactory.getLocal(), cppSymbolMakeConfiguration);
            target.setPid(longPid);
            // local case only
            target.setHostName("localhost"); // NOI18N
            target.getConfig().setDevelopmentHost(new DevelopmentHostConfiguration(ExecutionEnvironmentFactory.getLocal()));
            target.setProjectMode(DebugTarget.ProjectMode.NO_PROJECT);

            final CountDownLatch latch = new CountDownLatch(1);
            final NativeDebuggerManager.DebuggerStateListener listener = new NativeDebuggerManager.DebuggerStateListener() {
                @Override
                public void notifyAttached(NativeDebugger debugger, long pid) {
                    if (pid == longPid) {
                        // we now remove in finally block - should we still remove it here?
                        NativeDebuggerManager.get().removeDebuggerStateListener(this);
                        //introduce the possibility to attach to JavaProcess (breakpoints are set already)
                        if (!isAutoStart || funcName == null) {
                            debugger.stepTo(funcName);
                        }
                        latch.countDown();
                    }
                }
            };
            NativeDebuggerManager.get().addDebuggerStateListener(listener);
            //use new method to attach and do not continue after attach. see bz#256134
            NativeDebuggerManager.get().attach(isAutoStart, target);
            try {
                latch.await(100, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } finally {                
                NativeDebuggerManager.get().removeDebuggerStateListener(listener);
            }
            NativeDebugger currentDebugger = NativeDebuggerManager.get().currentDebugger();
            if (currentDebugger != null) {
                NativeSession nativeSession = currentDebugger.session();
                if (nativeSession != null) {
                    ret = nativeSession.coreSession();
                }
            }
        } else {
            final NativeDebugger debugger = NativeSession.map(ret).getDebugger();
            debugger.addStateListener(new StateListener() {

                @Override
                public void update(State state) {
                    if (!state.isRunning) {
                        debugger.stepTo(funcName);
                        debugger.removeStateListener(this);
                    }
                }
            });
            debugger.pause();
        }

        return ret;
    }
    
    private static Session recognizeSessionByPid(long pid) {
        for (NativeSession nativeSession : NativeDebuggerManager.get().getSessions()) {
            if (nativeSession.getPid() == pid) {
                return nativeSession.coreSession();
            }
        }
        return null;
    }

    private static void setDefaultEngine(DebugTarget target, ExecutionEnvironment env, MakeConfiguration conf) {
        if (conf != null) {
            CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
            if (compilerSet != null) {
                EngineType engine = getEngineFromCompilerSet(compilerSet);
                if (engine != null) {
                    target.setEngine(engine);
                    target.setCompilerSet(compilerSet);
                    String id = EngineTypeManager.engine2DebugProfileID(engine);
                    target.getDbgProfile().assign(conf.getAuxObject(id));
                    return;
                }
            }
        }
        CompilerSetManager compilerSetManager = CompilerSetManager.get(env);
        if (compilerSetManager != null) {
            CompilerSet defaultCompilerSet = compilerSetManager.getDefaultCompilerSet();
            if (defaultCompilerSet != null) {
                EngineType engine = getEngineFromCompilerSet(defaultCompilerSet);
                if (engine != null) {
                    target.setEngine(engine);
                    target.setCompilerSet(defaultCompilerSet);
                }
            }
        }
    }

    private static EngineType getEngineFromCompilerSet(CompilerSet cs) {
        Tool debuggerTool = cs.getTool(PredefinedToolKind.DebuggerTool);
        if (debuggerTool != null) {
            ToolchainManager.DebuggerDescriptor descriptor = (ToolchainManager.DebuggerDescriptor) debuggerTool.getDescriptor();
            return EngineTypeManager.getEngineTypeForDebuggerDescriptor(descriptor);
        }
        return null;
    }

//    private static void insertBreakpoint(NativeDebugger debugger, String functionName) {
//        NativeBreakpoint bpt = NativeBreakpoint.newFunctionBreakpoint(functionName);
//        if (bpt != null) {
//            int routingToken = RoutingToken.BREAKPOINTS.getUniqueRoutingTokenInt();
//            bpt.setTemp(true);
//            Handler.postNewHandler(debugger, bpt, routingToken);
//        }
////        debugger.stepTo(functionName);
//    }
    

    private static final class MethodMapper {
        /*package*/ static String getNativeName(String javaName) {
            return JNISupport.getCppMethodSignature(javaName.replaceAll("[.]", "/")); // NOI18N
        }
    }

//    /*package*/ String resolveSymbol(CharSequence funcName) {
//        Project[] projects = OpenProjects.getDefault().getOpenProjects();
//        for (Project prj : projects) {
//            NativeProject nativeProject = prj.getLookup().lookup(NativeProject.class);
//            if (nativeProject != null) {
//                Collection<CsmOffsetable> candidates = CsmSymbolResolver.resolveSymbol(nativeProject, funcName);
//                if (!candidates.isEmpty()) {
//                    CsmOffsetable candidate = candidates.iterator().next();
////                    CsmUtilities.openSource(candidate);
//                    if (CsmKindUtilities.isFunction(candidate)) {
//                        return ((CsmFunction) candidate).getQualifiedName().toString();
//                    }
//                    break;
//                }
//            }
//        }
//        return null;
//    }

    private MixedDevUtils() {
        throw new AssertionError("Not instantiable");
    }
}
