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

package org.netbeans.lib.profiler.instrumentation;

import java.io.IOException;
import java.util.*;
import org.netbeans.lib.profiler.classfile.*;
import org.netbeans.lib.profiler.client.RuntimeProfilingPoint;
import org.netbeans.lib.profiler.global.CommonConstants;
import org.netbeans.lib.profiler.global.ProfilingSessionStatus;
import org.netbeans.lib.profiler.utils.MiscUtils;
import org.netbeans.lib.profiler.wireprotocol.RootClassLoadedCommand;


/**
 * Basic utility methods used by all scanner classes.
 *
 * @author Tomas Hurka
 * @author Misha Dmitriev
 */
public class ClassManager implements JavaClassConstants, CommonConstants {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static final RuntimeProfilingPoint[] EMPTY_PROFILEPOINT_ARRAY = new RuntimeProfilingPoint[0];

    /**
     * Sorts profiling points by bytecode index so that injector can insert them sequentially.
     */
    private static Comparator ByBciComparator = new Comparator() {
        public int compare(Object aa, Object bb) {
            RuntimeProfilingPoint a = (RuntimeProfilingPoint) aa;
            RuntimeProfilingPoint b = (RuntimeProfilingPoint) bb;

            return a.getBci() - b.getBci();
        }
    };


    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    protected ProfilingSessionStatus status;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    protected ClassManager(ProfilingSessionStatus status) {
        this.status = status;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    /**
     * Filters profiling points for given class.
     *
     * @param points profiling points to scan
     * @param classInfo searches for points in this class
     * @return RuntimeProfilingPoint[] array of profiling points inside the specified method
     */
    protected static RuntimeProfilingPoint[] getRuntimeProfilingPoints(RuntimeProfilingPoint[] points, ClassInfo classInfo) {
        List newPoints = null;

        String className = classInfo.getName().replace('/', '.'); // NOI18N

        for (int i = 0; i < points.length; i++) {
            RuntimeProfilingPoint point = points[i];

            if (className.equals(point.getClassName()) && point.resolve(classInfo)) {
                if (newPoints == null) {
                    newPoints = new ArrayList(2);
                }

                newPoints.add(point);
            }
        }

        if (newPoints == null) {
            return EMPTY_PROFILEPOINT_ARRAY;
        }

        return (RuntimeProfilingPoint[]) newPoints.toArray(new RuntimeProfilingPoint[0]);
    }

    /**
     * Filters profiling points for given method.
     *
     * @param points profiling points to scan
     * @param methodIdx method index in the given class
     * @return RuntimeProfilingPoint[] array of profiling points inside the specified method
     */
    protected static RuntimeProfilingPoint[] getRuntimeProfilingPoints(RuntimeProfilingPoint[] points, int methodIdx) {
        List newPoints = null;

        for (int i = 0; i < points.length; i++) {
            RuntimeProfilingPoint point = points[i];

            if (point.getMethodIdx() == methodIdx) {
                if (newPoints == null) {
                    newPoints = new ArrayList(2);
                }

                newPoints.add(point);
            }
        }

        if (newPoints == null) {
            return EMPTY_PROFILEPOINT_ARRAY;
        } else if (newPoints.size() > 1) {
            newPoints.sort(ByBciComparator);
        }

        return (RuntimeProfilingPoint[]) newPoints.toArray(new RuntimeProfilingPoint[0]);
    }

    /**
     * Filters profiling points for given class and method.
     *
     * @param points profiling points to scan
     * @param classInfo searches for points in this class
     * @param methodIdx method index in the given class
     * @return RuntimeProfilingPoint[] array of profiling points inside the specified method
     */
    protected static RuntimeProfilingPoint[] getRuntimeProfilingPoints(RuntimeProfilingPoint[] points, ClassInfo classInfo,
                                                                       int methodIdx) {
        List newPoints = null;
        String className = classInfo.getName().replace('/', '.'); // NOI18N

        for (int i = 0; i < points.length; i++) {
            RuntimeProfilingPoint point = points[i];

            if (className.equals(point.getClassName())) {
                if (point.resolve(classInfo)) {
                    if (point.getMethodIdx() == methodIdx) {
                        if (newPoints == null) {
                            newPoints = new ArrayList(2);
                        }

                        newPoints.add(point);
                    }
                }
            }
        }

        if (newPoints == null) {
            return EMPTY_PROFILEPOINT_ARRAY;
        } else if (newPoints.size() > 1) {
            newPoints.sort(ByBciComparator);
        }

        return (RuntimeProfilingPoint[]) newPoints.toArray(new RuntimeProfilingPoint[0]);
    }

    /**
     * Returns a ClassInfo for a given non-array class name. If actualCPLength >= 0 is provided, the constant
     * pool length in the returned ClassInfo is set to that value. Otherwise it is not touched, i.e. remains
     * the same as for the .class file on the CLASSPATH.
     */
    protected static DynamicClassInfo javaClassForName(String className, int classLoaderId) {
        try {
            return ClassRepository.lookupClass(className, classLoaderId);
        } catch (IOException ex2) {
            MiscUtils.printWarningMessage("Error reading class " + className); // NOI18N
            MiscUtils.printWarningMessage(ex2.getMessage());
        } catch (ClassFormatError er) {
            MiscUtils.printWarningMessage(er.getMessage());
        }

        return null;
    }

    protected static BaseClassInfo javaClassForObjectArrayType(String elementTypeName) {
        BaseClassInfo clazz = ClassRepository.lookupSpecialClass("[" + elementTypeName); // NOI18N

        return clazz;
    }

    protected static BaseClassInfo javaClassForPrimitiveArrayType(int arrayTypeId) {
        BaseClassInfo clazz = ClassRepository.lookupSpecialClass(PRIMITIVE_ARRAY_TYPE_NAMES[arrayTypeId]);

        return clazz;
    }

    /** This is currently used only in memory profiling */
    protected static BaseClassInfo javaClassOrPlaceholderForName(String className, int classLoaderId) {
        return ClassRepository.lookupClassOrCreatePlaceholder(className, classLoaderId);
    }

    protected static BaseClassInfo loadedJavaClassOrExistingPlaceholderForName(String className, int classLoaderId) {
        return ClassRepository.lookupLoadedClass(className, classLoaderId, true);
    }

    protected static void registerPlaceholder(PlaceholderClassInfo pci) {
        ClassRepository.addClassInfo(pci);
    }

    protected static void resetLoadedClassData() {
        ClassRepository.clearCache();
    }

    /**
     * Given a list of classes (normally all classes currently loaded by the JVM), determine those that are loaded using
     * custom classloaders, get their cached bytecodes from the JVM, and put them into ClassRepository.
     */
    protected static void storeClassFileBytesForCustomLoaderClasses(RootClassLoadedCommand rootLoaded) {
        String[] loadedClasses = rootLoaded.getAllLoadedClassNames();
        byte[][] cachedClassFileBytes = rootLoaded.getCachedClassFileBytes();
        int[] loadedClassLoaderIds = rootLoaded.getAllLoadedClassLoaderIds();
        int[] superClasses = rootLoaded.getAllLoaderSuperClassIds();
        int[][] interfaceNames = rootLoaded.getAllLoadedInterfaceIds();
        int nClasses = loadedClasses.length;
        Set allInterfacesIndexes = new HashSet();

        for (int i = 0; i < nClasses; i++) {
            if (cachedClassFileBytes[i] != null) {
                String name = loadedClasses[i];
                int loaderId = loadedClassLoaderIds[i];
                byte[] bytes = cachedClassFileBytes[i];
                if (bytes != null && bytes.length == 0) {
                    String superClass;
                    int sidx = superClasses[i];
                    if (sidx != -1 ) {
                        superClass = loadedClasses[sidx];
                    } else {
                        superClass = OBJECT_SLASHED_CLASS_NAME;
                    }
                    int[] interfaceNamedIdxs = interfaceNames[i];
                    List interfaces = new ArrayList();
                    for (int j = 0; j < interfaceNamedIdxs.length; j++) {
                        int iidx = interfaceNamedIdxs[j];
                        if (iidx != -1) {
                            interfaces.add(loadedClasses[iidx]);
                            allInterfacesIndexes.add(Integer.valueOf(iidx));
                        }
                    }
                    ClassRepository.addVMSuppliedClassFile(name, loaderId, bytes, superClass, (String[])interfaces.toArray(new String[0]));
                } else {
                    ClassRepository.addVMSuppliedClassFile(name, loaderId, bytes);            
                }
            }
        }
        // set interfaces
        for (Object intIndex : allInterfacesIndexes) {
            int iidx = ((Integer)intIndex).intValue();
            if (cachedClassFileBytes[iidx] != null) {
                DynamicClassInfo iface = javaClassForName(loadedClasses[iidx], loadedClassLoaderIds[iidx]);
                iface.setInterface();
            }
        }
    }
}
