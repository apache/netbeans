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

