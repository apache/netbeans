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

package org.netbeans.installer.downloader.queue;

import static org.netbeans.installer.downloader.DownloadConfig.DISPATCHER_POOL;
import static org.netbeans.installer.downloader.DownloadConfig.DISPATCHER_QUANTUM;
import org.netbeans.installer.downloader.DownloadManager;
import org.netbeans.installer.downloader.Pumping;
import static org.netbeans.installer.downloader.Pumping.State;
import org.netbeans.installer.downloader.dispatcher.ProcessDispatcher;
import org.netbeans.installer.downloader.impl.Pump;
import org.netbeans.installer.downloader.dispatcher.impl.RoundRobinDispatcher;
import org.netbeans.installer.downloader.impl.PumpingImpl;

import java.io.File;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Danila_Dugurov
 */
public class DispatchedQueue extends QueueBase {
  
  /////////////////////////////////////////////////////////////////////////////////
  // Instance
  private final ProcessDispatcher dispatcher = new RoundRobinDispatcher(DISPATCHER_QUANTUM, DISPATCHER_POOL);
  
  private final Map<String, Pump> pId2p = new LinkedHashMap<String, Pump>();
  
  public DispatchedQueue(File stateFile) {
    super(stateFile);
  }
  
  public synchronized void reset() {
    final boolean wasActive = dispatcher.isActive();
    terminate();
    for (String id: id2Pumping.keySet().toArray(new String[0])) {
      delete(id);
    }
    fire("queueReset");
    if (wasActive) invoke();
  }
  
  public synchronized Pumping add(URL url) {
    return add(url, DownloadManager.instance.defaultFolder());
  }
  
  public synchronized Pumping add(URL url, File folder) {
    final PumpingImpl newOne = new PumpingImpl(url, folder, this);
    final String id = newOne.getId();
    id2Pumping.put(id, newOne);
    if (dispatcher.isActive()) {
      final Pump pump = new Pump(newOne);
      pId2p.put(id, pump);
      dispatcher.schedule(pump);
    }
    fire("pumpingAdd", id);
    return newOne;
  }
  
  public synchronized Pumping delete(String id) {
    final PumpingImpl oldOne = id2Pumping.remove(id);
    if (oldOne == null) return null;
    dispatcher.terminate(pId2p.get(id));
    fire("pumpingDelete", id);
    pId2p.remove(id);
    if (oldOne.state() != State.FINISHED)
      oldOne.reset();
    return oldOne;
  }
  
  public synchronized void delete(URL url) {
    for (Pumping pumping: toArray()) {
      if (pumping.declaredURL() == url) {
        delete(pumping.getId());
      }
    }
  }
    
    public synchronized void invoke() {
    if (dispatcher.isActive()) return;
    fire("pumpsInvoke");
    for (Pumping pumping : toArray()) {
      if (pumping.state() != State.FINISHED) {
        final Pump newOne = new Pump(pumping);
        pId2p.put(pumping.getId(), newOne);
        dispatcher.schedule(newOne);
      }
    }
    dispatcher.start();
  }
  
  public synchronized void terminate() {
    if (!dispatcher.isActive()) return;
    dispatcher.stop();
    dump();
    fire("pumpsTerminate");
  }
  
  public synchronized boolean isActive() {
    return dispatcher.isActive();
  }
}
