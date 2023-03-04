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
 * File:   util.h
 * Author: akrasny
 *
 * Created on 26 Сентябрь 2012 г., 13:18
 */

#ifndef UTIL_H
#define	UTIL_H

#include <sys/types.h>
#include <unistd.h>
#include <stdio.h>

#ifdef	__cplusplus
extern "C" {
#endif

    struct buffer{
        char buf[BUFSIZ];
        int offset;
        int length; 
   };

    ssize_t writen(int fd, const void *ptr, size_t n);
    ssize_t writen_no_block(int fd, struct buffer *ptr);

#if defined (__CYGWIN__) || defined (WINDOWS)
        extern char *strsignal(int);
#endif    

#ifdef	__cplusplus
}
#endif

#endif	/* UTIL_H */

