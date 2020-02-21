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
