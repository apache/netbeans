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
public abstract class TimedGlobalProfilingPoint extends GlobalProfilingPoint {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    public static class TimeCondition {
        //~ Static fields/initializers -------------------------------------------------------------------------------------------

        public static final String PROPERTY_TIMECOND_STARTTIME = "p_timecond_starttime"; // NOI18N
        public static final String PROPERTY_TIMECOND_REPEATS = "p_timecond_repeats"; // NOI18N
        public static final String PROPERTY_TIMECOND_PERIODTIME = "p_timecond_periodtime"; // NOI18N
        public static final String PROPERTY_TIMECOND_PERIODUNITS = "p_timecond_periodunits"; // NOI18N
        public static final int UNITS_MINUTES = 1;
        public static final int UNITS_HOURS = 2;

        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private boolean repeats;
        private int periodTime;
        private int periodUnits;
        private long scheduledTime;
        private long startTime;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public TimeCondition() {
            this(System.currentTimeMillis());
        }

        public TimeCondition(long startTime) {
            this(startTime, false, 1, UNITS_MINUTES);
        }

        public TimeCondition(long startTime, boolean repeats, int periodTime, int periodUnits) {
            setStartTime(startTime);
            setRepeats(repeats);
            setPeriodTime(periodTime);
            setPeriodUnits(periodUnits);
            setScheduledTime(startTime);
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public void setPeriodTime(int periodTime) {
            this.periodTime = periodTime;
        }

        public int getPeriodTime() {
            return periodTime;
        }

        public void setPeriodUnits(int periodUnits) {
            this.periodUnits = periodUnits;
        }

        public int getPeriodUnits() {
            return periodUnits;
        }

        public void setRepeats(boolean repeats) {
            this.repeats = repeats;
        }

        public boolean getRepeats() {
            return repeats;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
            this.scheduledTime = startTime;
        }

        public long getStartTime() {
            return startTime;
        }

        public boolean equals(Object object) {
            if (!(object instanceof TimeCondition)) {
                return false;
            }

            TimeCondition condition = (TimeCondition) object;

            return (startTime == condition.startTime) && (repeats == condition.repeats) && (periodTime == condition.periodTime)
                   && (periodUnits == condition.periodUnits);
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 53 * hash + (this.repeats ? 1 : 0);
            hash = 53 * hash + this.periodTime;
            hash = 53 * hash + this.periodUnits;
            hash = 53 * hash + (int) (this.startTime ^ (this.startTime >>> 32));
            return hash;
        }

        public static TimeCondition load(Lookup.Provider project, int index, Properties properties) {
            return load(project, index, null, properties);
        }

        public static TimeCondition load(Lookup.Provider project, int index, String prefix, Properties properties) {
            String absPrefix = (prefix == null) ? (index + "_") : (index + "_" + prefix); // NOI18N
            String startTimeStr = properties.getProperty(absPrefix + PROPERTY_TIMECOND_STARTTIME, null);
            String repeatsStr = properties.getProperty(absPrefix + PROPERTY_TIMECOND_REPEATS, null);
            String periodTimeStr = properties.getProperty(absPrefix + PROPERTY_TIMECOND_PERIODTIME, null);
            String periodUnitsStr = properties.getProperty(absPrefix + PROPERTY_TIMECOND_PERIODUNITS, null);

            if ((startTimeStr == null) || (repeatsStr == null) || (periodTimeStr == null) || (periodUnitsStr == null)) {
                return null;
            }

            TimeCondition condition = null;

            try {
                condition = new TimeCondition(Long.parseLong(startTimeStr), Boolean.parseBoolean(repeatsStr),
                                              Integer.parseInt(periodTimeStr), Integer.parseInt(periodUnitsStr));
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
            properties.put(absPrefix + PROPERTY_TIMECOND_STARTTIME, Long.toString(startTime));
            properties.put(absPrefix + PROPERTY_TIMECOND_REPEATS, Boolean.toString(repeats));
            properties.put(absPrefix + PROPERTY_TIMECOND_PERIODTIME, Integer.toString(periodTime));
            properties.put(absPrefix + PROPERTY_TIMECOND_PERIODUNITS, Integer.toString(periodUnits));
        }

        void setScheduledTime(long scheduledTime) {
            this.scheduledTime = scheduledTime;
        }

        long getScheduledTime() {
            return scheduledTime;
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    static final String PROPERTY_TIME = "p_timecond"; // NOI18N

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private TimeCondition condition;

    //~ Constructors -------------------------------------------------------------------------------------------------------------
    TimedGlobalProfilingPoint(String name, Lookup.Provider project, ProfilingPointFactory factory) {
        super(name, project, factory);
        condition = new TimeCondition();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void setCondition(TimeCondition condition) {
        if (this.condition.equals(condition)) {
            return;
        }

        TimeCondition oldCondition = this.condition;
        this.condition = condition;
        getChangeSupport().firePropertyChange(PROPERTY_TIME, oldCondition, condition);
    }

    public TimeCondition getCondition() {
        return condition;
    }
}
