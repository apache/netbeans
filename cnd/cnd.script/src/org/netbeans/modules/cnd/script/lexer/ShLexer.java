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

package org.netbeans.modules.cnd.script.lexer;

import org.netbeans.modules.cnd.api.script.ShTokenId;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;


/**
 *
 */
class ShLexer implements Lexer<ShTokenId> {

    private static final Set<String> keywords = new HashSet<String> ();
    private static final Set<String> commands = new HashSet<String> ();

    static {
        keywords.add ("aux"); // NOI18N
        keywords.add ("alias"); // NOI18N
        keywords.add ("argv"); // NOI18N
        keywords.add ("autologout"); // NOI18N
        keywords.add ("break"); // NOI18N
        keywords.add ("case"); // NOI18N
        keywords.add ("continue"); // NOI18N
        keywords.add ("do"); // NOI18N
        keywords.add ("done"); // NOI18N
        keywords.add ("elif"); // NOI18N
        keywords.add ("else"); // NOI18N
        keywords.add ("end"); // NOI18N
        keywords.add ("endif"); // NOI18N
        keywords.add ("endsw"); // NOI18N
        keywords.add ("esac"); // NOI18N
        keywords.add ("exit"); // NOI18N
        keywords.add ("fi"); // NOI18N
        keywords.add ("for"); // NOI18N
        keywords.add ("function"); // NOI18N
        keywords.add ("history"); // NOI18N
        keywords.add ("if"); // NOI18N
        keywords.add ("ignoreeof"); // NOI18N
        keywords.add ("in"); // NOI18N
        keywords.add ("noclobber"); // NOI18N
        keywords.add ("path"); // NOI18N
        keywords.add ("prompt"); // NOI18N
        keywords.add ("return"); // NOI18N
        keywords.add ("select"); // NOI18N
        keywords.add ("set"); // NOI18N
        keywords.add ("setenv"); // NOI18N
        keywords.add ("shift"); // NOI18N
        keywords.add ("switch"); // NOI18N
        keywords.add ("term"); // NOI18N
        keywords.add ("then"); // NOI18N
        keywords.add ("trap"); // NOI18N
        keywords.add ("unalias"); // NOI18N
        keywords.add ("unset"); // NOI18N
        keywords.add ("until"); // NOI18N
        keywords.add ("while"); // NOI18N
        keywords.add ("source"); // NOI18N
        keywords.add ("alias"); // NOI18N
        keywords.add ("bg"); // NOI18N
        keywords.add ("bind"); // NOI18N
        keywords.add ("break"); // NOI18N
        keywords.add ("builtin"); // NOI18N
        keywords.add ("cd"); // NOI18N
        keywords.add ("command"); // NOI18N
        keywords.add ("compgen"); // NOI18N
        keywords.add ("complete"); // NOI18N
        keywords.add ("continue"); // NOI18N
        keywords.add ("dirs"); // NOI18N
        keywords.add ("disown"); // NOI18N
        keywords.add ("enable"); // NOI18N
        keywords.add ("eval"); // NOI18N
        keywords.add ("exec"); // NOI18N
        keywords.add ("exit"); // NOI18N
        keywords.add ("fc"); // NOI18N
        keywords.add ("fg"); // NOI18N
        keywords.add ("getopts"); // NOI18N
        keywords.add ("hash"); // NOI18N
        keywords.add ("help"); // NOI18N
        keywords.add ("history"); // NOI18N
        keywords.add ("jobs"); // NOI18N
        keywords.add ("kill"); // NOI18N
        keywords.add ("let"); // NOI18N
        keywords.add ("logout"); // NOI18N
        keywords.add ("popd"); // NOI18N
        keywords.add ("printf"); // NOI18N
        keywords.add ("pushd"); // NOI18N
        keywords.add ("pwd"); // NOI18N
        keywords.add ("return"); // NOI18N
        keywords.add ("set"); // NOI18N
        keywords.add ("shift"); // NOI18N
        keywords.add ("shopt"); // NOI18N
        keywords.add ("suspend"); // NOI18N
        keywords.add ("test"); // NOI18N
        keywords.add ("times"); // NOI18N
        keywords.add ("trap"); // NOI18N
        keywords.add ("type"); // NOI18N
        keywords.add ("ulimit"); // NOI18N
        keywords.add ("umask"); // NOI18N
        keywords.add ("unalias"); // NOI18N
        keywords.add ("wait"); // NOI18N

        keywords.add ("export"); // NOI18N
        keywords.add ("unset"); // NOI18N
        keywords.add ("declare"); // NOI18N
        keywords.add ("typeset"); // NOI18N
        keywords.add ("local"); // NOI18N
        keywords.add ("read"); // NOI18N
        keywords.add ("readonly"); // NOI18N

        commands.add ("arch"); // NOI18N
        commands.add ("awk"); // NOI18N
        commands.add ("bash"); // NOI18N
        commands.add ("bunzip2"); // NOI18N
        commands.add ("bzcat"); // NOI18N
        commands.add ("bzcmp"); // NOI18N
        commands.add ("bzdiff"); // NOI18N
        commands.add ("bzegrep"); // NOI18N
        commands.add ("bzfgrep"); // NOI18N
        commands.add ("bzgrep"); // NOI18N
        commands.add ("bzip2"); // NOI18N
        commands.add ("bzip2recover"); // NOI18N
        commands.add ("bzless"); // NOI18N
        commands.add ("bzmore"); // NOI18N
        commands.add ("cat"); // NOI18N
        commands.add ("chattr"); // NOI18N
        commands.add ("chgrp"); // NOI18N
        commands.add ("chmod"); // NOI18N
        commands.add ("chown"); // NOI18N
        commands.add ("chvt"); // NOI18N
        commands.add ("cp"); // NOI18N
        commands.add ("date"); // NOI18N
        commands.add ("dd"); // NOI18N
        commands.add ("deallocvt"); // NOI18N
        commands.add ("df"); // NOI18N
        commands.add ("dir"); // NOI18N
        commands.add ("dircolors"); // NOI18N
        commands.add ("dmesg"); // NOI18N
        commands.add ("dnsdomainname"); // NOI18N
        commands.add ("domainname"); // NOI18N
        commands.add ("du"); // NOI18N
        commands.add ("dumpkeys"); // NOI18N
        commands.add ("echo"); // NOI18N
        commands.add ("ed"); // NOI18N
        commands.add ("egrep"); // NOI18N
        commands.add ("false"); // NOI18N
        commands.add ("fgconsole"); // NOI18N
        commands.add ("fgrep"); // NOI18N
        commands.add ("fuser"); // NOI18N
        commands.add ("gawk"); // NOI18N
        commands.add ("getkeycodes"); // NOI18N
        commands.add ("gocr"); // NOI18N
        commands.add ("grep"); // NOI18N
        commands.add ("groups"); // NOI18N
        commands.add ("gunzip"); // NOI18N
        commands.add ("gzexe"); // NOI18N
        commands.add ("gzip"); // NOI18N
        commands.add ("hostname"); // NOI18N
        commands.add ("igawk"); // NOI18N
        commands.add ("install"); // NOI18N
        commands.add ("kbd_mode"); // NOI18N
        commands.add ("kbdrate"); // NOI18N
        commands.add ("killall"); // NOI18N
        commands.add ("last"); // NOI18N
        commands.add ("lastb"); // NOI18N
        commands.add ("link"); // NOI18N
        commands.add ("ln"); // NOI18N
        commands.add ("loadkeys"); // NOI18N
        commands.add ("loadunimap"); // NOI18N
        commands.add ("login"); // NOI18N
        commands.add ("ls"); // NOI18N
        commands.add ("lsattr"); // NOI18N
        commands.add ("lsmod"); // NOI18N
        commands.add ("lsmod.old"); // NOI18N
        commands.add ("mapscrn"); // NOI18N
        commands.add ("mesg"); // NOI18N
        commands.add ("mkdir"); // NOI18N
        commands.add ("mkfifo"); // NOI18N
        commands.add ("mknod"); // NOI18N
        commands.add ("mktemp"); // NOI18N
        commands.add ("more"); // NOI18N
        commands.add ("mount"); // NOI18N
        commands.add ("mv"); // NOI18N
        commands.add ("nano"); // NOI18N
        commands.add ("netstat"); // NOI18N
        commands.add ("nisdomainname"); // NOI18N
        commands.add ("openvt"); // NOI18N
        commands.add ("pgawk"); // NOI18N
        commands.add ("pidof"); // NOI18N
        commands.add ("ping"); // NOI18N
        commands.add ("ps"); // NOI18N
        commands.add ("pstree"); // NOI18N
        commands.add ("pwd"); // NOI18N
        commands.add ("rbash"); // NOI18N
        commands.add ("readlink"); // NOI18N
        commands.add ("red"); // NOI18N
        commands.add ("resizecons"); // NOI18N
        commands.add ("rm"); // NOI18N
        commands.add ("rmdir"); // NOI18N
        commands.add ("run-parts"); // NOI18N
        commands.add ("sash"); // NOI18N
        commands.add ("sed"); // NOI18N
        commands.add ("setfont"); // NOI18N
        commands.add ("setkeycodes"); // NOI18N
        commands.add ("setleds"); // NOI18N
        commands.add ("setmetamode"); // NOI18N
        commands.add ("setserial"); // NOI18N
        commands.add ("sh"); // NOI18N
        commands.add ("showkey"); // NOI18N
        commands.add ("shred"); // NOI18N
        commands.add ("sleep"); // NOI18N
        commands.add ("ssed"); // NOI18N
        commands.add ("stat"); // NOI18N
        commands.add ("stty"); // NOI18N
        commands.add ("su"); // NOI18N
        commands.add ("sync"); // NOI18N
        commands.add ("tar"); // NOI18N
        commands.add ("tempfile"); // NOI18N
        commands.add ("touch"); // NOI18N
        commands.add ("true"); // NOI18N
        commands.add ("umount"); // NOI18N
        commands.add ("uname"); // NOI18N
        commands.add ("unicode_start"); // NOI18N
        commands.add ("unicode_stop"); // NOI18N
        commands.add ("unlink"); // NOI18N
        commands.add ("utmpdump"); // NOI18N
        commands.add ("uuidgen"); // NOI18N
        commands.add ("vdir"); // NOI18N
        commands.add ("wall"); // NOI18N
        commands.add ("wc"); // NOI18N
        commands.add ("ypdomainname"); // NOI18N
        commands.add ("zcat"); // NOI18N
        commands.add ("zcmp"); // NOI18N
        commands.add ("zdiff"); // NOI18N
        commands.add ("zegrep"); // NOI18N
        commands.add ("zfgrep"); // NOI18N
        commands.add ("zforce"); // NOI18N
        commands.add ("zgrep"); // NOI18N
        commands.add ("zless"); // NOI18N
        commands.add ("zmore"); // NOI18N
        commands.add ("znew"); // NOI18N
        commands.add ("zsh"); // NOI18N
        commands.add ("aclocal"); // NOI18N
        commands.add ("aconnect"); // NOI18N
        commands.add ("aplay"); // NOI18N
        commands.add ("apm"); // NOI18N
        commands.add ("apmsleep"); // NOI18N
        commands.add ("apropos"); // NOI18N
        commands.add ("ar"); // NOI18N
        commands.add ("arecord"); // NOI18N
        commands.add ("as"); // NOI18N
        commands.add ("as86"); // NOI18N
        commands.add ("autoconf"); // NOI18N
        commands.add ("autoheader"); // NOI18N
        commands.add ("automake"); // NOI18N
        commands.add ("awk"); // NOI18N
        commands.add ("basename"); // NOI18N
        commands.add ("bc"); // NOI18N
        commands.add ("bison"); // NOI18N
        commands.add ("c++"); // NOI18N
        commands.add ("cal"); // NOI18N
        commands.add ("cat"); // NOI18N
        commands.add ("cc"); // NOI18N
        commands.add ("cdda2wav"); // NOI18N
        commands.add ("cdparanoia"); // NOI18N
        commands.add ("cdrdao"); // NOI18N
        commands.add ("cd-read"); // NOI18N
        commands.add ("cdrecord"); // NOI18N
        commands.add ("chfn"); // NOI18N
        commands.add ("chgrp"); // NOI18N
        commands.add ("chmod"); // NOI18N
        commands.add ("chown"); // NOI18N
        commands.add ("chroot"); // NOI18N
        commands.add ("chsh"); // NOI18N
        commands.add ("clear"); // NOI18N
        commands.add ("cmp"); // NOI18N
        commands.add ("co"); // NOI18N
        commands.add ("col"); // NOI18N
        commands.add ("comm"); // NOI18N
        commands.add ("cp"); // NOI18N
        commands.add ("cpio"); // NOI18N
        commands.add ("cpp"); // NOI18N
        commands.add ("cut"); // NOI18N
        commands.add ("dc"); // NOI18N
        commands.add ("dd"); // NOI18N
        commands.add ("df"); // NOI18N
        commands.add ("diff"); // NOI18N
        commands.add ("diff3"); // NOI18N
        commands.add ("dir"); // NOI18N
        commands.add ("dircolors"); // NOI18N
        commands.add ("directomatic"); // NOI18N
        commands.add ("dirname"); // NOI18N
        commands.add ("du"); // NOI18N
        commands.add ("env"); // NOI18N
        commands.add ("expr"); // NOI18N
        commands.add ("fbset"); // NOI18N
        commands.add ("file"); // NOI18N
        commands.add ("find"); // NOI18N
        commands.add ("flex"); // NOI18N
        commands.add ("flex++"); // NOI18N
        commands.add ("fmt"); // NOI18N
        commands.add ("free"); // NOI18N
        commands.add ("ftp"); // NOI18N
        commands.add ("funzip"); // NOI18N
        commands.add ("fuser"); // NOI18N
        commands.add ("g++"); // NOI18N
        commands.add ("gawk"); // NOI18N
        commands.add ("gc"); // NOI18N
        commands.add ("gcc"); // NOI18N
        commands.add ("gdb"); // NOI18N
        commands.add ("getent"); // NOI18N
        commands.add ("getopt"); // NOI18N
        commands.add ("gettext"); // NOI18N
        commands.add ("gettextize"); // NOI18N
        commands.add ("gimp"); // NOI18N
        commands.add ("gimp-remote"); // NOI18N
        commands.add ("gimptool"); // NOI18N
        commands.add ("gmake"); // NOI18N
        commands.add ("gs"); // NOI18N
        commands.add ("head"); // NOI18N
        commands.add ("hexdump"); // NOI18N
        commands.add ("id"); // NOI18N
        commands.add ("install"); // NOI18N
        commands.add ("join"); // NOI18N
        commands.add ("kill"); // NOI18N
        commands.add ("killall"); // NOI18N
        commands.add ("ld"); // NOI18N
        commands.add ("ld86"); // NOI18N
        commands.add ("ldd"); // NOI18N
        commands.add ("less"); // NOI18N
        commands.add ("lex"); // NOI18N
        commands.add ("ln"); // NOI18N
        commands.add ("locate"); // NOI18N
        commands.add ("lockfile"); // NOI18N
        commands.add ("logname"); // NOI18N
        commands.add ("lp"); // NOI18N
        commands.add ("lpr"); // NOI18N
        commands.add ("ls"); // NOI18N
        commands.add ("lynx"); // NOI18N
        commands.add ("m4"); // NOI18N
        commands.add ("make"); // NOI18N
        commands.add ("man"); // NOI18N
        commands.add ("mkdir"); // NOI18N
        commands.add ("mknod"); // NOI18N
        commands.add ("msgfmt"); // NOI18N
        commands.add ("mv"); // NOI18N
        commands.add ("namei"); // NOI18N
        commands.add ("nasm"); // NOI18N
        commands.add ("nawk"); // NOI18N
        commands.add ("nice"); // NOI18N
        commands.add ("nl"); // NOI18N
        commands.add ("nm"); // NOI18N
        commands.add ("nm86"); // NOI18N
        commands.add ("nmap"); // NOI18N
        commands.add ("nohup"); // NOI18N
        commands.add ("nop"); // NOI18N
        commands.add ("od"); // NOI18N
        commands.add ("passwd"); // NOI18N
        commands.add ("patch"); // NOI18N
        commands.add ("pcregrep"); // NOI18N
        commands.add ("pcretest"); // NOI18N
        commands.add ("perl"); // NOI18N
        commands.add ("perror"); // NOI18N
        commands.add ("pidof"); // NOI18N
        commands.add ("pr"); // NOI18N
        commands.add ("printf"); // NOI18N
        commands.add ("procmail"); // NOI18N
        commands.add ("prune"); // NOI18N
        commands.add ("ps2ascii"); // NOI18N
        commands.add ("ps2epsi"); // NOI18N
        commands.add ("ps2frag"); // NOI18N
        commands.add ("ps2pdf"); // NOI18N
        commands.add ("ps2ps"); // NOI18N
        commands.add ("psbook"); // NOI18N
        commands.add ("psmerge"); // NOI18N
        commands.add ("psnup"); // NOI18N
        commands.add ("psresize"); // NOI18N
        commands.add ("psselect"); // NOI18N
        commands.add ("pstops"); // NOI18N
        commands.add ("rcs"); // NOI18N
        commands.add ("rev"); // NOI18N
        commands.add ("rm"); // NOI18N
        commands.add ("scp"); // NOI18N
        commands.add ("sed"); // NOI18N
        commands.add ("seq"); // NOI18N
        commands.add ("setterm"); // NOI18N
        commands.add ("shred"); // NOI18N
        commands.add ("size"); // NOI18N
        commands.add ("size86"); // NOI18N
        commands.add ("skill"); // NOI18N
        commands.add ("slogin"); // NOI18N
        commands.add ("snice"); // NOI18N
        commands.add ("sort"); // NOI18N
        commands.add ("sox"); // NOI18N
        commands.add ("split"); // NOI18N
        commands.add ("ssh"); // NOI18N
        commands.add ("ssh-add"); // NOI18N
        commands.add ("ssh-agent"); // NOI18N
        commands.add ("ssh-keygen"); // NOI18N
        commands.add ("ssh-keyscan"); // NOI18N
        commands.add ("stat"); // NOI18N
        commands.add ("strings"); // NOI18N
        commands.add ("strip"); // NOI18N
        commands.add ("sudo"); // NOI18N
        commands.add ("suidperl"); // NOI18N
        commands.add ("sum"); // NOI18N
        commands.add ("tac"); // NOI18N
        commands.add ("tail"); // NOI18N
        commands.add ("tee"); // NOI18N
        commands.add ("test"); // NOI18N
        commands.add ("tr"); // NOI18N
        commands.add ("uniq"); // NOI18N
        commands.add ("unlink"); // NOI18N
        commands.add ("unzip"); // NOI18N
        commands.add ("updatedb"); // NOI18N
        commands.add ("updmap"); // NOI18N
        commands.add ("uptime"); // NOI18N
        commands.add ("users"); // NOI18N
        commands.add ("vmstat"); // NOI18N
        commands.add ("w"); // NOI18N
        commands.add ("wc"); // NOI18N
        commands.add ("wget"); // NOI18N
        commands.add ("whatis"); // NOI18N
        commands.add ("whereis"); // NOI18N
        commands.add ("which"); // NOI18N
        commands.add ("who"); // NOI18N
        commands.add ("whoami"); // NOI18N
        commands.add ("write"); // NOI18N
        commands.add ("xargs"); // NOI18N
        commands.add ("yacc"); // NOI18N
        commands.add ("yes"); // NOI18N
        commands.add ("zip"); // NOI18N
        commands.add ("zsoelim"); // NOI18N
        commands.add ("dcop"); // NOI18N
        commands.add ("kdialog"); // NOI18N
        commands.add ("kfile"); // NOI18N
        commands.add ("xhost"); // NOI18N
        commands.add ("xmodmap"); // NOI18N
        commands.add ("xset"); // NOI18N
    }

