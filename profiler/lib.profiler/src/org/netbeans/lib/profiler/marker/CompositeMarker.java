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

import org.netbeans.lib.profiler.results.cpu.marking.MarkMapping;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;


/**
 *
 * @author Jaroslav Bachorik
 */
public class CompositeMarker implements Marker {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private Set delegates;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates a new instance of CompositeMarker */
    public CompositeMarker() {
        delegates = new LinkedHashSet();
    }

    public CompositeMarker(Set markerList) {
        this();
        delegates.addAll(markerList);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public MarkMapping[] getMappings() {
        Set markerMethods = new HashSet();

        for (Iterator iter = delegates.iterator(); iter.hasNext();) {
            Marker delegate = (Marker) iter.next();
            MarkMapping[] mMethods = delegate.getMappings();
            markerMethods.addAll(Arrays.asList(mMethods));
        }

        return (MarkMapping[]) markerMethods.toArray(new MarkMapping[0]);
    }

    public Mark[] getMarks() {
        Set allMarks = new HashSet();

        for (Iterator iter = delegates.iterator(); iter.hasNext();) {
            Marker delegate = (Marker) iter.next();
            Mark[] marks = delegate.getMarks();
            allMarks.addAll(Arrays.asList(marks));
        }
        return (Mark[]) allMarks.toArray(new Mark[0]);
    }

    public void addMarker(Marker marker) {
        if (marker == null) {
            return;
        }

        delegates.add(marker);
    }

    public void addMarkers(Collection markers) {
        if (markers == null) {
            return;
        }

        delegates.addAll(markers);
    }

    public void removeMarker(Marker marker) {
        if (marker == null) {
            return;
        }

        delegates.remove(marker);
    }

    public void removeMarkers(Collection markers) {
        if (markers == null) {
            return;
        }

        delegates.removeAll(markers);
    }
}
