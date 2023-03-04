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

import org.MyTestCase;
import org.netbeans.installer.downloader.dispatcher.Process;
import org.netbeans.installer.downloader.dispatcher.ProcessDispatcher;
import org.netbeans.installer.downloader.dispatcher.impl.RoundRobinDispatcher;

/**
 *
 * @author Danila_Dugurov
 */

/**
 * be aware of that if time quantum or sleep time change - failes may occur.
 * it's all ok becouse asynchronious dispatcher wroking.
 */
public class DispatcherTest extends MyTestCase {
  
  public void testRunStop() {
    final ProcessDispatcher dispatcher = new RoundRobinDispatcher(500, 1);
    assertFalse(dispatcher.isActive());
    dispatcher.start();
    assertTrue(dispatcher.isActive());
    dispatcher.stop();
    assertFalse(dispatcher.isActive());
    
    assertFalse(dispatcher.isActive());
    dispatcher.start();
    assertTrue(dispatcher.isActive());
    dispatcher.stop();
    assertFalse(dispatcher.isActive());
  }
  
  public void testSingleProcessAddAndTerminate() {
    final ProcessDispatcher dispatcher = new RoundRobinDispatcher(50, 1);
    final DummyProcess dummy = new DummyProcess();
    dispatcher.schedule(dummy);
    shortSleep();
    assertEquals(0, dispatcher.activeCount());
    assertEquals(1, dispatcher.waitingCount());
    assertFalse(dummy.isProcessed());
    dispatcher.start();
    shortSleep();
    assertTrue(dummy.isProcessed());
    assertEquals(1, dispatcher.activeCount());
    assertEquals(0, dispatcher.waitingCount());
    dummy.terminate();
    shortSleep();
    assertFalse(dummy.isProcessed());
    assertEquals(0, dispatcher.activeCount());
    assertEquals(0, dispatcher.waitingCount());
    dispatcher.stop();
  }
  
  public void testSingleProcessAddAndDispatcherTerminate() {
    final ProcessDispatcher dispatcher = new RoundRobinDispatcher(50, 1);
    final DummyProcess dummy = new DummyProcess();
    dispatcher.schedule(dummy);
    dispatcher.start();
    shortSleep();
    assertTrue(dummy.isProcessed());
    assertEquals(1, dispatcher.activeCount());
    assertEquals(0, dispatcher.waitingCount());
    dispatcher.stop();
    assertFalse(dummy.isProcessed());
    assertEquals(0, dispatcher.activeCount());
    assertEquals(0, dispatcher.waitingCount());
  }
  
  public void testReuseThreadWorker() {
    final ProcessDispatcher dispatcher = new RoundRobinDispatcher(50, 1);
    final DummyProcess dummy = new DummyProcess();
    final DummyProcess dummy2 = new DummyProcess();
    dispatcher.schedule(dummy);
    dispatcher.start();
    shortSleep();
    dummy.terminate();
    shortSleep();
    assertEquals(0, dispatcher.activeCount());
    assertEquals(0, dispatcher.waitingCount());
    assertFalse(dummy.isProcessed());
    Thread worker = dummy.getWorker();
    assertTrue(worker.isAlive());
    assertTrue(dispatcher.isActive());
    assertTrue(dispatcher.schedule(dummy2));
    assertEquals(1, dispatcher.waitingCount() + dispatcher.activeCount());
    shortSleep();
    assertEquals(1, dispatcher.activeCount());
    assertTrue(dummy2.isProcessed());
    assertEquals(worker, dummy2.getWorker());//key line in this test
    dispatcher.stop();
    assertFalse(dummy2.isProcessed());
  }
  
  public void testTwoProcessWhenPoolOnlyOne() {
    final ProcessDispatcher dispatcher = new RoundRobinDispatcher(50, 1);
    final DummyProcess dummy = new DummyProcess();
    final DummyProcess dummy2 = new DummyProcess();
    dispatcher.start();
    dispatcher.schedule(dummy);
    dispatcher.schedule(dummy2);
    shortSleep();
    assertTrue(dummy.isProcessed());
    assertFalse(dummy2.isProcessed());
    assertEquals(1, dispatcher.activeCount());
    assertEquals(1, dispatcher.waitingCount());
    final Thread worker = dummy.getWorker();
    dummy.terminate();
    shortSleep();
    assertFalse(dummy.isProcessed());
    assertTrue(dummy2.isProcessed());
    assertEquals(1, dispatcher.activeCount());
    assertEquals(0, dispatcher.waitingCount());
    assertEquals(worker, dummy2.getWorker());
    dispatcher.stop();
    assertEquals(0, dispatcher.activeCount());
    assertEquals(0, dispatcher.waitingCount());
  }
  
