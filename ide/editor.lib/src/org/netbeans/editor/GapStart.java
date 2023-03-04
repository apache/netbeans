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

package org.netbeans.editor;

/**
 * A given object can publish this interface if it allows
 * an efficient access to its gap-based data storage
 * and wants to give its clients a hint about how to access
 * the data efficiently.
 * <P>For example {@link javax.swing.text.Document} instance
 * having gap-based document content can allow to get an instance
 * of GapStart as a property:<PRE>
 *      GapStart gs = (GapStart)doc.getProperty(GapStart.class);
 *      int gapStart = gs.getGapStart();
 * <PRE>
 * Once the start of the gap is known the client can optimize
 * access to the document's data. For example if the client
 * does not care about the chunks in which it gets the document's data
 * it can access the characters so that no character copying is done:<PRE>
 *      Segment text = new Segment();
 *      doc.getText(0, gapStart, text); // document's data below gap
 *      ...
 *      doc.getText(gapStart, doc.getLength(), text); // document's data over gap
 *      ...
 * <PRE>
 *
 * @author Miloslav Metelka
 * @version 1.00
 * @deprecated deprecated without replacement. Possibly use document's view as CharSequence
 *  by {@link org.netbeans.lib.editor.util.swing.DocumentUtilities#getText(javax.swing.text.Document)}.
 */

@Deprecated
public interface GapStart {

    /**
     * Get the begining of the gap in the object's gap-based data.
     * @return &gt;=0 and &lt;= total size of the data of the object.
     */
    public int getGapStart();

}
