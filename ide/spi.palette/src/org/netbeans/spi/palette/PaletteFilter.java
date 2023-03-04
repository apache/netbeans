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

package org.netbeans.spi.palette;

import org.openide.util.Lookup;

/**
 * <p>A palette filter than can prevent some categories and/or items from being
 * displayed in palette's window.</p>
 *
 * @author S. Aubrecht
 */
public abstract class PaletteFilter {

    /**
     * @param lkp Lookup representing a palette category.
     * @return True if the category should be displayed in palette's window, false otherwise.
     */
    public abstract boolean isValidCategory( Lookup lkp );

    /**
     * @param lkp Lookup representing a palette item.
     * @return True if the item should be displayed in palette's window, false otherwise.
     */
    public abstract boolean isValidItem( Lookup lkp );
}
