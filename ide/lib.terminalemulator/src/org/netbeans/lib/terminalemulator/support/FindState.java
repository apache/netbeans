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
package org.netbeans.lib.terminalemulator.support;

/**
 * FindState is the "model" and engine for find operations where {@link FindBar}
 * is the controller. Different Term's in one window may share one
 * {@link FindBar} which can multiplex between per-instance FindState.
 * @author ivan
 */
public interface FindState {

    public enum Status {
        /** Pattern found w/o the need for wrapping */
        OK,

        /** Pattern not found */
        NOTFOUND,

        /** pattern found but need to wrap by issuing one next() // or prev */
        WILLWRAP,

        /** pattern is null or "" */
        EMPTYPATTERN
    }

    /**
     * Set the pattern to be searched.
     * At the moment no searches are performed on setting.
     * Upon return status is either OK or EMPTYPATTERN.
     */
    public void setPattern(String pattern);

    /**
     * Return the pattern being searched.
     * @return the pattern.
     */
    public String getPattern();

    /**
     * Remember whether the FindBar is visible in this instance of Term.
     * @param visible the FindBar is visible.
     */
    public void setVisible(boolean visible);

    /**
     * Recall whether the FindBar is visible in this instance of Term.
     * @return the FindBar is visible.
     */
    public boolean isVisible();

    /**
     * Search for the next occurance of the pattern.
     */
    public void next();

    /**
     * Search for the previous occurance of the pattern.
     */
    public void prev();

    /**
     * Get the status of a search after a call to prev() or next().
     * @return the status.
     */
    public Status getStatus();
}
