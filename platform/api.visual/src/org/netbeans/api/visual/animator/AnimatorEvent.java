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
package org.netbeans.api.visual.animator;

/**
 * This is an animator event which is used by <code>AnimatorListener</code>.
 * It contains a reference to the animator and animation progress value which is can be used only in case of
 * <code>AnimatorListener.animatorPreTick</code> and <code>AnimatorListener.animatorPostTick</code> methods.
 *
 * @author David Kaspar
 */
public final class AnimatorEvent {

    private Animator animator;
    private double progress;

    AnimatorEvent (Animator animator) {
        this (animator, Double.NaN);
    }

    AnimatorEvent (Animator animator, double progress) {
        this.animator = animator;
        this.progress = progress;
    }

    /**
     * Returns the related animator instance.
     * @return the animator
     */
    public Animator getAnimator () {
        return animator;
    }

    /**
     * The animation progress value. Contains valid value only when the event is received as an argument of
     * <code>AnimatorListener.animatorPreTick</code> and <code>AnimatorListener.animatorPostTick</code> methods.
     * @return the progress value; valid range is from 0.0 to 1.0 where 0.0 represents animator-start and 1.0 represents animator-end;
     *     Double.NaN if the progress value is not available
     */
    public double getProgress () {
        return progress;
    }

}