    private LexerRestartInfo<ShTokenId> info;

    private static enum State {
        OTHER,
        AFTER_DOLLAR_BRACE,
        AFTER_SEPARATOR,
        FOR,
        FOR_ID,
        CASE,
        CASE_ID
    }
    
    State state;

    ShLexer(LexerRestartInfo<ShTokenId> info) {
        this.info = info;
        state = info.state() == null ? State.AFTER_SEPARATOR : State.values()[(Integer) info.state()];
    }

    public Token<ShTokenId> nextToken () {
        LexerInput input = info.input ();
        int i = input.read ();
        switch (i) {
            case LexerInput.EOF:
                return null;
            case '+':
            case '<':
            case '>':
            case '!':
            case '@':
            case '=':
            case ';':
            case ',':
            case '(':
            case ')':
            case '{':
            case '}':
            case '[':
            case ']':
            case '-':
            case '*':
            case '/':
            case ':':
            case '?':
            case '^':
            case '.':
            case '`':
            case '%':
                if ((i == '@' || i == '*') && state == State.AFTER_DOLLAR_BRACE) {
                    // expected # or ## next
                } else if (i == ';' || ((state == State.AFTER_SEPARATOR) && (i == '@' || i == '+' || i == '-'))) {
                    state = State.AFTER_SEPARATOR;
                } else {
                    state = State.OTHER;
                }
                return info.tokenFactory().createToken(ShTokenId.OPERATOR);
            case '$':
                i = input.read();
                if (i == '{') {
                    state = State.AFTER_DOLLAR_BRACE;
                    return info.tokenFactory().createToken(ShTokenId.OPERATOR);
                } else if (i == '#') {
                    state = State.OTHER;
                    return info.tokenFactory().createToken(ShTokenId.OPERATOR);
                } else {
                    state = State.OTHER;
                    input.backup(1);
                    return info.tokenFactory().createToken(ShTokenId.OPERATOR);
                }
            case '&':
                i = input.read();
                if(i == '&') {
                    state = State.AFTER_SEPARATOR;
                    return info.tokenFactory().createToken(ShTokenId.OPERATOR);
                } else {
                    state = State.OTHER;
                    input.backup(1);
                    return info.tokenFactory().createToken(ShTokenId.OPERATOR);
                }
            case '|':
                i = input.read();
                if(i == '|') {
                    state = State.AFTER_SEPARATOR;
                    return info.tokenFactory().createToken(ShTokenId.OPERATOR);
                } else {
                    state = State.OTHER;
                    input.backup(1);
                    return info.tokenFactory().createToken(ShTokenId.OPERATOR);
                }
            case '\\':
                i = input.read();
                if(i != '\n') {
                    state = State.OTHER;
                }
                return info.tokenFactory().createToken(ShTokenId.OPERATOR);
            case ' ':
            case '\n':
            case '\r':
            case '\t':
                if(i == '\n' || i == '\r') {
                    state = State.AFTER_SEPARATOR;
                }
                do {
                    i = input.read ();
                    if(i == '\n' || i == '\r') {
                        state = State.AFTER_SEPARATOR;
                    }
                } while (
                    i == ' ' ||
                    i == '\n' ||
                    i == '\r' ||
                    i == '\t'
                );
                if (i != LexerInput.EOF) {
                    input.backup(1);
                }
                return info.tokenFactory ().createToken (ShTokenId.WHITESPACE);
            case '#':
                if (state == State.AFTER_DOLLAR_BRACE) {
                    return info.tokenFactory().createToken(ShTokenId.OPERATOR);
                } else {
                    do {
                        i = input.read ();
                    } while (
                        i != '\n' &&
                        i != '\r' &&
                        i != LexerInput.EOF
                    );
                    state = State.AFTER_SEPARATOR;
                    return info.tokenFactory ().createToken (ShTokenId.COMMENT);
                }
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                do {
                    i = input.read ();
                } while (
                    i >= '0' &&
                    i <= '9'
                );
                if (i == '.') {
                    do {
                        i = input.read ();
                    } while (
                        i >= '0' &&
                        i <= '9'
                    );
                }
                input.backup (1);
                state = State.OTHER;
                return info.tokenFactory ().createToken (ShTokenId.NUMBER);
            case '"':
                do {
                    i = input.read ();
                    if (i == '\\') {
                        i = input.read ();
                        i = input.read ();
                    }
                } while (
                    i != '"' &&
                    i != '\n' &&
                    i != '\r' &&
                    i != LexerInput.EOF
                );
                state = State.OTHER;
                return info.tokenFactory ().createToken (ShTokenId.STRING);
            case '\'':
                do {
                    i = input.read ();
                    if (i == '\\') {
                        i = input.read ();
                        i = input.read ();
                    }
                } while (
                    i != '\'' &&
                    i != '\n' &&
                    i != '\r' &&
                    i != LexerInput.EOF
                );
                state = State.OTHER;
                return info.tokenFactory ().createToken (ShTokenId.STRING);
            default:
                if (
                    (i >= 'a' && i <= 'z') ||
                    (i >= 'A' && i <= 'Z') ||
                    i == '_' ||
                    i == '~'
                ) {
                    do {
                        i = input.read ();
                    } while (
                        (i >= 'a' && i <= 'z') ||
                        (i >= 'A' && i <= 'Z') ||
                        (i >= '0' && i <= '9') ||
                        i == '_' ||
                        i == '~'
                    );
                    input.backup (1);
                    String idstr = input.readText().toString();
                    if (state == State.AFTER_SEPARATOR) {
                        state = State.OTHER;
                        if (keywords.contains(idstr)) {
                            if(idstr.equals("for")) { // NOI18N
                                state = State.FOR;
                            }
                            if(idstr.equals("case")) { // NOI18N
                                state = State.CASE;
                            }
                            return info.tokenFactory().createToken(ShTokenId.KEYWORD);
                        } else if (commands.contains(idstr)) {
                            return info.tokenFactory().createToken(ShTokenId.COMMAND);
                        }
                    } else if (state == State.FOR) {
                        state = State.FOR_ID;
                    } else if (state == State.CASE) {
                        state = State.CASE_ID;
                    } else if (state == State.FOR_ID || state == State.CASE_ID) {
                        state = State.OTHER;
                        if(idstr.equals("in")) { // NOI18N
                            return info.tokenFactory().createToken(ShTokenId.KEYWORD);
                        }
                    } else if (state == State.AFTER_DOLLAR_BRACE) {
                        // keep state
                        // shell script parameter expansion syntax:
                        // ${parameter#word}
                        // ${parameter##word}
                    } else {
                        state = State.OTHER;
                    }
                    return info.tokenFactory().createToken(ShTokenId.IDENTIFIER);
                }
                return info.tokenFactory ().createToken (ShTokenId.ERROR);
        }
    }

    public Object state() {
        return state == State.AFTER_SEPARATOR ? null : state.ordinal();
    }

    public void release() {
    }
}
