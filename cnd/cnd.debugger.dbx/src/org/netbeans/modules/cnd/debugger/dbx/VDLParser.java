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


package org.netbeans.modules.cnd.debugger.dbx;

import org.openide.ErrorManager;

import com.sun.tools.swdev.lisp.*;

class VDLParser {
   static class MyLispBox extends LispBox {
	final LispVal key_vdl = intern(":vdl"); // NOI18N
	final LispVal key_version = intern(":version"); // NOI18N
	final LispVal key_root = intern(":root"); // NOI18N
	final LispVal key_code = intern(":code"); // NOI18N
	final LispVal key_id = intern(":id"); // NOI18N
	final LispVal key_derefid = intern(":derefid"); // NOI18N
	final LispVal key_ltype = intern(":ltype"); // NOI18N
	final LispVal key_qual = intern(":qual"); // NOI18N
	final LispVal key_addr = intern(":addr"); // NOI18N
	final LispVal key_lang = intern(":lang"); // NOI18N
	final LispVal key_pid = intern(":pid"); // NOI18N
	final LispVal key_time = intern(":time"); // NOI18N
	final LispVal key_delta = intern(":delta"); // NOI18N
	final LispVal key_isopen = intern(":isopen"); // NOI18N
	final LispVal key_rflg = intern(":rflg"); // NOI18N
	final LispVal key_ch = intern(":ch"); // NOI18N
	final LispVal key_aggr = intern(":aggr"); // NOI18N
	final LispVal key_smpl = intern(":smpl"); // NOI18N
	final LispVal key_array = intern(":array"); // NOI18N
	final LispVal key_count = intern(":count"); // NOI18N
	final LispVal key_eptr = intern(":eptr"); // NOI18N
	final LispVal key_shape = intern(":shape"); // NOI18N
	final LispVal key_sep_comma = intern(":sep,"); // NOI18N
	final LispVal key_sep_semicolon = intern(":sep-semicolon"); // NOI18N
	final LispVal key_action = intern(":action"); // NOI18N
	final LispVal key_set = intern(":set"); // NOI18N
	final LispVal key_deref = intern(":deref"); // NOI18N
	final LispVal key_hint = intern(":hint"); // NOI18N
	final LispVal key_note = intern(":note"); // NOI18N
	final LispVal key_member = intern(":member"); // NOI18N
	final LispVal key_identifier = intern(":identifier"); // NOI18N
	final LispVal key_value = intern(":value"); // NOI18N
	final LispVal key_tag = intern(":tag"); // NOI18N
	final LispVal key_smplval = intern(":smplval"); // NOI18N
	final LispVal key_flen = intern(":flen"); // NOI18N
	final LispVal key_comment = intern(":comment"); // NOI18N
	final LispVal key_offset = intern(":offset"); // NOI18N
	final LispVal key_size = intern(":size"); // NOI18N
	final LispVal key_sval = intern(":sval"); // NOI18N
	final LispVal key_eval = intern(":eval"); // NOI18N
    };
    class Format {
        Format() {
            flen = 0;
            f_array_index_id_on = false;
        }
        int flen;
        boolean f_array_index_id_on;
    }

    class Dim {
        String str;
        int lo;
        int hi;
        int stride;
        int cur;
    }

    class Dimensions {
        Dimensions() {
            for (int dx = 0; dx < 10; dx++)
                dim[dx] = new Dim();
            ndim = 0;
        }

        void fill(LispVal dinfo) throws LispException {
            ndim = dinfo.car().numberValue();
            dinfo = dinfo.cdr();
            for (int dx = 0; dx < ndim; dx++) {
                LispVal rinfo = dinfo.car();
                Dim d = dim[dx];
                d.lo = rinfo.car().numberValue();
                d.cur = d.lo;
                d.hi = rinfo.cadr().numberValue();
                if (rinfo.cddr() != null) {
                    d.stride = rinfo.cadr().numberValue();
                } else {
                    d.stride = 0;      // yes, 0. it's used as a test later
                }
                dinfo = dinfo.cdr();
            }
        }

