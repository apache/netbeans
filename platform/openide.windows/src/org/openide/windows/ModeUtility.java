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
 * to that XML with a mechanism to reproduce a Mode from XML so you can construct 
 * ways to save Mode and TopComponent combinations, which can subsequently be
 * chosen and reloaded at a later time.
 * <br>
 * <br>
 * Note that this is a different approach to how the Platform tends to save the layout
 * of TopComponents at shut-down time in the Windows2Local file system. Note also that 
 * this is not connected with the deprecated notion of {@link Workspace}.
 * 
 * @see http://wiki.apidesign.org/wiki/ExtendingInterfaces
 * 
 * @author Mark Phipps
 */
public final class ModeUtility {

    private ModeUtility() {
    }

    /**
     * Expose the Mode's configuration as XML.
     * 
     * @param mode the {@link Mode} whose XML configuration is required.
     * @return the XML of the Mode's configuration or {@code null} if not supported.
     */
    public final static String toXml(Mode mode) {
        return mode instanceof Mode.Xml
                ? ((Mode.Xml) mode).toXml()
                : null;
    }
    
    /**
     * Given some XML, attempts to ask the WindowManager to create a Mode that can
     * subsequently be used to dock a TopComponent into. Usually this will be an 
     * anonymous Mode.
     * 
     * @param windowManager typically WindowManager.getDefault()
     * @param xml the XML that was originally produced by {@link #toXml}
     */
    public final static void createModeFromXml(WindowManager windowManager, String xml) {
        if(windowManager instanceof WindowManager.ModeManager) {
            ((WindowManager.ModeManager)windowManager).createModeFromXml(xml);
        }
    }
    
    /**
     * Before restoring anonymous Modes, it is useful to update whatever defined Modes
     * may exist like editor, explorer etc., so that all the Modes will eventually
     * re-appear in the desired locations.
     * 
     * @param windowManager typically WindowManager.getDefault()
     * @param xml the XML that was originally produced by {@link #toXml}
     */
    public final static void updateModeConstraintsFromXml(WindowManager windowManager, String xml) {
        if(windowManager instanceof WindowManager.ModeManager) {
            ((WindowManager.ModeManager)windowManager).updateModeConstraintsFromXml(xml);
        }
    }
    
    /**
     * Before restoring a whole bunch of Modes (for example with XML that has been
     * previously saved somewhere and now loaded), it is useful to remove the
     * anonymous modes from the system.
     * 
     * @param windowManager typically WindowManager.getDefault()
     * @param mode the {@link Mode} to remove
     * @return success if the Mode can no longer be found in the Window Manager
     */
    public final static boolean removeMode(WindowManager windowManager, Mode mode) {
        return windowManager instanceof WindowManager.ModeManager
                ? ((WindowManager.ModeManager)windowManager).removeMode(mode)
                : false;
    }
}
