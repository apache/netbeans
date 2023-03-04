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

package org.netbeans.lib.profiler.results.cpu;

import java.util.ArrayList;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;


/**
 * This class provides a map between method ids and class (package) ids, which is needed when
 * constructing an aggregated class- (package-) level view of CPU profiling results out of the
 * initial method-level view
 *
 * @author Misha Dmitriev
 */
public class MethodIdMap {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // -----
    // I18N String constants
    private static final String ANONYMOUS_PACKAGE_STRING = ResourceBundle.getBundle("org.netbeans.lib.profiler.results.cpu.Bundle").getString("MethodIdMap_AnonymousPackageString"); // NOI18N
                                                                                                                     // -----

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private ArrayList classOrPackageNames;
    private Map classIdCache; // Maps a class (package) name to its integer id
    private int[] classIds;
    private int curClassId;
    private int newView;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /**
     * @param methodLevelInstrClassNames names of classes for instrumented methods. The total number of entries is
     *                                   equal to the number of instrumented methods, but some entries may be the same of
     *                                   course.
     * @param nInstrMethods              number of entries in this array that are actually used
     * @param newView                    the new view for which we are creating ids - class-level or package-level
     */
    public MethodIdMap(String[] methodLevelInstrClassNames, int nInstrMethods, int newView) {
        this.newView = newView;
        classIds = new int[nInstrMethods];
        classIdCache = new ConcurrentHashMap();
        classOrPackageNames = new ArrayList();
        curClassId = 0;
        classOrPackageNames.add(methodLevelInstrClassNames[0]);

        classIds[0] = 0; // The hidden "Thread" quazi-method transforms into "Thread" quazi-class

        for (int i = 1; i < nInstrMethods; i++) {
            classIds[i] = getClassId(methodLevelInstrClassNames[i]);
        }

        classIdCache = null; // Not needed anymore - free memory
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public int getClassOrPackageIdForMethodId(int methodId) {
        return classIds[methodId];
    }

    public String[] getInstrClassesOrPackages() {
        String[] ret = (String[]) classOrPackageNames.toArray(new String[0]);
        classOrPackageNames = null;

        return ret;
    }

    public int getNInstrClassesOrPackages() {
        return curClassId + 1;
    }

    private int getClassId(String className) {
        String name = (newView == CPUResultsSnapshot.CLASS_LEVEL_VIEW) ? className : getPackageName(className);
        Integer classId = (Integer) classIdCache.get(name);

        if (classId == null) {
            curClassId++;
            classOrPackageNames.add(name);
            classIdCache.put(name, Integer.valueOf(curClassId));

            return curClassId;
        } else {
            return classId.intValue();
        }
    }

    private String getPackageName(String className) {
        int lastDivPos = className.lastIndexOf('.'); // NOI18N

        if (lastDivPos == -1) {
            return ANONYMOUS_PACKAGE_STRING;
        } else {
            return className.substring(0, lastDivPos).intern();
        }
    }
}
