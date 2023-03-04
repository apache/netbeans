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
package org.netbeans.modules.java.source.usages;

import java.util.Iterator;
import org.netbeans.modules.classfile.ByteCodes;

/**
 *
 * @author  Petr Hrebejk
 */
public class BytecodeDecoder implements Iterator<byte[]>, Iterable<byte[]> {

    static final int opcodeLengths[] = {
           1,           /* nop */
           1,           /* aconst_null */
           1,           /* iconst_m1 */
           1,           /* iconst_0 */
           1,           /* iconst_1 */
           1,           /* iconst_2 */
           1,           /* iconst_3 */
           1,           /* iconst_4 */
           1,           /* iconst_5 */
           1,           /* lconst_0 */
           1,           /* lconst_1 */
           1,           /* fconst_0 */
           1,           /* fconst_1 */
           1,           /* fconst_2 */
           1,           /* dconst_0 */
           1,           /* dconst_1 */
           2,           /* bipush */
           3,           /* sipush */
           2,           /* ldc */
           3,           /* ldc_w */
           3,           /* ldc2_w */
           2,           /* iload */
           2,           /* lload */
           2,           /* fload */
           2,           /* dload */
           2,           /* aload */
           1,           /* iload_0 */
           1,           /* iload_1 */
           1,           /* iload_2 */
           1,           /* iload_3 */
           1,           /* lload_0 */
           1,           /* lload_1 */
           1,           /* lload_2 */
           1,           /* lload_3 */
           1,           /* fload_0 */
           1,           /* fload_1 */
           1,           /* fload_2 */
           1,           /* fload_3 */
           1,           /* dload_0 */
           1,           /* dload_1 */
           1,           /* dload_2 */
           1,           /* dload_3 */
           1,           /* aload_0 */
           1,           /* aload_1 */
           1,           /* aload_2 */
           1,           /* aload_3 */
           1,           /* iaload */
           1,           /* laload */
           1,           /* faload */
           1,           /* daload */
           1,           /* aaload */
           1,           /* baload */
           1,           /* caload */
           1,           /* saload */
           2,           /* istore */
           2,           /* lstore */
           2,           /* fstore */
           2,           /* dstore */
           2,           /* astore */
           1,           /* istore_0 */
           1,           /* istore_1 */
           1,           /* istore_2 */
           1,           /* istore_3 */
           1,           /* lstore_0 */
           1,           /* lstore_1 */
           1,           /* lstore_2 */
           1,           /* lstore_3 */
           1,           /* fstore_0 */
           1,           /* fstore_1 */
           1,           /* fstore_2 */
           1,           /* fstore_3 */
           1,           /* dstore_0 */
           1,           /* dstore_1 */
           1,           /* dstore_2 */
           1,           /* dstore_3 */
           1,           /* astore_0 */
           1,           /* astore_1 */
           1,           /* astore_2 */
           1,           /* astore_3 */
           1,           /* iastore */
           1,           /* lastore */
           1,           /* fastore */
           1,           /* dastore */
           1,           /* aastore */
           1,           /* bastore */
           1,           /* castore */
           1,           /* sastore */
           1,           /* pop */
           1,           /* pop2 */
           1,           /* dup */
           1,           /* dup_x1 */
           1,           /* dup_x2 */
           1,           /* dup2 */
           1,           /* dup2_x1 */
           1,           /* dup2_x2 */
           1,           /* swap */
           1,           /* iadd */
           1,           /* ladd */
           1,           /* fadd */
           1,           /* dadd */
           1,           /* isub */
           1,           /* lsub */
           1,           /* fsub */
           1,           /* dsub */
           1,           /* imul */
           1,           /* lmul */
           1,           /* fmul */
           1,           /* dmul */
           1,           /* idiv */
           1,           /* ldiv */
           1,           /* fdiv */
           1,           /* ddiv */
           1,           /* irem */
           1,           /* lrem */
           1,           /* frem */
           1,           /* drem */
           1,           /* ineg */
           1,           /* lneg */
           1,           /* fneg */
           1,           /* dneg */
           1,           /* ishl */
           1,           /* lshl */
           1,           /* ishr */
           1,           /* lshr */
           1,           /* iushr */
           1,           /* lushr */
           1,           /* iand */
           1,           /* land */
           1,           /* ior */
           1,           /* lor */
           1,           /* ixor */
           1,           /* lxor */
           3,           /* iinc */
           1,           /* i2l */
           1,           /* i2f */
           1,           /* i2d */
           1,           /* l2i */
           1,           /* l2f */
           1,           /* l2d */
           1,           /* f2i */
           1,           /* f2l */
           1,           /* f2d */
           1,           /* d2i */
           1,           /* d2l */
           1,           /* d2f */
           1,           /* i2b */
           1,           /* i2c */
           1,           /* i2s */
           1,           /* lcmp */
           1,           /* fcmpl */
           1,           /* fcmpg */
           1,           /* dcmpl */
           1,           /* dcmpg */
           3,           /* ifeq */
           3,           /* ifne */
           3,           /* iflt */
           3,           /* ifge */
           3,           /* ifgt */
           3,           /* ifle */
           3,           /* if_icmpeq */
           3,           /* if_icmpne */
           3,           /* if_icmplt */
           3,           /* if_icmpge */
           3,           /* if_icmpgt */
           3,           /* if_icmple */
           3,           /* if_acmpeq */
           3,           /* if_acmpne */
           3,           /* goto */
           3,           /* jsr */
           2,           /* ret */
           99,          /* tableswitch */
           99,          /* lookupswitch */
           1,           /* ireturn */
           1,           /* lreturn */
           1,           /* freturn */
           1,           /* dreturn */
           1,           /* areturn */
           1,           /* return */
           3,           /* getstatic */
           3,           /* putstatic */
           3,           /* getfield */
           3,           /* putfield */
           3,           /* invokevirtual */
           3,           /* invokespecial */
           3,           /* invokestatic */
           5,           /* invokeinterface */
           0,           /* xxxunusedxxx */
           3,           /* new */
           2,           /* newarray */
           3,           /* anewarray */
           1,           /* arraylength */
           1,           /* athrow */
           3,           /* checkcast */
           3,           /* instanceof */
           1,           /* monitorenter */
           1,           /* monitorexit */
           0,           /* wide */
           4,           /* multianewarray */
           3,           /* ifnull */
           3,           /* ifnonnull */
           5,           /* goto_w */
           5,           /* jsr_w */
           1,           /* breakpoint */
           2,           /* ldc_quick */
           3,           /* ldc_w_quick */
           3,           /* ldc2_w_quick */
           3,           /* getfield_quick */
           3,           /* putfield_quick */
           3,           /* getfield2_quick */
           3,           /* putfield2_quick */
           3,           /* getstatic_quick */
           3,           /* putstatic_quick */
           3,           /* getstatic2_quick */
           3,           /* putstatic2_quick */
           3,           /* invokevirtual_quick */
           3,           /* invokenonvirtual_quick */
           3,           /* invokesuper_quick */
           3,           /* invokestatic_quick */
           5,           /* invokeinterface_quick */
           3,           /* invokevirtualobject_quick */
           3,           /* invokeignored_quick */
           3,           /* new_quick */
           3,           /* anewarray_quick */
           4,           /* multianewarray_quick */
           3,           /* checkcast_quick */
           3,           /* instanceof_quick */
           3,           /* invokevirtual_quick_w */
           3,           /* getfield_quick_w */
           3,           /* putfield_quick_w */
           1,           /* nonnull_quick */
           1,           /* exitinterpreter */
           -1,
           -1,
           -1,
           -1,
           -1,
           -1,
           -1,
           -1,
           -1,
           -1,
           -1,
           -1,
           -1,
           -1,
           -1,
           -1,
           -1,
           -1,
           -1,
           -1,
           -1,
           -1,
           -1,
           -1,
           -1,
        };
    
