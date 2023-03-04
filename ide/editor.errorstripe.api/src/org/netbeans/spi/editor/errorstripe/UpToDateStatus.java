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

package org.netbeans.spi.editor.errorstripe;

/**Up-to-date status enumeration. See {@link UpToDateStatusProvider#getUpToDate}
 * for more information.
 *
 * @author Jan Lahoda
 */
public final class UpToDateStatus implements Comparable {

    /**Up-to-date status saying everything is up-to-date.
     */
    private static final int UP_TO_DATE_OK_VALUE = 0;

    /**Up-to-date status saying that the list of marks is
     * not up-to-date, but a up-to-date list of marks is currently
     * being found.
     */
    private static final int UP_TO_DATE_PROCESSING_VALUE = 1;
    
    /**Up-to-date status saying that the list of marks is
     * not up-to-date, and nothing is currently done in order to
     * get the up-to-date list.
     */
    private static final int UP_TO_DATE_DIRTY_VALUE = 2;

    /**Up-to-date status saying everything is up-to-date.
     */
    public static final UpToDateStatus UP_TO_DATE_OK = new UpToDateStatus (UP_TO_DATE_OK_VALUE);
    
    /**Up-to-date status saying that the list of marks is
     * not up-to-date, but a up-to-date list of marks is currently
     * being found.
     */
    public static final UpToDateStatus UP_TO_DATE_PROCESSING = new UpToDateStatus (UP_TO_DATE_PROCESSING_VALUE);
    
    /**Up-to-date status saying that the list of marks is
     * not up-to-date, and nothing is currently done in order to
     * get the up-to-date list.
     */
    public static final UpToDateStatus UP_TO_DATE_DIRTY = new UpToDateStatus (UP_TO_DATE_DIRTY_VALUE);

    private int status;
    
    /** Creates a new instance of UpToDateStatus */
    private UpToDateStatus(int status) {
        this.status = status;
    }
    
    private int getStatus() {
        return status;
    }
    
    public int compareTo(Object o) {
        UpToDateStatus remote = (UpToDateStatus) o;
        
        return status - remote.status;
    }
    
    public int hashCode() {
        return 73 ^ status;
    }
    
    public boolean equals(Object obj) {
        if (!(obj instanceof UpToDateStatus))
            return false;
        
        return compareTo(obj) == 0;
    }
    
    private static final String[] statusNames = new String[] {
        "OK",
        "PROCESSING",
        "DIRTY",
    };
    
    public String toString() {
        return statusNames[getStatus()];
    }
}
