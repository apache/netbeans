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
#include "array.h"
#include <stdlib.h>
#include <assert.h>

static const int element_size = sizeof(void*);

void array_init(array *a, int capacity) {
    assert(capacity > 0);
    a->capacity = capacity;
    a->size = 0;
    a->data = malloc_wrapper(capacity * element_size);
    // TODO: error processing (if malloc returns null)
}

void array_ensure_capcity(array *a, int capacity) {
    if (a->capacity < capacity) {
        while (a->capacity < capacity) {
            a->capacity *= 2;
        }
        a->data = realloc_wrapper(a->data, a->capacity * element_size);
    }
}

void array_truncate(array *a) {
    if (a->size != a->capacity) {
        a->data = realloc_wrapper(a->data, a->capacity * element_size);
    }
}

void array_add(array *a, void* element) {
    array_ensure_capcity(a, a->size + 1);
    a->data[a->size++] = element;
}

void* array_get(array *a, int index) {
    assert(index >= 0);
    assert(index < a->size);
    return a->data[index];
}

int array_size(array *a) {
    return a->size;
}

const void *array_iterate(array *a, const void* (*iterator)(const void *element, void* arg), void *arg) {
    for (int i = 0; i < a->size; i++) {
        void* p = a->data[i];
        const void* res = iterator(p, arg);
        if (res) {
            return res;
        }
    }
    return NULL;
}

void array_qsort(array *a, int (*comparator)(const void *element1, const void *element2)) {
    qsort(a->data, a->size, element_size, comparator);
}

void array_free(array *a) {
    if (a) {
        for (int i = 0; i < a->size; i++) {
            free(a->data[i]);
        }
        free(a->data);
        a->size = 0;
        a->data = NULL;
    }
}
