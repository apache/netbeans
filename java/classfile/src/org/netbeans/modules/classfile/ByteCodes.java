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
/*
 * Contributor(s): Thomas Ball
 */

package org.netbeans.modules.classfile;

/**
 * Constant definitions for the bytecodes defined in the
 * Java Virtual Machine specification, Chapter 10.
 *
 * @author Thomas Ball
 */
public interface ByteCodes {
    /* JVM Spec, Chapter 10 */
    int bc_nop                      = 0;
    int bc_aconst_null              = 1;
    int bc_iconst_m1                = 2;
    int bc_iconst_0                 = 3;
    int bc_iconst_1                 = 4;
    int bc_iconst_2                 = 5;
    int bc_iconst_3                 = 6;
    int bc_iconst_4                 = 7;
    int bc_iconst_5                 = 8;
    int bc_lconst_0                 = 9;
    int bc_lconst_1                 = 10;
    int bc_fconst_0                 = 11;
    int bc_fconst_1                 = 12;
    int bc_fconst_2                 = 13;
    int bc_dconst_0                 = 14;
    int bc_dconst_1                 = 15;
    int bc_bipush                   = 16;
    int bc_sipush                   = 17;
    int bc_ldc                      = 18;
    int bc_ldc_w                    = 19;
    int bc_ldc2_w                   = 20;
    int bc_iload                    = 21;
    int bc_lload                    = 22;
    int bc_fload                    = 23;
    int bc_dload                    = 24;
    int bc_aload                    = 25;
    int bc_iload_0                  = 26;
    int bc_iload_1                  = 27;
    int bc_iload_2                  = 28;
    int bc_iload_3                  = 29;
    int bc_lload_0                  = 30;
    int bc_lload_1                  = 31;
    int bc_lload_2                  = 32;
    int bc_lload_3                  = 33;
    int bc_fload_0                  = 34;
    int bc_fload_1                  = 35;
    int bc_fload_2                  = 36;
    int bc_fload_3                  = 37;
    int bc_dload_0                  = 38;
    int bc_dload_1                  = 39;
    int bc_dload_2                  = 40;
    int bc_dload_3                  = 41;
    int bc_aload_0                  = 42;
    int bc_aload_1                  = 43;
    int bc_aload_2                  = 44;
    int bc_aload_3                  = 45;
    int bc_iaload                   = 46;
    int bc_laload                   = 47;
    int bc_faload                   = 48;
    int bc_daload                   = 49;
    int bc_aaload                   = 50;
    int bc_baload                   = 51;
    int bc_caload                   = 52;
    int bc_saload                   = 53;
    int bc_istore                   = 54;
    int bc_lstore                   = 55;
    int bc_fstore                   = 56;
    int bc_dstore                   = 57;
    int bc_astore                   = 58;
    int bc_istore_0                 = 59;
    int bc_istore_1                 = 60;
    int bc_istore_2                 = 61;
    int bc_istore_3                 = 62;
    int bc_lstore_0                 = 63;
    int bc_lstore_1                 = 64;
    int bc_lstore_2                 = 65;
    int bc_lstore_3                 = 66;
    int bc_fstore_0                 = 67;
    int bc_fstore_1                 = 68;
    int bc_fstore_2                 = 69;
    int bc_fstore_3                 = 70;
    int bc_dstore_0                 = 71;
    int bc_dstore_1                 = 72;
    int bc_dstore_2                 = 73;
    int bc_dstore_3                 = 74;
    int bc_astore_0                 = 75;
    int bc_astore_1                 = 76;
    int bc_astore_2                 = 77;
    int bc_astore_3                 = 78;
    int bc_iastore                  = 79;
    int bc_lastore                  = 80;
    int bc_fastore                  = 81;
    int bc_dastore                  = 82;
    int bc_aastore                  = 83;
    int bc_bastore                  = 84;
    int bc_castore                  = 85;
    int bc_sastore                  = 86;
    int bc_pop                      = 87;
    int bc_pop2                     = 88;
    int bc_dup                      = 89;
    int bc_dup_x1                   = 90;
    int bc_dup_x2                   = 91;
    int bc_dup2                     = 92;
    int bc_dup2_x1                  = 93;
    int bc_dup2_x2                  = 94;
    int bc_swap                     = 95;
    int bc_iadd                     = 96;
    int bc_ladd                     = 97;
    int bc_fadd                     = 98;
    int bc_dadd                     = 99;
    int bc_isub                     = 100;
    int bc_lsub                     = 101;
    int bc_fsub                     = 102;
    int bc_dsub                     = 103;
    int bc_imul                     = 104;
    int bc_lmul                     = 105;
    int bc_fmul                     = 106;
    int bc_dmul                     = 107;
    int bc_idiv                     = 108;
    int bc_ldiv                     = 109;
    int bc_fdiv                     = 110;
    int bc_ddiv                     = 111;
    int bc_irem                     = 112;
    int bc_lrem                     = 113;
    int bc_frem                     = 114;
    int bc_drem                     = 115;
    int bc_ineg                     = 116;
    int bc_lneg                     = 117;
    int bc_fneg                     = 118;
    int bc_dneg                     = 119;
    int bc_ishl                     = 120;
    int bc_lshl                     = 121;
    int bc_ishr                     = 122;
    int bc_lshr                     = 123;
    int bc_iushr                    = 124;
    int bc_lushr                    = 125;
    int bc_iand                     = 126;
    int bc_land                     = 127;
    int bc_ior                      = 128;
    int bc_lor                      = 129;
    int bc_ixor                     = 130;
    int bc_lxor                     = 131;
    int bc_iinc                     = 132;
    int bc_i2l                      = 133;
    int bc_i2f                      = 134;
    int bc_i2d                      = 135;
    int bc_l2i                      = 136;
    int bc_l2f                      = 137;
    int bc_l2d                      = 138;
    int bc_f2i                      = 139;
    int bc_f2l                      = 140;
    int bc_f2d                      = 141;
    int bc_d2i                      = 142;
    int bc_d2l                      = 143;
    int bc_d2f                      = 144;
    int bc_i2b                      = 145;
    int bc_i2c                      = 146;
    int bc_i2s                      = 147;
    int bc_lcmp                     = 148;
    int bc_fcmpl                    = 149;
    int bc_fcmpg                    = 150;
    int bc_dcmpl                    = 151;
    int bc_dcmpg                    = 152;
    int bc_ifeq                     = 153;
    int bc_ifne                     = 154;
    int bc_iflt                     = 155;
    int bc_ifge                     = 156;
    int bc_ifgt                     = 157;
    int bc_ifle                     = 158;
    int bc_if_icmpeq                = 159;
    int bc_if_icmpne                = 160;
    int bc_if_icmplt                = 161;
    int bc_if_icmpge                = 162;
    int bc_if_icmpgt                = 163;
    int bc_if_icmple                = 164;
    int bc_if_acmpeq                = 165;
    int bc_if_acmpne                = 166;
    int bc_goto                     = 167;
    int bc_jsr                      = 168;
    int bc_ret                      = 169;
    int bc_tableswitch              = 170;
    int bc_lookupswitch             = 171;
    int bc_ireturn                  = 172;
    int bc_lreturn                  = 173;
    int bc_freturn                  = 174;
    int bc_dreturn                  = 175;
    int bc_areturn                  = 176;
    int bc_return                   = 177;
    int bc_getstatic                = 178;
    int bc_putstatic                = 179;
    int bc_getfield                 = 180;
    int bc_putfield                 = 181;
    int bc_invokevirtual            = 182;
    int bc_invokespecial            = 183;
    int bc_invokestatic             = 184;
    int bc_invokeinterface          = 185;
    int bc_xxxunusedxxx             = 186;
    int bc_new                      = 187;
    int bc_newarray                 = 188;
    int bc_anewarray                = 189;
    int bc_arraylength              = 190;
    int bc_athrow                   = 191;
    int bc_checkcast                = 192;
    int bc_instanceof               = 193;
    int bc_monitorenter             = 194;
    int bc_monitorexit              = 195;
    int bc_wide                     = 196;
    int bc_multianewarray           = 197;
    int bc_ifnull                   = 198;
    int bc_ifnonnull                = 199;
    int bc_goto_w                   = 200;
    int bc_jsr_w                    = 201;
}
