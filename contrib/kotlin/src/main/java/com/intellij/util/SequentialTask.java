/**
 * *****************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************
 */
package com.intellij.util;

public interface SequentialTask {
    
    /**
     * Callback method that is assumed to be called before the processing.
     */
    void prepare();
    
    /**
     * @return <code>true</code> if the processing is complete;
     *         <code>false</code> otherwise
     */
    boolean isDone();
    
    /**
     * Asks current task to perform one more processing iteration.
     * 
     * @return <code>true</code> if the processing is done; <code>false</code>
     *         otherwise
     */
    boolean iteration();
    
    /**
     * Asks current task to stop the processing (if any).
     */
    void stop();
}