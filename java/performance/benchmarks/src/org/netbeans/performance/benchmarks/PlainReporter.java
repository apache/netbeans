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

package org.netbeans.performance.benchmarks;

/**
 * A reporter that will directly print out the results in raw form.
 *
 * @author  Petr Nejedly
 */
public class PlainReporter implements Reporter {

    /** Creates new PlainReporter */
    public PlainReporter() {
    }

    public void flush() {
        System.out.flush();
    }

    public void addSample(String className, String methodName, Object argument, float value) {
        System.out.println( className + '.' + methodName + "@" +
        argument2String(argument) + ": " + formatTime( value ) );
    }
    
    /** Formats a time */
    private static String formatTime(float time) {        
        if (time < 1e-3) {
            return (time * 1e6) + "[micro s]";
        } else if (time < 1) {
            return (time * 1e3) + "[ms]";
        } else {
            return time + "[s]";
        }        
    }

    /** Handles arrays */
    private static String argument2String( Object argument ) {
        StringBuffer sb = new StringBuffer(1000);
        argument2String(argument, sb);
        return sb.toString();
    }

    private static void argument2String( Object argument, StringBuffer sb ) {
        if (argument instanceof Object[]) {
            Object[] arg = (Object[]) argument;
            sb.append('[');
            for (int i = 0; i < arg.length - 1; i++) {
                argument2String(arg[i], sb);
                sb.append(',').append(' ');
            }
            argument2String(arg[arg.length - 1], sb);
            sb.append(']');
        } else if (argument instanceof int[]) {
            int[] arg = (int[]) argument;
            sb.append('[');
            for (int i = 0; i < arg.length - 1; i++) {
                sb.append(Integer.toString(arg[i]));
                sb.append(',').append(' ');
            }
            sb.append(Integer.toString(arg[arg.length - 1]));
            sb.append(']');
        } else {
            sb.append(argument.toString());
        }
    }

    
}
