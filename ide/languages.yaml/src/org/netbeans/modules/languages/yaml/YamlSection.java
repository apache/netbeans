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
package org.netbeans.modules.languages.yaml;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.spi.DefaultError;
import org.netbeans.modules.parsing.api.Snapshot;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.events.AliasEvent;
import org.snakeyaml.engine.v2.events.Event;
import org.snakeyaml.engine.v2.events.MappingEndEvent;
import org.snakeyaml.engine.v2.events.MappingStartEvent;
import org.snakeyaml.engine.v2.events.ScalarEvent;
import org.snakeyaml.engine.v2.events.SequenceEndEvent;
import org.snakeyaml.engine.v2.events.SequenceStartEvent;
import org.snakeyaml.engine.v2.exceptions.Mark;
import org.snakeyaml.engine.v2.exceptions.ParserException;
import org.snakeyaml.engine.v2.exceptions.ScannerException;
import org.snakeyaml.engine.v2.parser.Parser;
import org.snakeyaml.engine.v2.parser.ParserImpl;
import org.snakeyaml.engine.v2.scanner.ScannerImpl;
import org.snakeyaml.engine.v2.scanner.StreamReader;

import static org.netbeans.modules.languages.yaml.YamlStructureItem.NodeType.*;

/**
 *
 * @author lkishalmi
 */
public class YamlSection {

    private static final LoadSettings SETTINGS = LoadSettings.builder().build();
    final int offset;
    final String source;

    private Parser parser = null;

    YamlSection(int offset, String source) {
        this.offset = offset;
        this.source = source;
    }

    public YamlSection(String source) {
        this(0, source);
    }

    public YamlSection before(int index) {
        return new YamlSection(offset, source.substring(0, index));
    }

    public YamlSection after(int index) {
        return new YamlSection(offset + index, source.substring(index));
    }

    public YamlSection trimTail() {
        int index = source.length() - 1;
        while ((index > 0) && Character.isWhitespace(source.charAt(index))) {
            index--;
        }
        while ((index > -1) && !Character.isWhitespace(source.charAt(index))) {
            index--;
        }
        return before(index + 1);
    }

    public YamlSection trimHead() {
        int index = 0;
        while ((index < source.length()) && Character.isWhitespace(source.charAt(index))) {
            index++;
        }
        while ((index < source.length()) && !Character.isWhitespace(source.charAt(index))) {
            index++;
        }
        return after(index);
    }

    public boolean isEmpty() {
        return source.isEmpty();
    }

    public int length() {
        return source.length();
    }

    List<? extends StructureItem> collectItems(Snapshot snapshot) {
        if (parser != null) {
            throw new IllegalStateException("This YAML segment is already parsed.");
        }
        List< StructureItem> ret = new ArrayList<>();
        ScannerImpl scanner = new ScannerImpl(SETTINGS, new StreamReader(SETTINGS, source));
        parser = new ParserImpl(SETTINGS, scanner);
        while (parser.hasNext()) {
            YamlStructureItem root = processItem(snapshot);
            if (root != null) {
                ret.addAll(root.getNestedItems());
            }
        }
        return ret;
    }

    private YamlStructureItem processItem(Snapshot snapshot) {
        YamlStructureItem ret = null;
        while ((ret == null) && parser.hasNext()) {
            switch (parser.peekEvent().getEventId()) {
                case MappingStart:
                    ret = processMapping(snapshot, (MappingStartEvent) parser.next());
                    break;
                case SequenceStart:
                    ret = processSequence(snapshot, (SequenceStartEvent) parser.next());
                    break;
                case Scalar:
                    ret = processScalar(snapshot, (ScalarEvent) parser.next());
                    break;
                case Alias:
                    ret = processAlias(snapshot, (AliasEvent) parser.next());
                    break;
                default:
                    parser.next();
            }
        }
        return ret;
    }

    private YamlStructureItem processAlias(Snapshot snapshot, AliasEvent evt) {
        return new YamlStructureItem.Simple(ALIAS, snapshot.getSource().getFileObject(), evt.getAlias().getValue(), getIndex(evt.getStartMark()), getIndex(evt.getEndMark()));
    }

    private YamlStructureItem processScalar(Snapshot snapshot, ScalarEvent evt) {
        return new YamlStructureItem.Simple(SCALAR, snapshot.getSource().getFileObject(), evt.getValue(), getIndex(evt.getStartMark()), getIndex(evt.getEndMark()));
    }

