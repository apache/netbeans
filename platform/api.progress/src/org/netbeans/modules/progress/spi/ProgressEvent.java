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


package org.netbeans.modules.progress.spi;

/**
 *
 * @author Milos Kleint (mkleint@netbeans.org)
 * @since org.netbeans.api.progress/1 1.18
 */
public final class ProgressEvent {

    public static final int TYPE_START = 0;
    public static final int TYPE_FINISH = 4;
    public static final int TYPE_REQUEST_STOP = 3;
    public static final int TYPE_PROGRESS = 1;
    public static final int TYPE_SWITCH = 5;
    public static final int TYPE_SILENT = 6;

     private InternalHandle source;
     private long estimatedCompletion;
     private double percentageDone;
     private int workunitsDone;
     private String message;
     private int type;
     private boolean watched;
     private boolean switched;
     private String displayName;

    /** Creates a new instance of ProgressEvent 
     * @param type one of TYPE_START, TYPE_REQUEST_STOP, TYPE_FINISH, TYPE_SWITCHED
     */
    public ProgressEvent(InternalHandle src, int type, boolean isWatched) {
        source = src;
        estimatedCompletion = -1;
        percentageDone = -1;
        workunitsDone = -1;
        message = null;
        this.type = type;
        watched = isWatched;
        switched = (type == TYPE_SWITCH);
    }
    
    /** Creates a new instance of ProgressEvent 
     * @param type one of TYPE_SILENT
     */
    public ProgressEvent(InternalHandle src, int type, boolean isWatched, String msg) {
        this(src, type, isWatched);
        message = msg;
    }
    /**
     * @param percentage completed work percentage
     * @param estimate estimate of completion in seconds
     */
    public ProgressEvent(InternalHandle src, String msg, int units, double percentage, long estimate, boolean isWatched) {
        this(src, TYPE_PROGRESS, isWatched);
        workunitsDone = units;
        percentageDone = percentage;
        estimatedCompletion = estimate;
        message = msg;
    }
    public ProgressEvent(InternalHandle src, String msg, int units, double percentage, long estimate, boolean isWatched, String displayName) {
        this(src, msg, units, percentage, estimate, isWatched);
        this.displayName = displayName;
    }
    
    public InternalHandle getSource() {
        return source;
    }

    public long getEstimatedCompletion() {
        return estimatedCompletion;
    }

    public double getPercentageDone() {
        return percentageDone;
    }

    public int getWorkunitsDone() {
        return workunitsDone;
    }

    public String getMessage() {
        return message;
    }

    public int getType() {
        return type;
    }
    
    public boolean isWatched() {
        return watched;
    }
    
    /**
     * used in controller, preserve dynamic message from earlier events 
     * if this one doesn't have it's own.
     */
    public void copyMessageFromEarlier(ProgressEvent last) {
        if (message == null) {
            message = last.getMessage();
        }
        if (displayName == null) {
            displayName = last.getDisplayName();
        }
    }
    
    public void markAsSwitched() {
        switched = true;
    }
    
    public boolean isSwitched() {
        return switched;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("EV[").append(getSource());
        sb.append(", disp: ").append(displayName);
        
        String tName;
        switch (type) {
            case TYPE_FINISH: tName = "finish"; break;
            case TYPE_PROGRESS: tName = "progress"; break;
            case TYPE_REQUEST_STOP: tName = "stop"; break;
            case TYPE_SILENT: tName = "silent"; break;
            case TYPE_START: tName = "start"; break;
            case TYPE_SWITCH: tName = "switch"; break;
            default: tName = "" + type;
        }
        sb.append(", type: ").append(tName);
        sb.append(", pctDone: ").append(String.format("%3.2f", percentageDone));
        sb.append(", message: ").append(message);
        sb.append(", disp: ").append(displayName);
        sb.append("]");
        return sb.toString();
    }

    void markAsFinished() {
        type = TYPE_FINISH;
    }
}
