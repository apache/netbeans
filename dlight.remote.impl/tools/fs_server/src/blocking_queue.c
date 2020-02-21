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
