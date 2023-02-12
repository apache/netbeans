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
package org.netbeans.modules.gsf.testrunner.api;

/**
 * Enums for representing status of a test case or suite.
 * 
 * @author Erno Mononen
 */
public enum Status {

    PASSED(1,"00CC00"), PENDING(1<<1,"800080"), FAILED(1<<2,"FF0000"), ERROR(1<<3,"FF0000"), ABORTED(1<<4,"D69D29"), SKIPPED(1<<5,"585858"), PASSEDWITHERRORS(1<<6,"00CC00"), IGNORED(1<<7,"000000"); //NOI18N

    private final int bitMask;
    private final String displayColor;

    private Status(int bitMask, String displayColor) {
        this.bitMask = bitMask;
        this.displayColor = displayColor;
    }

    /**
     * @return the bit mask for this status.
     */
    public int getBitMask(){
        return bitMask;
    }

    /**
     * @return the html display color for this status.
     */
    public String getHtmlDisplayColor() {
        return displayColor;
    }

    /**
     * @return true if the given status represents a failure or an error.
     */
    public static boolean isFailureOrError(Status status) {
        return FAILED == status || ERROR == status;
    }

    /**
     * @return true if the given status represents a skipped test.
     */
    public static boolean isSkipped(Status status) {
        return SKIPPED == status;
    }

    /**
     * @return true if the given status represents an aborted test.
     */
    public static boolean isAborted(Status status) {
        return ABORTED == status;
    }

    /**
     *
     * @return true if the given mask is applied in this status.
     */
    public boolean isMaskApplied(int mask){
        return (mask & getBitMask()) != 0;
    }

}
