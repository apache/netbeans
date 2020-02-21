/*
 * Copyright (c) 2009-2010, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of Oracle nor the names of its contributors
 *   may be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
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
