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

typedef unsigned int        uint;

typedef signed char         int_8;
typedef signed short        int_16;

typedef unsigned char       uint_8;
typedef unsigned short      uint_16;
typedef signed int          int_32;
typedef signed long         int_64;
typedef unsigned int        uint_32;
typedef unsigned long       uint_64;

template <uint nFractionBitsArg>
class FixedPoint {
public:
    enum AlreadyFixed { final };
    enum MaxValue { maxValue };
    enum MinValue { minValue };
    enum { nFractionBits = nFractionBitsArg };

    FixedPoint()            : m_val(0) {}
    FixedPoint(double v)    : m_val(int_64(v * (1<<nFractionBits))) {}
    FixedPoint(uint_32 v)   : m_val(int_64(v)<<nFractionBits) {}
    FixedPoint(int_32 v)    : m_val(int_64(v)<<nFractionBits) {}
    FixedPoint(uint_64 v)   : m_val(int_64(v)<<nFractionBits) {}
    FixedPoint(int_64 v)    : m_val(int_64(v)<<nFractionBits) {}
    FixedPoint(int_64 v, AlreadyFixed)    : m_val(v) {}
    FixedPoint(MaxValue)    : m_val((int_64)((~uint_64(0)) >> 1)) {}
    FixedPoint(MinValue)    : m_val(int_64(1) << 63) {}

    int_64 fractionMask() const { return (uint_64(1) << nFractionBits) - 1; }

    int     intValue() const    { return int(m_val >> nFractionBits); }
    uint    uintValue() const   { return uint(m_val >> nFractionBits); }
    int_32  int32Value() const  { return int_32(m_val >> nFractionBits); }
    uint_32 uint32Value() const { return uint_32(m_val >> nFractionBits); }
    int_64  int64Value() const  { return m_val >> nFractionBits; }
    uint_64 uint64Value() const { return m_val >> nFractionBits; }
    int_64  rawValue() const    { return m_val; }
    float   floatValue() const;
    double  doubleValue() const;

    bool operator==(const FixedPoint& rhs) const
        { return m_val == rhs.m_val; }

    bool operator!=(const FixedPoint& rhs) const
        { return m_val != rhs.m_val; }

    bool operator<(const FixedPoint& rhs) const
        { return m_val < rhs.m_val; }

    bool operator<=(const FixedPoint& rhs) const
        { return m_val <= rhs.m_val; }

    bool operator>(const FixedPoint& rhs) const
        { return m_val > rhs.m_val; }

    bool operator>=(const FixedPoint& rhs) const
        { return m_val >= rhs.m_val; }

    FixedPoint& operator=(const FixedPoint& rhs)
        { m_val = rhs.m_val; return *this; }

    FixedPoint& operator+=(const FixedPoint& rhs)
        { m_val += rhs.m_val; return *this; }

    FixedPoint& operator-=(const FixedPoint& rhs)
        { m_val -= rhs.m_val; return *this; }

    FixedPoint& operator*=(const FixedPoint& rhs)
        { *this = *this * rhs; return *this; }

    FixedPoint& operator/=(const FixedPoint& rhs)
        { *this = *this / rhs; return *this; }

    FixedPoint operator+(const FixedPoint& rhs) const
        { return FixedPoint(m_val + rhs.m_val, final); }

    FixedPoint operator-() const
        { return FixedPoint(-m_val, final); }

    FixedPoint operator+(uint_32 rhs) const   { return *this + FixedPoint(rhs); }
    FixedPoint operator+(int_32 rhs) const    { return *this + FixedPoint(rhs); }
    FixedPoint operator+(uint_64 rhs) const   { return *this + FixedPoint(rhs); }
    FixedPoint operator+(int_64 rhs) const    { return *this + FixedPoint(rhs); }
    FixedPoint operator+(double rhs) const    { return *this + FixedPoint(rhs); }

