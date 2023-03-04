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
package org.netbeans.spi.print;

import java.util.Date;

/**
 * Print provider is the collection of the pages
 * to be printed, where collection is the 2D matrix.
 *
 * @author Vladimir Yaroslavskiy
 * @version 2006.04.24
 */
public interface PrintProvider {

    /**
     * Returns print pages being shown and printed.
     * The pages will be shown in the Print Preview dialog
     * as 2D matrix, e.g. page <code>pages[1][2]</code> will
     * be shown in second row and third column in the dialog.
     *
     * @param width specifies the width of pages in pixels.
     * @param height specifies the height of pages in pixels.
     * @param zoom specifies the scale of pages.
     * The zoom can take positive double value:
     * <code>0.2</code> means <code>20%</code>,
     * <code>1.0</code> - <code>100%</code>,
     * <code>3.1415</code> - <code>314.5%</code> etc.
     *
     * @return pages being printed for the given width, height and zoom
     */
    PrintPage[][] getPages(int width, int height, double zoom);

    /**
     * Indicates the name of the document being printed which
     * will be shown in the header/footer. By default, the
     * name is shown in the left part of the header.
     * @return name of the document which can be used in header/footer
     */
    String getName();

    /**
     * Indicates the time at which the user last made a modification to
     * the document, diagram, etc. being printed which might affect its
     * printed appearance. The document might not have been saved since then.
     * @return time at which the printable document was last changed
     */
    Date lastModified();
}
