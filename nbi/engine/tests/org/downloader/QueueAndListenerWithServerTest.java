/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.downloader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import org.MyTestCase;
import org.netbeans.installer.downloader.DownloadConfig;
import org.netbeans.installer.downloader.Pumping;
import org.netbeans.installer.downloader.queue.DispatchedQueue;
import org.netbeans.installer.downloader.services.EmptyQueueListener;
import org.server.WithServerTestCase;

/**
 *
 * @author Danila_Dugurov
 */
public class QueueAndListenerWithServerTest extends WithServerTestCase {
  
  public void testFailedDwonload() throws MalformedURLException {
    final DispatchedQueue queue = new DispatchedQueue(new File(MyTestCase.testWD, "queueState.xml"));
    final VerboseTracer listener = new VerboseTracer(queue);
    EmptyQueueListener notifier = new EmptyQueueListener() {
      public void pumpingStateChange(String id) {
        System.out.println(queue.getById(id).state());
        if (queue.getById(id).state() == Pumping.State.FAILED) {
          synchronized (queue) {
            queue.notifyAll();
          }
        }
      }
    };
    queue.addListener(notifier);
    queue.invoke();
    queue.add(new URL("http://www.oblom.com:8080/oblom.data"));
    synchronized (queue) {
      try {
        queue.wait();
      } catch (InterruptedException ex) {
        ex.printStackTrace();
      }
    }
    queue.terminate();
    assertEquals(2 + DownloadConfig.ATTEMPT_COUNT * 2 + 2, listener.verboseActions.size());
    assertEquals("invoke", listener.verboseActions.get(0).getFirst());
    assertEquals("add", listener.verboseActions.get(1).getFirst());
    for (int i = 2 ; i < 2 + DownloadConfig.ATTEMPT_COUNT * 2; i+=2) {
      assertEquals("stateChange", listener.verboseActions.get(i).getFirst());
      assertEquals(Pumping.State.CONNECTING.toString(), listener.verboseActions.get(i).getSecond()[1]);
      assertEquals("stateChange", listener.verboseActions.get(i + 1).getFirst());
      assertEquals(Pumping.State.WAITING.toString(), listener.verboseActions.get(i+1).getSecond()[1]);
    }
    assertEquals(Pumping.State.FAILED.toString(), listener.verboseActions.get(2 + DownloadConfig.ATTEMPT_COUNT * 2).getSecond()[1]);
    assertEquals("terminate", listener.verboseActions.get(2 + DownloadConfig.ATTEMPT_COUNT * 2 + 1).getFirst());
  }
}
