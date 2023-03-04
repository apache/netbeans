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
 * A placeholder for a real class that can be put into the table in ClassRepository. Used temporarily, in situations when some
 * info about class becomes known and/or has to be recorded before the class itself is loaded by the VM. Once a real class
 * is loaded, it replaces the placeholder, and the info from the latter is transferred into the real class using
 * transferDataIntoRealClass.
 *
 * @author Misha Dmitirev
 */
public class PlaceholderClassInfo extends BaseClassInfo {
    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public PlaceholderClassInfo(String className, int classLoaderId) {
        super(className, classLoaderId);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void transferDataIntoRealClass(DynamicClassInfo clazz) {
        clazz.setInstrClassId(this.getInstrClassId());
    }
}
