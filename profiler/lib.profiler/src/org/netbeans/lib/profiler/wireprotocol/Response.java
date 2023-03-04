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

package org.netbeans.lib.profiler.wireprotocol;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


/**
 * Instances of this class are sent back by both client and back end (server) in response to some Commands.
 * An instance of the base Response class is used to signal just success or failure (with possible additional
 * error message). Instances of its subclasses are used to pass additional information.
 *
 * @author Misha Dmitriev
 */
public class Response {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // Each new response class should have its own identifier, listed here.
    public static final int NO_TYPE = 0;
    public static final int CODE_REGION_CPU_RESULTS = 1;
    public static final int INSTRUMENT_METHOD_GROUP = 2;
    public static final int INTERNAL_STATS = 3;
    public static final int VM_PROPERTIES = 4;
    public static final int DUMP_RESULTS = 5;
    public static final int OBJECT_ALLOCATION_RESULTS = 6;
    public static final int METHOD_NAMES = 7;
    public static final int THREAD_LIVENESS_STATUS = 8;
    public static final int MONITORED_NUMBERS = 9;
    public static final int DEFINING_LOADER = 10;
    public static final int CALIBRATION_DATA = 11;
    public static final int CLASSID_RESPONSE = 12;
    public static final int HEAP_HISTOGRAM = 13;
    public static final int THREAD_DUMP = 14;
    public static final int GET_CLASS_FILE_BYTES_RESPONSE = 15;

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    protected String errorMessage;
    protected boolean yes;
    private int type;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public Response(boolean yes) {
        type = NO_TYPE;
        this.yes = yes;
    }

    public Response(String errorMessage) {
        type = NO_TYPE;
        this.errorMessage = errorMessage;
    }

    protected Response(boolean yes, int type) {
        this.yes = yes;
        this.type = type;
    }

    // Custom serialization support
    Response() {
        type = NO_TYPE;
    }

    Response(int type) {
        this.type = type;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isOK() {
        return errorMessage == null;
    }

    public int getType() {
        return type;
    }

    // For debugging
    public String toString() {
        String s = respTypeToString(type);

        return s + (isOK() ? (" Ok, " + (yes() ? "yes" : "no")) : (" Error, " + errorMessage)); // NOI18N
    }

    public boolean yes() {
        return yes;
    }

    void setErrorMessage(String msg) {
        this.errorMessage = msg;
    }

    void setType(int type) {
        this.type = type;
    }

    void setYes(boolean yes) {
        this.yes = yes;
    }

    void readObject(ObjectInputStream in) throws IOException {
    }

    static String respTypeToString(int type) {
        if (type != NO_TYPE) {
            switch (type) {
                case CODE_REGION_CPU_RESULTS:
                    return "CODE_REGION_CPU_RESULTS"; // NOI18N
                case INSTRUMENT_METHOD_GROUP:
                    return "INSTRUMENT_METHOD_GROUP"; // NOI18N
                case INTERNAL_STATS:
                    return "INTERNAL_STATS"; // NOI18N
                case VM_PROPERTIES:
                    return "VM_PROPERTIES"; // NOI18N
                case DUMP_RESULTS:
                    return "DUMP_RESULTS"; // NOI18N
                case OBJECT_ALLOCATION_RESULTS:
                    return "OBJECT_ALLOCATION_RESULTS"; // NOI18N
                case METHOD_NAMES:
                    return "METHOD_NAMES"; // NOI18N
                case THREAD_LIVENESS_STATUS:
                    return "THREAD_LIVENESS_STATUS"; // NOI18N
                case MONITORED_NUMBERS:
                    return "MONITORED_NUMBERS"; // NOI18N
                case DEFINING_LOADER:
                    return "DEFINING_LOADER"; // NOI18N
                case CALIBRATION_DATA:
                    return "CALIBRATION_DATA"; // NOI18N
                case CLASSID_RESPONSE:
                    return "CLASSID_RESPONSE"; // NOI18N
                case HEAP_HISTOGRAM:
                    return "HEAP_HISTOGRAM"; // NOI18N
                case THREAD_DUMP:
                    return "THREAD_DUMP";   // NOI18N
                case GET_CLASS_FILE_BYTES_RESPONSE:
                    return "GET_CLASS_FILE_BYTES_RESPONSE";
                default:
                    return "Unknown response"; // NOI18N
            }
        } else {
            return "NO TYPE"; // NOI18N
        }
    }

    void writeObject(ObjectOutputStream out) throws IOException {
    }
}
