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

package org.openide.execution;

import java.util.Enumeration;
import java.util.LinkedList;
import org.openide.*;
import org.openide.filesystems.EnvironmentNotSupportedException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystem$Environment;
import org.openide.filesystems.FileSystemCapability;
import org.openide.filesystems.FileSystemCompat;
import org.openide.modules.PatchFor;

/**
 * Backward binary compatibility support for deprecated/obsolete features
 * @author sdedic
 */
@PatchFor(NbClassPath.class)
public class NbClassPathCompat {
    
    /** Method to obtain class path for the current state of the repository.
    * The classpath should be scanned for all occured exception caused
    * by file systems that cannot be converted to class path by a call to
    * method getExceptions().
    *
    *
    * @return class path for all reachable systems in the repository
    * @deprecated Please use the <a href="@org-netbeans-api-java-classpath@/org/netbeans/api/java/classpath/api.html">ClassPath API</a> instead.
    */
    @Deprecated
    public static NbClassPath createRepositoryPath () {
        Thread.dumpStack();
        return createRepositoryPath (FileSystemCapability.ALL);
    }

    /** Method to obtain class path for the current state of the repository.
    * The classpath should be scanned for all occured exception caused
    * by file systems that cannot be converted to class path by a call to
    * method getExceptions().
    *
    *
    * @param cap the capability that must be satisfied by the file system
    *    added to the class path
    * @return class path for all reachable systems in the repository
    * @deprecated Please use the <a href="@org-netbeans-api-java-classpath@/org/netbeans/api/java/classpath/api.html">ClassPath API</a> instead.
    */
    @Deprecated
    public static NbClassPath createRepositoryPath (FileSystemCapability cap) {
        Thread.dumpStack();
        final LinkedList res = new LinkedList ();


        final class Env extends FileSystem$Environment {
            /* method of interface Environment */
            public void addClassPath(String element) {
                res.add (element);
            }
        }


        Env env = new Env ();
        Enumeration<? extends FileSystem> en = cap.fileSystems();
        while (en.hasMoreElements ()) {
            try {
                FileSystem fs = en.nextElement();
                FileSystemCompat.compat(fs).prepareEnvironment((FileSystem$Environment)env);
            } catch (EnvironmentNotSupportedException ex) {
                // store the exception
                res.add (ex);
            }
        }

        // return it
        return new NbClassPath (res.toArray ());
    }

}