    FixedPoint operator-(const FixedPoint& rhs) const
        { return FixedPoint(m_val - rhs.m_val, final); }

    FixedPoint operator-(uint_32 rhs) const   { return *this - FixedPoint(rhs); }
    FixedPoint operator-(int_32 rhs) const    { return *this - FixedPoint(rhs); }
    FixedPoint operator-(uint_64 rhs) const   { return *this - FixedPoint(rhs); }
    FixedPoint operator-(int_64 rhs) const    { return *this - FixedPoint(rhs); }
    FixedPoint operator-(double rhs) const    { return *this - FixedPoint(rhs); }

    FixedPoint operator*(const FixedPoint& rhs) const
    {
        if ((uint_64(m_val | rhs.m_val) >> 31) == 0) {
            // things are simple... just multiply, adjust and return
            return FixedPoint((m_val*rhs.m_val) >> nFractionBits, final);
        }

        // Each number can be separated: N = N1 + N2 where N1 is
        // the integer part and N2 is the fraction part.  So the product
        // of two numbers N*M can be expressed as (N1 + N2)(M1 + M2)
        // or N1*M1 + N1*M2 + N2*M1 + N2*M2.  We use that here (along with
        // careful shifting) to prevent bits from falling off the top
        // in the intermediate results.

        // l1 & l2 are the integer parts
        int_64 l1 = m_val >> nFractionBits;
        int_64 r1 = rhs.m_val >> nFractionBits;
        // l2 & r2 are the fractional parts
        int_64 l2 = m_val & fractionMask();
        int_64 r2 = rhs.m_val & fractionMask();

        int_64 result = l1*rhs.m_val // simplified form of l1*r1 + l1*r2
                      + l2*r1
                      + (l2*r2 >> nFractionBits);
        return FixedPoint(result, final);
    }

    FixedPoint operator*(uint_32 rhs) const   { return *this * FixedPoint(rhs); }
    FixedPoint operator*(int_32 rhs) const    { return *this * FixedPoint(rhs); }
    FixedPoint operator*(uint_64 rhs) const   { return *this * FixedPoint(rhs); }
    FixedPoint operator*(int_64 rhs) const    { return *this * FixedPoint(rhs); }
    FixedPoint operator*(double rhs) const    { return *this * FixedPoint(rhs); }

    FixedPoint operator/(const FixedPoint& rhs) const
    {
        uint_64 absVal = m_val > 0 ? m_val : -m_val;
        // make sure we find enough zero bits to allow for the << (hence, 64-1)
        if ((absVal >> (63-nFractionBits)) == 0) {
            // things are simple... just adjust, multiply and return
            return FixedPoint((m_val << nFractionBits) / rhs.m_val, final);
        }

        // We cannot shift m_val left nFractionBits without losing the upper bits.
        //
        // The calculation is performed as follows:  (N << nFraction) / M
        // becomes:
        // IntegerPart  = N/M
        // FractionalPart = ((N - IntegerPart*M) << nFraction) / M
        // result = (IntegerPart << nFraction) + FractionalPart
        int_64 intPart  = m_val/rhs.m_val;
        int_64 fracPart = ((m_val - intPart*rhs.m_val) << nFractionBits) / rhs.m_val;
        int_64 result   = (intPart << nFractionBits) + fracPart;

        // Again, each number can be separated: N = N1 + N2 where N1 is
        // the upper nFractionBits bits, and N2 is the rest.  So the quotient
        // of two numbers N/M can be expressed as (N1 + N2)/M
        // or N1/M + N2/M.  We use that here (along with
        // careful shifting) to prevent bits from falling off the top
        // in the intermediate results.

//        uint shiftBefore = 30 - Bitset::findLastSet(uint(absVal>>32));
//        uint shiftAfter = nFractionBits - shiftBefore;
//        int_64 l1 = (m_val & (~fractionMask())) << shiftBefore;
//        int_64 l2 = m_val << nFractionBits;

        // the result has effectively been quantized by
        // (1<<(nFractionBits-shiftBefore)) because the shift of
        // nFractionBits should occur before the divide, not after.
//        int_64 result = (l1/rhs.m_val << shiftAfter)
//                      + l2/rhs.m_val;
        return FixedPoint(result, final);
    }

