/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.api.debugger.jpda;

import com.sun.jdi.request.EventRequest;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.prefs.Preferences;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.NbPreferences;

/**
 * Abstract definition of JPDA breakpoint.
 *
 * @author   Jan Jancura
 */
public class JPDABreakpoint extends Breakpoint {

    // static ..................................................................

    static final ClassPath EMPTY_CLASSPATH = ClassPathSupport.createClassPath( new FileObject[0] );

    /** Property name constant. */
    public static final String          PROP_SUSPEND = "suspend"; // NOI18N
    /** Property name constant. */
    public static final String          PROP_HIDDEN = "hidden"; // NOI18N
    /** Property name constant. */
    public static final String          PROP_PRINT_TEXT = "printText"; // NOI18N

    /** Suspend property value constant. */
    public static final int             SUSPEND_ALL = EventRequest.SUSPEND_ALL;
    /** Suspend property value constant. */
    public static final int             SUSPEND_EVENT_THREAD = EventRequest.SUSPEND_EVENT_THREAD;
    /** Suspend property value constant. */
    public static final int             SUSPEND_NONE = EventRequest.SUSPEND_NONE;

    
    // private variables .....................................................

    /** Set of actions. */
    private boolean                     enabled = true;
    private boolean                     hidden = false;
    private int                         suspend;
    private String                      printText;
    private Collection<JPDABreakpointListener>  breakpointListeners = new HashSet<JPDABreakpointListener>();
    private volatile JPDADebugger       session;
    private List<DebuggerEngine> engines = new ArrayList<DebuggerEngine>();
    
   
    JPDABreakpoint () {
        Preferences preferences = NbPreferences.forModule(getClass()).node("debugging"); // NOI18N
        int num = preferences.getInt("default.suspend.action", -1); // NOI18N [TODO] create property name constant, use it in ActionsPanel
        if (num == -1) {
            Properties p = Properties.getDefault().getProperties("debugger.options.JPDA");
            num = p.getInt("BreakpointSuspend", JPDABreakpoint.SUSPEND_EVENT_THREAD);
        }
        switch (num) {
            case 0: suspend = SUSPEND_NONE; break;
            case 1: suspend = SUSPEND_EVENT_THREAD; break;
            case 2: suspend = SUSPEND_ALL;
        }
    }
    

    // main methods ............................................................
    
    /**
     * Gets value of suspend property.
     *
     * @return value of suspend property
     */
    public synchronized int getSuspend () {
        return suspend;
    }

    /**
     * Sets value of suspend property.
     *
     * @param s a new value of suspend property
     */
    public void setSuspend (int s) {
        int old;
        synchronized (this) {
            if (s == suspend) {
                return;
            }
            old = suspend;
            suspend = s;
        }
        firePropertyChange (PROP_SUSPEND, Integer.valueOf(old), Integer.valueOf(s));
    }
    
    /**
     * Gets value of hidden property.
     *
     * @return value of hidden property
     */
    public synchronized boolean isHidden () {
        return hidden;
    }

    /**
     * Sets value of hidden property.
     *
     * @param h a new value of hidden property
     */
    public void setHidden (boolean h) {
        boolean old;
        synchronized (this) {
            if (h == hidden) {
                return;
            }
            old = hidden;
            hidden = h;
        }
        firePropertyChange (PROP_HIDDEN, Boolean.valueOf (old), Boolean.valueOf (h));
    }
    
    /**
     * Gets value of print text property.
     *
     * @return value of print text property or <code>null</code>.
     */
    public synchronized String getPrintText () {
        return printText;
    }

    /**
     * Sets value of print text property.
     *
     * @param printText a new value of print text property. Can be <code>null</code>.
     */
    public void setPrintText (String printText) {
        String old;
        synchronized (this) {
            if (printText == null ? this.printText == null : printText.equals(this.printText)) {
                return;
            }
            old = this.printText;
            this.printText = printText;
        }
        firePropertyChange (PROP_PRINT_TEXT, old, printText);
    }

    /**
     * Test whether the breakpoint is enabled.
     *
     * @return <code>true</code> if so
     */
    @Override
    public synchronized boolean isEnabled () {
        return enabled;
    }
    
    /**
     * Disables the breakpoint.
     */
    @Override
    public void disable () {
        synchronized (this) {
            if (!enabled) {
                return;
            }
            enabled = false;
        }
        firePropertyChange 
            (PROP_ENABLED, Boolean.TRUE, Boolean.FALSE);
    }
    
    /**
     * Enables the breakpoint.
     */
    @Override
    public void enable () {
        synchronized (this) {
            if (enabled) {
                return;
            }
            enabled = true;
        }
        firePropertyChange 
            (PROP_ENABLED, Boolean.FALSE, Boolean.TRUE);
    }

    /**
     * Set the specific session where this breakpoint belongs to.
     * This will make the breakpoint session-specific
     *
     * @param session the specific session
     * @since 2.41
     */
    public void setSession(JPDADebugger session) {
        this.session = session;
    }

    /**
     * Get the specific session where this breakpoint belongs to.
     * If not <code>null</code>, the breakpoint is considered in this session only
     * and is discarded after this session finishes.
     *
     * @return the specific session or <code>null</code>.
     * @since 2.41
     */
    public JPDADebugger getSession() {
        return session;
    }

    @Override
    public boolean canHaveDependentBreakpoints() {
        return true;
    }
    
    /** 
     * Adds a JPDABreakpointListener.
     *
     * @param listener the listener to add
     */
    public synchronized void addJPDABreakpointListener (
        JPDABreakpointListener listener
    ) {
        breakpointListeners.add (listener);
    }

    /** 
     * Removes a JPDABreakpointListener.
     *
     * @param listener the listener to remove
     */
    public synchronized void removeJPDABreakpointListener (
        JPDABreakpointListener listener
    ){
        breakpointListeners.remove (listener);
    }

    /**
     * Fire JPDABreakpointEvent.
     *
     * @param event a event to be fired
     */
    void fireJPDABreakpointChange (JPDABreakpointEvent event) {
        JPDABreakpointListener[] listeners;
        synchronized (this) {
            listeners = breakpointListeners.toArray(new JPDABreakpointListener[0]);
        }
        for (JPDABreakpointListener l : listeners) {
            l.breakpointReached (event);
        }
    }

    void enginePropertyChange(PropertyChangeEvent evt) {
        if (DebuggerEngine.class.getName().equals(evt.getPropertyName())) {
            DebuggerEngine oldEngine = (DebuggerEngine) evt.getOldValue();
            DebuggerEngine newEngine = (DebuggerEngine) evt.getNewValue();
            if (oldEngine != null) {
                engines.remove(oldEngine);
            }
            if (newEngine != null) {
                engines.add(newEngine);
            }
            firePropertyChange(PROP_GROUP_PROPERTIES, null, null);
        }
    }

    DebuggerEngine[] getEngines() {
        if (engines.isEmpty()) {
            return null;
        } else {
            return engines.toArray(new DebuggerEngine[0]);
        }
    }

    static void fillFilesForClass(String className, List<FileObject> files) {
        int innerClassIndex = className.indexOf('$');
        if (innerClassIndex > 0) {
            className = className.substring(0, innerClassIndex);
        }
        String resource = className.replaceAll("\\.", "/")+".java";
        for (ClassPath cp : GlobalPathRegistry.getDefault().getPaths(ClassPath.SOURCE)) {
            FileObject f = cp.findResource(resource);
            if (f != null) {
                files.add(f);
            }
        }            
    }

}
