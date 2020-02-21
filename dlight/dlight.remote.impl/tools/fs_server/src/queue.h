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

#ifndef LIST_H
#define	LIST_H

#ifndef FS_COMMON_H
#error fs_common.h MUST be included before any other file
#endif

#ifdef	__cplusplus
extern "C" {
#endif

typedef struct queue_node {
    void *data;
    struct queue_node *next;
} queue_node;
    
typedef struct queue {
    queue_node* head;
    queue_node* tail;
} queue;

/** Initializes list. A list must be initialized before use */
void queue_init(queue *q);

/** gets the amunt of elements in the list */
int  queue_size(queue *q);

/** adds element to the list tail */
void queue_add(queue *q, void* data);

/** removes and returns element from the list's head */
void* queue_poll(queue *q);

#ifdef	__cplusplus
}
#endif

#endif	/* LIST_H */

