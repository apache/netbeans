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

  
package org.netbeans.modules.cnd.asm.base.att;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

import org.netbeans.modules.cnd.asm.base.syntax.fake.FakeInstruction;
import org.netbeans.modules.cnd.asm.model.lang.AsmElement;
import org.netbeans.modules.cnd.asm.model.lang.syntax.AsmToken;
import org.netbeans.modules.cnd.asm.model.lang.Register;
import org.netbeans.modules.cnd.asm.model.lang.syntax.AsmParser;
import org.netbeans.modules.cnd.asm.model.lang.syntax.FunctionBoundsResolver;

import org.netbeans.modules.cnd.asm.base.syntax.*;
import static org.netbeans.modules.cnd.asm.model.lang.syntax.AsmBaseTokenId.*;

import org.netbeans.modules.cnd.asm.model.lang.AsmElementBuilder;
import org.netbeans.modules.cnd.asm.model.lang.RegisterElement;
import org.netbeans.modules.cnd.asm.model.lang.impl.AbstractAsmElement;
import org.netbeans.modules.cnd.asm.model.lang.impl.AsmRootElement;
import org.netbeans.modules.cnd.asm.model.lang.impl.BaseInstructionElement;
import org.netbeans.modules.cnd.asm.model.lang.instruction.Instruction;
import org.netbeans.modules.cnd.asm.model.util.IntervalSet;

public class ATTParser implements AsmParser { 
        
    private final IdentResolver resolver;  
    private final ScannerFactory scannerFactory;
    
    private final Lookup myLookup;
    
    private List<AsmToken> tokens;        
    
    private AsmElementBuilder result; 
    private TokenIterator cur;
    
    private Set<String> globals;
    private IntervalSet<FunctionBoundsResolver.Entry> funcs;
            
    private String memOpBeginMark;
    private String memOpEndMark;
    private FunctionNameDetector funcDetector;
        
    public ATTParser(ScannerFactory scannerFactory, IdentResolver resolver) {                                   
        this.resolver = resolver;
        this.scannerFactory = scannerFactory;
        
        InstanceContent ic = new InstanceContent ();
        ic.add(new FunctionBoundsResolverImpl());
        this.myLookup = new AbstractLookup(ic);
        
        tokens = new ArrayList<AsmToken>();     
        globals = new HashSet<String>();
        funcs = new IntervalSet<FunctionBoundsResolver.Entry>();
        
        setMemOpMarks("(", ")"); // NOI18N        
        setFunctionNameDetector (new DefaultFunctionNameDetector());
    }         
      
    public void setMemOpMarks(String memOpBeginMark,
                              String memOpEndMark) {
        
        this.memOpBeginMark = memOpBeginMark;
        this.memOpEndMark = memOpEndMark;
    }
    
    public void setFunctionNameDetector(FunctionNameDetector funcDetector)  {
        this.funcDetector = funcDetector;
    }
    
    public Lookup getServices() {
        return myLookup;
    }
    
    private void reset(Reader src) {
        AsmToken tok;
        
        tokens.clear();
        globals.clear();
        funcs.clear();
                
        result = AsmElementBuilder.create(null);
        
        AntlrScanner scanner = scannerFactory.createScanner(src, null);
        
        int lastOffset = 0;

        while (true) {
             try { 
                tok =((AntlrToken) scanner.nextToken()).createAsmToken(resolver); 
                lastOffset = tok.getEndOffset();                
             }   
             catch(Exception ex) {
                 tokens.add(new AsmToken(ASM_EOF, null, lastOffset, lastOffset));
                 Logger.getLogger(this.getClass().getName()).
                     log(Level.SEVERE, "Antlr lexer faced error token on position " + lastOffset); // NOI18N
                 break;
             }
           
             if (tok.getId() != ASM_EMPTY) {                 
                 tokens.add(tok);
             }
             
             if(tok.getId() == ASM_EOF) {
                 break;
             }                     
        }     
                                               
        cur = new TokenIteratorImpl(); 
    }
    
    
    public AsmElement parse(Reader src) {
        AsmToken tok;
        
        int lastInstrIdx = -1;
        int lastFuncIdx = -1;
        String lastFuncName = "";
        
        reset(src);
                   
        while (cur.hasNext() && (tok = cur.next()).getId() != ASM_EOF) {
             
            if (tok.getId() == ASM_INSTRUCTION) {
                 result.add(readInstruction((InstructionToken) tok));
                 lastInstrIdx = getLastResultIdx();
             } 

             else if (tok.getId() == ASM_DIRECTIVE) {
                if (isDefinitionMark(tok.getText())) {
                      globals.add(cur.getCurrect().getText());                       
                }                                 
             }   
          
             else if(tok.getId() == ASM_UNKWN_ID || tok.getId() == ASM_LABEL) {                                                       
                   
                   AbstractAsmElement branch = 
                            BranchElementImpl.create(tok.getText());
                   branch.setOffset(tok);
                                     
                   // FAKE STARTED
                   lastInstrIdx = getLastResultIdx();
                   InstructionToken newTok = new InstructionToken(FakeInstruction.getInstance(),
                                                        tok.getText(),
                                                        tok.getStartOffset(),
                                                        tok.getEndOffset());
                   
                   
                   AsmElement iinst = readInstruction(newTok);                                      
                   iinst.getCompounds().add(0, branch);
                   // FAKE ENDED
                                                                                                                  
                   result.add(iinst);                   
             } 
             else if (tok.getId() == ASM_LABEL_INST) {
                 String name = tok.getText();                      
                 String cleanName = funcDetector.getCleanName(name);
                 
                 AbstractAsmElement comp = LabelElementImpl.create(cleanName);
                 comp.setOffset(tok);
                 
                 result.add(comp);
                 
                 if (funcDetector.isFunctionLabel(name)) {                                                               
                     if (lastFuncIdx != -1 && lastFuncIdx != lastInstrIdx) {                         
                         funcs.add(new FunctionBoundsResolver.Entry(lastFuncName,
                                                                    lastFuncIdx,
                                                                    lastInstrIdx
                                                                    ));
                     }
                     lastFuncName = cleanName;
                     lastFuncIdx = lastInstrIdx = getLastResultIdx();                                      
                 }
             }
        }
        
       if (lastFuncIdx != -1 && lastFuncIdx != lastInstrIdx) {                         
            funcs.add(new FunctionBoundsResolver.Entry(lastFuncName, lastFuncIdx,
                                                       lastInstrIdx
                                                      ));
        }
        
        tokens = null;
        
        return AsmRootElement.create(result.get());
    }
               
