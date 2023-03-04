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
package org.netbeans.api.debugger.jpda.testapps;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sample lambda expression breakpoints application.
 * @author aksinsin
 */
public class ExpressionLambdaBreakpointApp {

    public static void main(String... args) {
        
        List<String> nonEmptyListCollection = Arrays.stream(new String[]{"a", "", "b", "", "c"})
                .filter(s -> !s.isEmpty())          // LBREAKPOINT
                .collect(Collectors.toList());
        System.out.println(nonEmptyListCollection); // LBREAKPOINT
    }
}
