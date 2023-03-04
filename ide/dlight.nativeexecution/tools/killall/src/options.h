/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/* 
 * File:   options.h
 * Author: akrasny
 *
 * Created on 27 Сентябрь 2012 г., 14:44
 */

#ifndef OPTIONS_H
#define	OPTIONS_H

#include <stdio.h>

#ifdef	__cplusplus
extern "C" {
#endif

#define P_QUEUE 1000
#define P_ENV 1001

    typedef enum {
        S_PID, /* A process identifier */
        S_PGID, /* A process group */
        S_SID /* A session identifier */
    } sigscope_t;

    struct options {
        int id;
        sigscope_t scope;
        int sig;
        int nosignal;
    };

    typedef struct options options_t;

    /**
     * 
     * @param opts - return value
     * @return number of parsed options
     */
    int readopts(int argc, char** argv, options_t* opts);

#ifdef	__cplusplus
}
#endif

#endif	/* OPTIONS_H */