  public void testTwicetheSameProcess() {
    final ProcessDispatcher dispatcher = new RoundRobinDispatcher(50, 1);
    final DummyProcess dummy = new DummyProcess();
    dispatcher.schedule(dummy);
    dispatcher.start();
    shortSleep();
    assertTrue(dummy.isProcessed());
    dummy.terminate();
    shortSleep();
    assertTrue(dispatcher.schedule(dummy));
    assertEquals(1, dispatcher.activeCount() + dispatcher.waitingCount());
    dispatcher.stop();
    assertEquals(0, dispatcher.activeCount());
    assertEquals(0, dispatcher.waitingCount());
    assertTrue(dispatcher.schedule(dummy));
  }
  
  public void testTerminateByDispatcher() {
    final ProcessDispatcher dispatcher = new RoundRobinDispatcher(50, 1);
    final DummyProcess goodDummy = new DummyProcess();
    final DummyProcess badDummy = new DummyProcess() {
      public void run() {
        while(true) {
          try {
            Thread.sleep(1000);
          } catch (InterruptedException ex) {
            //no reaction - I'm very very bad dummy
          }
        }
      }
      public void terminate() {
      }
    };
    dispatcher.schedule(goodDummy);
    dispatcher.start();
    shortSleep();
    assertTrue(goodDummy.isProcessed());
    final Thread worker = goodDummy.getWorker();
    dispatcher.terminate(goodDummy);
    dispatcher.schedule(badDummy);
    shortSleep();
    assertTrue(badDummy.isProcessed());
    assertEquals(worker, badDummy.getWorker());
    dispatcher.terminate(badDummy);
    assertEquals(0, dispatcher.activeCount());
    assertEquals(0, dispatcher.waitingCount());
    dispatcher.schedule(goodDummy);
    assertEquals(1, dispatcher.activeCount() + dispatcher.waitingCount());
    shortSleep();
    assertEquals(1, dispatcher.activeCount());
    assertEquals(0, dispatcher.waitingCount());
    assertTrue(goodDummy.isProcessed());
    assertNotSame(worker, goodDummy.getWorker());
    dispatcher.stop();
  }
  
  public void testWorkability() {
    final ProcessDispatcher dispatcher = new RoundRobinDispatcher(50, 10);
    final DummyProcess[] dummies = new DummyProcess[15];
    for (int i = 0; i < 15; i++) {
      dummies[i] = new DummyProcess( i + 1);
    }
    for (int i = 0 ; i < 5; i++) {
      dispatcher.schedule(dummies[i]);
    }
    assertEquals(5, dispatcher.waitingCount());
    dispatcher.start();
    shortSleep();
    assertEquals(5, dispatcher.activeCount());
    for (int i = 5 ; i < 10; i++) {
      dispatcher.schedule(dummies[i]);
      shortSleep();
      assertEquals(i + 1, dispatcher.activeCount());
      assertEquals(0, dispatcher.waitingCount());
    }
    longSleep();
    for (int i = 0 ; i < 10; i++) {
      assertTrue(dummies[i].isProcessed());
    }
    dispatcher.schedule(dummies[11]);
    dispatcher.schedule(dummies[10]);
    assertEquals(10, dispatcher.activeCount());
    assertEquals(2, dispatcher.waitingCount());
    dummies[5].terminate();
    //dispatcher.terminate(dummies[5]);
    longSleep();
    longSleep();
    assertEquals(10, dispatcher.activeCount());
    assertEquals(1, dispatcher.waitingCount());
    dispatcher.schedule(dummies[12]);
    dispatcher.schedule(dummies[13]);
    dispatcher.schedule(dummies[14]);
    
    dispatcher.terminate(dummies[0]);
    dispatcher.terminate(dummies[1]);
    dispatcher.terminate(dummies[2]);
    dispatcher.terminate(dummies[3]);
    //   dummies[0].terminate();
    //   dummies[1].terminate();
    //  dummies[2].terminate();
    //  dummies[3].terminate();
    longSleep();
    assertEquals(10, dispatcher.activeCount());
    assertEquals(0, dispatcher.waitingCount());
    for (int i = 6; i < 15; i++) {
      assertTrue(dummies[i].isProcessed());
    }
    dispatcher.stop();
    assertEquals(0, dispatcher.activeCount());
    assertEquals(0, dispatcher.waitingCount());
    for (int i = 0; i < 15; i++) {
      assertFalse(dummies[i].isProcessed());
    }
    dispatcher.stop();
  }
  
  private void longSleep() {
    try {
      Thread.sleep(500);
    } catch (InterruptedException ex) {//skip
    }
  }
  
  private void shortSleep() {
    try {
      Thread.sleep(100);
    } catch (InterruptedException ex) {//skip
    }
  }
}
