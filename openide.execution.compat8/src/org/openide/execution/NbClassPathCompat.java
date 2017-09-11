/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
        Enumeration en = cap.fileSystems ();
        while (en.hasMoreElements ()) {
            try {
                FileSystem fs = (FileSystem)en.nextElement ();
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
