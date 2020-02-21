/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */


package org.netbeans.modules.cnd.asm.base.ident;

import java.io.Reader;
import java.util.Arrays;
import java.util.EventListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.modules.cnd.antlr.Token;
import org.netbeans.modules.cnd.antlr.TokenStreamException;

import org.netbeans.modules.cnd.asm.model.AbstractAsmModel;
import org.netbeans.modules.cnd.asm.model.AsmModelProvider;
import org.netbeans.modules.cnd.asm.model.AsmSyntaxProvider;
import org.netbeans.modules.cnd.asm.model.AsmTypesProvider;
import org.netbeans.modules.cnd.asm.model.util.DefaultAsmTypesEntry;
import org.netbeans.modules.cnd.asm.model.util.DefaultResolverResult;

import org.netbeans.modules.cnd.asm.base.SparcModelProvider;
import org.netbeans.modules.cnd.asm.base.X86ModelProvider;
import org.netbeans.modules.cnd.asm.base.att.ATTIdentResolver;
import org.netbeans.modules.cnd.asm.base.att.ATTSparcSyntaxProvider;
import org.netbeans.modules.cnd.asm.base.att.ATTx86SyntaxProvider;
import org.netbeans.modules.cnd.asm.base.dis.DisSparcSyntaxProvider;
import org.netbeans.modules.cnd.asm.base.dis.DisX86SyntaxProvider;
import org.netbeans.modules.cnd.asm.base.generated.IdentScanner;
import org.netbeans.modules.cnd.asm.base.generated.IdentScannerTokenTypes;
import org.netbeans.modules.cnd.asm.base.syntax.IdentResolver;

@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.asm.model.AsmTypesProvider.class)
public class SparcX86Resolver implements AsmTypesProvider {

    public static final AsmSyntaxProvider ATT_X86_SYNTAX =
                new ATTx86SyntaxProvider();
    public static final AsmSyntaxProvider ATT_SPARC_SYNTAX =
                new ATTSparcSyntaxProvider();
    public static final AsmSyntaxProvider DIS_X86_SYNTAX =
                new DisX86SyntaxProvider();
    public static final AsmSyntaxProvider DIS_SPARC_SYNTAX =
                new DisSparcSyntaxProvider();

    public static final AsmModelProvider AMD64 =
                 X86ModelProvider.getInstance();
    public static final AsmModelProvider SPARC =
                 SparcModelProvider.getInstance();

    public List<AsmTypesEntry> getAsmTypes() {
        AsmTypesEntry amd64 = new DefaultAsmTypesEntry(AMD64, ATT_X86_SYNTAX,
                                                       DIS_X86_SYNTAX);

        AsmTypesEntry sparc = new DefaultAsmTypesEntry(SPARC, ATT_SPARC_SYNTAX,
                                                       DIS_SPARC_SYNTAX);

        return Arrays.asList(amd64, sparc);
    }

    public ResolverResult resolve(Reader source) {

        AbstractAsmModel amd64 = (AbstractAsmModel) AMD64.getModel();
        AbstractAsmModel sparc = (AbstractAsmModel) SPARC.getModel();

        AsmSyntaxProvider syntRes;
        AsmModelProvider modelRes;

        IdentResolver []resolvers = new IdentResolver[] { new ATTIdentResolver(amd64),
                                                          new ATTIdentResolver(sparc)
                                                        };

        SyntaxChooser syntChooser = new SyntaxChooser();
        ModelChooser modelChooser = new ModelChooser(resolvers);

        resolver(source, new ScannerListener[] { syntChooser, modelChooser });

        int synt = modelChooser.getResult();


        if (synt == 0) {
            modelRes = AMD64;
        } else {
            modelRes = SPARC;
        }

        if (syntChooser.hasDis()) {
            if (synt == 0) {
                syntRes = DIS_X86_SYNTAX;

            } else {
                syntRes = DIS_SPARC_SYNTAX;
            }
        } else {
            if (synt == 0) {
                syntRes = ATT_X86_SYNTAX;

            } else {
                syntRes = ATT_SPARC_SYNTAX;
            }
        }


        return new DefaultResolverResult(modelRes, syntRes, null);
    }

    private void resolver(Reader source, ScannerListener[] listeners) {
        IdentScanner scanner = new IdentScanner(source);

        for (ScannerListener lis : listeners) {
            lis.start();
        }

        while (true) {
            Token tok;

            try {
                tok = scanner.nextToken();
            } catch (TokenStreamException ex) {
                Logger.getLogger(this.getClass().getName()).
                    log(Level.WARNING, "Ident lexer crashed"); // NOI18N

                break;
            }

            for (ScannerListener lis : listeners) {
                lis.token(tok);
            }

            if (tok.getType() == IdentScannerTokenTypes.EOF) {
                break;
            }
        }

        int numLines = scanner.getNumLines();
        for (ScannerListener lis : listeners) {
            lis.end(numLines);
        }
    }

    private interface ScannerListener extends EventListener {
        void start();
        void token(Token tok);
        void end(int lines);
    }

    private static class SyntaxChooser implements ScannerListener {

        private int numPluses;
        private int numOpcodes;
        private int numComments;
        private int numLines;

        public void token(Token tok) {
            String text = tok.getText();
            int type = tok.getType();

            switch (type) {
                case IdentScannerTokenTypes.Mark:
                    if ("+".equals(text)) { // NOI18N
                        numPluses++;
                    }
                    break;
                case IdentScannerTokenTypes.Comment:
                    numComments++;
                    break;
                case IdentScannerTokenTypes.Ident:
                    if (isOpcode(text)) {
                        numOpcodes++;
                    }
                    break;
                default:
                    break;
            }
        }

        public boolean hasDis() {
            float opCodesPerLine = 0.f;
            float plusesPerLine = 0.f;

            if (numLines > 0) {
                opCodesPerLine = (float) numOpcodes / (float) numLines;
                plusesPerLine = (float) numPluses / (float) numLines;
            }

            // VERY MAGIC NUMBERS :)
            return Float.compare(opCodesPerLine, 2f) > 0 ||
                   Float.compare(plusesPerLine, 0.9f) > 0;
        }

        private boolean isOpcode(String res) {
            if (res.length() == 2 &&
                isHexDigit(res.charAt(0)) &&
                isHexDigit(res.charAt(1))) {
                return true;
             }
             return false;
        }

        private boolean isHexDigit(char ch) {
           if ((ch >= '0' && ch <= '9') ||
               (ch >= 'a' && ch <= 'f') ||
               (ch >= 'A' && ch <= 'F')) {
               return true;
           }
           return false;
        }

        public void end(int lines) { numLines = lines - numComments; }

        public void start() { }
    }

    private static class ModelChooser implements  ScannerListener {

        private final int []results;
        private final IdentResolver[] resolvers;

        public ModelChooser(IdentResolver[] resolvers) {
            assert resolvers.length > 0;

            this.resolvers = resolvers;
            results = new int[resolvers.length];
        }

        public int getResult() {
            int maxIdx = 0;

            // ToDo: next index
            for(int i = 1; i < results.length; i++) {
                if (results[maxIdx] < results[i])
                    maxIdx = i;
            }

            if (results[maxIdx] == 0)
                return 0;

            return maxIdx;
        }

        public void token(Token tok) {
            String text = tok.getText();
            int type = tok.getType();

            if (type == IdentScannerTokenTypes.Register) {

                for (int i = 0; i < resolvers.length; i++) {
                    if (resolvers[i].getRegister(text) != null) {
                        results[i]++;
                    }
                }
            }
         }

         public void start() { }

         public void end(int lines) { }
    }
}
