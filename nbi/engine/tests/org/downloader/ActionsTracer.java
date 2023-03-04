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

package org.downloader;

import java.util.LinkedList;
import java.util.List;
import org.netbeans.installer.downloader.DownloadListener;
import org.netbeans.installer.downloader.PumpingsQueue;
import org.netbeans.installer.utils.helper.Pair;

/**
 *
 * @author Danila_Dugurov
 */
public class ActionsTracer implements DownloadListener {
  
  List<Pair<String,String>> actions = new LinkedList<Pair<String,String>>();

  protected PumpingsQueue queue;
  
  protected ActionsTracer(PumpingsQueue queue) {
    this.queue = queue;
    queue.addListener(this);
  }
  
  public void pumpingUpdate(String id) {
    actions.add(Pair.create("update", id));
  }
  
  public void pumpingStateChange(String id) {
    actions.add(Pair.create("stateChange", id));
  }
  
  public void pumpingAdd(String id) {
    actions.add(Pair.create("add", id));
  }
  
  public void pumpingDelete(String id) {
    actions.add(Pair.create("delete", id));
  }
  
  public void queueReset() {
    actions.add(Pair.create("reset", ""));
  }
  
  public void pumpsInvoke() {
    actions.add(Pair.create("invoke", ""));
  }
  
  public void pumpsTerminate() {
    actions.add(Pair.create("terminate", ""));
  }
}
