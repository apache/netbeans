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

import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.utils.Wildcards;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author Tomas Hurka
 */
class RootMethods {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    // In case of multiple roots, here we have 1 to 1 correspondence between classNames, methodNames and methodSignatures
    // E.g. we may have X,foo,() and X,bar,() as the respective elements of these three arrays.
    String[] classNames;
    boolean[] classesWildcard;
    boolean[] markerMethods;
    String[] methodNames;
    String[] methodSignatures;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    RootMethods(ClientUtils.SourceCodeSelection[] roots) {
        classNames = new String[roots.length];
        methodNames = new String[roots.length];
        methodSignatures = new String[roots.length];
        classesWildcard = new boolean[roots.length];
        markerMethods = new boolean[roots.length];

        for (int i = 0; i < roots.length; i++) {
            ClientUtils.SourceCodeSelection s = roots[i];

            if (s.definedViaSourceLines()) {
                classNames = new String[] { s.getClassName() };
            } else if (s.definedViaMethodName()) {
                // Convert all the class names into slash form
                String rootName = classNames[i] = s.getClassName().replace('.', '/').intern(); // NOI18N
                                                                                               //System.err.println("root rootName: "+rootName);

                if (Wildcards.isPackageWildcard(rootName)) {
                    classesWildcard[i] = true;
                    classNames[i] = Wildcards.unwildPackage(rootName);
                    //System.err.println("Uses wildcard: "+rootClasses[i]);
                    // root method name and signature is not used in this case
                } else {
                    methodNames[i] = s.getMethodName().intern();
                    methodSignatures[i] = s.getMethodSignature().intern();
                    classesWildcard[i] = false;
                }
            } else { // The third case, when no root methods or code region is defined ("Instrument all spawned threads")
                classNames = methodNames = methodSignatures = new String[0];
            }

            markerMethods[i] = s.isMarkerMethod();
        }
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    List /*<String>*/ getRootClassNames() {
        if (classNames.length > 0) {
            List rootClasses = new ArrayList();

            for (int i = 0; i < classNames.length; i++) {
                String name = classNames[i].replace('/', '.'); // NOI18N;

                if (!rootClasses.contains(name)) {
                    rootClasses.add(name);
                }
            }

            return rootClasses;
        }

        return null;
    }
}
