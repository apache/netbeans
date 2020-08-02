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

#include <stdio.h>
#include <stdlib.h>

/*
 * 
 */

#define num_steps 200000000

int
main(int argc, char** argv) {
    
    double pi = 0;
    int i;
    
    for (i = 0; i < num_steps ; i++) {
         
         pi += 1.0/(i*4.0 + 1.0);
         pi -= 1.0/(i*4.0 + 3.0);
       }
    
       pi = pi * 4.0;
       printf("pi done - %f \n", pi);    
    
    return (EXIT_SUCCESS);
}

