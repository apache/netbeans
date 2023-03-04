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

package org.netbeans.lib.profiler.marker;

import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.results.cpu.marking.MarkMapping;
import org.netbeans.lib.profiler.utils.Wildcards;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Jaroslav Bachorik
 */
public class ClassMarker implements Marker {
    private static Logger LOGGER = Logger.getLogger(ClassMarker.class.getName());
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private Map markMap;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a new instance of ClassMarker */
    public ClassMarker() {
        markMap = new HashMap();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public MarkMapping[] getMappings() {
        List mappings = new ArrayList();

        for (Iterator iter = markMap.keySet().iterator(); iter.hasNext();) {
            String className = (String) iter.next();
            ClientUtils.SourceCodeSelection markerMethod = new ClientUtils.SourceCodeSelection(className, Wildcards.ALLWILDCARD,
                                                                                               ""); // NOI18N
            markerMethod.setMarkerMethod(true);
            mappings.add(new MarkMapping(markerMethod, (Mark) markMap.get(className)));
        }

        return (MarkMapping[]) mappings.toArray(new MarkMapping[0]);
    }

    public Mark[] getMarks() {
        return (Mark[])new HashSet(markMap.values()).toArray(new Mark[0]);
    }

    public void addClassMark(String className, Mark mark) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("Marking class " + className + " with " + mark.getId());
        }

        markMap.put(className, mark);
    }

    public void removeClassMark(String className) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("Unmarking class " + className);
        }
        markMap.remove(className);
    }

    public void resetClassMarks() {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("Unmarking all classes");
        }
        markMap.clear();
    }
}