    private YamlStructureItem processMapping(Snapshot snapshot, MappingStartEvent evt) {
        YamlStructureItem.Collection item = new YamlStructureItem.Collection(MAP, snapshot.getSource().getFileObject(), getIndex(evt.getStartMark()));
        while (parser.hasNext() && !parser.checkEvent(Event.ID.MappingEnd)) {
            YamlStructureItem keyItem = processItem(snapshot);
            YamlStructureItem valueItem = processItem(snapshot);
            item.add(new YamlStructureItem.MapEntry(keyItem, valueItem));
        }
        if (parser.hasNext()) {
            MappingEndEvent eevt = (MappingEndEvent) parser.next();
            if (evt.isFlow()) {
                item.setEndMark(getIndex(eevt.getEndMark()));
            }
        }
        return item;
    }

    private YamlStructureItem processSequence(Snapshot snapshot, SequenceStartEvent evt) {
        YamlStructureItem.Collection item = new YamlStructureItem.Collection(SEQUENCE, snapshot.getSource().getFileObject(), getIndex(evt.getStartMark()));
        while (parser.hasNext() && !parser.checkEvent(Event.ID.SequenceEnd)) {
            item.add(processItem(snapshot));
        }
        if (parser.hasNext()) {
            SequenceEndEvent eevt = (SequenceEndEvent) parser.next();
            if (evt.isFlow()) {
                item.setEndMark(getIndex(eevt.getEndMark()));
            }
        }
        return item;
    }

    DefaultError processException(Snapshot snapshot, ScannerException se) {
        int problemIndex = getIndex(se.getProblemMark());
        int contextIndex = problemIndex;
        StringBuilder message = new StringBuilder();
        if (se.getContext() != null) {
            contextIndex = getIndex(se.getContextMark());
            message.append(se.getContext()).append(", ");
        }
        message.append(se.getProblem());
        char upper = Character.toUpperCase(message.charAt(0));
        message.setCharAt(0, upper);
        return new DefaultError(null, message.toString(), null, snapshot.getSource().getFileObject(), contextIndex, problemIndex, Severity.ERROR);
    }

    DefaultError processException(Snapshot snapshot, ParserException se) {
        int problemIndex = se.getProblemMark().isPresent() ? getIndex(se.getProblemMark()) : 0;
        int contextIndex = problemIndex;
        StringBuilder message = new StringBuilder();
        if (se.getContext() != null) {
            contextIndex = getIndex(se.getContextMark());
            message.append(se.getContext()).append(", ");
        }
        message.append(se.getProblem());
        char upper = Character.toUpperCase(message.charAt(0));
        message.setCharAt(0, upper);
        return new DefaultError(null, message.toString(), null, snapshot.getSource().getFileObject(), contextIndex, problemIndex, Severity.ERROR);
    }

    List<YamlSection> splitOnException(ScannerException se) {
        int problemIndex = se.getProblemMark().get().getIndex();
        if (se.getContextMark().isPresent()) {
            int contextIndex = se.getContextMark().get().getIndex();
            return split(contextIndex, problemIndex);
        } else {
            return split(problemIndex, problemIndex);
        }
    }

    List<YamlSection> splitOnException(ParserException pe) {
        if (pe.getContextMark().isPresent()) {
            int contextIndex = pe.getContextMark().get().getIndex();
            return split(contextIndex, contextIndex);
        } else {
            int problemIndex = pe.getProblemMark().get().getIndex();
            return split(problemIndex, problemIndex);
        }
    }

    List<YamlSection> split(int a) {
        List<YamlSection> ret = new LinkedList<>();
        YamlSection before = a < source.length() ? before(a) : trimTail();
        YamlSection after = a > 0 ? after(a) : trimHead();
        if (!after.isEmpty()) {
            ret.add(after);
        }
        if (!before.isEmpty()) {
            ret.add(before);
        }
        return ret;
    }
    
    List<YamlSection> split(int a, int b) {
        if (a == b){
            return split(a);
        }
        List<YamlSection> ret = new LinkedList<>();
        YamlSection before = before(a);
        YamlSection after = after(b);
        if (!after.isEmpty()) {
            ret.add(after);
        }
        if (!before.isEmpty()) {
            ret.add(before);
        }
        return ret;
    }

    private int getIndex(Optional<Mark> om) {
        return om.get().getIndex() + offset;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + this.offset;
        hash = 79 * hash + Objects.hashCode(this.source);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final YamlSection other = (YamlSection) obj;
        if (this.offset != other.offset) {
            return false;
        }
        if (!Objects.equals(this.source, other.source)) {
            return false;
        }
        return true;
    }

    
    @Override
    public String toString() {
        return "" + offset + ":" + source;
    }
}
