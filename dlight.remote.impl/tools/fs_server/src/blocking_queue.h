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

#ifndef BLOCKING_QUEUE_H
#define	BLOCKING_QUEUE_H

#include "queue.h"
#include <pthread.h>

#ifdef	__cplusplus
extern "C" {
#endif

typedef struct blocking_queue {
    queue q;
    pthread_mutex_t mutex;
    pthread_cond_t cond;
    bool shut_down;
    int size;
    int max_size;
} blocking_queue;

/** Initializes list. A list must be initialized before use */
void blocking_queue_init(blocking_queue *q);

/** gets the amunt of elements in the list */
int  blocking_queue_size(blocking_queue *q);

/** adds element to the list tail */
void blocking_queue_add(blocking_queue *q, void* data);

/** removes and returns element from the list's head */
void* blocking_queue_poll(blocking_queue *q);

/** shuts the queue down - after that blocking_queue_poll returns null on empty queue without waiting */
void blocking_queue_shutdown(blocking_queue *q);

int blocking_queue_max_size(blocking_queue *q);

#ifdef	__cplusplus
}
#endif

#endif	/* BLOCKING_QUEUE_H */

