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
#include <omp.h>
#include <time.h>

/*
 * 
 */

#define num_steps 200000000

double pi = 0;

int
main(int argc, char** argv) {
    
    int i;
    double start, stop;
    
#ifdef _OPENMP
    omp_set_num_threads(4);
    omp_set_dynamic(0);
#endif
    
    start = clock();
    
//           we want 1/1 - 1/3 + 1/5 - 1/7 etc.
//            therefore we count by fours (0, 4, 8, 12...) and take
//              1/(0+1) =  1/1
//            - 1/(0+3) = -1/3
//              1/(4+1) =  1/5
//            - 1/(4+3) = -1/7 and so on 
    
    #pragma omp parallel for  //reduction(+:pi) 
    for (i = 0; i < num_steps ; i++) {
         
         pi += 1.0/(i*4.0 + 1.0);
         pi -= 1.0/(i*4.0 + 3.0);
       }
    stop = clock();
    
       pi = pi * 4.0;
       printf("pi done - %f in %.3f seconds\n", pi, (stop-start)/1000000);    
    
    return (EXIT_SUCCESS);
}