    private byte[] code;
    
    int currentIndex;
    
    /** Creates a new instance of BytecodeDecoder */
    public BytecodeDecoder( byte[] code ) {
        this.code = code;
        this.currentIndex = 0;
    }
    
    
    public Iterator<byte[]> iterator() {
        return this;
    }
    
    
    
    public boolean hasNext() {
        if ( currentIndex < code.length ) {
            return true;
        }
        if ( currentIndex != code.length ) {
            throw new IllegalStateException( "Bad end " + currentIndex + " vs. " + code.length );
        }
        return false;
        
    }
        
    public byte[] next() {
        int opCode = toInt( code[currentIndex]);
        int length;
        
        if (opCode == 196) {
            // wide instruction prefix
            // either wide <opcode> byte_1 byte_2 where opcode is iload, fload, aload, lload, dload, istore, fstore, astore, lstore, dstore, or ret
            // or wide iinc index_byte_1 index_byte_2 const_byte_1 const_byte_2
            int wideInstruction = toInt( code[currentIndex+1]);
            switch (wideInstruction) {
                case 132:   //iinc
                    length = 6;
                    break;
                case  21:   //iload
                case  22:   //lload
                case  23:   //fload
                case  24:   //dload
                case  25:   //aload
                case  54:   //istore
                case  55:   //lstore
                case  56:   //fstore
                case  57:   //dstore
                case  58:   //astore
                case  169:  //ret
                    length = 4;
                    break;                                    
                default:
                    throw new IllegalArgumentException ("Bad wide instruction at index " + currentIndex + " wide instruction " + wideInstruction);
            }            
        }
        else {
            length  = opcodeLengths[opCode];
        }
        
        if ( length == -1 ) {
            throw new IllegalArgumentException( "Bad bytecode at index " + currentIndex + " opcode " + opCode);
        }
        
        if ( length == 99 ) {
            switch ( opCode ) {
                case ByteCodes.bc_lookupswitch: {
                    int padd =  4 - ( currentIndex % 4 );
                    int start = currentIndex + padd + 4; 
                    int npairs = toInt(code[start], code[start + 1], code[start + 2], code[start + 3] );
                    length = padd + 8 + npairs * 8;
                    /*
                    System.err.println("CI " + currentIndex );
                    System.err.println("NP " + npairs );
                   System.err.println("ST " + start );
                     */
                    break;
                }
                case ByteCodes.bc_tableswitch: {
                    int padd = 4 - ( currentIndex % 4 );
                    int start = currentIndex + padd + 4;
                    int low = toInt(code[start], code[start + 1], code[start + 2], code[start + 3] );
                    int high = toInt(code[start + 4], code[start + 5], code[start + 6], code[start + 7] );
                    length = padd + 12 + (high - low + 1) * 4;
                    
                    /*
                    System.err.println("CI " + currentIndex );
                    System.err.println("ST " + start );
                    System.err.println("CI " + currentIndex );
                    System.err.println("LO " + low );
                    System.err.println("HI " + high );
                    System.err.println("PD " + padd );
                    System.err.println("E  " + (currentIndex + length) );
                     */
                    break;
                }
                default:
                    throw new IllegalArgumentException( "Bad bytecode at index " + currentIndex );
            }
        }
        
        byte currCode[] = new byte[length];
        for( int i = 0; i < length; i++ ) {
            currCode[i] = code[ currentIndex + i ];
        }
        
        currentIndex += length;
        
        return currCode;        
    }
        
    public void remove() {
        throw new UnsupportedOperationException( "Byte code is read only" );
    }
    
    static int toInt( byte b ) {
        return ((int)b) & 0xFF;
    }
    
    static int toInt( byte b1, byte b2 ) {        
        return ( (toInt(b1) << 8) | toInt(b2) );
    }
    
    static int toInt( byte b1, byte b2, byte b3, byte b4 ) {        
        return (toInt(b1) << 24) | (toInt(b2) << 16) | (toInt(b3) << 8) | toInt(b4);
    }
 
}
