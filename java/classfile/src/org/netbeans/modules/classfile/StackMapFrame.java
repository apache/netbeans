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

package org.netbeans.modules.classfile;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * A stack map frame, as defined by a StackMapTable attribute.  A stack map
 * frame is defined by the Java Virtual Machine Specification, section 4.8.4,
 * as a C-like union of stack frame descriptions.  To map this union to Java
 * classes, this class is abstract and has a separate public subclass for each
 * union member.  The stack map frame type can be determined either by the
 * its <code>frame_type</code> or using an instanceof test.
 *
 * @author tball
 */
public abstract class StackMapFrame {
    int frameType;
    
    static StackMapFrame[] loadStackMapTable(DataInputStream in, ConstantPool pool) 
      throws IOException {
        int n = in.readUnsignedShort();
        StackMapFrame[] entries = new StackMapFrame[n];
        for (int i = 0; i < n; i++) {
            int tag = in.readUnsignedByte();
            StackMapFrame frame;
            if (tag >= 0 && tag <= 63)
                frame = new SameFrame(tag);
            else if (tag >= 64 && tag <= 127) {
                VerificationTypeInfo typeInfo = 
                    VerificationTypeInfo.loadVerificationTypeInfo(in, pool);
                frame = new SameLocals1StackItemFrame(tag, typeInfo);
            }
            else if (tag >= 128 && tag <= 246)
                throw new InvalidClassFormatException("reserved stack map frame tag used: " + tag);
            else if (tag == 247) {
                int offset = in.readUnsignedShort();
                VerificationTypeInfo typeInfo = 
                    VerificationTypeInfo.loadVerificationTypeInfo(in, pool);
                frame = new SameLocals1StackItemFrameExtended(tag, offset, typeInfo);
            }
            else if (tag >= 248 && tag <= 250) {
                int offset = in.readUnsignedShort();
                frame = new ChopFrame(tag, offset);
            }
            else if (tag == 251) {
                int offset = in.readUnsignedShort();
                frame = new SameFrameExtended(tag, offset);
            }
            else if (tag >= 252 && tag <= 254)
                frame = makeAppendFrame(tag, in, pool);
            else /* tag == 255 */
                frame = makeFullFrame(in, pool);
            entries[i] = frame;
        }
        return entries;
    }
    
    private static AppendFrame makeAppendFrame(int tag, DataInputStream in, ConstantPool pool) 
      throws IOException {
        int offset = in.readUnsignedShort();
        VerificationTypeInfo[] locals = new VerificationTypeInfo[tag - 251];
        for (int i = 0; i < locals.length; i++)
            locals[i] = VerificationTypeInfo.loadVerificationTypeInfo(in, pool);
        return new AppendFrame(tag, offset, locals);
    }
    
    private static FullFrame makeFullFrame(DataInputStream in, ConstantPool pool) 
      throws IOException {
        int offset = in.readUnsignedShort();
        int n = in.readUnsignedShort();
        VerificationTypeInfo[] locals = new VerificationTypeInfo[n];
        for (int i = 0; i < locals.length; i++)
            locals[i] = VerificationTypeInfo.loadVerificationTypeInfo(in, pool);
        n = in.readUnsignedShort();
        VerificationTypeInfo[] stackItems = new VerificationTypeInfo[n];
        for (int i = 0; i < stackItems.length; i++)
            stackItems[i] = VerificationTypeInfo.loadVerificationTypeInfo(in, pool);
        return new FullFrame(255, offset, locals, stackItems);
    }
    
    /** Creates new StackMapFrame */
    StackMapFrame(int tag) {
        frameType = tag;
    }

    /**
     * Returns the frame_type for this frame.  As documented in the JVM specification,
     * different tag ranges define different frame_type values.
     * @return frame_type for the frame
     */
    public final int getFrameType() {
        return frameType;
    }
    
    /**
     * Returns the <code>offset_delta</code> for this frame type.  From the 
     * Java Virtual Machine Specification, section 4.8.4:
     * <br>
     * "Each stack_map_frame structure specifies the type state at a particular 
     * byte code offset. Each frame type specifies (explicitly or implicitly) a 
     * value, <code>offset_delta</code>, that is used to calulate the actual byte 
     * code offset at which it applies. The byte code offset at which the frame 
     * applies is given by adding <code>1 + offset_delta</code> to the offset of 
     * the previous frame, unless the previous frame is the initial frame of
     * the method, in which case the byte code offset is <code>offset_delta</code>."
     * @return offset_delta for this frame type
     */
    public abstract int getOffsetDelta();
        
   /**
     * A frame type of <code>same_frame</code>, which means that the frame 
     * has exactly the same locals as the previous stack map frame and that 
     * the number of stack items is zero.  
     */
    public static final class SameFrame extends StackMapFrame {
        SameFrame(int tag) {
            super(tag);
        }
        
        /**
         * The <code>offset_delta</code> value for the frame is the value 
         * of the tag item, frame_type.
         * @return offset_delta
         */
        public int getOffsetDelta() {
            return frameType;
        }
    }
    
