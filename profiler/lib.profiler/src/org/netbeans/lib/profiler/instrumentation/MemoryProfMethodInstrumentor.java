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

package org.netbeans.lib.profiler.instrumentation;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.lib.profiler.classfile.BaseClassInfo;
import org.netbeans.lib.profiler.classfile.ClassInfo;
import org.netbeans.lib.profiler.classfile.ClassRepository;
import org.netbeans.lib.profiler.classfile.DynamicClassInfo;
import org.netbeans.lib.profiler.filters.InstrumentationFilter;
import org.netbeans.lib.profiler.global.ProfilingSessionStatus;
import org.netbeans.lib.profiler.utils.StringUtils;
import org.netbeans.lib.profiler.utils.VMUtils;
import org.netbeans.lib.profiler.wireprotocol.RootClassLoadedCommand;


/**
 * Base class providing common functionality for instrumenting TA methods to generate object allocation/liveness data.
 * The fact that there are two classes at this time, MemoryProfMethodInstrumentor and ObjLivenessMethodInstrumentor, is
 * explained by purely historical reasons - in the past we had instrumentation implemented differently for object allocation
 * and liveness profiling. Now the same kind of instrumentation is used for both profiling types (the exact method names
 * for injected calls are different, but the way of injecting these calls and their signatures are exactly the same).
 *
 * In principle, MemoryProfMethodInstrumentor and ObjLivenessMethodInstrumentor can be merged, but there is no compelling
 * need for that. Furthermore, if in future say some different memory instrumentation kind is introduced, this division
 * may help.
 *
 * @author Misha Dmitriev
 */
public abstract class MemoryProfMethodInstrumentor extends ClassManager {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    static class MethodScanerForNewOpcodes extends SingleMethodScaner {
        private final InstrumentationFilter instrFilter;

        MethodScanerForNewOpcodes(ClassInfo clazz, int methodIdx, InstrumentationFilter filter) {
            super(clazz, methodIdx);
            instrFilter = filter;
        }
        
        //~ Methods --------------------------------------------------------------------------------------------------------------

        boolean hasNewOpcodes(MemoryProfMethodInstrumentor minstr, boolean checkForOpcNew, boolean checkForOpcNewArray) {
            if (!checkForOpcNew && !checkForOpcNewArray) return false;
            int loaderId = clazz.getLoaderId();
            boolean found = false;
            int bc;
            int bci = 0;

            while (bci < bytecodesLength) {
                bc = (bytecodes[bci] & 0xFF);

                if ((bc == opc_new) && checkForOpcNew) {

                    int classCPIdx = getU2(bci + 1);
                    String refClassName = clazz.getRefClassName(classCPIdx);
                    if (instrFilter.passes(refClassName)) {
                        found = true;
                        BaseClassInfo refClazz = javaClassOrPlaceholderForName(refClassName, loaderId);

                        if (refClazz.getInstrClassId() == -1) {
                            refClazz.setInstrClassId(minstr.getNextClassId(refClazz.getName()));
                        }
                    }                    
                } else if ((bc == opc_anewarray || bc == opc_multianewarray) && checkForOpcNewArray) {

                    int classCPIdx = getU2(bci + 1);
                    String refClassName = clazz.getRefClassName(classCPIdx);
                    BaseClassInfo refClazz = null;
                    if (bc == opc_anewarray) {
                        if (instrFilter.passes(refClassName.concat("[]"))) {    // NOI18N
                            refClazz = javaClassForObjectArrayType(refClassName);
                        }
                    } else { // opc_multianewarray
                        if (instrFilter.passes(getMultiArrayClassName(refClassName))) {
                            refClazz = ClassRepository.lookupSpecialClass(refClassName);
                        }
                    }

                    if (refClazz != null) { // Warning already issued
                        found = true;

                        if (refClazz.getInstrClassId() == -1) {
                            refClazz.setInstrClassId(minstr.getNextClassId(refClazz.getName()));
                        }
                    }
                } else if (bc == opc_newarray && checkForOpcNewArray) {
                    int arrayClassId = getByte(bci + 1);
                    BaseClassInfo refClazz = javaClassForPrimitiveArrayType(arrayClassId);
                    String className = StringUtils.userFormClassName(refClazz.getName());

                    if (instrFilter.passes(className)) {
                        found = true;
                        if (refClazz.getInstrClassId() == -1) {
                            refClazz.setInstrClassId(minstr.getNextClassId(refClazz.getName()));
                        }
                    }
                }

                bci += opcodeLength(bci);
            }

            return found;
        }

