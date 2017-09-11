/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.core.startup;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.netbeans.junit.NbTestCase;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Modules;
import org.openide.util.Task;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class NbStartStopTest extends NbTestCase{
    private InstanceContent stop;
    private InstanceContent start;
    private NbStartStop onStartStop;
    
    public NbStartStopTest(String n) {
        super(n);
    }

    @Override
    protected void setUp() throws Exception {
        start = new InstanceContent();
        stop = new InstanceContent();
        onStartStop = new NbStartStop(
            new AbstractLookup(start),
            new AbstractLookup(stop)
        );
    }
    
    public void testStartIsInvokedDuringInit() {
        final boolean[] ok = { false };
        start.add(new Runnable() {
            @Override public void run() {
                ok[0] = true;
            }
        });
        
        onStartStop.initialize();
        onStartStop.waitOnStart();
        
        assertTrue("Initialized", ok[0]);
    }

    public void testStartIsInvokedWhenModuleIsAdded() {
        final boolean[] ok = { false };
        
        onStartStop.initialize();
        onStartStop.waitOnStart();
        start.add(new Runnable() {
            @Override public void run() {
                ok[0] = true;
            }
        });
        onStartStop.waitOnStart();
        
        assertTrue("Initialized", ok[0]);
    }

    public void testStopping() {
        final boolean[] ok = { false };
        
        onStartStop.initialize();
        onStartStop.waitOnStart();
        final Callable<Boolean> run = new Callable<Boolean>() {
            @Override public Boolean call() {
                return ok[0] = true;
            }
        };
        stop.add(run);
        List<ModuleInfo> modules = Collections.singletonList(Modules.getDefault().ownerOf(run.getClass()));
        assertTrue("Close approved", onStartStop.closing(modules));
        
        assertTrue("Initialized", ok[0]);
    }

    public void testStoppingFalse() {
        final boolean[] ok = { false };
        
        onStartStop.initialize();
        onStartStop.waitOnStart();
        final Callable<Boolean> run = new Callable<Boolean>() {
            @Override public Boolean call() {
                ok[0] = true;
                return false;
            }
        };
        stop.add(run);
        List<ModuleInfo> modules = Collections.singletonList(Modules.getDefault().ownerOf(run.getClass()));
        assertFalse("Close rejected", onStartStop.closing(modules));
        
        assertTrue("Initialized", ok[0]);
    }

    public void testStop() {
        final boolean[] ok = { false };
        
        onStartStop.initialize();
        onStartStop.waitOnStart();
        final Runnable run = new Runnable() {
            @Override public void run() {
                ok[0] = true;
            }
        };
        stop.add(run);
        List<ModuleInfo> modules = Collections.singletonList(Modules.getDefault().ownerOf(run.getClass()));
        for (Task t : onStartStop.startClose(modules)) {
            t.waitFinished();
        }
        
        assertTrue("Initialized", ok[0]);
    }
}
