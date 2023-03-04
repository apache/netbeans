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
package org.openide.windows;

/**
 * Utility class to help clients manage {@link Mode}s in order to layout TopComponents
 * in predefined "work-spaces".
 * <br>
 * <br>
 * In a NetBeans Platform application, a user may create various TopComponents and
 * drag them around, into and out of the existing defined Modes (e.g. editor, explorer) or 
 * new "anonymous" Modes which get created by the system as needed.
 * <br>
 * <br>
 * The configuration of each Mode can be expressed in XML. This class gives access
 * to that XML. {@link WindowManager} provides a 
 * {@link WindowManager#createModeFromXml mechanism to reproduce a Mode from XML}
 * so you can construct ways to save Mode and TopComponent combinations, which can 
 * subsequently be chosen and reloaded at a later time.
 * <br>
 * <br>
 * Note that this is a different approach to how the Platform tends to save the layout
 * of TopComponents at shut-down time in the Windows2Local file system. Note also that 
 * this is not connected with the deprecated notion of {@link Workspace}.
 * 
 * @see http://wiki.apidesign.org/wiki/ExtendingInterfaces
 * 
 * @author Mark Phipps
 * @since 6.82
 */
public final class ModeUtilities {

    private ModeUtilities() {
    }

    /**
     * Expose the Mode's configuration as XML.
     * 
     * @param mode the {@link Mode} whose XML configuration is required.
     * @return the XML of the Mode's configuration or {@code null} if not supported.
     */
    public static final String toXml(Mode mode) {
        return mode instanceof Mode.Xml
                ? ((Mode.Xml) mode).toXml()
                : null;
    }
}
