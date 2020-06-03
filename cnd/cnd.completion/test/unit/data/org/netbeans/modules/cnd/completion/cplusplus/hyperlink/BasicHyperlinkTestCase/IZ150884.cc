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

int main_150884() {
    typedef enum {
        NONE, FILE_IO, SHELLOUT, PERMITTED_PATHS, UNKNOWN
    } SectionVal;
    SectionVal section;

    typedef struct {
        const char *label;
        const SectionVal value;
    } Section;
    const Section sections[] ={
        { "", NONE}, // init
        { "[File I/O Security]", FILE_IO},
        { "[Shellout Security]", SHELLOUT},
        { "[Permitted Paths]", PERMITTED_PATHS},
        { 0, UNKNOWN} // sentinel
    };
    return 0;
}
