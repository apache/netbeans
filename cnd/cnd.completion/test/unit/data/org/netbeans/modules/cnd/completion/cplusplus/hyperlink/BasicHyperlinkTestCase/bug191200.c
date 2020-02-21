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

typedef struct fsr191200 {
    int fsr_value;
} fsr_convert_t191200;

typedef struct F191200 {
    int value;
    fsr_convert_t191200* pdata;
    fsr_convert_t191200* data;
} f_t191200;

void f191200() {
    f_t191200
    table[] = {
        { .value = 1, .data = (fsr_convert_t191200) { .fsr_value = 1}},
        { .value = 2, .pdata = &(fsr_convert_t191200) { .fsr_value = 1}},
        { .value = 3, .data = (fsr_convert_t191200) { .fsr_value = 1}},
        { .value = 0}
    };    

    f_t191200
    table2[] = {
        { .value = 1, .pdata = &(fsr_convert_t191200) { .fsr_value = 1}},
        { .value = 2, .data = (fsr_convert_t191200) { .fsr_value = 1}},
        { .value = 3, .pdata = &(fsr_convert_t191200) { .fsr_value = 1}},
        { .value = 0}
    };    
}
