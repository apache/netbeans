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

#ifndef _RFS_FILEDATA_H
#define	_RFS_FILEDATA_H

#include <pthread.h>

#ifdef	__cplusplus
extern "C" {
#endif

enum file_state {
    INITIAL = 'i',
    TOUCHED = 't',
    COPIED = 'c',

    /** 
     * Local host already knows that it's owned by remote one;
     * the file has not been modified remotely during the last build
     */
    UNCONTROLLED = 'u',

    /** The file has been modified remotely during the last build */
    MODIFIED = 'm',

    ERROR = 'e',
    DIRECTORY = 'D',
    LINK = 'L',
    LINK_FILE = 'l',
    PENDING = 'p',
    
    /** The file does not exist on local host (although belongs to the project) */
    INEXISTENT = 'n',
};

typedef struct file_data {
    volatile enum file_state state;
    pthread_mutex_t mutex;
    char filename[];
} file_data;

/**
 * Finds file_data for the given file name;
 */
file_data *find_file_data(const char* filename);

/** Should be called before first file data is added */
void start_adding_file_data();

/**
 * Adds file_data for the given file name;
 * returns a reference to the newly inserted one.
 *
 * Note that you should call start_adding_file_data() once
 * before adding file data
 * and call stop_adding_file_data
 * once all file data is added
 */
file_data *add_file_data(const char* filename, enum file_state state);

/** Should be called after the last file data is added */
void stop_adding_file_data();

/**
 * Visits all file_data elements - calls function passed as a 1-st parameter
 * for each file_data element.
 * Two parameters are passed to the function on each call:
 * 1) current file_data
 * 2) pointer that is passed as 2-nd visit_file_data parameter
 * In the case function returns 0, the tree traversal is stopped
 */
void visit_file_data(int (*) (file_data*, void*), void*);

#ifdef	__cplusplus
}
#endif

#endif	/* _RFS_FILEDATA_H */