    private boolean isDefinitionMark(String str) {
        return ".globl".equals(str)  || // NOI18N
               ".local".equals(str)  || // NOI18N
               ".global".equals(str); // NOI18N
    }
    
    private int getLastResultIdx() {
        return result.size() - 1;
    }
    
    private AsmElement readInstruction(InstructionToken instr) {
        AsmToken tok;

        List<Register> read = new ArrayList<Register>();
        List<Register> write = new ArrayList<Register>();                    
        
        AsmElementBuilder instrBuilder = 
                AsmElementBuilder.create(null);                
        
        Instruction instruction = instr.getInstruction();
        
        int inner = 0;
        
        // default arg number is 2
        int argNo = 2;
        // otherwise it should be the number of arguments
        if (!instruction.getArguments().isEmpty()) {
            List params = instruction.getArguments().iterator().next().getParamMnemonic();
            if (!params.isEmpty()) {
                argNo = params.size();
            }
        }
        
        while ((tok = cur.getCurrect()).getId() != ASM_EOF) {
            
            if (tok.getId() == ASM_REGISTER) {                
                Register reg = resolver.getRegister(tok.getText());

                if (reg != null) {
                    AbstractAsmElement regInst;

                    if (inner == 0) {
                        RegisterElement.Usage usage = RegisterElement.Usage.OP_USE_NO_USE;
                        if (instruction.getReadArgIdxs().contains(argNo)) {
                            read.add(reg);
                            usage = RegisterElement.Usage.OP_USE_READ;
                        }
                        if (instruction.getWriteArgIdxs().contains(argNo)) {
                            write.add(reg);
                            usage = usage.apply(RegisterElement.Usage.OP_USE_READ_WRITE);
                        }
                        regInst = RegisterElementImpl.create(reg, usage);
                    } else {
                        read.add(reg);
                        regInst = RegisterElementImpl.create(reg, RegisterElement.Usage.OP_USE_READ);
                    }
                    regInst.setOffset(tok);
                    instrBuilder.add(regInst);   
                }
                    
            } else if (tok.getId() == ASM_MARK) {
                if (tok.getText().equals(",") && inner == 0) { // NOI18N
                    argNo--;
                }                                                     
                else if (tok.getText().equals(memOpBeginMark)) {
                    inner++;
                }
                else if (tok.getText().equals(memOpEndMark)) {
                    inner--;
                }
            } else if (tok.getId() == ASM_UNKWN_ID || tok.getId() == ASM_INSTRUCTION || 
                       tok.getId() == ASM_LABEL_INST || tok.getId() == ASM_DIRECTIVE) {
                break;
            }
            cur.next();
        }               
        
        AbstractAsmElement resInstr = BaseInstructionElement.create(instrBuilder.get(),
                                                                    instruction,
                                                                    read,
                                                                    write);
        resInstr.setStartOffset(instr.getStartOffset());
        resInstr.setEndOffset(cur.getCurrect().getStartOffset());
        
        return resInstr;
    }
    
    private class FunctionBoundsResolverImpl implements FunctionBoundsResolver {
        public IntervalSet<Entry> getFunctions() {
            return funcs;
        }        
    }
    
    public interface FunctionNameDetector {
        boolean isFunctionLabel(String name);
        String getCleanName(String name);        
    }
    
    public class DefaultFunctionNameDetector 
                            implements FunctionNameDetector {
                                                        
        public boolean isFunctionLabel(String name) {
            name = name.substring(0, name.length() - 1);
            return  (!name.startsWith(".") &&  !Character.isDigit(name.charAt(0))) || // NOI18N
                     globals.contains(name);
        }

        public String getCleanName(String name) {
            if(name.endsWith(":")) // NOI18N
                return name.substring(0, name.length() - 1);
            return name;
        }        
    }
    
    private class TokenIteratorImpl implements TokenIterator {
         
        private int curPos;
        
        public TokenIteratorImpl() {
            this(0);
        }
        
        private TokenIteratorImpl(int pos) {                
            assert tokens.size() > 0;
            
            curPos = pos;
        } 
        
        public AsmToken getCurrect() {
            if (curPos >= tokens.size()) {
                return tokens.get(tokens.size() - 1); // EOF
            }
              
            return tokens.get(curPos);
        }
    
        public AsmToken guess(int pos) {
            if (curPos + pos < tokens.size())
                return tokens.get(curPos + pos);
            return tokens.get(tokens.size() - 1);
        }
        
        public AsmToken next() {
            try {
                return getCurrect();
            }
            finally {
                curPos ++;
            }
        }
        
        public TokenIterator createRecovery() {
            return new TokenIteratorImpl(curPos);
        }

        public boolean hasNext() {
            return curPos < tokens.size() - 1;
        }
    }                                                             
}
