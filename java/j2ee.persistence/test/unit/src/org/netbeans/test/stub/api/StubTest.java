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

package org.netbeans.test.stub.api;

import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Andrei Badea
 */
public class StubTest extends NbTestCase {
    
    public StubTest(String name) {
        super(name);
    }
    
    public void testDefault() {
        Primitives p = (Primitives)Stub.create(new Class[] { Primitives.class });
        
        assertEquals(System.identityHashCode(p), p.hashCode());
        
        assertTrue(p.equals(p));
        assertFalse(p.equals(new Object()));
        
        assertEquals((byte)0, p.getByte());
        assertEquals((short)0, p.getShort());
        assertEquals(0, p.getInteger());
        assertEquals(0L, p.getLong());
        assertEquals(Float.floatToRawIntBits(0), Float.floatToRawIntBits(p.getFloat()));
        assertEquals(Double.doubleToRawLongBits(0.0), Double.doubleToRawLongBits(p.getDouble()));
        assertEquals('\0', p.getCharacter());
        assertEquals(false, p.getBoolean());
    }
    
    private static interface Primitives {
        
        public byte getByte();
        
        public short getShort();
        
        public int getInteger();
        
        public long getLong();
        
        public float getFloat();
        
        public double getDouble();
        
        public char getCharacter();
        
        public boolean getBoolean();
    }
}
