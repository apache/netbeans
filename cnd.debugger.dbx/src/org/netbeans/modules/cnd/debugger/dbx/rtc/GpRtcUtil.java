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

package org.netbeans.modules.cnd.debugger.dbx.rtc;

import com.sun.tools.swdev.glue.dbx.*;

public class GpRtcUtil {
    public static RtcModel.Frame rtcFrame(GPDbxFrame gframe) {
	return new RtcModel.Frame(gframe.frameno,
				  gframe.func,
				  gframe.args,
				  gframe.source,
				  gframe.lineno,
				  gframe.pc);
    }

    private static RtcModel.Stack rtcStack(RtcModel model, GPDbxStack gstack) {
	RtcModel.Stack rstack = model.newStack(gstack.nframes);
	for (int fx = 0; fx < gstack.nframes; fx++)
	    rstack.setFrame(fx, rtcFrame(gstack.frame[fx]));
	return rstack;
    }

    private static RtcModel.Location rtcLocation(GPDbxLocation glocation) {
	return new RtcModel.Location(glocation.func,
				     glocation.src,
				     glocation.line,
				     0);
    }

    public static RtcModel.ErrorCode rtcErrorCode(int type) {
	switch (type) {
	    case 0: return RtcModel.ErrorCode.ADDRESS_IN_BLOCK;
	    case 1: return RtcModel.ErrorCode.ADDRESS_IN_REGISTER;
	    case 2: return RtcModel.ErrorCode.BAD_FREE;
	    case 3: return RtcModel.ErrorCode.BLOCK_INUSE;
	    case 4: return RtcModel.ErrorCode.DUPLICATE_FREE;
	    case 5: return RtcModel.ErrorCode.MEMORY_LEAK;
	    case 6: return RtcModel.ErrorCode.MISALIGNED_FREE;
	    case 7: return RtcModel.ErrorCode.MISALIGNED_READ;
	    case 8: return RtcModel.ErrorCode.MISALIGNED_WRITE;
	    case 9: return RtcModel.ErrorCode.OUT_OF_MEMORY;
	    case 10: return RtcModel.ErrorCode.READ_FROM_UNALLOCATED;
	    case 11: return RtcModel.ErrorCode.READ_FROM_UNINITIALIZED;
	    case 12: return RtcModel.ErrorCode.WRITE_TO_READ_ONLY;
	    case 13: return RtcModel.ErrorCode.WRITE_TO_UNALLOCATED;
	    case 14: return RtcModel.ErrorCode.WRITE_TO_OUT_OF_BOUND;
	    case 15: return RtcModel.ErrorCode.READ_FROM_OUT_OF_BOUND;
	    default: return RtcModel.ErrorCode.UNKNOWN;
	}
    }

    public static RtcModel.AccessError accessError(RtcModel rtcModel,
						   GPDbxRtcItem item) {
	return new RtcModel.AccessError(rtcErrorCode(item.type),
				        item.buf,
				        item.addr,
				        item.size,
				        item.region,
				        rtcStack(rtcModel, item.stack),
				        rtcLocation(item.err_location),
				        item.var_name);
    }

    public static RtcModel.MemoryReportHeader reportHeader(GPDbxMprofHeader header) {
	return new RtcModel.MemoryReportHeader(rtcErrorCode(header.type),
					       header.buf,
					       header.match,
					       header.num_rec,
					       header.verbose,
					       header.all,
					       header.total_num,
					       header.total_bytes);
    }

    public static RtcModel.MemoryReportItem leakItem(RtcModel rtcModel,
						     GPDbxMprofItem item) {
	return new RtcModel.MemoryReportItem(rtcErrorCode(item.type),
				             item.buf,
				             item.count,
				             item.addr,
				             item.size,
				             item.percentage,
				             rtcStack(rtcModel, item.stack));
    }

    public static RtcModel.MemoryReportItem useItem(RtcModel rtcModel,
						    GPDbxMprofItem item) {

	return new RtcModel.MemoryReportItem(rtcErrorCode(item.type),
				             item.buf,
				             item.count,
				             item.addr,
				             item.size,
				             item.percentage,
				             rtcStack(rtcModel, item.stack));
    }
}
