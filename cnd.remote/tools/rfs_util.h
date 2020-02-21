/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

#include <stdio.h>
#include <unistd.h>
#include <limits.h>
#include <stdlib.h>
#include <errno.h>
#include <string.h>

typedef int bool;

enum {
    true = 1,
    false = 0
};

extern bool trace_flag;

void init_trace_flag(const char* env_var);

void report_error(const char *format, ...);

void report_unresolved_path(const char* path);

#define trace(...) if (trace_flag) { _trace(__VA_ARGS__); }
void _trace(const char *format, ...);

#define trace_startup(prefix, env_var, binary) if (trace_flag) { _trace_startup(prefix, env_var, binary); }
void _trace_startup(const char* prefix, const char* env_var, const char* binary);

#define trace_shutdown() if (trace_flag) { _trace_shutdown(); }
void _trace_shutdown();

#define trace_unresolved_path(path, action) if (trace_flag) { _trace_unresolved_path(path, action); }
void _trace_unresolved_path(const char* path, const char* action);

#define dbg_sleep(time) if (trace_flag) { _dbg_sleep(time); }
void _dbg_sleep(int time);

void *malloc_wrapper(size_t size);
