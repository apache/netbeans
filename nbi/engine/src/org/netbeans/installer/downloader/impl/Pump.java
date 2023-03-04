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

package org.netbeans.installer.downloader.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import org.netbeans.installer.downloader.DownloadConfig;
import org.netbeans.installer.downloader.Pumping;
import org.netbeans.installer.downloader.connector.URLConnector;
import org.netbeans.installer.downloader.dispatcher.Process;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.StreamUtils;

/**
 * @author Danila_Dugurov
 */
public class Pump implements Process {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    final PumpingImpl pumping;
    URLConnector connector = URLConnector.getConnector();
    
    InputStream in;
    OutputStream out;
    
    public Pump(Pumping pumping) {
        this.pumping = (PumpingImpl) pumping;
    }
    
    public PumpingImpl pumping() {
        return pumping;
    }
    
    public void init() {
    }
    
    public void run() {
        if (!initPumping()) return;
        pumping.fireChanges("pumpingUpdate");
        if (!processPumping()) return;
    }
    
    private boolean initPumping() {
        int attemptCount = 0;
        while (attemptCount < MAX_ATTEMPT_COUNT) {
            pumping.changeState(Pumping.State.CONNECTING);
            try {
                final URL url = pumping.declaredURL();
                URLConnection connection = connector.establishConnection(url);
                in = connection.getInputStream();
                if (exitOnInterrupt()) return false;
                initPumping(connection);
                pumping.changeState(Pumping.State.WAITING);
                return true;
            } catch (IOException ex) {
                LogManager.log(ex);
                attemptCount++;
                try {
                    pumping.changeState(Pumping.State.WAITING);
                    Thread.sleep(ATTEMPT_TIME_DELAY);
                } catch (InterruptedException exit) {
                    pumping.changeState(Pumping.State.INTERRUPTED);
                    return false;
                }
            } finally {
                try {
                    if (in != null) in.close();
                } catch (IOException ignored) {
                    LogManager.log(ignored);
                }
            }
        }
        pumping.changeState(Pumping.State.FAILED);
        return false;
    }
    
    private void initPumping(URLConnection connection) throws IOException {
        final Date lastModif = new Date(connection.getLastModified());
        final URL realUrl = connection.getURL();
        final String accept = connection.getHeaderField("Accept-Ranges");
        final boolean acceptBytes = accept != null ? accept.contains("bytes"): false;
        final long length = connection.getContentLength();
        pumping.init(realUrl, length, lastModif, acceptBytes);
    }
    
    private boolean processPumping() {
        int attemptCount = 0;
        while (attemptCount < MAX_ATTEMPT_COUNT) {
            pumping.changeState(org.netbeans.installer.downloader.Pumping.State.CONNECTING);
            try {
                final SectionImpl section = pumping.getSection();
                final URL connectingUrl = pumping.realURL();
                URLConnection connection = connector.establishConnection(connectingUrl, section.headers());
                in = connection.getInputStream();
                if (exitOnInterrupt()) return false;
                out = ChannelUtil.channelFragmentAsStream(pumping.outputFile(), section);
                pumping.changeState(Pumping.State.PUMPING);
                StreamUtils.transferData(in, out);
                if (section.length() > 0) {
                    if (section.offset() != section.start() + section.length()) {
                        attemptCount++;
                        continue;
                    }
                }
                pumping.changeState(Pumping.State.FINISHED);
                return true;
            } catch (IOException ex) {
                LogManager.log(ex);
                if (exitOnInterrupt()) return false;
                attemptCount++;
                try {
                    pumping.changeState(Pumping.State.WAITING);
                    Thread.sleep(ATTEMPT_TIME_DELAY);
                } catch (InterruptedException exit) {
                    pumping.changeState(Pumping.State.INTERRUPTED);
                    return false;
                }
            } finally {
                if (in != null) try {
                    in.close();
                } catch (IOException ignored) {
                    LogManager.log(ignored);
                }
                if (out != null) try {
                    out.close();
                } catch (IOException ignored) {
                    LogManager.log(ignored);
                }
            }
        }
        pumping.changeState(Pumping.State.FAILED);
        return false;
    }
    
    private boolean exitOnInterrupt() {
        if (!Thread.interrupted()) return false;
        pumping.changeState(Pumping.State.INTERRUPTED);
        return true;
    }
    
    public void terminate() {
        if (in != null) try {
            in.close();
        } catch (IOException ignored) {
            LogManager.log(ignored);
        }
        if (out != null) try {
            out.close();
        } catch (IOException ignored) {
            LogManager.log(ignored);
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    private static final int ATTEMPT_TIME_DELAY = DownloadConfig.REATTEMPT_DELAY;
    private static final int MAX_ATTEMPT_COUNT = DownloadConfig.ATTEMPT_COUNT;
}
