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
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.event.ChangeListener;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.spi.DefaultError;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.openide.util.NbBundle;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.composer.Composer;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.parser.ParserImpl;
import org.snakeyaml.engine.v2.scanner.ScannerImpl;
import org.snakeyaml.engine.v2.scanner.StreamReader;

/**
 * Parser for YAML. Delegates to the YAML parser shipped with JRuby (jvyamlb)
 *
 * @author Tor Norbye
 */
public class YamlParser extends Parser {

    private static final Logger LOGGER = Logger.getLogger(YamlParser.class.getName());
    /**
     * The max length for files we will try to parse (to avoid OOMEs).
     */
    private static final int MAX_LENGTH = 512 * 1024;
    private YamlParserResult lastResult;

    @Override
    public void addChangeListener(ChangeListener changeListener) {
        // FIXME parsing API
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
        // FIXME parsing API
    }

    @Override
    public void cancel() {
        // FIXME parsing API
    }

    @Override
    public Result getResult(Task task) throws ParseException {
        assert lastResult != null : "getResult() called prior parse()"; //NOI18N
        return lastResult;
    }

    private static String asString(CharSequence sequence) {
        if (sequence instanceof String) {
            return (String) sequence;
        } else {
            return sequence.toString();
        }
    }

    private boolean isTooLarge(String source) {
        return source.length() > MAX_LENGTH;
    }

    private YamlParserResult resultForTooLargeFile(Snapshot snapshot) {
        YamlParserResult result = new YamlParserResult(Collections.<Node>emptyList(), this, snapshot, false);
        // FIXME this can violate contract of DefaultError (null fo)
        DefaultError error = new DefaultError(null, NbBundle.getMessage(YamlParser.class, "TooLarge"), null,
                snapshot.getSource().getFileObject(), 0, 0, Severity.WARNING);
        result.addError(error);
        return result;
    }

    // package private for unit tests
    static String replacePhpFragments(String source) {
        // this is a hack. The right solution would be to create a toplevel language, which
        // will have embeded yaml and php.
        // This code replaces php fragments with space, because jruby parser fails
        // on this.
        int startReplace = source.indexOf("<?");
        if (startReplace == -1) {
            return source;
        }

        StringBuilder result = new StringBuilder(source);

        while (startReplace > -1) {
            int endReplace = result.indexOf("?>", startReplace);
            if (endReplace > -1) {
                endReplace = endReplace + 1;
                StringBuilder spaces = new StringBuilder(endReplace - startReplace);
                for (int i = 0; i <= endReplace - startReplace; i++) {
                    spaces.append(' ');
                }
                result.replace(startReplace, endReplace + 1, spaces.toString());
                startReplace = result.indexOf("<?", endReplace);
            } else {
                startReplace = -1;
            }
        }
        return result.toString();
    }

    static String replaceMustache(String source) {
        // this is a hack. The right solution would be to create a toplevel language, which
        // will have embeded yaml and something.
        // This code replaces mouthstache fragments with space.
        int startReplace = source.indexOf("{{");
        if (startReplace == -1) {
            return source;
        }

        StringBuilder result = new StringBuilder(source);

        while (startReplace > -1) {
            int endReplace = result.indexOf("}}", startReplace);
            if (endReplace > -1) {
                endReplace = endReplace + 1;
                StringBuilder spaces = new StringBuilder(endReplace - startReplace);
                for (int i = 0; i <= endReplace - startReplace; i++) {
                    spaces.append(' ');
                }
                result.replace(startReplace, endReplace + 1, spaces.toString());
                startReplace = result.indexOf("{{", endReplace);
            } else {
                startReplace = -1;
            }
        }
        return result.toString();
    }

    private static String replaceCommonSpecialCharacters(String source) {
        source = source.replace('@', '_'); //NOI18N
        source = source.replace('?', '_'); //NOI18N
        source = source.replaceAll("!(?!(omap|!omap))", "_"); //NOI18N
        return source;
    }

    private static String replaceInlineRegexBrackets(String source) {
        // XXX this is just a workaround for issue #246787
        // it is caused by jvyamlb yaml parser
        Pattern p = Pattern.compile("\\^/.*?\\[.*?\\].*?,");
        Matcher m = p.matcher(source);
        while (m.find()) {
            String found = m.group();
            String replaced = found.replace('[', '_').replace(']', '_');
            source = source.replace(found, replaced);
        }
        return source;
    }