        void inc_index(int i) {
          dim[i].cur += dim[i].stride;
          if (dim[i].stride > 0) {
            if (dim[i].cur > dim[i].hi) {
                dim[i].cur = dim[i].lo;
                inc_index(i+1);
            }
          } else if (dim[i].stride < 0) {
            // for negative stride
            // the stride field could be set to 0 by Dimensions::fill,
            // this else clause is for negative stride only
            if (dim[i].cur < dim[i].hi) {
                dim[i].cur = dim[i].lo;
                inc_index(i+1);
            }
          }
        }

        Dim dim[] = new Dim[10];
        int ndim;
    }


    private static final MyLispBox box = new MyLispBox();

    private final VDLActions acts;
    private int version;

    public VDLParser(VDLActions acts) {
	this.acts = acts;
    }

    public void parse(String vdl_raw) {
	if (vdl_raw == null ||
	    vdl_raw.startsWith("<not active>") || // NOI18N
	    vdl_raw.startsWith("<none>") ) { // NOI18N

	    return;
	}

	LispDocument doc = new LispDocument(box);
	try {
	    doc.fill(vdl_raw);
	} catch(Exception x) {
	    ErrorManager.getDefault().annotate(x, "Bad Lisp VDL"); // NOI18N
	    ErrorManager.getDefault().notify(x);
	}

	LispVal lv = doc.root();

	try {
	    if (lv != null && lv.car() == box.key_vdl) {
		if (Log.VDL.debug) {
		    LispVal.pp(lv);    // dump VDL tree
		}
		version = lv.cdr().car().cadr().numberValue();
		if (version == 4)
		    parse_vdl2(lv.cdr());
		else if (version == 3)
		    parse_vdl2(lv.cdr());
		else if (version == 2)
		    parse_vdl2(lv.cdr());
		else
		    error("Invalid vdl lisp version " + version); // NOI18N
	    } else {
		error("Invalid vdl "); // NOI18N
	    }
	} catch (LispException x) {
	    final String msg = "VDLParser.parse(): Couldn't format VDL -- Lisp error -- " + x.getMessage();
	    ErrorManager.getDefault().annotate(x, msg);
	    ErrorManager.getDefault().notify(x);
	    if (org.netbeans.modules.cnd.debugger.common2.debugger.Log.Variable.ctx) {
		LispVal.pp(lv);
		System.out.println();
		System.out.printf("\n\nRaw VDL:\n'%s'\n", vdl_raw); // NOI18N
	    }
	}
	doc.delete();
    }

