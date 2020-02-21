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


package org.netbeans.modules.cnd.asm.model.xml;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xml.sax.Attributes;

import org.netbeans.modules.cnd.asm.model.lang.BitWidth;
import org.netbeans.modules.cnd.asm.model.lang.instruction.Instruction;
import org.netbeans.modules.cnd.asm.model.lang.Register;
import org.netbeans.modules.cnd.asm.model.lang.instruction.InstructionArgs;
import static org.netbeans.modules.cnd.asm.model.xml.ModelXMLAttributes.*;
import org.openide.util.Pair;

public class ModelXMLRootContext implements XMLReaderContext {
        
    private String asmName;
    private final List<Instruction> instrs;
    private final List<Register> regs;
    
    public ModelXMLRootContext() {
        instrs = new LinkedList<Instruction>();
        regs = new LinkedList<Register>();
    }    
             
    public List<Instruction> getInstructions() {
        return instrs;                
    }
    
    public List<Register> getRegisters() {
        return regs;
    }
    
    private String checkForName(Attributes attributes, String attribute) {
        String res = attributes.getValue(attribute);
        
        if (res == null)
            throw new ModelXMLReaderException("Can't find attribute: " + attribute); //NOI18N
        
        return res;
    }
    
    private Map<String, String> copyAttribute(Attributes attributes, String ...excl) {
        int count = attributes.getLength();
        Map<String, String> res = new HashMap<String, String>(count);
                
   L1: for (int i = 0; i < count; i++) {
            String name = attributes.getQName(i);
            for (String el : excl) {
                if(name.equals(el))
                    continue L1;
            }
            
           res.put(name, attributes.getValue(i));            
        }
   
        return res;
    }
    
    public XMLReaderContext assemblerStart(Attributes attributes) {
        asmName = attributes.getValue(ASM_NAME_ATTRIBUTE);
        return this;
    }      
    
    public XMLReaderContext register_groupStart(Attributes attributes) {                
        return new RegisterGroupContext(attributes);  
    } 
    
    public void register_groupEnd(RegisterGroupContext ctx) {                
        regs.addAll(ctx.makeRegisters());
    } 
    
    public XMLReaderContext instruction_groupStart(Attributes attributes) {
        return new InstructionGroupContext(attributes);        
    }       
    
    private class RegisterGroupContext implements XMLReaderContext {
        private final String groupName;
        private final List<RegisterContext> regCtxs;
        
        public RegisterGroupContext(Attributes attributes) {
            this.groupName = checkForName(attributes, REG_NAME_ATTRIBUTE);
            this.regCtxs = new LinkedList<RegisterContext>();
        }
        
        public List<Register> makeRegisters() {
            List<Register> res = new ArrayList<Register>(regCtxs.size());
            
            RegisterMakeHelper helper = new RegisterMakeHelper();
            for (RegisterContext ctx: regCtxs) {
                XMLBaseRegister reg = helper.process(ctx);
                
                for (Map.Entry<String, String> attr : ctx.getAttributes().entrySet()) {
                    reg.setProperty(attr.getKey(), attr.getValue());
                }
                
                res.add(reg);
            }
            
            return res;
        }
        
        public XMLReaderContext registerStart(Attributes attributes) {                         
             return new RegisterContext(attributes);
        }    
        
        public void registerEnd(RegisterContext ctx) {
            regCtxs.add(ctx);
        }
        
        private class RegisterMakeHelper {            
            private final Set<RegisterContext> inProcesses;
            private final XMLBaseRegister []results;
            
            public RegisterMakeHelper() {
                this.inProcesses = new HashSet<RegisterContext>();
                this.results = new XMLBaseRegister[regCtxs.size()];                                
            }
            
            public XMLBaseRegister process(RegisterContext ctx) {                 
                int idx = regCtxs.indexOf(ctx);
                
                if (results[idx] == null) {
                     if (inProcesses.add(ctx)) {
                         // TODO: check if we need to throw the exception
                         //new ModelXMLReaderException("Cyclic dependence for register:  " + ctx.getName()); //NOI18N
                     }
                     
                     XMLBaseRegister result = new DefaultXMLBaseRegister(ctx.getName(), ctx.getWidth());
                     Set<Register> children = new HashSet<Register>();
                     for (Pair<String, Integer> pair : ctx.getChildren()) {
                         XMLBaseRegister res = process(getForName(pair.first()));
                         children.addAll(res.getChildren());
                         children.add(res);
                         res.setDirectParent(result);
                     }
                     
                     result.setChildren(children);
                     results[idx] = result;
                 }          
                 
                 return results[idx];
            }
            
            private RegisterContext getForName(String name) {
                for (RegisterContext ctx: regCtxs) {
                    if (ctx.getName().equals(name)) {
                        return ctx;
                    }
                }
                
                throw new ModelXMLReaderException("Undefined register:  " + name); //NOI18N
            }            
        }
        
