/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.parsing.impl;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.parsing.implspi.ProfilerSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Self profiling Support
 * @author Tomas Zezula
 */
final class SelfProfile {
    private static final Logger LOG = Logger.getLogger(SelfProfile.class.getName());
    
    private final ProfilerSupport profiler;
    private final long time;
    private volatile boolean profiling;

    SelfProfile (long when) {
        time = when;
        
        ProfilerSupport.Factory f = Lookup.getDefault().lookup(ProfilerSupport.Factory.class);
        if (f != null) {
            this.profiler = f.create("taskcancel"); // NOI18N
        } else {
            this.profiler = null;
        }
        this.profiling = true;
    
        LOG.finest("STARTED");  //NOI18N
        if (profiler != null) {
            profiler.start();
            LOG.log(
                Level.FINE,
                "Profiling started {0} at {1}", //NOI18N
                new Object[] {
                    profiler,
                    time
                });
        }
    }

    final synchronized void stop() {
        if (!profiling) {
            return;
        }
        try {
            stopImpl();
        } catch (Exception ex) {
            LOG.log(Level.INFO, "Cannot stop profiling", ex);
        } finally {
            profiling = false;
        }
    }

    private void stopImpl() throws Exception {
        final long now = System.currentTimeMillis();
        long delta = now - time;
        LOG.log(Level.FINE, "Profiling stopped at {0}", now);
        int report = Integer.getInteger("org.netbeans.modules.parsing.api.taskcancel.slowness.report", 1000); // NOI18N
        if (delta < report) {
            LOG.finest("CANCEL");  //NOI18N
            if (profiler != null) {
                profiler.cancel();
                LOG.log(
                    Level.FINE,
                    "Cancel profiling of {0}. Profiling {1}. Time {2} ms.",     //NOI18N
                    new Object[] {
                        profiler,
                        profiling,
                        delta
                    });
            }
            return;
        }
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(out);
            LOG.finest("LOGGED");  //NOI18N
            if (profiler != null) {
                profiler.stopAndSnapshot(dos);
                LOG.log(
                    Level.FINE,
                    "Obtaining snapshot for {0} ms.",   //NOI18N
                    delta);
            }
            dos.close();
            if (dos.size() > 0) {
                Object[] params = new Object[]{out.toByteArray(), delta, "ParserResultTask-cancel"};    //NOI18N
                Logger.getLogger("org.netbeans.ui.performance").log(Level.CONFIG, "Slowness detected", params); //NOI18N
                LOG.log(Level.FINE, "Snapshot sent to UI logger. Size {0} bytes.", dos.size()); //NOI18N
            } else {
                LOG.log(Level.WARNING, "No snapshot taken"); // NOI18N
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
