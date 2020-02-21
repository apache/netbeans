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
