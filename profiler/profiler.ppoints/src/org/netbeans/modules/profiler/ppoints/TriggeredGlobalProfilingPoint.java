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

package org.netbeans.modules.profiler.ppoints;

import org.openide.ErrorManager;
import java.util.Properties;
import org.openide.util.Lookup;


/**
 *
 * @author Jiri Sedlacek
 */
public abstract class TriggeredGlobalProfilingPoint extends GlobalProfilingPoint {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    public static class TriggerCondition {
        //~ Static fields/initializers -------------------------------------------------------------------------------------------

        public static final String PROPERTY_TRIGGCOND_METRIC = "p_triggcond_metric"; // NOI18N
        public static final String PROPERTY_TRIGGCOND_VALUE = "p_triggcond_value"; // NOI18N
        public static final String PROPERTY_TRIGGCOND_ONETIME = "p_triggcond_onetime"; // NOI18N
        public static final int METRIC_HEAPUSG = 1;
        public static final int METRIC_HEAPSIZ = 2;
        public static final int METRIC_SURVGEN = 3;
        public static final int METRIC_LDCLASS = 4;
        public static final int METRIC_THREADS = 5;
        public static final int METRIC_CPUUSG  = 6;
        public static final int METRIC_GCUSG   = 7;

        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private boolean onetime;
        private boolean triggered;
        private int metric;
        private long value; // [bytes] for HEAPSIZ, [percent <0 ~ 100>] for HEAPUSG, [count] for SURVGEN, LDCLASS

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public TriggerCondition() {
            this(METRIC_HEAPUSG, 95);
        }

        public TriggerCondition(int metric, long value) {
            this(metric, value, true);
        }

        public TriggerCondition(int metric, long value, boolean onetime) {
            setMetric(metric);
            setValue(value);
            setOnetime(onetime);
            setTriggered(false);
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public void setMetric(int metric) {
            this.metric = metric;
        }

        public int getMetric() {
            return metric;
        }

        public void setOnetime(boolean onetime) {
            this.onetime = onetime;
        }

        public boolean isOnetime() {
            return onetime;
        }

        public void setValue(long value) {
            this.value = value;
        }

        public long getValue() {
            return value;
        }

        public boolean equals(Object object) {
            if (!(object instanceof TriggerCondition)) {
                return false;
            }

            TriggerCondition condition = (TriggerCondition) object;

            return (metric == condition.metric) && (value == condition.value) && (onetime == condition.onetime);
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 61 * hash + (this.onetime ? 1 : 0);
            hash = 61 * hash + this.metric;
            hash = 61 * hash + (int) (this.value ^ (this.value >>> 32));
            return hash;
        }

        public static TriggerCondition load(Lookup.Provider project, int index, Properties properties) {
            return load(project, index, null, properties);
        }

        public static TriggerCondition load(Lookup.Provider project, int index, String prefix, Properties properties) {
            String absPrefix = (prefix == null) ? (index + "_") : (index + "_" + prefix); // NOI18N
            String metricStr = properties.getProperty(absPrefix + PROPERTY_TRIGGCOND_METRIC, null);
            String valueStr = properties.getProperty(absPrefix + PROPERTY_TRIGGCOND_VALUE, null);
            String onetimeStr = properties.getProperty(absPrefix + PROPERTY_TRIGGCOND_ONETIME, null);

            if ((metricStr == null) || (valueStr == null) || (onetimeStr == null)) {
                return null;
            }

            TriggerCondition condition = null;

            try {
                condition = new TriggerCondition(Integer.parseInt(metricStr), Long.parseLong(valueStr),
                                                 Boolean.parseBoolean(onetimeStr));
            } catch (Exception e) {
                ErrorManager.getDefault().log(ErrorManager.ERROR, e.getMessage());
            }

            return condition;
        }

        public void store(Lookup.Provider project, int index, Properties properties) {
            store(project, index, null, properties);
        }

        public void store(Lookup.Provider project, int index, String prefix, Properties properties) {
            String absPrefix = (prefix == null) ? (index + "_") : (index + "_" + prefix); // NOI18N
            properties.put(absPrefix + PROPERTY_TRIGGCOND_METRIC, Integer.toString(metric));
            properties.put(absPrefix + PROPERTY_TRIGGCOND_VALUE, Long.toString(value));
            properties.put(absPrefix + PROPERTY_TRIGGCOND_ONETIME, Boolean.toString(onetime));
        }

        void setTriggered(boolean triggered) {
            this.triggered = triggered;
        }

        boolean isTriggered() {
            return triggered;
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    static final String PROPERTY_TRIGGER = "p_triggcond"; // NOI18N

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private TriggerCondition condition;

    //~ Constructors -------------------------------------------------------------------------------------------------------------
    TriggeredGlobalProfilingPoint(String name, Lookup.Provider project, ProfilingPointFactory factory) {
        super(name, project, factory);
        condition = new TriggerCondition();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void setCondition(TriggerCondition condition) {
        if (this.condition.equals(condition)) {
            return;
        }

        TriggerCondition oldCondition = this.condition;
        this.condition = condition;
        getChangeSupport().firePropertyChange(PROPERTY_TRIGGER, oldCondition, condition);
    }

    public TriggerCondition getCondition() {
        return condition;
    }
    
    
    void reset() {
        condition.setTriggered(false);
    }
}
