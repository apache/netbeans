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

#include "fs_common.h"
#include "util.h"
#include "blocking_queue.h"

/** Initializes list. A list must be initialized before use */
void blocking_queue_init(blocking_queue *q) {
    pthread_mutex_init(&q->mutex, NULL);
    pthread_cond_init(&q->cond, NULL);
    mutex_lock_wrapper(&q->mutex);
    queue_init(&q->q);
    q->shut_down = false;
    q->size = 0;
    q->max_size = 0;
    mutex_unlock_wrapper(&q->mutex);
}

/** gets the amunt of elements in the list */
int  blocking_queue_size(blocking_queue *q) {    
    mutex_lock_wrapper(&q->mutex);
    int size = queue_size(&q->q);
    mutex_unlock_wrapper(&q->mutex);
    return size;
}

/** adds element to the list tail */
void blocking_queue_add(blocking_queue *q, void* data) {
    mutex_lock_wrapper(&q->mutex);
    queue_add(&q->q, data);
    if (++q->size > q->max_size) {
        q->max_size = q->size;
    }
    pthread_cond_broadcast(&q->cond);
    mutex_unlock_wrapper(&q->mutex);
}

/** removes and returns element from the list's head */
void* blocking_queue_poll(blocking_queue *q) {
    while (true) {
        mutex_lock_wrapper(&q->mutex);
        void* result = queue_poll(&q->q);
        if (result) {
            q->size--;
            mutex_unlock_wrapper(&q->mutex);
            return result;
        } else {
            if (q->shut_down) {
                mutex_unlock_wrapper(&q->mutex);
                return NULL;
            }
            pthread_cond_wait(&q->cond, &q->mutex);
            mutex_unlock_wrapper(&q->mutex);
        }
    }
}

void blocking_queue_shutdown(blocking_queue *q) {
    mutex_lock_wrapper(&q->mutex);
    q->shut_down = true;
    pthread_cond_broadcast(&q->cond);
    mutex_unlock_wrapper(&q->mutex);    
}

int blocking_queue_max_size(blocking_queue *q) {
    int max_size;
    mutex_lock_wrapper(&q->mutex);
    max_size = q->max_size;
    mutex_unlock_wrapper(&q->mutex);        
    return max_size;
}
