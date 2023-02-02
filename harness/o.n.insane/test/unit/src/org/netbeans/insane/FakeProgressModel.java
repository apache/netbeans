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
package org.netbeans.insane;

//------------------------------------------------------------------------------
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoundedRangeModel;
import javax.swing.event.ChangeListener;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.netbeans.insane.live.CancelException;

final class FakeProgressModel implements BoundedRangeModel {

    final List<Integer> setValueArgs = new ArrayList<>();
    final List<SetRangePropertiesRecord> setRangePropertiesArgs = new ArrayList<>();
    boolean setValueThrowsCancelException = false;
    //--------------------------------------------------------------------------
    void assertThatLastProgressIsEqualToRangeMax() {
        
        assertEquals(this.setRangePropertiesArgs.get(0).max,
              this.setValueArgs.get(this.setValueArgs.size() -1).intValue());
    }
    //--------------------------------------------------------------------------
    void assertThatPrgressValuesAreGrowing() {
        
        assertTrue(this.setValueArgs.size() > 2);
        int lower = 0;
        int higher = 1;
        while(higher < this.setValueArgs.size()) {
            if(this.setValueArgs.get(lower) > this.setValueArgs.get(higher)) {
                fail("progres values not monotonic.");
            }
            ++lower;
            ++higher;
        }
    }
    //--------------------------------------------------------------------------
    @Override
    public int getMinimum() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    //--------------------------------------------------------------------------
    @Override
    public void setMinimum(int newMinimum) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    //--------------------------------------------------------------------------
    @Override
    public int getMaximum() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    //--------------------------------------------------------------------------
    @Override
    public void setMaximum(int newMaximum) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    //--------------------------------------------------------------------------
    @Override
    public int getValue() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    //--------------------------------------------------------------------------
    @Override
    public void setValue(int newValue) {

        if(this.setValueThrowsCancelException) {
            throw new CancelException();
        }
        this.setValueArgs.add(newValue);
    }
    //--------------------------------------------------------------------------
    @Override
    public void setValueIsAdjusting(boolean b) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    //--------------------------------------------------------------------------
    @Override
    public boolean getValueIsAdjusting() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    //--------------------------------------------------------------------------
    @Override
    public int getExtent() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    //--------------------------------------------------------------------------
    @Override
    public void setExtent(int newExtent) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    //--------------------------------------------------------------------------
    @Override
    public void setRangeProperties(int value, int extent, int min, int max, boolean adjusting) {

        SetRangePropertiesRecord r = new SetRangePropertiesRecord();
        r.value = value;
        r.extent = extent;
        r.min = min;
        r.max = max;
        r.adjusting = adjusting;
        this.setRangePropertiesArgs.add(r);
    }
    //--------------------------------------------------------------------------
    @Override
    public void addChangeListener(ChangeListener x) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    //--------------------------------------------------------------------------
    @Override
    public void removeChangeListener(ChangeListener x) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    //--------------------------------------------------------------------------
    static class SetRangePropertiesRecord {

        int value;
        int extent;
        int min;
        int max;
        boolean adjusting;
    }
}
