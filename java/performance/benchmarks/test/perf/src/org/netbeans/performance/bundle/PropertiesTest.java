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

package org.netbeans.performance.bundle;

import org.netbeans.performance.Benchmark;
import java.util.Properties;

/**
 * Benchmark measuring the difference between using plain Properties
 * vs. Properties that intern either keys of both keys and vaules.
 *
 * @author  Petr Nejedly
 * @version 1.0
 */
public class PropertiesTest extends Benchmark {

    public PropertiesTest (String name) {
        super (name);
    }

    Properties[] holder;

    protected void setUp () {
        holder = new Properties[getIterationCount ()];
    }

    /** Creates an instance of standard java.util.Properties and feeds it with
     * a stream from a Bundle.properties.file
     */
    public void testOriginalProperties () throws Exception {
        int count = getIterationCount ();

        while (count-- > 0) {
            holder[count] = new Properties ();
            holder[count].load (
                PropertiesTest.class.getResourceAsStream ("Bundle.properties"));
        }
    }

    /** Creates an instance of a special subclass of java.util.Properties
     * which interns keys during properties parsing, then 
     * feeds it with a stream from a Bundle.properties.file
     */
    public void testInternKeys () throws Exception {
        int count = getIterationCount ();
        
        while (count-- > 0) {
            holder[count] = new KeyProperties ();
            holder[count].load (
                PropertiesTest.class.getResourceAsStream ("Bundle.properties"));
        }
    }
    
    /** Creates an instance of a special subclass of java.util.Properties
     * which interns both keys and parsed strings during properties parsing,
     * then feeds it with a stream from a Bundle.properties.file
     */
    public void testInternBoth () throws Exception {
        int count = getIterationCount ();

        while (count-- > 0) {
            holder[count] = new BothProperties ();
            holder[count].load (
                PropertiesTest.class.getResourceAsStream ("Bundle.properties"));
        }
    }


    public static void main (String[] args) {
	simpleRun (PropertiesTest.class);
    }

    private static class KeyProperties extends java.util.Properties {
        public KeyProperties () {
            super ();
        }

        public Object put (Object key, Object value) {
            return super.put (key.toString ().intern (), value);
        }
    }

    private static class BothProperties extends java.util.Properties {
        public BothProperties () {
            super ();
        }

        public Object put (Object key, Object value) {
            return super.put (key.toString ().intern (),
                              value.toString ().intern ());
        }
    }
}