    private void error(String msg) {
	final String info = "VDL: " + msg; // NOI18N
	ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, info);
    }

    private void parse_vdl2(LispVal vdl) throws LispException {
	for (; vdl != null; vdl = vdl.cdr()) {
	    LispVal item = vdl.car();
	    if (item.car() == box.key_version) {
		version = item.cadr().numberValue();
	    } else if (item.car() == box.key_root) {
		parse_root(item.cdr());
	    }  else {
		if (Log.VDL.debug)
		    error("vdl: Expected :version|:root but got " + item.car()); // NOI18N
	    }
	}
    }

    private void parse_root(LispVal root) throws LispException {
	LispVal code;
	boolean is_const = false;

	for (; root != null; root = root.cdr()) {
	    LispVal item = root.car();

	    if (item.car() == box.key_code) {
		code = item.cadr();
	    } else if (item.car() == box.key_id) {
		;
	    } else if (item.car() == box.key_ltype) {
		//acts.setType(item.cadr().displayToString(), item.cadr().displayToString());
		;
	    } else if (item.car() == box.key_addr) {
		;
	    } else if (item.car() == box.key_lang) {
		if (item.cadr().displayToString().equals("java")) // NOI18N
		    acts.setJava(true);
	    } else if (item.car() == box.key_pid) {
		;
	    } else if (item.car() == box.key_time) {
		;
	    } else if (item.car() == box.key_delta) {
		if (item.cadr().displayToString().equals("1")) { // NOI18N
		    acts.setDelta(true);
		} else
		    acts.setDelta(false);
	    } else if (item.car() == box.key_rflg) {
		;
	    } else if (item.car() == box.key_ch) {
		parse_ch(item.cdr());
	    } else {
		if (Log.VDL.debug) {
		    error("root: Expected " + // NOI18N
	                  ":code|:id|:ltype|:qual|:addr|:lang|:pid|:time|:delta|:rflg|:ch but got " + // NOI18N
	                  item.car());
		}
	    }
	}
    }

    private void parse_ch(LispVal child) throws LispException {
	for (; child != null; child = child.cdr()) {
	    LispVal ch = child.car();
	    if (ch.car() == box.key_aggr) {
		parse_aggr(ch.cdr());
	    } else if (ch.car() == box.key_smpl) {
		parse_smpl(ch.cdr());
	    } else if (ch.car() == box.key_array) {
		parse_array(ch.cdr());
	    } else {
		if (Log.VDL.debug)
		    error("ch: Expected :aggr|:smpl|:array but got " + ch.car()); // NOI18N
	    } 
	}
    }
    
    private void parse_c_array(Dimensions dims, long count, LispVal elems) throws LispException {

        boolean scalar = false;
        boolean array = false;
            scalar = (elems.car().car() == box.key_smpl);
            array = (elems.car().car() == box.key_array);

        for(; elems != null; elems = elems.cdr()) {
            LispVal elem = elems.car();
            if (elem == box.key_sep_comma) {
            } else if (elem == box.key_sep_semicolon) {
                //if (scalar || array)
                    //at.br();
            } else if (elem.car() == box.key_aggr) {
                parse_aggr(elem.cdr());
            } else if (elem.car() == box.key_array) {
                parse_array(elem.cdr());
            } else if (elem.car() == box.key_smpl) {
                Format fmt = new Format();
                parse_smpl(elem.cdr());
            } else {
		if (Log.VDL.debug)
		    error("c_array (regular): Expected :aggr|:array|:smpl but got " + elem.car()); // NOI18N
            }
        }
    }
    
    private void parse_array2(LispVal array, Dimensions dims,
			      String name, String deref_name,
			      String type, String atype, boolean stat)

			      throws LispException {

	// We've already parsed the :shape, :id, :derefid :ltype 
	// Now should only see :count, :ch ...
	// So the checks for :id etc seem redundant (???)

	int count = 0;
	boolean delta = false;
	boolean isopen = false;

	try {
	    while (array != null) {
		LispVal aitem = array.car();
		if (aitem.car() == box.key_id) {
		    name = aitem.cadr().displayToString();
		    //if (fmt.flen != 0) // this array is a member
			//render_id(aitem.cadr(), fmt.flen);

		} else if (aitem.car() == box.key_derefid) {
		    deref_name = aitem.cadr().displayToString();

		} else if (aitem.car() == box.key_ltype) {
		    type = aitem.cadr().displayToString();
		    atype = aitem.cadr().displayToString();

		} else if (aitem.car() == box.key_offset) {
		    ;

		} else if (aitem.car() == box.key_delta) {
		    if (aitem.cadr().displayToString().equals("1")) { // NOI18N
			delta = true;
		    } else
			delta = false;

		} else if (aitem.car() == box.key_isopen) {
		    if (aitem.cadr().displayToString().equals("1")) { // NOI18N
			isopen = true;
		    } else
			isopen = false;

		} else if (aitem.car() == box.key_size) {
		    ;

		} else if (aitem.car() == box.key_eptr) {
		    ;

		} else if (aitem.car() == box.key_shape) {
		    LispVal dims_info = aitem.cadr();
		    dims.fill(dims_info);
		    ;

		} else if (aitem.car() == box.key_count) {
		    count = aitem.cadr().numberValue();

		} else if (aitem.car() == box.key_ch) {
		    acts.startAggregate(name, deref_name, type, atype, delta, stat, isopen);
		    // (:ch <ch>)
		    LispVal children = aitem.cdr();
		    parse_c_array(dims, count, children);
		    // CR 6384274, invalid type displayed in local view for array of struct
		    acts.setType(type, atype);
		    acts.endAggregate();

		} else {
		    if (Log.VDL.debug) {
			error("array2: Expected :colm|:id|:ltype|:qual|:size|:offset|:shape|:count|:eptr|:ch but got " + aitem.car()); // NOI18N
		    }
		}
		array = array.cdr();
	    }
	} catch (LispException x) {
	    final String msg = "VDLParser.parse_array2(): Couldn't format VDL -- Lisp error -- " + x.getMessage();
	    ErrorManager.getDefault().annotate(x, msg);
	    ErrorManager.getDefault().notify(x);
	}
    }
    
    private void parse_array(LispVal array) throws LispException {

        Dimensions dims = new Dimensions();
	String name = null;
	String deref_name = null;
	String type = null;
	String atype = null;
	boolean stat = false;

        while (array != null) {
            LispVal aitem = array.car();
            if (aitem.car() == box.key_id) {
		name = aitem.cadr().displayToString();
                //if (fmt.flen != 0) // this array is a member
                    //render_id(aitem.cadr(), fmt.flen);

            } else if (aitem.car() == box.key_derefid) {
		deref_name = aitem.cadr().displayToString();

            } else if (aitem.car() == box.key_ltype) {
		type = aitem.cadr().displayToString();
		atype = aitem.cadr().displayToString();

            } else if (aitem.car() == box.key_qual) {
		// e.g. (:qual "static")
		LispVal qlist = aitem.cdr();
		while (qlist != null) {
		    LispVal q = qlist.car();
		    if (q.displayToString().equals("static")) // NOI18N
			stat = true;
		    qlist = qlist.cdr();
		}

            } else if (aitem.car() == box.key_offset) {
                ;
	    } else if (aitem.car() == box.key_delta) {
	    /*
		if (aitem.cadr().displayToString().equals("1")) {
		    acts.setDelta(true);
		} else
		    acts.setDelta(false);
            */
                ;
	    } else if (aitem.car() == box.key_size) {
                ;
            } else if (aitem.car() == box.key_eptr) {
                ;
            } else if (aitem.car() == box.key_shape) {
                LispVal dims_info = aitem.cadr();
                dims.fill(dims_info);
                ;
            } else if (aitem.car() == box.key_count) {
                parse_array2(array, dims, name, deref_name, type, atype, stat);
                break;
            } else {
		if (Log.VDL.debug) {
                    error("array: Expected :colm|:id|:ltype|:qual|:size|:offset|:shape|:count|:eptr|:ch but got " + // NOI18N
                    aitem.car());
		}
            }
            array = array.cdr();
        }
    }

    private void parse_aggr(LispVal aggr) throws LispException {
	String name = null;
	String deref_name = null;
	String type = null;
	String atype = null;
	boolean stat = false;
	boolean delta = false;
	boolean isopen = false;
	boolean no_ch = true;
	boolean has_inherit = false;

	for (aggr = aggr.cdr(); aggr != null; aggr = aggr.cdr()) {
	    LispVal aitem = aggr.car();
	    if (aitem.car() == box.key_id) {
		name = aitem.cadr().displayToString();
	    } else if (aitem.car() == box.key_derefid) {
		deref_name = aitem.cadr().displayToString();
	    } else if (aitem.car() == box.key_ltype) {
		atype = type = aitem.cadr().displayToString();
		//acts.setType(atype, type);

            } else if (aitem.car() == box.key_qual) {
		// e.g. (:qual "static")
		LispVal qlist = aitem.cdr();
		while (qlist != null) {
		    LispVal q = qlist.car();
		    if (q.displayToString().equals("static")) // NOI18N
			stat = true;
		    qlist = qlist.cdr();
		}
	    } else if (aitem.car() == box.key_flen) {
	    } else if (aitem.car() == box.key_comment) {
	    } else if (aitem.car() == box.key_offset) {
	    } else if (aitem.car() == box.key_delta) {
		if (aitem.cadr().displayToString().equals("1")) // NOI18N
		    delta = true;
		else
		    delta = false;
	    } else if (aitem.car() == box.key_isopen) {
		if (aitem.cadr().displayToString().equals("1")) // NOI18N
		    isopen = true;
		else
		    isopen = false;

	    } else if (aitem.car() == box.key_size) {

	    } else if (aitem.car() == box.key_ch) {
		// Is it the case that we might get a whole series
		// of (:ch <ch>)'s, one per inherited aggregate?

		no_ch = false;
		if (!has_inherit)
		    acts.startAggregate(name, deref_name, type, atype, stat, delta, isopen);
		parse_ch(aitem.cdr());
		// CR 6189942, for c++ inherit members
		// moved out of the for loop
		//acts.endAggregate();
		has_inherit = true;
	    } else {
		if (Log.VDL.debug) {
		    error("aggr: Expected :id|:ltype|:qual|:offset|:size|:ch but got " + aitem.car()); // NOI18N
		}
	    }
	} // for loop

	if (no_ch) {
	    // CR 6871201, member struct is missing if no child
	    // still needs to create a var for struct member struct
	    acts.newSmplval(name, deref_name,
			    type, atype, stat,
			    " ", null, null, null, delta); //NOI18N
	    // CR 5032536, empty struct show <unset type> in local view
	    // acts.setType(type, atype);
	    // CR 4925431, 6218025, struct with no child is not expandable
	    // CR 6853316, presence of a std::map member causes this to not expand
	    //acts.setLeaf(true); 
	    return;
        }
	// CR 6384274, invalid type displayed in local view for array of struct
	acts.setType(type, atype);
	// CR 6189942, for c++ inherit members
	acts.endAggregate();
    }

    private void parse_smpl(LispVal smpl) throws LispException {
	String hint = null;
	String deref_action = null;
	String set_str = null;
	String name = null;
	String deref_name = null;
	String type = null;
	String atype = null;
	boolean stat = false;
	String value = null;
	boolean delta = false;

	for (; smpl != null; smpl = smpl.cdr()) {
	    LispVal item = smpl.car();
	    if (item.car() == box.key_hint) {
		hint = item.cadr().displayToString();
	    } else if (item.car() == box.key_action) {
		set_str = parse_action(item.cdr(), box.key_set);
		deref_action = parse_action(item.cdr(), box.key_deref);
	    } else if (item.car() == box.key_id) {
		name = item.cadr().displayToString();
	    } else if (item.car() == box.key_derefid) {
		deref_name = item.cadr().displayToString();

	    } else if (item.car() == box.key_ltype) {
		type = item.cadr().displayToString();
		atype = item.cdr().cadr().displayToString();

            } else if (item.car() == box.key_qual) {
		// e.g. (:qual "static")
		LispVal qlist = item.cdr();
		while (qlist != null) {
		    LispVal q = qlist.car();
		    if (q.displayToString().equals("static")) // NOI18N
			stat = true;
		    qlist = qlist.cdr();
		}

	    } else if (item.car() == box.key_sval) {
		value = item.cadr().displayToString();

            } else if (item.car() == box.key_eval) {
	    // CR 6825138, also show numerical value of enum
                if (value != null) {
                    value += "  "; // NOI18N
                    value += item.cadr().displayToString();
		}

	    } else if (item.car() == box.key_delta) {
		if (item.cadr().displayToString().equals("1")) // NOI18N
		    delta = true;
		else
		    delta = false;
	    }
	    // other stuff LATER
	}

	acts.newSmplval(name, deref_name,
			type, atype, stat,
			value, set_str, deref_action, hint, delta);
    }

    private String parse_action(LispVal aitem, LispVal action) throws LispException {
	String action_str = null;

	for (; aitem != null; aitem = aitem.cdr()) {
	    LispVal a = aitem.car();
	    LispVal action_type = a.cadr();

	    if (a.car() == action && a.car() == box.key_set) {
		action_str = action_type.displayToString();
	    } else if (a.car() == action && a.car() == box.key_deref) {
		action_str = action_type.displayToString();
	    }
	}
	return action_str;
    }
};
