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

//Implementation of System class: collection of modules

#include "system.h"
#include <iostream>
#include <assert.h>

System::System() :
    supportMetric(0) {
}

void System::AddModule(Module* module) {
    moduleList.push_back(module);
    supportMetric += module->GetSupportMetric();
}

Module& System::GetModule(int i) const {
    assert(i >= 0 && (unsigned)i < moduleList.size());
    
    return (*moduleList[i]);
}

int System::GetModuleCount() const {
    return moduleList.size();
}

int System::GetSupportMetric() const {
    return supportMetric;
};

ostream& operator <<(ostream& output, const System& system) {
    int size = system.GetModuleCount();
    
    output << "System consists of " << size << " module(s):" << endl << endl;
    
    for (int i = 0; i < size; i++) {
        output << system.GetModule(i) << endl;
    }
    
    return output;
}

// end system.cc
