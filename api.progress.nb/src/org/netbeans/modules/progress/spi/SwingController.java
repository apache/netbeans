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
package org.netbeans.modules.progress.spi;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Swing-oriented Controller of Progress API. Unlike the basic {@link Controller},
 * it provides access to Worker's {@link  Component}.
 * 
 * @author sdedic
 * @since 1.40
 */
public class SwingController extends Controller implements Executor, Runnable {
    private static final SwingController INSTANCE = new SwingController(null);
    private static final int TIMER_QUANTUM = 400;

    final Timer timer;
    
    public SwingController(ProgressUIWorker comp) {
        super(comp);
        timer = new Timer(TIMER_QUANTUM, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runNow();
            }
        });
        timer.setRepeats(false);
        runCompatInit(this);
    }

    @Override
    protected Executor getEventExecutor() {
        return this;
    }
    
    protected void runEvents() {
        if (SwingUtilities.isEventDispatchThread()) {
           runNow();
        } else {
            execute(this);
        }
    }

    @Override
    public void run() {
        runNow();
    }

    public void execute(final Runnable r) {
        SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                r.run();
             }
        });
    }
    
    protected void resetTimer(int delay, boolean restart) {
        if (delay > 0) {
            timer.setInitialDelay(delay);
            if (restart) {
                timer.restart();
            }
        } else {
            timer.stop();
        }
    }

    public static SwingController getDefault() {
        return INSTANCE;
    }
    
    // to be called on the default instance only..
    public Component getVisualComponent() {
        Object component = getProgressUIWorker();
        if (component instanceof Component) {
            return (Component)component;
        }
        return null;
    }
    
    protected ProgressUIWorkerWithModel createWorker() {
        return Lookup.getDefault().lookup(ProgressUIWorkerProvider.class).getDefaultWorker();
    }
    
    protected Timer getTimer() {
        return timer;
    }
    
    //////// Begin backward compatibilty hack ////////
    
    static final Method compatInit;
    
    static {
        // this is a compatibility hack: superclass is Controller, and its super
        // is either j.l.O or compat class in the compat mode.
        Class clazz = SwingController.class.getSuperclass().getSuperclass();
        Method m = null;
        try {
            m = clazz.getDeclaredMethod("compatPostInit", javax.swing.Timer.class);
            m.setAccessible(true);
        } catch (NoSuchMethodException ex) {
            // OK
        } catch (SecurityException ex) {
            Exceptions.printStackTrace(ex);
        }
        compatInit = m;
    }

    /**
     * Compatibility hack. The injected superclass cannot force run any code after
     * subclass constructors are run, yet it needs to provide its client with a
     * Timer instance from its protected field. So if we really inhetrit from
     * the compat injected class, call reflectively its setup method.
     * @param instance 
     */
    private static void runCompatInit(SwingController instance) {
        if (compatInit != null) {
            try {
                compatInit.invoke(instance, instance.timer);
            } catch (IllegalAccessException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
