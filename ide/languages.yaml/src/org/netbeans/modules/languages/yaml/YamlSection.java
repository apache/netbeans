/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.List;
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

    public boolean isEmpty() {
        return source.isEmpty();
    }
    
    public int getLength() {
        return source.length();
    }
    
    List<? extends StructureItem> collectItems() {
        if (parser != null) {
            throw new IllegalStateException("This YAML segment is already parsed.");
        }
        List< StructureItem> ret = new ArrayList<>();
        ScannerImpl scanner = new ScannerImpl(SETTINGS, new StreamReader(SETTINGS, source));
        parser = new ParserImpl(SETTINGS, scanner);
        while (parser.hasNext()) {
            YamlStructureItem root = processItem(); 
            if (root != null) {
                ret.addAll(root.getNestedItems());
            }
        }
        return ret;
    }

    private YamlStructureItem processItem() {
        YamlStructureItem ret = null;
        while ((ret == null) && parser.hasNext()) {
            switch (parser.peekEvent().getEventId()) {
                case MappingStart:
                    ret = processMapping((MappingStartEvent) parser.next());
                    break;
                case SequenceStart:
                    ret = processSequence((SequenceStartEvent) parser.next());
                    break;
                case Scalar:
                    ret = processScalar((ScalarEvent) parser.next());
                    break;
                case Alias:
                    ret = processAlias((AliasEvent) parser.next());
                    break;
                default:
                    parser.next();
            }
        }
        return ret;
    }

    private YamlStructureItem processAlias(AliasEvent evt) {
        return new YamlStructureItem.Simple(ALIAS, evt.getAlias().getValue(), getIndex(evt.getStartMark()), getIndex(evt.getEndMark()));
    }

    private YamlStructureItem processScalar(ScalarEvent evt) {
        return new YamlStructureItem.Simple(SCALAR,evt.getValue(), getIndex(evt.getStartMark()), getIndex(evt.getEndMark()));
    }

    
    private YamlStructureItem processMapping(MappingStartEvent evt) {
        YamlStructureItem.Collection item = new YamlStructureItem.Collection(MAP, getIndex(evt.getStartMark()));
        while (parser.hasNext() && !parser.checkEvent(Event.ID.MappingEnd)) {
            YamlStructureItem keyItem = processItem();
            YamlStructureItem valueItem = processItem();
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

    private YamlStructureItem processSequence(SequenceStartEvent evt) {
        YamlStructureItem.Collection item = new YamlStructureItem.Collection(SEQUENCE, getIndex(evt.getStartMark()));
        while (parser.hasNext() && !parser.checkEvent(Event.ID.SequenceEnd)) {
            item.add(processItem());
        }
        if (parser.hasNext()) {
            SequenceEndEvent eevt = (SequenceEndEvent) parser.next();
            if (evt.isFlow()) {
                item.setEndMark(getIndex(eevt.getEndMark()));
            }
        }
        return item;
    }

    DefaultError processScannerException(Snapshot snapshot, ScannerException se) {
        int contextIndex = getIndex(se.getContextMark());
        int problemIndex = getIndex(se.getProblemMark());
        StringBuilder message = new StringBuilder(se.getContext());
        message.append(", ").append(se.getProblem());
        char upper = Character.toUpperCase(message.charAt(0));
        message.setCharAt(0, upper);
        return new DefaultError(null, message.toString(), null, snapshot.getSource().getFileObject(), contextIndex, problemIndex, Severity.ERROR);
    }

    DefaultError processParserException(Snapshot snapshot, ParserException se) {
        int problemIndex = getIndex(se.getProblemMark());
        StringBuilder message = new StringBuilder(se.getContext());
        message.append(", ").append(se.getProblem());
        char upper = Character.toUpperCase(message.charAt(0));
        message.setCharAt(0, upper);
        return new DefaultError(null, message.toString(), null, snapshot.getSource().getFileObject(), problemIndex, problemIndex, Severity.ERROR);
    }

    private int getIndex(Optional<Mark> om) {
        return om.get().getIndex() + offset;
    }
    
    @Override
    public String toString() {
        return "" + offset + ":" + source;
    }
}
