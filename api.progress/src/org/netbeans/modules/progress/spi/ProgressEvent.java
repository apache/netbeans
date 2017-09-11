/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
    
}