    // for test package private

    YamlParserResult parse(String source, Snapshot snapshot) {

        source = replacePhpFragments(source);
        source = replaceMustache(source);
        source = replaceCommonSpecialCharacters(source);
        source = replaceInlineRegexBrackets(source);

        List<Node> nodes = new ArrayList<Node>();
        try {
            if (isTooLarge(source)) {
                return resultForTooLargeFile(snapshot);
            }
            LoadSettings settings = LoadSettings.builder().build();
            ScannerImpl scanner = new ScannerImpl(settings, new StreamReader(settings, source));
            ParserImpl parser = new ParserImpl(settings, scanner);
            Composer composer = new Composer(settings, parser);
            
            while (composer.hasNext()) {
                Node node = composer.next();
                if (node == null) {
                    break;
                }
                nodes.add(node);
            }
            //TODO: add errors
            YamlParserResult result = new YamlParserResult(nodes, this, snapshot, true);
            return result;
        } catch (Exception ex) {
            YamlParserResult result = new YamlParserResult(Collections.<Node>emptyList(), this, snapshot, false);
            String message = ex.getMessage();
            if (message != null && message.length() > 0) {
                result.addError(processError(message, snapshot, 0));
            }
            return result;
        }
    }

    private DefaultError processError(String message, Snapshot snapshot, int pos) {
        // Strip off useless prefixes to make errors more readable
        if (message.startsWith("ScannerException null ")) { // NOI18N
            message = message.substring(22);
        } else if (message.startsWith("ParserException ")) { // NOI18N
            message = message.substring(16);
        }
        // Capitalize sentences
        char firstChar = message.charAt(0);
        char upcasedChar = Character.toUpperCase(firstChar);
        if (firstChar != upcasedChar) {
            message = upcasedChar + message.substring(1);
        }
        // FIXME this can violate contract of DefaultError (null fo)
        return new DefaultError(null, message, null, snapshot.getSource().getFileObject(),
                pos, pos, Severity.ERROR);
    }

    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {

        String source = asString(snapshot.getText());
        if (isTooLarge(source)) {
            LOGGER.log(Level.FINE,
                    "Skipping {0}, too large to parse (length: {1})",
                    new Object[]{snapshot.getSource().getFileObject(), source.length()});
            lastResult = resultForTooLargeFile(snapshot);
            return;
        }

        try {
//                int caretOffset = reader.getCaretOffset(file);
//                if (caretOffset != -1 && job.translatedSource != null) {
//                    caretOffset = job.translatedSource.getAstOffset(caretOffset);
//                }

            // Construct source by removing <% %> tokens etc.
            StringBuilder sb = new StringBuilder();
            TokenHierarchy hi = TokenHierarchy.create(source, YamlTokenId.language());

            TokenSequence ts = hi.tokenSequence();

            // If necessary move ts to the requested offset
            int offset = 0;
            ts.move(offset);

//                int adjustedOffset = 0;
//                int adjustedCaretOffset = -1;
            while (ts.moveNext()) {
                Token t = ts.token();
                TokenId id = t.id();

                if (id == YamlTokenId.RUBY_EXPR) {
                    String marker = "__"; // NOI18N
                    // Marker
                    sb.append(marker);
                    // Replace with spaces to preserve offsets
                    for (int i = 0, n = t.length() - marker.length(); i < n; i++) { // -2: account for the __
                        sb.append(' ');
                    }
                } else if (id == YamlTokenId.RUBY || id == YamlTokenId.RUBYCOMMENT || id == YamlTokenId.DELIMITER) {
                    // Replace with spaces to preserve offsets
                    for (int i = 0; i < t.length(); i++) {
                        sb.append(' ');
                    }
                } else {
                    sb.append(t.text().toString());
                }

//                    adjustedOffset += t.length();
            }

            source = sb.toString();

            lastResult = parse(source, snapshot);
        } catch (Exception ioe) {
            lastResult = new YamlParserResult(Collections.<Node>emptyList(), this, snapshot, false);
        }
    }
}
