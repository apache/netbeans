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
package org.netbeans.modules.java.source.util;

import com.sun.tools.javac.util.Abort;
import org.netbeans.lib.nbjavac.services.CancelAbort;

/**
 * The NetBeans java parser infrastructure relies on {@link CancelAbort} being
 * thrown to abort the compilation process. There are situation however, where
 * javac does not special case {@link Abort} (superclass of {@link CancelAbort}).
 * In these cases the cause holds an instance of {@link Abort}. These cases also
 * need to be detected.
 *
 * <p>The methods in this class check the exception cause chain to see if the
 * caught exceptions is caused by an {@link Abort} or {@link CancelAbort}.
 */
public class AbortChecker {
    public static boolean isCancelAbort(Throwable thrw) {
        Throwable curr = thrw;
        while (true) {
            if(curr instanceof CancelAbort) {
                return true;
            } else if (curr == null) {
                return false;
            }
            curr = curr.getCause();
        }
    }
    public static boolean isAbort(Throwable thrw) {
        Throwable curr = thrw;
        while (true) {
            if(curr instanceof Abort) {
                return true;
            } else if (curr == null) {
                return false;
            }
            curr = curr.getCause();
        }
    }
}
