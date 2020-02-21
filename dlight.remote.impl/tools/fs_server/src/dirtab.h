/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 */

#ifndef DIRTABLE_H
#define	DIRTABLE_H

#ifndef FS_COMMON_H
#error fs_common.h MUST be included before any other file
#endif
 
#include "util.h"

#include <pthread.h>

#ifdef	__cplusplus
extern "C" {
#endif

/**
 * dirtab_* maintains a list of all known directories
 */

typedef enum {
    DE_STATE_INITIAL = 0,
    DE_STATE_LS_SENT = 1,
    DE_STATE_REFRESH_SENT = 2,
    DE_STATE_REMOVED = 3
} dirtab_state;

typedef enum {
    /** not watched */
    DE_WSTATE_NONE = 0,
    /** polled periodically */        
    DE_WSTATE_POLL = 1,
    /** watched natively */        
    DE_WSTATE_WATCH = 2
} dirtab_watch_state;

/** 
 * refresh state; concerns only directory refresh is called for -
 * not its subdirectories
 */
typedef enum {
    /** refresh for this path is not in progress now */
    DRS_NONE = 0,
    /** refresh for this path is in progress now */
    DRS_REFRESHING = 1,
    /** refresh for this path is in progress and a new refresh is scheduled */
    DRS_PENDING_REFRESH = 2
} dirtab_refresh_state;

typedef struct dirtab_element dirtab_element;
    
void dirtab_set_persistence_dir(const char* dir);

const char* dirtab_get_persistence_dir();

/** initializes dirtab;must be called before any other dirtab_* function */    
void dirtab_init(bool clear_persistence, dirtab_watch_state default_watch_state);

/** stores dirtab to file */
bool dirtab_flush();

/** to be called only after init */
const char* dirtab_get_basedir(); 

/** to be called only after init */
const char* dirtab_get_tempdir(); 

dirtab_element *dirtab_get_element(const char* abspath);

void dirtab_lock(dirtab_element *el);

void dirtab_unlock(dirtab_element *el);

const char*  dirtab_get_element_cache_path(dirtab_element *e);

void dirtab_visit(bool (*visitor) (const char* path, int index, dirtab_element* el, void *data), void *data);

bool dirtab_is_empty();

/** frees all resources*/    
void dirtab_free();

/** call dirtab_lock() before!  */
dirtab_state dirtab_get_state(dirtab_element *el);

/** call dirtab_lock() before!  */
void dirtab_set_state(dirtab_element *el, dirtab_state state);

/** call dirtab_lock() before!  */
dirtab_watch_state dirtab_get_watch_state(dirtab_element *el);

/** call dirtab_lock() before!  */
void dirtab_set_watch_state(dirtab_element *el, dirtab_watch_state state);

/** call dirtab_lock() before!  */
dirtab_refresh_state dirtab_get_refresh_state(dirtab_element *el);

/** call dirtab_lock() before!  */
void dirtab_set_refresh_state(dirtab_element *el, dirtab_refresh_state state);

#ifdef	__cplusplus
}
#endif

#endif	/* DIRTABLE_H */

