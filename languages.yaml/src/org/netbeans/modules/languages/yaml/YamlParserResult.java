/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.languages.yaml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jvyamlb.Position.Range;
import org.jvyamlb.Positionable;
import org.jvyamlb.nodes.Node;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 * A result from Parsing YAML
 *
 * @author Tor Norbye
 */
public class YamlParserResult extends ParserResult {

    private final List<Error> errors = new ArrayList<Error>();
    private List<Node> nodes;
    private List<? extends StructureItem> items;
    private int[] byteToUtf8;
    private int[] utf8ToByte;

    public YamlParserResult(List<Node> nodes, YamlParser parser, Snapshot snapshot, boolean valid, int[] byteToUtf8, int[] utf8ToByte) {
        super(snapshot);
        assert nodes != null;
        this.nodes = nodes;
        this.byteToUtf8 = byteToUtf8;
        this.utf8ToByte = utf8ToByte;
    }

    public List<Node> getRootNodes() {
        return nodes;
    }

    public void addError(Error error) {
        errors.add(error);
    }

    @Override
    public List<? extends Error> getDiagnostics() {
        return Collections.unmodifiableList(errors);
    }

    @Override
    protected void invalidate() {
        // FIXME parsing API
        // remove from parser cache (?)
    }

    public synchronized List<? extends StructureItem> getItems() {
        if (items == null) {
            items = new YamlScanner().scanStructure(this);
        }

        return items;
    }

    public void setItems(List<? extends StructureItem> items) {
        this.items = items;
    }

    public int convertUtf8ToByte(int utf8Pos) {
        if (utf8ToByte == null) {
            return utf8Pos;
        }
        if (utf8Pos < utf8ToByte.length) {
            return utf8ToByte[utf8Pos];
        } else {
            return utf8ToByte.length;
        }
    }

    public int convertByteToUtf8(int bytePos) {
        if (byteToUtf8 == null) {
            return bytePos;
        }
        if (bytePos < byteToUtf8.length) {
            return byteToUtf8[bytePos];
        } else {
            return byteToUtf8.length;
        }
    }

    public OffsetRange getAstRange(Range range) {
        int start = range.start.offset;
        int end = range.end.offset;
        if (byteToUtf8 == null) {
            return new OffsetRange(start, end);
        } else {
            int s, e;
            if (start >= byteToUtf8.length) {
                s = byteToUtf8.length;
            } else {
                s = byteToUtf8[start];
            }
            if (end >= byteToUtf8.length) {
                e = byteToUtf8.length;
            } else {
                e = byteToUtf8[end];
            }

            return new OffsetRange(s, e);
        }
    }

    public OffsetRange getAstRange(Node node) {
        return getAstRange(((Positionable) node).getRange());
    }
}