    /**
     * A frame type of <code>same_locals_1_stack_item_frame</code>, which means
     * that the frame has exactly the same locals as the previous stack map 
     * frame and that the number of stack items is 1.  
     */
    public static final class SameLocals1StackItemFrame extends StackMapFrame {
        VerificationTypeInfo typeInfo;
        SameLocals1StackItemFrame(int tag, VerificationTypeInfo typeInfo) {
            super(tag);
            this.typeInfo = typeInfo;
        }
 
        /**
         * The <code>offset_delta</code> value for the frame is the value 
         * <code>(frame_type - 64)</code>.
         * @return offset_delta
         */
        public int getOffsetDelta() {
            return frameType - 64;
        }
        
        /**
         * Returns the verification type info for the single stack item
         * referenced by this frame.
         * @return verification type info
         */
        public VerificationTypeInfo getVerificationTypeInfo() {
            return typeInfo;
        }
    }
    
    /**
     * A frame type of <code>same_locals_1_stack_item_frame_extended</code>,
     * which means that the frame has exactly the same locals as the previous 
     * stack map frame and that the number of stack items is 1.  
     */
    public static final class SameLocals1StackItemFrameExtended extends StackMapFrame {
        int offset;
        VerificationTypeInfo typeInfo;
        SameLocals1StackItemFrameExtended(int tag, int offset, VerificationTypeInfo typeInfo) {
            super(tag);
            this.offset = offset;
            this.typeInfo = typeInfo;
        }
        
        /**
         * Returns the <code>offset_delta</code> for this frame type.
         * @return offset_delta
         */
        public int getOffsetDelta() {
            return offset;
        }
        
        /**
         * Returns the verification type info for the single stack item for 
         * this frame.
         * @return verification type info
         */
        public VerificationTypeInfo getVerificationTypeInfo() {
            return typeInfo;
        }
    }
    
    /**
     * A frame type of <code>chop_frame</code>, which means that the operand 
     * stack is empty and the current locals are the same as the locals in 
     * the previous frame, except that the <i>k</i> last locals are absent. 
     * The value of <i>k</i> is given by the formula <code>251-frame_type</code>.
     */
    public static final class ChopFrame extends StackMapFrame {
        int offset;
        ChopFrame(int tag, int offset) {
            super(tag);
            this.offset = offset;
        }
        
        /**
         * Returns the <code>offset_delta</code> for this frame type.
         * @return offset_delta
         */
        public int getOffsetDelta() {
            return offset;
        }
    }
    
    /**
     * A frame type of <code>same_frame_extended</code>, which means the frame 
     * has exactly the same locals as the previous stack map frame and that the 
     * number of stack items is zero.
     */
    public static final class SameFrameExtended extends StackMapFrame {
        int offset;
        SameFrameExtended(int tag, int offset) {
            super(tag);
            this.offset = offset;
        }
        
        /**
         * Returns the <code>offset_delta</code> for this frame type.
         * @return offset_delta
         */
        public int getOffsetDelta() {
            return offset;
        }
    }
    
    /**
     * A frame type of <code>append_frame</code>, which means that the operand 
     * stack is empty and the current locals are the same as the locals in the 
     * previous frame, except that <i>k</i> additional locals are defined.  The 
     * value of <i>k</i> is given by the formula <code>frame_type-251</code>.
     */
    public static final class AppendFrame extends StackMapFrame {
        int offset;
        VerificationTypeInfo[] locals;
        AppendFrame(int tag, int offset, VerificationTypeInfo[] locals) {
            super(tag);
            this.offset = offset;
            this.locals = locals;
        }
        
        /**
         * Returns the <code>offset_delta</code> for this frame type.
         * @return offset_delta
         */
        public int getOffsetDelta() {
            return offset;
        }
        
        /**
         * Returns the verification type info for this frame's set of
         * locals.
         * @return verification type info
         */
        public VerificationTypeInfo[] getLocals() {
            return locals.clone();
        }
    }
    
    /**
     * A frame type of <code>full_frame</code>, which declares all of its
     * locals and stack items.
     */
    public static final class FullFrame extends StackMapFrame {
        int offset;
        VerificationTypeInfo[] locals;
        VerificationTypeInfo[] stackItems;
        FullFrame(int tag, int offset, VerificationTypeInfo[] locals,
                  VerificationTypeInfo[] stackItems) {
            super(tag);
            this.offset = offset;
            this.locals = locals;
            this.stackItems = stackItems;
        }
        
        /**
         * Returns the <code>offset_delta</code> for this frame type.
         * @return offset_delta
         */
        public int getOffsetDelta() {
            return offset;
        }
        
        /**
         * Returns the verification type info for this frame's set of
         * locals.
         * @return verification type info
         */
        public VerificationTypeInfo[] getLocals() {
            return locals.clone();
        }
        
        /**
         * Returns the verification type info for this frame's set of
         * stack items.
         * @return verification type info
         */
        public VerificationTypeInfo[] getStackItems() {
            return stackItems.clone();
        }
    }
}
