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

package org.netbeans.editor.view.spi;

import javax.swing.text.View;

/**
 * Interface describing a view being fragment of some original view.
 * <br>
 * It provides information that the fragment usually has to know anyway
 * but that it's not available through
 * the {@link javax.swing.text.View} class that the fragment is instance of.
 *
 * <p>
 * The instances of this interface are created once fragments
 * of a view have to created e.g. for line-wrapping purposes.
 * <br>
 * If the returned fragment does not implement this interface
 * a default wrapper gets created.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public interface ViewFragment {

    /**
     * Return original view for which this wrapper is being created.
     */
    public View getOriginalView();
    
    /**
     * Return relative starting offset against the starting offset
     * of the original view.
     */
    public int getRelativeStartOffset();
    
    /**
     * Return relative ending offset against the starting offset
     * of the original view.
     */
    public int getRelativeEndOffset();

}
