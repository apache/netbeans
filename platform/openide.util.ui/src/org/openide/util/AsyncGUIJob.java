/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.openide.util;


/** Service provider interface (SPI) for executing of time consuming task which
 * results are visible in UI.
 *
 * Typical usage is post-initialization of UI components or various long lasting
 * operations like network accessing invoked directly or indirectly by user
 * from UI.
 *
 * Note that it's often desirable to provide cancel support, at least for
 * longer lasting jobs. See {@link org.openide.util.Cancellable} support.
 * Keep in mind that methods {@link #construct} and
 * {@link org.openide.util.Cancellable#cancel} can be called concurrently and
 * require proper synchronization as such.
 *
 * @author  Dafe Simonek
 *
 * @see org.openide.util.Utilities#attachInitJob
 * @since 3.36
 */
public interface AsyncGUIJob {
    /** Worker method, can be called in any thread but event dispatch thread.
     * Implement your time consuming work here.
     * Always called and completed before {@link #finished} method.
     */
    public void construct();

    /** Method to update UI using given data constructed in {@link #construct}
     * method. Always called in event dispatch thread, after {@link #construct}
     * method completed its execution.
     */
    public void finished();
}
