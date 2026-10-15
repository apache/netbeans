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

package org.netbeans.modules.j2ee.ejbcore.ejb.wizard.session;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.openide.util.NbBundle;

/**
 * Class which is holding information for creation of {@code @Schedule} annotation.
 *
 * @author Martin Fousek
 */
public final class TimerOptions {

    private static final Set<String> SCHEDULE_ATTRIBUTES = new HashSet<>(
            Arrays.asList("second", "minute", "hour", "dayOfMonth", "month", //NOI18N
                    "dayOfWeek", "year", "info", "persistent", "timezone") //NOI18N
    );

    private final Map<String, String> timerOptions = new HashMap<>();
    /**
     * Gets {@code Map} with entries of {@code @Schedule} annotation.
     *
     * @return {@code Map} of entries
     */
    public Map<String, String> getTimerOptionsAsMap() {
        return timerOptions;
    }

    /**
     * Set values into {@code Map} of {@code @Schedule} annotation entries, if valid.
     *
     * @param scheduleString {@code String} which will be parsed for all attributes,
     * the value of {@code TimerOption} change just in case of valid scheduleString
     */
    public void setTimerOptions(String scheduleString) {
        if (validate(scheduleString) == null) {
            String[] sections = splitScheduleSections(omitNewLines(scheduleString));
            parseSectionsIntoMap(sections, timerOptions);
        }
    }

    /**
     * Check if given scheduleString can be successfully parsed for annotation attributes.
     *
     * @param scheduleString input string for parsing
     * @return {@code String} with error message, {@code null} otherwise
     */
    public static String validate(String scheduleString) {
        String[] sections = splitScheduleSections(omitNewLines(scheduleString));

        Map<String, String> actualSchedule = new HashMap<String, String>();
        if (!parseSectionsIntoMap(sections, actualSchedule)) {
            return NbBundle.getMessage(TimerOptions.class, "ERR_TO_UnparsableSchedule"); //NOI18N
        }

        if (actualSchedule.isEmpty()) {
            return NbBundle.getMessage(TimerOptions.class, "ERR_TO_NotEnoughAttributes"); //NOI18N
        } else if (actualSchedule.size() > 10) {
            return NbBundle.getMessage(TimerOptions.class, "ERR_TO_ToMuchAttributes");    //NOI18N
        } else if (actualSchedule.containsKey("persistent")) {                            //NOI18N
            return NbBundle.getMessage(TimerOptions.class, "ERR_TO_PersistentParameter",  //NOI18N
                    new Object[]{NbBundle.getMessage(TimerOptions.class, "LBL_NonPersistentTimer").replace("&", "")}); //NOI18N
        } else {
            String invalidAttributesString = invalidAttributes(actualSchedule.keySet());
            if (invalidAttributesString != null) {
                return NbBundle.getMessage(TimerOptions.class, "ERR_TO_InvalidAtributes", invalidAttributesString); //NOI18N
            }
        }
        return null;
    }

    private static String omitNewLines(String string) {
        return string.replace("\n", ""); //NOI18N
    }

    private static String[] splitScheduleSections(String scheduleValue) {
        String[] sections = scheduleValue.split(","); //NOI18N
        List<String> finalSections = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sections.length; i++) {
            sb.append(sections[i]);
            if (getCountOfQuotes(sb.toString()) < 2) {
                sb.append(","); //NOI18N
                continue;
            }
            finalSections.add(sb.toString());
            sb = new StringBuilder();
        }

        if (!"".equals(sb.toString())) {
            finalSections.add(sb.toString());
        }
        return finalSections.toArray(new String[0]);
    }

    private static int getCountOfQuotes(String string) {
        int count = string.split("\"").length - 1; //NOI18N
        if (string.endsWith("\"") || string.startsWith("\"")) { //NOI18N
            return count + 1;
        } else {
            return count;
        }
    }

    private static boolean parseSectionsIntoMap(String[] sections, Map<String, String> map) {
        for (String section : sections) {
            String[] row = section.split("="); //NOI18N
            if (row.length != 2) {
                return false;
            } else {
                map.put(row[0].trim(), row[1].trim().replace("\"", "")); //NOI18N
            }
        }
        return true;
    }

    private static String invalidAttributes(Set<String> actualAttributes) {
        Set<String> copy = new HashSet<String>(actualAttributes);
        copy.removeAll(SCHEDULE_ATTRIBUTES);

        if (copy.isEmpty()) {
            return null;
        } else {
            StringBuilder invalidAttributes = new StringBuilder();
            for (String attribute : copy) {
                invalidAttributes.append(attribute).append(", "); //NOI18N
            }
            return invalidAttributes.substring(0, invalidAttributes.length() - 2);
        }
    }

    public String getAnnotationValue() {
        StringBuilder sb = new StringBuilder();
        Iterator<Entry<String, String>> iterator = timerOptions.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, String> entry = iterator.next();
            sb.append(entry.getKey()).append(" = ").append("\"").append(entry.getValue()).append("\""); //NOI18N
            if (iterator.hasNext()) {
                sb.append(", "); //NOI18N
            }
        }
        return sb.toString();
    }

}
