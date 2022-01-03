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

namespace bug244524 {
    typedef struct {
        int bb;
    } test_t_0244524;

    typedef struct {
        int aa;
    } test_t_1244524;

    typedef struct {
        test_t_0244524 t0;
        test_t_1244524 t1;
    } example_t244524;

    example_t244524 example244524[] = { 
        {        
            .t0 = {
                bb : 0
            },
            t1 : {       
                aa : 0   
            }           
        },              
        {               
            t0 : {
                bb : 0
            },        
            .t1 = {       
                aa : 1   
            }           
        },
        {               
            t0 : {
                bb : 0
            },        
            t1 : {       
                aa : 1   
            }           
        }    
    };
}