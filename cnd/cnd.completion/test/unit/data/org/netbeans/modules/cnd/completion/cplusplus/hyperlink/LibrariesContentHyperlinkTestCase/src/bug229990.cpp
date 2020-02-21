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

#include <sys_stat_h.h>
#include "bug229990.h"

namespace AAAA229990 {
    namespace Inner229990 {
        void A229990::foo229990(struct stat229990* stat, struct ssss229990* sss) {
                        // stat it
                        struct stat229990 sb1;
                        // get length from stat
                        long _length = sb1.st_size;

                        struct ssss229990 sb2;
                        _length = sb2.size;
        }
    }
}
