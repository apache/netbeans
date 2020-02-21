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
#include "queue.h"
#include "util.h"

#include <stddef.h>
#include <stdlib.h>
#include <assert.h>

void queue_init(queue *q) {
    q->head = 0;
    q->tail = 0;
}

int  queue_size(queue *q) {
    int size= 0;
    for(queue_node *curr = q->head; curr; curr = curr->next) {
        size++;
    }
    return size;
}

void queue_add(queue *q, void* data) {
    queue_node *n = (queue_node*) malloc_wrapper(sizeof(queue_node));
    n->data = data;
    n->next = 0;
    if (q->tail){
        assert(q->head);
        assert(!q->tail->next);
        q->tail->next = n;
    } else {
        assert(!q->head);
        q->head = n;        
    }
    q->tail = n;
}

void* queue_poll(queue *q) {
    queue_node* n = 0;
    if (q->head) {
        n = q->head;
        if (q->head->next) {
            q->head = q->head->next;
        } else {
            assert(q->tail == q->head);
            q->head = 0;
            q->tail = 0;
        }
    }
    if (n) {
        void* result = n->data;
        free(n);
        return result;
    } else {
        return NULL;
    }
}