        private static String getMultiArrayClassName(String refClassName) {
            int dimension = refClassName.lastIndexOf('[');
            String baseClass = refClassName.substring(dimension + 1);

            if (VMUtils.isVMPrimitiveType(baseClass)) {
                return StringUtils.userFormClassName(refClassName);
            } else {
                StringBuilder arrayClass = new StringBuilder(refClassName.length() + dimension + 1);
                arrayClass.append(refClassName.substring(dimension + 1));

                for (int i = 0; i <= dimension; i++) {
                    arrayClass.append("[]");        // NOI18N
                }
                return arrayClass.toString();
            }
        }
    }

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    protected ArrayList instrClasses = new ArrayList();
    protected String[] instantiatableClasses;
    protected int injType;
    protected int instrClassId;
    protected int nInstantiatableClasses;
    protected int nInstrClasses;
    protected int nInstrMethods;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public MemoryProfMethodInstrumentor(ProfilingSessionStatus status, int injType) {
        super(status);
        this.status = status;
        instantiatableClasses = new String[100];
        this.injType = injType;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public Object[] getInitialMethodsToInstrument(RootClassLoadedCommand rootLoaded) {
        List classes = new ArrayList();
        resetLoadedClassData();
        initInstrumentationPackData();
        instrClassId = 0;

        storeClassFileBytesForCustomLoaderClasses(rootLoaded);

        String[] loadedClasses = rootLoaded.getAllLoadedClassNames();
        int[] loadedClassLoaderIds = rootLoaded.getAllLoadedClassLoaderIds();
        for (int i = 0; i < loadedClasses.length; i++) {
            DynamicClassInfo clazz = javaClassForName(loadedClasses[i], loadedClassLoaderIds[i]);

            if (classNeedsInstrumentation(clazz)) {
                clazz.preloadBytecode();
            }
            classes.add(clazz);
        }
        for (Object clazz : classes) {
            findAndMarkMethodsToInstrumentInClass((DynamicClassInfo) clazz);
        }

        return createInstrumentedMethodPack();
    }

    public String[] getInstantiatableClasses() {
        return instantiatableClasses;
    }

    public Object[] getMethodsToInstrumentUponClassLoad(String className, int classLoaderId) {
        initInstrumentationPackData();
        findAndMarkMethodsToInstrumentInClass(className, classLoaderId);

        return createInstrumentedMethodPack();
    }

    public int getNInstantiatableClasses() {
        return nInstantiatableClasses;
    }

    /** Checks if there are any methods in this class that need to be instrumented. */
    protected void findAndMarkMethodsToInstrumentInClass(String className, int classLoaderId) {
        findAndMarkMethodsToInstrumentInClass(javaClassForName(className, classLoaderId));
    }
    
    /** Checks if there are any methods in this class that need to be instrumented. */
    protected void findAndMarkMethodsToInstrumentInClass(DynamicClassInfo clazz) {
        if (clazz == null) {
            return; // Warning has already been reported
        }

        if (!clazz.isLoaded()) {
            clazz.setLoaded(true);

            // We assign an ID to class no matter whether or not this class is going to be instantiated anywhere in the program
            // As a result, we may have quite some classes that are in the appropriate table in 'status', but have zero objects
            // associated with them forever.
            // I am not sure why this is done here, but perhaps there was a good reason for this. Need to comment such things immediately...
            if ((clazz.getInstrClassId() == -1) && !clazz.isInterface()) {
                clazz.setInstrClassId(getNextClassId(clazz.getName()));
            }

            if (classNeedsInstrumentation(clazz)) {
                String[] methodNames = clazz.getMethodNames();
                boolean found = false;

                for (int i = 0; i < methodNames.length; i++) {
                    if (clazz.isMethodNative(i) || clazz.isMethodAbstract(i)) {
                        clazz.setMethodUnscannable(i);

                        continue;
                    }

                    if (methodNeedsInstrumentation(clazz, i)) {
                        nInstrMethods++;
                        clazz.setMethodInstrumented(i);
                        found = true;
                    }
                }

                if (found) {
                    nInstrClasses++;
                    instrClasses.add(clazz);
                }
            }
        }
    }

    protected void initInstrumentationPackData() {
        instrClasses.clear();
        nInstrClasses = nInstrMethods = 0;
        nInstantiatableClasses = 0;
    }

    protected abstract boolean classNeedsInstrumentation(ClassInfo clazz);
    protected abstract boolean methodNeedsInstrumentation(ClassInfo clazz, int methodIdx);

    /** Creates a multi-class packet of instrumented methods or classes */
    protected Object[] createInstrumentedMethodPack() {
        if (nInstrMethods == 0) {
            return null;
        }

        return createInstrumentedMethodPack15();
    }

    protected boolean hasNewOpcodes(ClassInfo clazz, int methodIdx, boolean checkForOpcNew, boolean checkForOpcNewArray, InstrumentationFilter instrFilter) {
        MethodScanerForNewOpcodes msfno = new MethodScanerForNewOpcodes(clazz, methodIdx, instrFilter);

        return msfno.hasNewOpcodes(this, checkForOpcNew, checkForOpcNewArray);
    }

    protected abstract byte[] instrumentMethod(DynamicClassInfo clazz, int methodIdx);

    protected boolean methodNeedsRewriting(DynamicClassInfo clazz, int methodIdx) {
        return clazz.isMethodInstrumented(methodIdx);
    }

    int getNextClassId(String className) {
        if (nInstantiatableClasses == instantiatableClasses.length) {
            String[] oldInstantiatableClasses = instantiatableClasses;
            instantiatableClasses = new String[oldInstantiatableClasses.length + 100];
            System.arraycopy(oldInstantiatableClasses, 0, instantiatableClasses, 0, oldInstantiatableClasses.length);
        }

        instantiatableClasses[nInstantiatableClasses++] = className;
        status.updateAllocatedInstancesCountInfoInClient(className);

        return instrClassId++;
    }

    /** Creates the 1.5-style array of instrumented class files. */
    private Object[] createInstrumentedMethodPack15() {
        String[] instrMethodClasses = new String[nInstrClasses];
        int[] instrClassLoaderIds = new int[nInstrClasses];
        byte[][] replacementClassFileBytes = new byte[nInstrClasses][];

        for (int j = 0; j < nInstrClasses; j++) {
            DynamicClassInfo clazz = (DynamicClassInfo) instrClasses.get(j);
            instrMethodClasses[j] = clazz.getName().replace('/', '.'); // NOI18N
            instrClassLoaderIds[j] = clazz.getLoaderId();

            String[] methodNames = clazz.getMethodNames();
            int nMethods = methodNames.length;
            byte[][] replacementMethodInfos = new byte[nMethods][];

            DynamicConstantPoolExtension.getCPFragment(clazz, injType);

            for (int i = 0; i < nMethods; i++) {
                if (methodNeedsRewriting(clazz, i)) {
                    replacementMethodInfos[i] = instrumentMethod(clazz, i);
                } else {
                    replacementMethodInfos[i] = clazz.getMethodInfo(i);
                }
            }

            DynamicConstantPoolExtension wholeECP = DynamicConstantPoolExtension.getAllAddedCPFragments(clazz);
            int nAddedCPEntries = wholeECP.getNEntries();
            byte[] addedCPContents = wholeECP.getContents();
            replacementClassFileBytes[j] = ClassRewriter.rewriteClassFile(clazz, replacementMethodInfos, nAddedCPEntries,
                                                                          addedCPContents);
        }

        return new Object[] { instrMethodClasses, instrClassLoaderIds, replacementClassFileBytes };
    }
}
