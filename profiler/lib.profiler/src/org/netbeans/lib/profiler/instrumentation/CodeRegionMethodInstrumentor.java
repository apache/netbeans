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

import org.netbeans.lib.profiler.classfile.ClassInfo;
import org.netbeans.lib.profiler.classfile.ClassRepository;
import org.netbeans.lib.profiler.classfile.ClassRepository.CodeRegionBCI;
import org.netbeans.lib.profiler.classfile.DynamicClassInfo;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.client.ClientUtils.SourceCodeSelection;
import org.netbeans.lib.profiler.global.ProfilingSessionStatus;
import org.netbeans.lib.profiler.utils.MiscUtils;
import java.io.IOException;
import java.util.ArrayList;


/**
 * High-level access to functionality that instruments a (so far single) code region in a (single again) TA method.
 *
 * @author Tomas Hurka
 * @author Misha Dmitriev
 * @author Ian Formanek
 */
public class CodeRegionMethodInstrumentor extends ClassManager {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private ClientUtils.SourceCodeSelection sourceCodeSelection;
    private ArrayList instrClasses;
    private String className;
    private int nInstrClasses;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public CodeRegionMethodInstrumentor(ProfilingSessionStatus status, SourceCodeSelection codeSelection) {
        super(status);
        sourceCodeSelection = codeSelection;
        className = sourceCodeSelection.getClassName().replace('.', '/').intern(); // NOI18N
        instrClasses = new ArrayList();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public Object[] getFollowUpInstrumentCodeRegionResponse(int classLoaderId) {
        DynamicClassInfo clazz = javaClassForName(className, classLoaderId);

        instrClasses.clear();
        instrClasses.add(clazz);
        nInstrClasses = 1;

        return createInstrumentedMethodPack();
    }

    protected Object[] createInstrumentedMethodPack() {
        if (nInstrClasses == 0) {
            return null;
        }

        return createInstrumentedMethodPack15();
    }

    Object[] getInitialInstrumentCodeRegionResponse(String[] loadedClasses, int[] loadedClassLoaderIds) {
        DynamicClassInfo clazz = null;

        // We may have more than one version of the class with the given name, hence this search and instrClasses array
        for (int i = 0; i < loadedClasses.length; i++) {
            String loadedClassName = loadedClasses[i];

            if (className == loadedClassName) {
                clazz = javaClassForName(loadedClasses[i], loadedClassLoaderIds[i]);

                if (clazz != null) {
                    CodeRegionBCI instrLocation = computeCodeRegionFromSourceCodeSelection(clazz);

                    if (instrLocation != null) {
                        int mIdx = clazz.getMethodIndex(instrLocation.methodName, instrLocation.methodSignature);

                        if (mIdx != -1) { // Not all class versions may have this method
                            clazz.setLoaded(true);
                            instrClasses.add(clazz);
                        }
                    }
                }
            }
        }

        nInstrClasses = instrClasses.size();

        return createInstrumentedMethodPack();
    }

    private CodeRegionBCI computeCodeRegionFromSourceCodeSelection(ClassInfo clazz) {
        try {
            if (sourceCodeSelection.definedViaSourceLines()) {
                int startLine = sourceCodeSelection.getStartLine();
                int endLine = sourceCodeSelection.getEndLine();
                CodeRegionBCI loc = ClassRepository.getMethodForSourceRegion(clazz, startLine, endLine);

                status.beginTrans(true);

                try {
                    status.setInstrMethodNames(new String[] { loc.methodName });
                    status.setInstrMethodSignatures(new String[] { loc.methodSignature });
                } finally {
                    status.endTrans();
                }

                return loc;
            } else if (sourceCodeSelection.definedViaMethodName()) {
                String methodName = sourceCodeSelection.getMethodName();
                String methodSignature = sourceCodeSelection.getMethodSignature();

                return ClassRepository.getMethodMinAndMaxBCI(clazz, methodName, methodSignature);
            }
        } catch (IOException ex) {
            MiscUtils.printErrorMessage(ex.getMessage());
        } catch (BadLocationException ex) {
            MiscUtils.printErrorMessage(ex.getMessage());
        } catch (ClassNotFoundException ex) {
            MiscUtils.printErrorMessage(ex.getMessage());
        }

        return null;
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

            CodeRegionBCI instrLocation = computeCodeRegionFromSourceCodeSelection(clazz);
            int mIdx = clazz.getMethodIndex(instrLocation.methodName, instrLocation.methodSignature); // TODO CHECK: local variable hides member variable
            clazz.setMethodInstrumented(mIdx);

            DynamicConstantPoolExtension ecp = DynamicConstantPoolExtension.getCPFragment(clazz, INJ_CODE_REGION);
            byte[] newMethodInfo = InstrumentationFactory.instrumentCodeRegion(clazz, mIdx, instrLocation.bci0, instrLocation.bci1);

            int nMethods = clazz.getMethodNames().length;
            byte[][] replacementMethodInfos = new byte[nMethods][];

            for (int i = 0; i < nMethods; i++) {
                replacementMethodInfos[i] = clazz.getMethodInfo(i);
            }

            replacementMethodInfos[mIdx] = newMethodInfo;

            int nAddedCPEntries = ecp.getNEntries();
            byte[] addedCPContents = ecp.getContents();
            replacementClassFileBytes[j] = ClassRewriter.rewriteClassFile(clazz, replacementMethodInfos, nAddedCPEntries,
                                                                          addedCPContents);
        }

        return new Object[] { instrMethodClasses, instrClassLoaderIds, replacementClassFileBytes };
    }
}
