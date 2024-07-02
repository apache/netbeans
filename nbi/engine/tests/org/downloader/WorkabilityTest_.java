/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.downloader;

import java.io.File;
import org.MyTestCase;
import org.netbeans.installer.downloader.DownloadListener;
import org.netbeans.installer.downloader.Pumping;
import org.netbeans.installer.downloader.Pumping.Section;
import org.netbeans.installer.downloader.queue.DispatchedQueue;
import org.netbeans.installer.downloader.services.EmptyQueueListener;
import org.server.TestDataGenerator;
import org.server.WithServerTestCase;

/**
 *
 * @author Danila_Dugurov
 */
public class WorkabilityTest_ extends WithServerTestCase {
  
  public void testStepByStepWorkability() {
    final DispatchedQueue queue = new DispatchedQueue(new File(MyTestCase.testWD, "queueState.xml"));
    final DownloadListener listener = new EmptyQueueListener() {
      int i = 0;
      public void pumpingStateChange(String id) {
        final Pumping pumping = queue.getById(id);
        System.out.println("pumping file " + pumping.outputFile() + " " + pumping.state());
        if (pumping.state() == Pumping.State.FINISHED) {
          assertEquals(pumping.length(), TestDataGenerator.testFileSizes[i++]);
          synchronized (WorkabilityTest_.this) {
            WorkabilityTest_.this.notify();
          }
        } else if (pumping.state() == Pumping.State.FAILED) {fail();}
      }
      public void pumpingUpdate(String id) {
        //  System.out.print("Update downloading file.." + queue.getById(id).outputFile().getName());
        //    System.out.println("  Size = " + downperc(queue.getById(id)));
      }
      
      private long downperc(Pumping pumping) {
        long size = 0;
        for (Section section : pumping.getSections()) {
          size +=section.offset() - section.getRange().getFirst();
        }
        return /*pumping.length() > 0 ? size * 100 / pumping.length():*/ size;
      }
    };
    queue.addListener(listener);
    assertFalse(queue.isActive());
    queue.invoke();
    assertTrue(queue.isActive());
    int i = 0 ;
    while (i < TestDataGenerator.testUrls.length) {
      synchronized (this) {
        queue.add(TestDataGenerator.testUrls[i], MyTestCase.testOutput);
        try {
          wait();
        } catch (InterruptedException ex) {
          fail();
        }
      }
      i++;
    }
    queue.terminate();
  }
  
  public void testConcurrentlyWorkability() {
    final DispatchedQueue queue = new DispatchedQueue(new File(MyTestCase.testWD, "queueState.xml"));
    final DownloadListener listener = new EmptyQueueListener() {
      int i = 0;
      public void pumpingStateChange(String id) {
        final Pumping pumping = queue.getById(id);
        System.out.println("pumping file " + pumping.outputFile() + " " + pumping.state());
        if (pumping.state() == Pumping.State.FINISHED) {
          i++;
          if (i == TestDataGenerator.testUrls.length) {
            synchronized (WorkabilityTest_.this) {
              WorkabilityTest_.this.notify();
            }
          }
        } else if (pumping.state() == Pumping.State.FAILED) {fail();}
      }
      
      public void pumpingUpdate(String id) {
        //  System.out.print("Update downloading file.." + queue.getById(id).outputFile().getName());
        //    System.out.println("  Size = " + downperc(queue.getById(id)));
      }
      
      private long downperc(Pumping pumping) {
        long size = 0;
        for (Section section : pumping.getSections()) {
          size +=section.offset() - section.getRange().getFirst();
        }
        return /*pumping.length() > 0 ? size * 100 / pumping.length():*/ size;
      }
    };
    queue.addListener(listener);
    queue.invoke();
    int i = 0 ;
    while (i < TestDataGenerator.testUrls.length) {
      queue.add(TestDataGenerator.testUrls[i], MyTestCase.testOutput);
      i++;
    }
    synchronized (this) {
      try {
        wait();
      } catch (InterruptedException ex) {
        fail();
      }
    }
    queue.terminate();
  }
}
