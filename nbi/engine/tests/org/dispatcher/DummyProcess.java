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

package org.dispatcher;

import org.netbeans.installer.downloader.dispatcher.Process;

/**
 *
 * @author Danila_Dugurov
 */
public class DummyProcess implements Process {
  
  public Thread worker = null;
  
  private boolean interrupted = false;
  
  private boolean isProcessed = false;
  
  private long workingStartTime = 0;
  private long workingEndTime = 0;

  private int id;
  
  public DummyProcess() {}
  
  public DummyProcess(int id) {
    this.id = id;
  }
  
  public Thread getWorker() {
    return worker;
  }
  
  public boolean isProcessed() {
    return isProcessed;
  }
  
  public long workingStartTime() {
    return workingStartTime;
  }
  
  public long workingEndTime() {
    return workingEndTime;
  }
  
  public void init() {
    workingStartTime = System.currentTimeMillis();
    isProcessed = true;
    worker = Thread.currentThread();
  }
  
  public void run() {
    while (!interrupted) {
      int TwoPlusTwo = 0;
      TwoPlusTwo = 2 + 2;
      //bla bla bla some work..
      try {
        Thread.sleep(1000);
      } catch (InterruptedException exit) {
        break;
      }
    }
    isProcessed = false;
    interrupted = false;
    workingEndTime = System.currentTimeMillis();
  }
  
  public void terminate() {
    interrupted = true;
    workingEndTime = System.currentTimeMillis();
    if (worker != null) worker.interrupt();
    else System.out.println("worker: " + null);
  }
}
