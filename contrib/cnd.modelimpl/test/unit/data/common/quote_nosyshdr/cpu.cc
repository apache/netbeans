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

// Implementation of CPU module class

#include "cpu.h"

Cpu::Cpu(int type /*= MEDIUM */, int architecture /*= OPTERON */, int units /*= 1*/) :
    Module("CPU", "generic", type, architecture, units) {
        ComputeSupportMetric();
}
    
/*
 * Heuristic for CPU module complexity is based on number of CPUs and
 * target use ("category"). CPU architecture ("type") is not considered in
 * heuristic
 */

void Cpu::ComputeSupportMetric() {
    int metric = 100 * GetUnits();

    switch (GetTypeID()) {
        case MEDIUM:
            metric += 100;
            break;

        case HIGH:
            metric += 400;
            break;
    }

    SetSupportMetric(metric);
}

const char* Cpu::GetType() const {
    switch (GetTypeID()) {
        case MEDIUM:
            return "Middle Class CPU";

        case HIGH:
            return "High Class CPU";

        default:
            return "Undefined";
    }
}

const char* Cpu::GetCategory() const {
    switch (GetCategoryID()) {
        case OPTERON:
            return "AMD Opteron Processor";

        case INTEL:
            return "Intel Processor";

        case SPARC:
            return "SUN Sparc Processor";

        default: return "Undefined";
    }
}

// end cpu.cc
