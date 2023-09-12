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

package org.netbeans.modules.bugtracking.spi;

import java.util.Date;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Ondrej Vrabec
 */
public class SchedulingPickerTest {
    
    @Test
    public void testSetInfo () {
        SchedulePicker picker = new SchedulePicker();
        Date date = new Date();
        int interval = 7;
        IssueScheduleInfo info = new IssueScheduleInfo(date, interval);
        
        // set to a specific value
        picker.setScheduleDate(info);
        Assert.assertEquals(info, picker.getScheduleDate());
        
        // set to not scheduled
        picker.setScheduleDate(null);
        Assert.assertNull(picker.getScheduleDate());
    }

    @Test
    public void testGetComponent () {
        SchedulePicker picker = new SchedulePicker();
        
        Assert.assertNotNull(picker.getComponent());
    }
    
}
