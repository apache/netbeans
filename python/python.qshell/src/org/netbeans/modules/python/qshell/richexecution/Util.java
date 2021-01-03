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

package org.netbeans.modules.python.qshell.richexecution;

import java.io.FileDescriptor;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Util {

    /**
     * Assign a numeric value to the {@link FileDescriptor} object.
     * @param fd Numeric file descriptor value.
     * @param FD Java object.
     */
    public static void assignFd(int fd, FileDescriptor FD) {
	Class cls = FileDescriptor.class;
	try {
	    Field fieldFd = cls.getDeclaredField("fd");
	    // Allow setting of private fields:
	    fieldFd.setAccessible(true);
	    fieldFd.setInt(FD, fd);
	} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException ex) {
	    Logger.getLogger(JNAPty.class.getName()).log(Level.SEVERE, null, ex);
	}
    }

    /**
     * Get the numeric value to the {@link FileDescriptor} object.
     * @param FD Java object.
     * @return Numeric file descriptor value.
     */
    public static int getFd(FileDescriptor FD) {
	Class cls = FileDescriptor.class;
        try {
            Field fieldFd = cls.getDeclaredField("fd");
            // Allow getting of private fields:
            fieldFd.setAccessible(true);
            return fieldFd.getInt(FD);
	} catch (IllegalAccessException | NoSuchFieldException ex) {
	    Logger.getLogger(JNAPty.class.getName()).log(Level.SEVERE, null, ex);
	}
        return -1;
    }
}
