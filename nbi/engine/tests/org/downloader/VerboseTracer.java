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
import org.netbeans.installer.downloader.Pumping;
import org.netbeans.installer.downloader.PumpingsQueue;
import org.netbeans.installer.utils.helper.Pair;

/**
 *
 * @author Danila_Dugurov
 */
public class VerboseTracer extends ActionsTracer {
  List<Pair<String, String[]>> verboseActions = new LinkedList<Pair<String, String[]>>();
  
  public VerboseTracer(PumpingsQueue queue) {
    super(queue);
  }
  
  public void pumpingUpdate(String id) {
    super.pumpingUpdate(id);
    verboseActions.add(Pair.create("update", new String[] {id}));
  }
  
  public void pumpingStateChange(String id) {
    super.pumpingStateChange(id);
    final Pumping pumping = queue.getById(id);
    verboseActions.add(Pair.create("stateChange", new String[] {id, pumping.state().toString()}));
  }
  
  public void pumpingAdd(String id) {
    super.pumpingAdd(id);
    verboseActions.add(Pair.create("add", new String[] {id}));
  }
  
  public void pumpingDelete(String id) {
    super.pumpingDelete(id);
    verboseActions.add(Pair.create("delete", new String[] {id}));
  }
  
  public void queueReset() {
    super.queueReset();
    verboseActions.add(Pair.create("reset", new String[0]));
  }
  
  public void pumpsInvoke() {
    super.pumpsInvoke();
    verboseActions.add(Pair.create("invoke", new String[0]));
  }
  
  public void pumpsTerminate() {
    super.pumpsTerminate();
    verboseActions.add(Pair.create("terminate", new String[0]));
  }
}
