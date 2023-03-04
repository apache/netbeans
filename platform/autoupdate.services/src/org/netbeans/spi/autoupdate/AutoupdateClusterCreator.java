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

package org.netbeans.spi.autoupdate;

import java.io.File;
import java.io.IOException;

/** Class that is supposed to be implemented by application
 * providers that can control launcher in order to modify
 * the list of provided clusters.
 *
 * @since 1.2
 * @author  Jaroslav Tulach
 */
public abstract class AutoupdateClusterCreator extends Object {
    /** Finds the right cluster directory for given cluster name.
     * This method can return null if no such cluster name is known or 
     * understandable, otherwise it returns a file object representing
     * <b>not existing</b> directory that will be created later
     * to host hold the content of the cluster.
     * 
     * @param clusterName the name of the cluster the autoupdate client is searching for
     * @return null or File object of the cluster to be created
     */
    protected abstract File findCluster(String clusterName);
    
    /** Changes the launcher to know about the new cluster and 
     * use it next time the system starts.
     * 
     * @param clusterName the name of the cluster
     * @param cluster file previously returned by findCluster
     * @return the list of current cluster directories, including the newly added one
     * @exception IOException if the registration fails
     */
    protected abstract File[] registerCluster(String clusterName, File cluster) throws IOException;
}
