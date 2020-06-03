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

//Implementation of disk module class

#include "disk.h"

Disk::Disk(int type /*= SINGLE */, int size /*= T100 */, int units /*= 1 */) :
    Module("Disk storage", "generic", type, size, units) {
        ComputeSupportMetric();
}
    
/*
 * Heuristic for disk module complexity is based on number of disk sub-modules 
 * and architecture. Size of individual disks is not considered in heuristic
 */

void Disk::ComputeSupportMetric() { 
    int metric = 200 * GetUnits();
     
    if (GetTypeID() == RAID) {
        metric += 500;
    }
        
    SetSupportMetric(metric);
}
    
const char* Disk::GetType() const {
    switch (GetTypeID()) {
        case SINGLE: 
            return "Single disk";
        
        case RAID: 
            return "Raid";
         
        default: 
            return "Undefined";
    }
}
    
const char* Disk::GetCategory() const {
    switch (GetCategoryID()) {
        case T100: 
            return "100 Gb disk";
        
        case T200: 
            return "200 Gb disk";
        
        case T500: 
            return "500 Gb or more";
            
        default: 
            return "Undefined";
    }
}
    
// end disk.cc
