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

namespace bug240016 {
    struct my_struct240016 {
      const char *identifier240016;
      int xx;
      double zz;
    };

    const char * identifier240016 = "test";

    static my_struct240016 my_struct_instance240016 = { 
        identifier240016 : identifier240016,
        .xx = 1,
        zz : 1.0,
    };
    
    struct teststruct240016 {
        int xx;
        int yy;
    };    
    
    struct testouterstruct240016 {
        int yy = 1;      
        
        struct teststruct240016 inner = {
            xx : 1,
            yy : yy
        };
        
    };    
}