        private class RegisterContext implements XMLReaderContext {
            private final String name;
            private final BitWidth width;
            private final List<Pair<String, Integer>> children;
            
            private Map<String, String> attr;
            
            public RegisterContext(Attributes attributes) {
                this.name = checkForName(attributes, REG_NAME_ATTRIBUTE);  
                String widthStr = checkForName(attributes, REG_BITWIDTH_ATTRIBUTE);
              
                this. width = BitWidth.getBitWidth(Integer.parseInt(widthStr));
                
                this.children = new LinkedList<Pair<String, Integer>>();
                this.attr = new HashMap<String, String>();
                                
                // Copy attributes (purpose: SAX may change attributes on next iteration)                
                attr = copyAttribute(attributes, REG_NAME_ATTRIBUTE, 
                                                 REG_BITWIDTH_ATTRIBUTE);                
            }
            
            public XMLReaderContext childStart(Attributes attributes) {
                String name = checkForName(attributes, REG_NAME_ATTRIBUTE); 
                String offset = checkForName(attributes, REG_OFFSET_ATTRIBUTE);
                
                children.add(Pair.<String, Integer>of(name, Integer.parseInt(offset)));
                return null;
            }
            
            public String getName() {
                return name;                
            }
            
            public BitWidth getWidth() {
                return width;
            }
            
            public Map<String, String> getAttributes() {
                return attr;
            }
            
            public List<Pair<String, Integer>> getChildren() {
                return children;
            }
        }
    }
        
    private class InstructionGroupContext implements XMLReaderContext {
        public String groupName; 
        
        public InstructionGroupContext(Attributes attributes) {
             this.groupName = checkForName(attributes, INSTR_NAME_ATTRIBUTE);
        }
        
        public XMLReaderContext instructionStart(Attributes attributes) {
            return new InstructionContext(attributes);
        }
        
        public void instructionEnd(InstructionContext ctx) {
            instrs.add(ctx.makeInstruction());
        }
        
        class InstructionContext implements XMLReaderContext {
            private final String name;
            private String clazz;
            private final String descr;
            // Other, optional, attributes
            private Map<String, String> attr;
            private final List<InstructionArgs> arguments;
            
            public InstructionContext(Attributes attributes) {
                this.name = checkForName(attributes, INSTR_NAME_ATTRIBUTE);              
                this.clazz = checkForName(attributes, INSTR_CLAZZ_ATTRIBUTE);
                this.descr = checkForName(attributes, INSTR_DESCR_ATTRIBUTE);
                                                
                this.attr = new HashMap<String, String>();
                this.arguments = new LinkedList<InstructionArgs>();
                
                // Copy attributes (purpose: SAX may change attributes on next iteration)                
                attr = copyAttribute(attributes, INSTR_NAME_ATTRIBUTE, INSTR_CLAZZ_ATTRIBUTE,
                                                 INSTR_DESCR_ATTRIBUTE);
            }
            
            public XMLReaderContext argStart(Attributes attributes) {            
                return new ArgContext(attributes) ;
            }
            
            public void argEnd(ArgContext context) {            
                arguments.add(context.getArgument());
            }
            
            public Instruction makeInstruction() {
                final String defClazz = 
                         "org.netbeans.modules.cnd.asm.model.xml.DefaultXMLBaseInstruction";   //NOI18N
                
                XMLBaseInstruction instr = null;
                
                if (true) {//clazz.equals("")) {
                    clazz = defClazz;
                }
                
                try {                                        
                    Class<?> cl = Class.forName(clazz);                       
                    if (!XMLBaseInstruction.class.isAssignableFrom(cl)) {
                        throw new Exception(clazz + " is't super class of " + defClazz);   //NOI18N
                    }                    
                    Constructor<?> constr = cl.getConstructor(String.class, String.class, String.class, 
                                                              Collection.class); 
                                            
                    instr = (XMLBaseInstruction) constr.newInstance(name, descr, groupName, arguments);                    
                    
                } catch (Exception ex) {                                        
                    throw new ModelXMLReaderException(ex.getMessage());                    
                }
                                            
                for (Map.Entry<String, String> el : attr.entrySet()) {
                    instr.setProperty(el.getKey(), el.getValue());                    
                }
                
                return instr;
            }
            
            private class ArgContext implements XMLReaderContext {
                private final String descr;
                private final List<String> params;
                
                public ArgContext(Attributes attributes) {
                    this.descr = checkForName(attributes, INSTR_DESCR_ATTRIBUTE);
                    this.params = new LinkedList<String>();
                }
                
                public XMLInstructionArgs getArgument() {
                    return new XMLInstructionArgs(params, descr);
                }
                
                public XMLReaderContext paramStart(Attributes attributes) {
                    String val = checkForName(attributes, INSTR_ARG_VAL_ATTRIBUTE);
                    params.add(val);
                    return null;
                }                                
            }
        }
    }
}