    FixedPoint operator/(int_32 rhs) const
        { return FixedPoint(m_val / rhs, final); }

    FixedPoint operator/(uint_32 rhs) const
    {
        if ((rhs >> 31) == 0)
            return *this / int_32(rhs);
        return *this / FixedPoint(rhs);
    }

    FixedPoint operator/(uint_64 rhs) const   { return *this / FixedPoint(rhs); }
    FixedPoint operator/(int_64 rhs) const    { return *this / FixedPoint(rhs); }
    FixedPoint operator/(double rhs) const    { return *this / FixedPoint(rhs); }

    FixedPoint round() const
        { return FixedPoint((m_val + (1<<(nFractionBits-1))) & ~fractionMask(), final); }
    FixedPoint abs() const
        { return FixedPoint(m_val < 0 ? -m_val : m_val, final); }

private:
    int_64 m_val;
};

typedef FixedPoint<16> FixedPoint16;

template <int size>
FixedPoint<size> abs(FixedPoint<size> rhs)
    { return rhs < 0 ? -rhs : rhs; }

inline FixedPoint16 operator-(uint_64 lhs, const FixedPoint16& rhs)
    { return FixedPoint16(lhs) - rhs; }
inline FixedPoint16 operator-(int_64 lhs, const FixedPoint16& rhs)
    { return FixedPoint16(lhs) - rhs; }
inline FixedPoint16 operator-(uint_32 lhs, const FixedPoint16& rhs)
    { return FixedPoint16(lhs) - rhs; }
inline FixedPoint16 operator-(int_32 lhs, const FixedPoint16& rhs)
    { return FixedPoint16(lhs) - rhs; }

inline FixedPoint16 operator+(uint_64 lhs, const FixedPoint16& rhs)
    { return FixedPoint16(lhs) + rhs; }
inline FixedPoint16 operator+(int_64 lhs, const FixedPoint16& rhs)
    { return FixedPoint16(lhs) + rhs; }
inline FixedPoint16 operator+(uint_32 lhs, const FixedPoint16& rhs)
    { return FixedPoint16(lhs) + rhs; }
inline FixedPoint16 operator+(int_32 lhs, const FixedPoint16& rhs)
    { return FixedPoint16(lhs) + rhs; }

inline FixedPoint16 operator*(uint_64 lhs, const FixedPoint16& rhs)
    { return FixedPoint16(lhs) * rhs; }
inline FixedPoint16 operator*(int_64 lhs, const FixedPoint16& rhs)
    { return FixedPoint16(lhs) * rhs; }
inline FixedPoint16 operator*(uint_32 lhs, const FixedPoint16& rhs)
    { return FixedPoint16(lhs) * rhs; }
inline FixedPoint16 operator*(int_32 lhs, const FixedPoint16& rhs)
    { return FixedPoint16(lhs) * rhs; }

inline FixedPoint16 operator/(uint_64 lhs, const FixedPoint16& rhs)
    { return FixedPoint16(lhs) / rhs; }
inline FixedPoint16 operator/(int_64 lhs, const FixedPoint16& rhs)
    { return FixedPoint16(lhs) / rhs; }
inline FixedPoint16 operator/(uint_32 lhs, const FixedPoint16& rhs)
    { return FixedPoint16(lhs) / rhs; }
inline FixedPoint16 operator/(int_32 lhs, const FixedPoint16& rhs)
    { return FixedPoint16(lhs) / rhs; }

int bug211534_main(int argc, char** argv) {
    return (240000 * 8 * (30000 / FixedPoint<16>(1001))).uint32Value();
}