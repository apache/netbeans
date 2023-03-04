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
package org.netbeans.api.io;

import org.netbeans.spi.io.support.OutputColorType;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.spi.io.support.OutputColors;

/**
 *
 * @author jhavlin
 */
public class OutputColorTest {

    @Test
    public void testRgbColor() {
        OutputColor c = OutputColor.rgb(127, 255, 1);
        assertEquals(OutputColorType.RGB, OutputColors.getType(c));
        int value = OutputColors.getRGB(c);
        int r = value >> 16;
        int g = value >> 8 & 0xFF;
        int b = value & 0xFF;
        assertEquals(127, r);
        assertEquals(255, g);
        assertEquals(1, b);
    }

    @Test
    public void testWarningColor() {
        OutputColor c = OutputColor.warning();
        assertEquals(OutputColorType.WARNING, c.getType());
    }

    @Test
    public void testFailureColor() {
        OutputColor c = OutputColor.failure();
        assertEquals(OutputColorType.FAILURE, c.getType());
    }

    @Test
    public void testDebugColor() {
        OutputColor c = OutputColor.debug();
        assertEquals(OutputColorType.DEBUG, c.getType());
    }

    @Test
    public void testSuccessColor() {
        OutputColor c = OutputColor.success();
        assertEquals(OutputColorType.SUCCESS, c.getType());
    }
}
