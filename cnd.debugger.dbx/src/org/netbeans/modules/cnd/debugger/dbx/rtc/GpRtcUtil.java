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
