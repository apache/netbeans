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

package org.netbeans.modules.autoupdate.ui;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jiri Rechtacek
 */
public class UnitCategory {
    final String name;
    boolean isExpanded = true;
    List<Unit> units = new ArrayList<Unit> ();

    /** Creates a new instance of UpdateCategory */
    public UnitCategory (String name) {
        //assert name != null;
        this.name = name != null ? name : "";
    }

    /** Creates a new instance of UpdateCategory */
    public UnitCategory (String name, List<Unit> units, boolean isExpanded) {
        this.name = name;
        this.units.addAll (units);
        this.isExpanded = isExpanded;
    }

    public List<Unit> getMarkedUnits() {
        List<Unit> markedUnits = new ArrayList<Unit> ();        
        List<Unit> allUnits = getUnits ();
        for (Unit u : allUnits) {
            if (u.isMarked()) {
                markedUnits.add(u);
            }
        }
        return markedUnits;        
    }
    
    
    public String getCategoryName () {
        return name;
    }

    public boolean isExpanded () {
        return isExpanded;
    }

    public void setExpanded (boolean expanded) {
        isExpanded = expanded;
    }

    public boolean addUnit (Unit u) {
        return units.add (u);
    }

    public void addUnits (List<Unit> units) {
        for (Unit unit : units) {
            addUnit(unit);
        }
    }
    
    public boolean removeUnit (Unit u) {
        return units.remove (u);
    }
    
    public List<Unit> getUnits () {
        return units;
    }
    
    void toggleExpanded () {
        this.isExpanded = ! isExpanded;
    }

    @Override
    public String toString() {
        return super.toString() + "[" + name + "]";
    }
}
