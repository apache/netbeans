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
        Class<?> clazz = SwingController.class.getSuperclass().getSuperclass();
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
