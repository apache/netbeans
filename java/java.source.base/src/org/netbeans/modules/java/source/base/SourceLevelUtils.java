/**
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

package org.netbeans.modules.java.source.base;

import com.sun.tools.javac.code.Source;

/**
 *
 * @author lahvac
 */
public class SourceLevelUtils {
    
    public static final Source JDK1_5 = Source.lookup("5");
    public static final Source JDK1_6 = Source.lookup("6");
    public static final Source JDK1_7 = Source.lookup("7");
    public static final Source JDK1_8 = Source.lookup("8");
    public static final Source JDK1_9 = Source.lookup("9");
    public static final Source JDK14 = Source.lookup("14");
    public static final Source JDK15 = Source.lookup("15");

    public static boolean allowDefaultMethods(Source in) {
        return in.compareTo(JDK1_8) >= 0;
    }
}
