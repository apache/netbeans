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

#include <alloca.h>

#include "rfs_filedata.h"
#include "rfs_util.h"

/** */
static file_data **data = NULL;
static int data_cnt;

// --- start of adding file data stuff

/**
 * Files are added one by one, so we don't know file count.
 * That's why on the adding phase we use linked list
 */
struct adding_file_data_node {
    struct adding_file_data_node *next;
    file_data* fd;
};

static struct adding_file_data_node *adding_file_data_root;

static enum {
    FILE_DATA_INITIAL,
    FILE_DATA_ADDING,
    FILE_DATA_ADDED }
adding_file_data_state = FILE_DATA_INITIAL;


// --- end of adding file data stuff

void visit_file_data(int (*visitor) (file_data*, void*), void *data) {
    //visit_file_data_impl...
}

static int compare_file_data(const void *d1, const void *d2) {
    if (!d1) {
        return d2 ? -1 : 0;
    } else if (!d2) {
        return 1;
    } else {
        file_data *fd1 = *((file_data **) d1);
        file_data *fd2 = *((file_data **) d2);
        int result = strcmp(fd1->filename, fd2->filename);
        return result;
    }
}

file_data *find_file_data(const char* filename) {
    if (adding_file_data_state != FILE_DATA_ADDED || data == NULL) {
        report_error("wrong state: find_file_data is called before filling file data\n");
        return NULL;
    }
    file_data *key = (file_data*) alloca(sizeof(file_data) + strlen(filename) + 1);
    strcpy(key->filename, filename);
    int el_size = sizeof(struct file_data *);
    file_data **result = (file_data**) bsearch(&key, data, data_cnt, el_size, compare_file_data);
    return result ? *result : NULL;
}

void start_adding_file_data() {
    if (adding_file_data_state != FILE_DATA_INITIAL) {
        report_error("wrong state: start_adding_file_data should be only called once!\n");
        return;
    }
    adding_file_data_root = NULL;
    adding_file_data_state = FILE_DATA_ADDING;
    data_cnt = 0;
}

void stop_adding_file_data() {
    int el_size = sizeof(struct file_data *);
    data = malloc_wrapper(data_cnt * el_size);
    int next = 0;
    while (adding_file_data_root) {
        data[next++] = adding_file_data_root->fd;
        void* to_be_freed = adding_file_data_root;
        adding_file_data_root = adding_file_data_root->next;
        free(to_be_freed);
    }
    qsort(data, data_cnt, el_size, compare_file_data);
    adding_file_data_root = NULL;
    adding_file_data_state = FILE_DATA_ADDED;
}

file_data *add_file_data(const char* filename, enum file_state state) {
    if (adding_file_data_state != FILE_DATA_ADDING) {
        report_error("wrong state: add_file_data is called before start_adding_file_data\n");
        return NULL;
    }
    data_cnt++;
    struct adding_file_data_node *node = malloc_wrapper(sizeof(struct adding_file_data_node));

    node->fd = (file_data*) malloc_wrapper(sizeof(file_data) + strlen(filename) + 1);
    pthread_mutex_init(&node->fd->mutex, NULL);
    strcpy(node->fd->filename, filename);
    node->fd->state = state;
    node->next = adding_file_data_root;
    adding_file_data_root = node;
    return node->fd;
}
