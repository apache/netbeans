/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.ui.debugging;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.AbstractList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.util.WeakCacheMap;
import org.netbeans.spi.debugger.ui.DebuggingView.DVSupport;
import org.netbeans.spi.debugger.ui.DebuggingView.DVThread;

/**
 *
 * @author Martin Entlicher
 */
public class JPDADVThread implements DVThread, WeakCacheMap.KeyedValue<JPDAThreadImpl> {
    
    private final DebuggingViewSupportImpl dvSupport;
    private final JPDAThreadImpl t;
    private PropertyChangeProxyListener proxyListener;
    
    public JPDADVThread(DebuggingViewSupportImpl dvSupport, JPDAThreadImpl t) {
        this.dvSupport = dvSupport;
        this.t = t;
    }

    @Override
    public String getName() {
        return t.getName();
    }

    @Override
    public boolean isSuspended() {
        return t.isSuspended();
    }

    @Override
    public void resume() {
        t.resume();
    }

    @Override
    public void suspend() {
        t.suspend();
    }

    @Override
    public void makeCurrent() {
        t.makeCurrent();
    }

    @Override
    public DVSupport getDVSupport() {
        return dvSupport;
    }

    @Override
    public List<DVThread> getLockerThreads() {
        List<JPDAThread> lockerThreads = t.getLockerThreads();
        if (lockerThreads == null) {
            return null;
        }
        return new ThreadListDelegate(lockerThreads);
    }

    @Override
    public void resumeBlockingThreads() {
        t.resumeBlockingThreads();
    }

    @Override
    public Breakpoint getCurrentBreakpoint() {
        return t.getCurrentBreakpoint();
    }

    @Override
    public boolean isInStep() {
        return t.isInStep();
    }

    @Override
    public synchronized void addPropertyChangeListener(PropertyChangeListener pcl) {
        if (proxyListener == null) {
            proxyListener = new PropertyChangeProxyListener();
            t.addPropertyChangeListener(proxyListener);
        }
        proxyListener.add(pcl);
    }

    @Override
    public synchronized void removePropertyChangeListener(PropertyChangeListener pcl) {
        if (proxyListener != null) {
            proxyListener.remove(pcl);
            if (proxyListener.isEmpty()) {
                t.removePropertyChangeListener(proxyListener);
                proxyListener = null;
            }
        }
    }

    @Override
    public JPDAThreadImpl getKey() {
        return t;
    }
    
    private class ThreadListDelegate extends AbstractList<DVThread> {

        private final List<JPDAThread> threads;
        
        public ThreadListDelegate(List<JPDAThread> threads) {
            this.threads = threads;
        }

        @Override
        public DVThread get(int index) {
            return dvSupport.get((JPDAThreadImpl) threads.get(index));
        }

        @Override
        public int size() {
            return threads.size();
        }

    }


    
    private class PropertyChangeProxyListener implements PropertyChangeListener {
        
        private final List<PropertyChangeListener> listeners = new CopyOnWriteArrayList<>();

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            PropertyChangeEvent proxyEvent = new PropertyChangeEvent(JPDADVThread.this, evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            proxyEvent.setPropagationId(evt.getPropagationId());
            for (PropertyChangeListener pchl : listeners) {
                pchl.propertyChange(proxyEvent);
            }
        }
        
        void add(PropertyChangeListener pcl) {
            listeners.add(pcl);
        }
        
        void remove(PropertyChangeListener pcl) {
            listeners.remove(pcl);
        }
        
        boolean isEmpty() {
            return listeners.isEmpty();
        }
        
    }
    
}
