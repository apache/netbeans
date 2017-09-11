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
