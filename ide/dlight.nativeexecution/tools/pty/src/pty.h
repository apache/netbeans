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

#ifndef PTY_H
#define	PTY_H

#ifdef	__cplusplus
extern "C" {
#endif
#include "pty_fork.h"
#include "env.h"
#include "loop.h"
#include "error.h"
#include "util.h"
#include "options.h"
#include <sys/termios.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <sys/time.h>
#include <sys/times.h>
#include <sys/resource.h>
#include <unistd.h>
#include <signal.h>
#include <fcntl.h>
#include <libgen.h>
#include <limits.h>

#if defined __CYGWIN__ && !defined WCONTINUED
    //added for compatibility with cygwin 1.5
#define WCONTINUED 0
#endif

    extern int putenv(char *);

#ifdef	__cplusplus
}
#endif

#endif	/* PTY_H */

