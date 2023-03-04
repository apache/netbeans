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

package org.netbeans.modules.parsing.spi;

import java.util.Collection;
import org.netbeans.modules.parsing.api.Snapshot;


/**
 * The factory for {@link Parser}s. <code>ParserFactory</code> implemementations
 * can be registered in <code>MimeLoolup</code> either for a specific
 * mime type or for all languages. The example below shows <code>ParserFactory</code>
 * registration for <code>text/x-something</code> mime type in an XML layer.
 *
 * <pre>
 * &lt;folder name="Editors"&gt;
 *  &lt;folder name="text"&gt;
 *   &lt;folder name="x-something"&gt;
 *    &lt;file name="org-some-module-MyParserFactory.instance" /&gt;
 *   &lt;/folder&gt;
 *  &lt;/folder&gt;
 * &lt;/folder&gt;
 * </pre>
 * 
 * @author Jan Jancura
 */
public abstract class ParserFactory {

    /**
     * Creates a new instance of {@link Parser}. In general parsers are created
     * for either one or several snapshots (ie. document sections), which are provided
     * to this method. It is guaranteed that all snapshots in the collection will be
     * of the same mime type and it will be the mime type, which this factory
     * was registered for (ie. in <code>MimeLookup</code>). Typical factories won't
     * need the snapshots for creating a parser, but factories that serve multiple
     * languages (eg. in GSF or other language support frameworks) may find this useful.
     *
     * <p>It is important to remember that the snapshots are provided <b>only</b>
     * for the factory and they reflect document states at the time when the factory
     * is called. Since the parsing infrastructure may cache parser instances it
     * will provide a new snapshot when it asks <code>Parser</code>s to do their job.
     * Therefore the snapshots here may only be used for the parser creation, but must
     * <b>never</b> be used for parsing!
     * 
     * @param snapshots Snaphots of documents, which the new parser will be asked
     *   to parse.
     *
     * @return The new <code>Parser</code> instance or <code>null</code> if this
     *   factory does not have a parser suitable for parsing this type of snapshots.
     */
    public abstract Parser createParser(Collection<Snapshot> snapshots);
}
