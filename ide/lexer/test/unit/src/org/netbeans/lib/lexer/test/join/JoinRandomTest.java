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

package org.netbeans.lib.lexer.test.join;

import java.util.logging.Level;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.lang.TestJoinTopTokenId;
import org.netbeans.lib.lexer.test.FixedTextDescriptor;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.lib.lexer.test.RandomCharDescriptor;
import org.netbeans.lib.lexer.test.RandomModifyDescriptor;
import org.netbeans.lib.lexer.test.RandomTextProvider;
import org.netbeans.lib.lexer.test.TestRandomModify;

/**
 * Test join updating algorithm TokenListUpdater.updateJoined() by random document modifications.
 *
 * @author mmetelka
 */
public class JoinRandomTest extends NbTestCase {

    public JoinRandomTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        LexerTestUtilities.setTesting(true);
    }

    protected void tearDown() throws java.lang.Exception {
    }

    @Override
    protected Level logLevel() {
        return Level.INFO;
//        return super.logLevel();
    }

    public void testRandom() throws Exception {
        test(1214497020179L);
//        test(1212495582649L);
//        test(0L);
    }
    
    private void test(long seed) throws Exception {
        TestRandomModify randomModify = new TestRandomModify(seed);
        randomModify.setLanguage(TestJoinTopTokenId.language());

//        randomModify.setStartDebugOpCount(7967);
//        randomModify.setDebugOperation(true);
//        randomModify.setDebugDocumentText(true);
//        randomModify.setDebugHierarchy(true);
//
//        Logger.getLogger(org.netbeans.lib.lexer.inc.TokenListUpdater.class.getName()).setLevel(Level.FINE); // Extra logging
//        Logger.getLogger(TokenHierarchyOperation.class.getName()).setLevel(Level.FINEST);
//        Logger.getLogger(LexerInput.class.getName()).setLevel(Level.FINEST);

        // Certain explicit cases that caused errors to be found follow:
        //                          000000000011111111112222222222333333333344444444445555555555
        //                          012345678901234567890123456789012345678901234567890123456789
        randomModify.insertText(0, "<><Bj><[><]>}}(J)>QO]v><[[{x}>]vU()jbO<`aV]cM}])jw`D<[['E");
        randomModify.removeText(20, 6);
        randomModify.clearDocument();

        //                          000000000011111111112222222222333333333344444444445555555555
        //                          012345678901234567890123456789012345678901234567890123456789
        randomModify.insertText(0, "<><Bj><[><]>}}J)]>QO]v><[[>]vU()jHbO<`<BraV]cM}])j`D<>[['E");
        randomModify.removeText(53, 1);
        randomModify.clearDocument();

        randomModify.insertText(0, "{g}WvWq)T}df(W(d>}H}[Q<[>O[]{CR[WB'>t}H[}rOx](da[]U)(>`{F[{}R[c]<]>[]j<({<>" +
                "[`wl<uDD['']C'[bN'{`>)}ZMxx}[<[y]`[r]x}[y]Bi<`<>vs<[>r[)<F>}}`(y>['DC`{x}{Dz[zJjLx{mY]<w>m'[si()" +
                "[]<>z}}RAaLWkg>(`<[]`Hb[)([K<>((bBBn([>P<>m`<>]u'o[>](()><>[X`o(>Yk]d]>'n<D(>}UbI<k>[nX[`S'L]{>");
        randomModify.clearDocument();

        //             0000000000111111111122222222223333333333
        //             0123456789012345678901234567890123456789
        //     text = 
        randomModify.insertText(0, "{g}WvWq)T}df(W(d>}H}[Q<[>O[]{CR[WB'>t}H[}rOx](da[]U)(>`{F[{}R[c]<]>[]j<({<>" +
                "[`wl<uDD['']C'[bN'{`>)}ZMxx}[<[y]`[r]x}[y]Bi<`<>vs<[>r[)<F>}}`(y>['DC`{x}{Dz[zJjLx{mY]<w>m'[si()" +
                "[]<>z}}RAaLWkg>(`<[]`Hb[)([K<>((bBBn([>P<>m`<>]u'o[>](()><>[X`o(>Yk]d]>'n<D(>}UbI<k>[nX[`S'L]{>" +
                "ME{(d{)GC[}y{]s{x}M[R}w{cvhR`)(r");
        //                                                                       |
//        Logger.getLogger(org.netbeans.lib.lexer.inc.TokenListUpdater.class.getName()).setLevel(Level.FINE); // Extra logging
        randomModify.insertText(216, "[]");
        randomModify.clearDocument();

        //             0000000000111111111122222222223333333333
        //             0123456789012345678901234567890123456789
        //     text = "tt<g[h>ab<i]jk>[y]<{>x}[]o[<[x]<[><]>"
        randomModify.insertText(0, "tt<g[h>ab<i]jk>[y]<{>x}[]o[<[x]<[><]>");
        randomModify.removeText(14, 5); // removed-text: ">[y]<"
        //             0000000000111111111122222222223333333333
        //             0123456789012345678901234567890123456789
        //     text = "tt<g[h>ab<i]jk{>x}[]o[<[x]<[><]>"
        randomModify.clearDocument();

        randomModify.insertText(0, "ZCg('[]k)Rz'IFLM<`jBT[[y)'{G`>[X'qGt({)`[(a)`[[{x}}[}f<]]mb)[<m'q<)`ox}][]<>" +
                "(P[Jy<y]iI}]PEZ<<z'x(Vc'>f}W['P(G`SM()]Z][][u]xL)>](>wZ({]vrGI](aAR(B><a>[{gZ)e)<nb'`L[<a>>'A[E" +
                "]]i'f'M}L[y]'d>Y(V]UmL>(nj>`{x}fY[xy'<)<>u`UD]][}<(`[>)o>c>{}JA{bDTL<[(iGuPvzZ<`'TN([]WYy)Xji'q" +
                "h'{`'XL(<a>`]>}x><[bBn>[]{(<a>]<y]}<[>[K]``Ig>JQ]td<jX[R<o'<)Bq)x{a<[`>ckj}(e>((Q(P)ER}<A>n}D)N" +
                "()){<)tO<a)pl><>('<{)g{X<)](LgP`t{`>U{[w`<(N)Br{'`xc]]<[>[[]Xe{({{{{Sc]>[bg(izL(`]KF)F{v'[`ljQ]" +
                "})(<J<]e>'{{{x}o[k{x}]{}))P`x}(mG]><M)a>Y]>'m}>I]`]'Ox()M'][lK<a{x}>)}jf]>lC<]>u'm(N`[B>XO{x<oR" +
                "[Pj`<<(`(`lXANwN<({s[lYpA)][]f]yG{iD]K[<{k{Li<(X}xy>a><(Y<<a>I)N[ZoH]XY]y[K``N{[eTGw']T]<d<VS'E" +
                "B(K[x}]Hb([f{x})}jO[m)KroiNmRD`N[']><c]G>R)(HUI}FS'[DWadI)[A]>{DB`)])}<>[y]m<j{<aJ'Ir{<[)>'>lms" +
                "'w<q<'ohd'{iSE<o`hsk(]]'RLYe[>>J}(LX)hCi>E']l{(GI'u{kKw}{'U(DO[ly]<]>(E[E]'U}{'`k(<e}E[(}'B'gW{" +
                "x}>[lE'GTsw}N[}Q]}<[>zD'uN>Mv[`']']>Y{Sv}'RP>lnqc{'Y><>l>z'T<[p><[>RD{x}P}><><a>ct()K{T{bnP]]zc" +
                "TROhK]eI>C<}[]i)}e`eK}IU]'{)]`a}'eQw})(<<<x`[<>>j[{TS{b>K({}('(`>Cwjv`]}nTQT>kYYso()b[]({iE([]r" +
                "m)x<`>ky[vL}n[y]{J{>})dk[[(]){)<]]>L())DQ>}b{zTqGRU`<a>IL<V'WI}LwYT[]N<><{{{]'BS<><]>}]<E<]l]xX" +
                "JW{}O[j``I((>>Q<H<<]{j}]<[]}z}osK}<aFceX`>>CZ`{`DXr[)bc)}rM(Z]]YNs}Vl{W[[`QY(uvfk[``UYW<S)lHD}'" +
                "i`)>(<<{[R]ti>(]<`}W{cjU)zO}g>'WR`b}u<V))Og([tj>[((b[[>w{<<<()i<ia>UB<<[]<A>V{E<s'<<a>qbI'JY(C[" +
                "{x}[<pKhk[][]{[nxh}[LK)Wp]`]{oLE({[tE[M`sb<[>V`jT[[]j)]'ef<[>BRP{wI)M(]>)[XIx]{QW`<X[OMcHbU>'`i" +
                "X>V)F[kf<>'`[Bh{zC<U[]'(F>`Nfu'}>o([c}Oc{x}J<g]]>`[]hY{D}()[)z<nUj)UCD]}U}R<M)m]}lPD{KQA}dl'DrZ" +
                "Bhmd[{>]{u[R['qV})]>I)(yW]RPV}<>})<'r}<e>g<[)l)>'C'({['k{x}}W)q<[BS<{D>[d[D>dFO'<(RCJ<U][{'>m<`" +
                "m(<>)F<`V]qy<a>[y]<{>x}[]y]Cg[)[eZS><[y]<[>acjQ[y]]E[hv<ke'>sX][E>)rG[fW<[E<O{{}VT)C}F`i]JBD'qO" +
                "bx(''``w}yHTa}vn>SO<O'Ex<>[][r`<w[y]asr<]>P]DyXO[]iP{x}(bYW<<c[<Az()}u'})uu}I)]]y{{C[d(<'}(>ufc" +
                "[}AQU{t]<eR>ee{pjL[y]`M{[[{Ip<a>[<`Iw{txp[]>XYzFi>L<Z}]rEm(D`({x}}{qxh]]]QE[y]oZ]}<a>[y])(in[}a" +
                "(]q<D'(xF[]bXsT]U{]}'}'EU>`kS><V}'l{x<H<p}<)]>>w>(<Np}<]>Eoje{(l'{<`[n]a>WRL)Uy()[[]]nMX'w[][q}" +
                "<a{Y]Gg`uAO`>U{>>[][>>O`B`h}bKYpSG{bO{eA<aC(hux}ez>ki<]>gH<[<(<>N<>)[]qR><K}A`><kB(rs({}'Yra<]>" +
                "[]{vp`P}('){(HA><['){<uM{)ansZBb");
        randomModify.removeText(1610, 5); // removed-text: ">[y]<"
        randomModify.clearDocument();


        //             0000000000111111111122222222223333333333
        //             0123456789012345678901234567890123456789
        //     text = 
        randomModify.insertText(0, "[[]QYa)");
        randomModify.insertText(0, "y");
        randomModify.clearDocument();


        //             0000000000111111111122222222223333333333
        //             0123456789012345678901234567890123456789
        //     text = 
        randomModify.insertText(0, "<x>a<]>c");
        randomModify.removeText(5, 1); // removed-text: "]"
        randomModify.clearDocument();


        //             0000000000111111111122222222223333333333
        //             0123456789012345678901234567890123456789
        //     text = 
        randomModify.insertText(0, ">{[X'ZW>}e<>MG(K))[(>n{<[B]<a')<v(<a>oYvp>x}(s><`L]<'T)x}E>YWWnExa(>]FrU[Y(`" +
                "<>t`>[<]']vI{nLHq]P[ax}][]k'I'<j<>]>(yX[>][y[{x[]'>[)<yUIjO`[`x<>e`i}GMBKx)pLuEuh<]>>{i{`)<j''>w>" +
                "')]><a][y]<]}<(zgN<)og())[>Cg({L[jI[H(}{F}]u)}>]<<)B'iPV(}aEX])[''Z<]>t`)aa[]C[{]o>NU]>Zx<p)by]]" +
                "{x}[K{x}kk>q){PcGE>F>y(]c<<]>LZG[(ByCP)VM)[(t[y]}]iI)<>)j>qq('Hu`d<>s}>CU{<kRi{>)Qr)][(T[]S't{v}" +
                "v`>)T(w})hOh[][mk)G<uO(ty{z<e'`[Lw''jO][sh``}VL)uD}rh>U(AD(O}qp(tw]e>`)][]>{<`<><{}('ZCYR[]]><ht]]" +
                "f'(gJtx((og[']]J`]Po<>'))}Dbq'`(>WZ{J)Oz`{<w'['nY<>X)(II{Rz][(}Ak)y]}C<Ga>]ADmz)<>BB<CY<w<]>)'[Y" +
                "(K)<[]D){`TDy]IW(])Zp]<]>Y]c]>p`]C'`Xb}E}'akD<}>'x{x}}N[h<[y][>]J]<{<{KI'h>>Q[]<[]okI]dn'<a[>{f{[B" +
                "<>{hjP>x{U`z}q]'`EH'`aqeJ)hb]g}<>>hkb's><[>((][(D]'}><y'<>[]Bn{T<[>VUmwknRU{x}}'r'O{{YIBGo)IQ<[>l)" +
                "[NI)X`><]]iRLv'<]'M>)}eu}q'<Q]'L[[q(`LA}`ndr<>XAwv><>(]z'c){ZL{r]{x}<<a<a>E'M<I)><Y[[{<k]>N`l'(m<]" +
                ">[U'Q{`{x}T('Z}IeB<]>[(zGH[y]h>}(waeoCG}({(])]rd`<]>B");
        randomModify.removeText(872, 1); // removed-text: "]"
        randomModify.clearDocument();


        //             0000000000111111111122222222223333333333
        //             0123456789012345678901234567890123456789
        //     text = 
        randomModify.insertText(0, "{WqT}df(W(d>'H}<a>Q{x}<[Q>]{CR[B'>t}H[}>)rOx](daU)dus(>{F[`{}R[c]<({<>[`wl" +
                "<uDD['p'][bN'`>)})ZMxxM}h[][<[>y]{xVI}`f'`''Wx}]Bi<`<>s<[>r>)<Om[ou}Wc{F}`(<y)>k}QLu['<H]kDC`{x}" +
                "{Dz[)JjLx<{mYm'[]<>z}}RoAaLW<>k<]>g>(`<[]`Hu{<hSDTb[G>e'k{<rMbBBxSK<'n<(n([>m`<[{]>]u'o[k<>d]'n" +
                "<D(>}bI<kS<>'L]{>ME{(d{)G[}y)i<L(zyH{]{s{}M[R}w{vhR)(r");
        randomModify.insertText(183, "<]>");
        randomModify.clearDocument();


        //             0000000000111111111122222222223333333333
        //             0123456789012345678901234567890123456789
        //     text = 
        randomModify.insertText(0, "a<x>b<>c<>d<y>");
        randomModify.insertText(9, "[]");
        randomModify.clearDocument();


        //             0000000000111111111122222222223333333333
        //             0123456789012345678901234567890123456789
        //     text = 
        randomModify.insertText(0, "iA`e(()]Wo`BS>x>`IQ>r{Nwo>F{W{M)x`Gx>Z({)>>{x}'Lp{x<a>rU]x(]>ea[[]<c)}}C)p>a" +
                "[T]JzG>k(<<a>'a<{wMLDZ}Mu(<gP`ha<<>}jI<BT{'OC{j(DMsx<<>f><`'[><>g[b{[x}`mcu[x'H{{A'R]uj[]cb>(ix}ik>Ys(F>tDJ'w)");
        //                                                                       |
//        Logger.getLogger(org.netbeans.lib.lexer.inc.TokenListUpdater.class.getName()).setLevel(Level.FINE); // Extra logging
        randomModify.insertText(140, "]'oJ[");
        randomModify.clearDocument();


        //             0000000000111111111122222222223333333333
        //             0123456789012345678901234567890123456789
        //     text = 
        randomModify.insertText(0, "a<>b[de<>");
        randomModify.insertText(3, "c[");
        randomModify.clearDocument();


        //             0000000000111111111122222222223333333333
        //             0123456789012345678901234567890123456789
        //     text = "Zl>J{O{c}<>}}TK>>}}n}jUs<>{x}}>>l<<a>psM{F<FN}E}vd{}}>>{{{>{{<}"
        randomModify.insertText(0, "Zl>J{O{c}<>}}TK>>}}n}jUs<>{x}}>>l<<a>psM{F<FN}E}vd{}}>>{{{>{{<}" +
                ">V}e>A<>y{Ns}I<T>}{{lT}vJ{oOD{lK{}OrDKb}i}<XS>vfJhtx}{r{{x}x}}f<C}g{}VuRQ<<a>}K{AdGQ<{}<ZYVS<}>vdHD<}");
//        Logger.getLogger(org.netbeans.lib.lexer.inc.TokenListUpdater.class.getName()).setLevel(Level.FINE); // Extra logging
        randomModify.insertText(22, "{");
        randomModify.clearDocument();


        //             0000000000111111111122222222223333333333
        //             0123456789012345678901234567890123456789
        //     text = "{o}abc<>{x}}de<><T>"
        randomModify.insertText(0, "{o}abc<>{x}}de<><T>");
        randomModify.insertText(5, "{");
        randomModify.clearDocument();


        randomModify.insertText(0, "b'");
        //             0000000000111111111122222222223333333333
        //             0123456789012345678901234567890123456789
        //     text = "b'"
        randomModify.insertText(0, "a");
        randomModify.clearDocument();


        randomModify.insertText(0, "a<xy>");
        //             0000000000111111111122222222223333333333
        //             0123456789012345678901234567890123456789
        //     text = "uot<((WkdUL)(CO>"
        randomModify.removeText(4, 1);
        randomModify.clearDocument();


        randomModify.insertText(0, "uot<((WkdUL)(CO>");
        //             0000000000111111111122222222223333333333
        //             0123456789012345678901234567890123456789
        //     text = "uot<((WkdUL)(CO>"
        randomModify.removeText(10, 6);
        randomModify.clearDocument();


        randomModify.insertText(0, "y<>");
        randomModify.insertText(0, "x<a>");
        //             0123456789
        //     text = "x<a>y<>"
        randomModify.clearDocument();


        randomModify.insertText(0, "<>Z{N}A<{>P<v}}>");
        //             0000000000111111111122222222223333333333
        //             0123456789012345678901234567890123456789
        //     text = "<>Z{{{}zN}A<{>P<v}}>"
        randomModify.removeText(2, 1);
        randomModify.clearDocument();


        randomModify.insertText(0, ")({x}G<q{}W>(Z");
        //             0000000000111111111122222222223333333333
        //             0123456789012345678901234567890123456789
        //     text = ")({x}G<q{}W>(Z"
        randomModify.insertText(0, "ABC");
        randomModify.clearDocument();

        randomModify.insertText(0, "{x}<a>y");
        //             0000000000111111111122222222223333333333
        //             0123456789012345678901234567890123456789
        //     text = "{x}<a>y"
        randomModify.insertText(3, "u");
        randomModify.clearDocument();


        randomModify.insertText(0, "E{r}Ia{x}>k}A}}e{}BIX{<<OBx}<}><c<}><{<}>n<<arz>i<xs" +
            "BMKhim{<gK>n><<<{s}F{Sx>}{{>{{DU{x}<a<a>zV{B}}u<a><k><><>jWFn<>}<iy}QlSt" +
            "}{OOz<a<{Rh>v{<{}{{{pV}fH}<<g{{>}>r><a>><}<XG{<u<a>>{QT<<}Y><>r}x{p}>}Q{Lx}}" +
            "n{QMn><<<<>>}yyj<<Enj}<a>u}rO>z{}>{}{{a>o<<a}yT{x}<>cwRQf{PF<zm>}>>}{}{{<TTo" +
            "<<}<Gw<>CaZ<>mBG>{K{<<a><X<}v<Kni}H><{}<>}}>>s}K}}LP>yxeGC>}}<BQ<><W}y{{}GUK" +
            "<fY>>X<lg>>}{vx}u}e{x}<>oB{r>{{<>>>{<BcL{x}>v<{y>k>{{W<<aVe{{i>w<>>S}{ey}}U}" +
            "{QVYBG<ta{{x}Y>Ms<>gQ<{}Yp{K}VD<a<{x}N}t<a<a>a>{rF{{S>PCfx}N}<>>n{<aj{TP}{hr" +
            "><<{<gp<>{ib}<mMTs{x}}<a>{x}}{x{x<>i<<T>A<{A<lx>emo{}opO>Y}J><G><{QLIIa{Lto<" +
            "YFsb}t<a>z}<><a><P<<KJYx}<}a>{x}<i<Q><w{{><>>}}<tQf<cr<{g>}{p{>}<<faMKT}<a>{" +
            "t{}>i<a><>OloX}}>Um<{x}a>bx{}}>K<}><>><}<{<a>R>>>{g>z{xhYs{}}ikbFV{ND<w{}}>a" +
            "i><{{x}Esx>}}Q>}{}>Fv>>{x}<<>}r}n<e<a><wmF>Y<>>BUQ}}jbG>lacFn<l}>}"
        );
        randomModify.insertText(612, ">");
        randomModify.clearDocument();


        randomModify.insertText(0, "a{<a>lm<xyz>c<u>d<>x}<");
        //             0000000000111111111122222222223333333333
        //             0123456789012345678901234567890123456789
        //     text = "a{<a>lm<xyz>c<u>d<>x}<"
        randomModify.removeText(20, 1);
        randomModify.clearDocument();


        randomModify.insertText(0, "<a>}<}a>S<{x}JD<><a>{k>D>>}}ZxF}<}no>Q{}>z");
        //             0000000000111111111122222222223333333333
        //             0123456789012345678901234567890123456789
        //     text = "<a>}<}a>S<{x}JD<><a>{k>D>>}}ZxF}<}no>Q{}>z"
        randomModify.insertText(8, "j");
        randomModify.clearDocument();


        randomModify.insertText(0, "<a>}s<}<NGFxT>a>jN{BN<<oHTS{x}JD<><a<a>B>xF}<}n<a>o<>x}Q{}>z");
        //             0000000000111111111122222222223333333333
        //             0123456789012345678901234567890123456789
        //     text = "<a>}s<}<NGFxT>a>jN{BN<<oHTS{x}JD<><a<a>B>xF}<}n<a>o<>x}Q{}>z"
        randomModify.insertText(52, ">}enPM");
        randomModify.clearDocument();


        randomModify.insertText(0, "<a>}s<>sU}<TTG}<NGFxT>a>jS<{x}JD<><a>xF}<}n<a>o>{x}Q{}>z");
        //             00000000001111111111222222222233333333334444444444
        //             01234567890123456789012345678901234567890123456789
        //     text = "<a>}s<>sU}<TTG}<NGFxT>a>jS<{x}JD<><a>xF}<}n<a>o>{x}Q{}>z"
        randomModify.removeText(10, 1);
        randomModify.clearDocument();


        randomModify.insertText(0, "a{b<{}<x>y{c}");
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        //     text = "a{b<{}<x>y{c}";
        randomModify.insertText(1, "<");
        randomModify.clearDocument();


        randomModify.insertText(0, ">F{WGCha<{}<E>J>R{a}K{g}{x}<}Y");
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        //     text = "ab{<x";
        randomModify.insertText(1, "<");
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        //     text = "c{><n>ab{<x";
        randomModify.clearDocument();


        randomModify.insertText(0, "a{b<{}<x>y{c}");
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        //     text = "a{b<{}<x>y{c}";
        randomModify.insertText(3, "}");
        randomModify.clearDocument();


        randomModify.insertText(0, "ab{<x");
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        //     text = "ab{<x";
        randomModify.insertText(0, "c{><n>");
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        //     text = "c{><n>ab{<x";
        randomModify.clearDocument();


        randomModify.insertText(0, "ab{<x");
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        //     text = "ab{<x";
        randomModify.insertText(0, "c{><n>");
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        //     text = "c{><n>ab{<x";
        randomModify.clearDocument();


        randomModify.insertText(0, "a<b>c");
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        //     text = "a<b>c";
        randomModify.removeText(3, 1);
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        //     text = "a<bc";
        randomModify.clearDocument();


        randomModify.insertText(0, "<b>c");
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        //     text = "<b>c";
        randomModify.removeText(0, 1);
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        //     text = "b>c";
        randomModify.clearDocument();


        randomModify.insertText(0, ")}M)s{i)<}><p)}>");
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        //     text = ")}M)s{i)<}><p)}>"
        randomModify.insertText(10, "(RdB");
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        //     text = ")}M)s{i)<}(RdB><p)}>"
        randomModify.clearDocument();


        randomModify.insertText(0, "a{x<b>y}z");
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        //     text = "a{x<b>y}z"
        randomModify.insertText(6, "u");
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        //     text = "a{x<b>uy}z"
        randomModify.clearDocument();


        randomModify.insertText(0, "}M)s{i)<}(RB><W>dVpv)p)}>");
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        //     text = "}M)s{i)<}(RB><W>dVpv)p)}>"
        randomModify.insertText(16, "L");
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        //     text = "}M)s{i)<}(RB><W>LdVpv)p)}>"
        randomModify.clearDocument();


        randomModify.insertText(0, "{<>x<p>x");
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        //     text = "{<>x<p>x"
        randomModify.insertText(4, ")");
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        //     text = "{<>x(<p>x"
        randomModify.clearDocument();


        randomModify.insertText(0, "<a>x<>y<c>");
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        //     text = "<a>x<>y<c>"
        randomModify.insertText(4, "b");
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        //     text = "<a>x<b>y<c>"
        randomModify.clearDocument();


        randomModify.insertText(0, "a<>b<y>c");
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        //     text = "<a>x<>y<c>"
        randomModify.removeText(1, 1);
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        //     text = "<a>x<b>y<c>"
        randomModify.clearDocument();


        randomModify.insertText(0, "a<>");
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        //     text = "a<>"
        randomModify.insertText(2, "x");
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        //     text = "a<x>"
        randomModify.clearDocument();


        randomModify.insertText(0, "}w(><<()<>SV<(<}}{IaW><v<");
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        //     text = "}w(><<()<>SV<(<}}{IaW><v<"
        randomModify.insertText(9, "t");
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        //     text = "}w(><<()<>SV<(<}}{IaW><v<"
        randomModify.clearDocument();


        randomModify.insertText(0, "a<x><y>v()");
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        //     text = "a<x><y>v()"
        randomModify.insertText(7, "u");
        //             000000000011111111112222222222
        //             012345678901234567890123456789
        //     text = "a<x><y>uv()"
        randomModify.clearDocument();


        // Begin really randomized testing
        FixedTextDescriptor[] fixedTexts = new FixedTextDescriptor[] {
            FixedTextDescriptor.create("<a>", 0.2),
            FixedTextDescriptor.create("{x}", 0.2),
            FixedTextDescriptor.create("[y]", 0.2),
            FixedTextDescriptor.create("<>", 0.2),
            FixedTextDescriptor.create("()", 0.2),
            FixedTextDescriptor.create("[]", 0.2),
            FixedTextDescriptor.create("<[>", 0.2),
            FixedTextDescriptor.create("<]>", 0.2),
        };
        
        RandomCharDescriptor[] regularChars = new RandomCharDescriptor[] {
            RandomCharDescriptor.letter(0.3),
            RandomCharDescriptor.chars(new char[] { '<', '>', '{', '}', '(', ')', '[', ']', '\'', '`' }, 0.3),
        };

        RandomTextProvider textProvider = new RandomTextProvider(regularChars, fixedTexts);
        
        randomModify.test(
            new RandomModifyDescriptor[] {
                new RandomModifyDescriptor(1000, textProvider,
                        0.2, 0.2, 0.1,
                        0.2, 0.2,
                        0.0, 0.0), // snapshots create/destroy
            }
        );

    }
    
}
