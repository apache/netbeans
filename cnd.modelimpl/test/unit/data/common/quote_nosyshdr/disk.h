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

#ifndef DISK_H
#define DISK_H
#include "module.h"

class Disk : public Module {
public:
    enum DiskType { SINGLE,  RAID };
    enum DiskCapacity { T100,  T200, T500 }; // category represents disk sub-module size in GB

    Disk(int type = SINGLE, int size = T100, int units =1);
    virtual const char* GetType() const;
    virtual const char* GetCategory() const;
        
protected:        
    void ComputeSupportMetric();
};

#endif // DISK_H
