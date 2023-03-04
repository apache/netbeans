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

package org.netbeans.lib.profiler.results.cpu.marking;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import org.netbeans.lib.profiler.marker.Mark;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.global.ProfilingSessionStatus;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.lib.profiler.marker.Marker;


/**
 *
 * @author Jaroslav Bachorik
 */
public class MarkingEngine {
    private static String INVALID_MID = ResourceBundle.getBundle("org.netbeans.lib.profiler.results.cpu.Bundle").getString("MSG_INVALID_METHODID"); // NOI18N
    
    private static Logger LOGGER = Logger.getLogger(MarkingEngine.class.getName());
    
    //~ Inner Interfaces ---------------------------------------------------------------------------------------------------------

    public static interface StateObserver {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        void stateChanged(MarkingEngine instance);
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static MarkingEngine instance;

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private final Object markGuard = new Object();

    private final MarkMapper mapper;

    // @GuardedBy markGuard
    private MarkMapping[] marks;

    private Set observers = new HashSet();

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /**
     * Creates a new instance of MarkingEngine
     */
    private MarkingEngine() {
        mapper = new MarkMapper();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public static synchronized MarkingEngine getDefault() {
        if (instance == null) {
            instance = new MarkingEngine();
        }

        return instance;
    }

    // configure the engine for a given set of {@linkplain MarkMapping}
    public synchronized void configure(MarkMapping[] mappings, Collection observers) {
        setMarks(mappings != null ? mappings : Marker.DEFAULT.getMappings());
        this.observers.clear();
        this.observers.add( mapper );
        this.observers.addAll(observers);
    }

    public synchronized void deconfigure() {
        setMarks(Marker.DEFAULT.getMappings());
    }

    public ClientUtils.SourceCodeSelection[] getMarkerMethods() {
        synchronized (markGuard) {
            if (marks == null) {
                return new ClientUtils.SourceCodeSelection[0];
            }

            ClientUtils.SourceCodeSelection[] methods = new ClientUtils.SourceCodeSelection[marks.length];

            for (int i = 0; i < marks.length; i++) {
                methods[i] = marks[i].markMask;
            }

            return methods;
        }
    }

    public int getNMarks() {
        synchronized (markGuard) {
            return (marks != null) ? marks.length : 0;
        }
    }

    public Mark markMethod(int methodId, ProfilingSessionStatus status) {
        synchronized(mapper) {
            return mapper.getMark(methodId, status);
        }
    }

    Mark mark(int methodId, ProfilingSessionStatus status) {
        ClientUtils.SourceCodeSelection method = null;

        synchronized (markGuard) {
            if (marks == null || marks.length == 0 || status == null) {
                return Mark.DEFAULT;
            }

            status.beginTrans(false);

            try {
                String[] cNames = status.getInstrMethodClasses();
                String[] mNames = status.getInstrMethodNames();
                String[] sigs = status.getInstrMethodSignatures();
                
                if (mNames.length <= methodId || cNames.length <= methodId || sigs.length <= methodId) {
                    int maxMid = Math.min(Math.min(mNames.length, cNames.length), sigs.length);
                    LOGGER.log(Level.WARNING, INVALID_MID, new Object[]{methodId, maxMid});
                } else {
                    method = new ClientUtils.SourceCodeSelection(cNames[methodId],
                                                                 mNames[methodId],
                                                                 sigs[methodId]);
                }
            } finally {
                status.endTrans();
            }

            if (method != null) {
                String methodSig = method.toFlattened();

                for (int i = 0; i < marks.length; i++) {
                    if (methodSig.startsWith(marks[i].markSig)) {
                        return marks[i].mark;
                    }
                }
            }

            return Mark.DEFAULT;
        }
    }

    private void setMarks(MarkMapping[] marks) {
        boolean stateChange = false;

        synchronized (markGuard) {
            stateChange = !Arrays.equals(this.marks,marks);
            this.marks = marks;
        }
        if (stateChange) {
            fireStateChanged();
        }
    }

    private void fireStateChanged() {
        for (Iterator iter = observers.iterator(); iter.hasNext();) {
            ((StateObserver) iter.next()).stateChanged(this);
        }
    }
}
