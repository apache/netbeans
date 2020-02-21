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

#ifndef ARRAY_H
#define	ARRAY_H

#ifdef	__cplusplus
extern "C" {
#endif

typedef struct {
    int size;
    int capacity;
    void** data;
} array;

void array_init(array *a, int capacity);

void array_ensure_capcity(array *a, int capacity);

void array_truncate(array *a);

void array_add(array *a, void* element);

void* array_get(array *a, int index);

int array_size(array *a);

const void *array_iterate(array *a, const void* (*iterator)(const void *element, void* arg), void *arg);

void array_qsort(array *a, int (*comparator)(const void *element1, const void *element2));

void array_free(array *a);

#ifdef	__cplusplus
}
#endif

#endif	/* ARRAY_H */

