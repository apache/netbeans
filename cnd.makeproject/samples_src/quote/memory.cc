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

#include "memory.h"

Memory::Memory(int type /*= STANDARD */, int size /*= MEDIUM */, int units /*= 1 */) :
    Module("Memory", "generic", type, size, units) {
        ComputeSupportMetric();
    }
    
/*
 *  Heuristic for memory module complexity is based on number of memory 
 *  sub-modules and memory speed. 
 *
 *  Size of sub-module is not considered in heuristic.
 */

void Memory::ComputeSupportMetric() { 
    
    int metric = 200 * GetUnits();
        
    switch (GetTypeID()) {
        case FAST: 
            metric += 100; 
            break;
            
        case ULTRA: 
            metric += 200;
            break;
    }
        
    SetSupportMetric(metric);            
}
    
const char* Memory::GetType() const {
    switch (GetTypeID()) {
        case STANDARD: 
            return "Standard Memory";
            
        case FAST: 
            return "Fast Memory";
            
        case ULTRA: 
            return "UltraFast Memory";
            
        default: 
            return "Undefined";
    }
}

const char* Memory::GetCategory() const {
    switch (GetCategoryID()) {
        case SMALL:
            return "<= 1 Gb RAM";
            
        case MEDIUM: 
            return "1 - 2 Gb RAM";
            
        case BIG: return "4+ Gb RAM";
        
        default: 
            return "Undefined";
    }
}
  
// end memory.cc
