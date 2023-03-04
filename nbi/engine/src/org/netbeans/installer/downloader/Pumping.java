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
import org.netbeans.installer.utils.helper.Pair;

/**
 *
 * @author Danila_Dugurov
 */

public interface Pumping {
    /**
     * It's runtime property of pumping. It means that it's not persistence 
     * property. So if downloader client maintain it's state persistance - it 
     * mustn't base on pumpings ids.
     */
    String getId();

    /**
     * @return declared pumping url.
     */
    URL declaredURL();

    /**
     * @return real pumping url. It is url which was obtain at runtime. It's may be 
     * the same as declared url if no redirect may occur.
     */
    URL realURL();

    /**
     * @return file corresponding to this pumping.
     */
    File outputFile();

    File folder();

    long length();

    /**
     * @return mode in which downloader process it. So if Single thread mode - it's
     * means that only one thread process pumping(so one section invoked). If multi
     * thread mode - it's means that downloader allowed to process pumping in more
     * then one thread concurrently. But it's not means that downloader do it.
     * The issue process or not in multy thread deal with some external issues:
     * for example domain police and server side speed reducing for client who try 
     * to obtain more then one connection at time. Base implementation in any case
     * download in one thread.
     */
    DownloadMode mode();

    State state();

    /**
     * one section  - one thread. Section - data structure for representation and 
     * manage downloading unit
     */
    Section[] getSections();

    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public static interface Section {
        /**
         * range of bytes this section responsible for.
         */
        Pair<Long, Long> getRange();

        /**
         * absolute offset. Means if range: 12345 - 23456. initially offset equals 
         * 12345 when section downloaded it's equals 23456.
         */
        long offset();
    }

    public enum State {
        NOT_PROCESSED, 
        CONNECTING, 
        PUMPING, 
        WAITING, 
        INTERRUPTED, 
        FAILED, 
        FINISHED, 
        DELETED
    }
}
