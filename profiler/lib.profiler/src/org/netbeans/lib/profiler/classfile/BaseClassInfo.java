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

package org.netbeans.lib.profiler.classfile;


/**
 * Minimum representation of a class. Used as a base class for the full-fledged ClassInfo, but also
 * may used as is for e.g. array classes.
 *
 * @author Misha Dmitirev
 */
public class BaseClassInfo {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    protected String name;
    protected String nameAndLoader; // A combinarion of class name and loader, uniquely identifying this ClassInfo

    // Management of multiple versions for the same-named (but possibly not same-code) class, loaded by different classloaders
    protected int classLoaderId; // IDs of all loaders with which versions of this class are loaded

    // Data used by our object allocation instrumentation mechanism: integer class ID
    private int instrClassId;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public BaseClassInfo(String className, int classLoaderId) {
        this.name = className.intern();
        this.classLoaderId = classLoaderId;
        nameAndLoader = (name + "#" + classLoaderId).intern(); // NOI18N
        instrClassId = -1;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void setInstrClassId(int id) {
        instrClassId = id;
    }

    public int getInstrClassId() {
        return instrClassId;
    }

    public void setLoaderId(int loaderId) {
        classLoaderId = loaderId;
    }

    public int getLoaderId() {
        return classLoaderId;
    }

    public String getName() {
        return name;
    }

    public String getNameAndLoader() {
        return nameAndLoader;
    }

    public String toString() {
        return name;
    }
}
