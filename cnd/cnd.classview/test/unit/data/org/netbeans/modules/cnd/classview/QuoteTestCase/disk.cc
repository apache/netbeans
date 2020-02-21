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
