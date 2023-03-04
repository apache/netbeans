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

package org.netbeans.swing.tabcontrol;


/*
 * Data model of slide bar. It's the same as TabDataModel, but has
 * orientation property in addition.
 *
 * @author Dafe Simonek
 */
public interface SlideBarDataModel extends TabDataModel {

    public static final int EAST = 1;
    public static final int WEST = 2;
    public static final int SOUTH = 3;
    /**
     * @since 1.27
     */
    public static final int NORTH = 4;

    /** Orientation of slide bar
     */
    public int getOrientation ();

    /** Sets orientation of slide bar, possible values are EAST, WEST, SOUTH.
     */
    public void setOrientation (int orientation);

    /* Basic implementation of SlideBarDataModel.
     */
    public static class Impl extends DefaultTabDataModel implements SlideBarDataModel {

        /** Holds orientation of slide bar */
        private int orientation = EAST;

        /** Constructs new data model */
        public Impl () {
            super();
        }

        @Override
        public int getOrientation() {
            return orientation;
        }

        @Override
        public void setOrientation(int orientation) {
            this.orientation = orientation;
        }
        
    } // end of Impl
    
}
