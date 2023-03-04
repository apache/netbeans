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
 * File:   err.h
 * Author: ak119685
 *
 * Created on 23 Апрель 2010 г., 0:25
 */

#ifndef _ERROR_H
#define	_ERROR_H


#ifdef	__cplusplus
extern "C" {
#endif

#define MAXLINE 4096 /* max line length */

#include <errno.h>
#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

    void err_sys(const char *fmt, ...);
    void warn_sys(const char *fmt, ...);
    void err_quit(const char *fmt, ...);

#ifdef	__cplusplus
}
#endif

#endif	/* _ERROR_H */

