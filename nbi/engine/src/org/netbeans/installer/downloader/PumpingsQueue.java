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

package org.netbeans.installer.downloader;

import java.io.File;
import java.net.URL;

/**
 * This interface - entry point for clients.
 * It's allow to client create new pumping and monitoring hole pumping process
 * 
 * @author Danila_Dugurov
 */
public interface PumpingsQueue {

    /**
     * In synchronious mode listener will be notified
     * about any updates in pumping process.
     * So the implementation of listeners must be worktime short.
     */
    void addListener(DownloadListener listener);

    /**
     * Terminate downloading process. Delete all pumpings.
     * If downloading process was runnig start it again.
     */
    void reset();

    Pumping getById(String id);

    /**
     * return all pumpings in queue.
     */
    Pumping[] toArray();

    /**
     * add new pumping. Output file in default folder
     */
    Pumping add(URL url);

    /**
     * add new pumping. Output file in specified folder
     */
    Pumping add(URL url, File folder);

    Pumping delete(String id);

    void delete(URL url);
}
