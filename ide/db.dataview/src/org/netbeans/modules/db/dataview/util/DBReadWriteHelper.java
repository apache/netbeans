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
package org.netbeans.modules.db.dataview.util;


import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.db.dataview.meta.DBColumn;
import org.netbeans.modules.db.dataview.meta.DBException;
import org.openide.util.NbBundle;

/**
 *
 * @author Ahimanikya Satapathy
 */
public class DBReadWriteHelper {
    public static final int SQL_TYPE_ORACLE_TIMESTAMP = -100; // Oracle Timestamp
    public static final int SQL_TYPE_ORACLE_TIMESTAMP_WITH_TZ = -101; // Oracle Timestamp with Timezone
    private static final BigInteger maxUnsignedLong = new BigInteger("18446744073709551615");
    private static final BigInteger maxLong = BigInteger.valueOf(Long.MAX_VALUE);
    private static final BigInteger minLong = BigInteger.valueOf(Long.MIN_VALUE);
    private static final long maxUnsignedInt = 4294967295L;
    private static final int maxUnsignedShort = 65535;
    private static final short maxUnsignedByte = 255;
    private static final Logger mLogger = Logger.getLogger(DBReadWriteHelper.class.getName());

    @SuppressWarnings(value = "fallthrough") // NOI18N
    public static Object readResultSet(ResultSet rs, DBColumn col, int index) throws SQLException {
        int colType = col.getJdbcType();

        if (colType == Types.BIT && col.getPrecision() <= 1) {
            colType = Types.BOOLEAN;
        }

        switch (colType) {
            case Types.BOOLEAN: {
                boolean bdata = rs.getBoolean(index);
                if (rs.wasNull()) {
                    return null;
                } else {
                    return bdata;
                }
            }
            case Types.TIME: {
                Time tdata = rs.getTime(index);
                if (rs.wasNull()) {
                    return null;
                } else {
                    return tdata;
                }
            }
            case Types.DATE: {
                Date ddata = rs.getDate(index);
                if (rs.wasNull()) {
                    return null;
                } else {
                    return ddata;
                }
            }
            case Types.TIMESTAMP:
            case SQL_TYPE_ORACLE_TIMESTAMP:
            case SQL_TYPE_ORACLE_TIMESTAMP_WITH_TZ:
            {
                try {
                    Timestamp tsdata = rs.getTimestamp(index);

                    if (rs.wasNull()) {
                        return null;
                    } else {
                        return tsdata;
                    }
                } catch (SQLException sqe) {
                    if (sqe.getSQLState().equals("S1009")) { // NOI18N
                        return null;
                    } else {
                        throw sqe;
                    }
                }
            }
            case Types.BIGINT: {
                try {
                    long ldata = rs.getLong(index);
                    if (rs.wasNull()) {
                        return null;
                    } else {
                        return ldata;
                    }
                } catch (java.sql.SQLDataException ex) {
                    // In case of unsigned BIGINT, long is to small to take it
                    // for now getString is asumed to produce greates compatiblity
                    // The returned string is used to create a BigInteger
                    String sdata = rs.getString(index);
                    if (sdata == null) {
                        return null;
                    } else {
                        return new BigInteger(sdata);
                    }
                }
            }
            case Types.DOUBLE: {
                double fdata = rs.getDouble(index);
                if (rs.wasNull()) {
                    return null;
                } else {
                    return fdata;
                }
            }

            case Types.FLOAT:
            case Types.REAL: {
                float rdata = rs.getFloat(index);
                if (rs.wasNull()) {
                    return null;
                } else {
                    return rdata;
                }
            }
            case Types.DECIMAL:
            case Types.NUMERIC: {
                BigDecimal bddata = rs.getBigDecimal(index);
                if (rs.wasNull()) {
                    return null;
                } else {
                    return bddata;
                }
            }
            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.TINYINT: {
                try {
                    int idata = rs.getInt(index);
                    if (rs.wasNull()) {
                        return null;
                    } else {
                        return idata;
                    }
                } catch (java.sql.SQLDataException ex) {
                    // in case of an unsigned integer, the java Integer is
                    // to small to hold it => switch to long in that case
                    long ldata = rs.getLong(index);
                    if (rs.wasNull()) {
                        return null;
                    } else {
                        return ldata;
                    }
                }
            }

            case Types.CHAR:
            case Types.VARCHAR:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.ROWID: {
                String sdata = rs.getString(index);
                if (rs.wasNull()) {
                    return null;
                } else {
                    return sdata;
                }
            }
            case Types.BIT: {
                byte[] bdata = rs.getBytes(index);
                if (rs.wasNull() || bdata == null) {
                    return null;
                } else {
                    String bStr = BinaryToStringConverter.convertToString(bdata, BinaryToStringConverter.BINARY, true);
                    if (colType == Types.BIT && col.getPrecision() != 0 && col.getPrecision() < bStr.length()) {
                        return bStr.substring(bStr.length() - col.getPrecision());
                    }
                }
            }
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
            case Types.BLOB: {
                // Load binary data as stream and hold it internally as a pseudoblob
                try {
                    InputStream is = rs.getBinaryStream(index);
                    if (is == null) {
                        return null;
                    } else {
                        return new FileBackedBlob(is);
                    }
                } catch (NullPointerException ex) {
                    // The xerial sqlite-jdbc driver fails to return null and instead throws a NullPointer Exception
                    // see bug 244313 for details
                    return null;
                }
            }
            case Types.LONGVARCHAR:
            case Types.LONGNVARCHAR:
            case Types.CLOB:
            case Types.NCLOB: {
                // Try to get a clob object
                try {
                    Clob clob = rs.getClob(index);

                    if (clob == null) {
                        return null;
                    }

                    Object result = null;
                    
                    if (! rs.wasNull()) {
                        result =  new FileBackedClob(clob.getCharacterStream());
                    }
                    
                    try {
                        clob.free();
                    } catch (java.lang.AbstractMethodError err) {
                        // Blob gained a new method in jdbc4 (drivers compiled
                        // against older jdks don't provide this methid
                    } catch (SQLException ex) {
                        // DBMS failed to free resource or does not support call
                        // ignore this, as we can't do more
                    }
                    
                    return result;
                    // Ok - can happen - the jdbc driver might not support
                    // clob data or can for example not provide a longvarchar
                    // as clob - so fall back to our implementation of clob
                } catch (SQLException | java.lang.UnsupportedOperationException ex) {
                }
                String sdata = rs.getString(index);
                if (rs.wasNull()) {
                    return null;
                } else {
                    return new FileBackedClob(sdata);
                }
            }
            case Types.OTHER:
            default:
                return rs.getObject(index);
        }
    }

    public static void setAttributeValue(PreparedStatement ps, int index, int jdbcType, Object valueObj) throws DBException {
        Number numberObj;

        try {

            if (valueObj == null) {
                ps.setNull(index, jdbcType);
                return;
            }

            if (jdbcType == Types.BIT && valueObj instanceof Boolean) {
                jdbcType = Types.BOOLEAN;
            }

            switch (jdbcType) {
                case Types.BOOLEAN:
                    ps.setBoolean(index, (Boolean) valueObj);
                    break;
                    
                case Types.TIME:
                    ps.setTime(index, TimeType.convert (valueObj));
                    break;
                    
                case Types.DATE:
                    ps.setDate(index, DateType.convert(valueObj));
                    break;
                    
                case Types.TIMESTAMP:
                case SQL_TYPE_ORACLE_TIMESTAMP:
                case SQL_TYPE_ORACLE_TIMESTAMP_WITH_TZ:
                    ps.setTimestamp(index, TimestampType.convert (valueObj));
                    break;
                    
                case Types.BIGINT:
                    if(valueObj instanceof BigInteger) {
                        BigInteger biValue = (BigInteger) valueObj;
                        if(biValue.compareTo(maxLong) > 0 || biValue.compareTo(minLong) < 0) {
                            ps.setString(index, valueObj.toString());
                        } else {
                            ps.setLong(index, biValue.longValue());
                        }
                    } else if (valueObj instanceof Number) {
                        Number numberValue = (Number) valueObj;
                        ps.setLong(index, numberValue.longValue());
                    } else {
                        BigInteger biValue = new BigInteger(valueObj.toString());
                        if(biValue.compareTo(maxLong) > 0 || biValue.compareTo(minLong) < 0) {
                            ps.setString(index, valueObj.toString());
                        } else {
                            ps.setLong(index, biValue.longValue());
                        }
                    }
                    break;
                    
                case Types.DOUBLE:
                    numberObj = (valueObj instanceof Number) ? (Number) valueObj : Double.valueOf(valueObj.toString());
                    ps.setDouble(index, numberObj.doubleValue());
                    break;

                case Types.FLOAT:
                case Types.REAL:
                    numberObj = (valueObj instanceof Number) ? (Number) valueObj : Float.valueOf(valueObj.toString());
                    ps.setFloat(index, numberObj.floatValue());
                    break;
                    
                case Types.NUMERIC:
                case Types.DECIMAL:
                    BigDecimal bigDec = (valueObj instanceof BigDecimal)
                            ? (BigDecimal) valueObj
                            : new BigDecimal(valueObj.toString());
                    ps.setBigDecimal(index, bigDec);
                    break;

                case Types.INTEGER:
                    numberObj = (valueObj instanceof Number) ? (Number) valueObj : Long.valueOf(valueObj.toString());
                    if(numberObj.longValue() > ((long) Integer.MAX_VALUE)) {
                        ps.setLong(index, numberObj.longValue());
                    } else {
                        ps.setInt(index, numberObj.intValue());
                    }
                    break;

                case Types.SMALLINT:
                    numberObj = (valueObj instanceof Number) ? (Number) valueObj : Integer.valueOf(valueObj.toString());
                    if(numberObj.longValue() > ((long) Short.MAX_VALUE)) {
                        ps.setInt(index, numberObj.intValue());
                    } else {
                        ps.setShort(index, numberObj.shortValue());
                    }
                    break;

                case Types.TINYINT:
                    numberObj = (valueObj instanceof Number) ? (Number) valueObj : Short.valueOf(valueObj.toString());
                    if(numberObj.longValue() > ((long) Byte.MAX_VALUE)) {
                        ps.setShort(index, numberObj.shortValue());
                    } else {
                        ps.setByte(index, numberObj.byteValue());
                    }
                    break;

                case Types.CHAR:
                case Types.VARCHAR:
                case Types.NCHAR:
                case Types.NVARCHAR:
                case Types.ROWID:
                    ps.setString(index, valueObj.toString());
                    break;
                    
                case Types.BIT:
                    ps.setBytes(index, BinaryToStringConverter.convertBitStringToBytes(valueObj.toString()));
                    break;

                case Types.BINARY:
                case Types.VARBINARY:
                case Types.LONGVARBINARY:
                case Types.BLOB:
                    ps.setBinaryStream(index, ((Blob) valueObj).getBinaryStream(), (int) ((Blob) valueObj).length());
                    break;

                case Types.LONGVARCHAR:
                case Types.LONGNVARCHAR:
                case Types.CLOB:
                case Types.NCLOB: /*NCLOB */
                    ps.setCharacterStream(index, ((Clob) valueObj).getCharacterStream(), (int) ((Clob) valueObj).length());
                    break;

                default:
                    ps.setObject(index, valueObj, jdbcType);
            }
        } catch (RuntimeException | SQLException | DBException e) {
            mLogger.log(Level.SEVERE, "Invalid Data for" + jdbcType + "type -- ", e); // NOI18N
            throw new DBException(NbBundle.getMessage(DBReadWriteHelper.class, "DBReadWriteHelper_Validate_InvalidType", jdbcType, e)); // NOI18N
        }
    }

    public static Object validate(Object valueObj, DBColumn col) throws DBException {
        int colType = col.getJdbcType();
        if (valueObj == null) {
            return null;
        }


        if (colType == Types.BIT && col.getPrecision() <= 1) {
            colType = Types.BOOLEAN;
        }

        try {
            switch (colType) {
                case Types.BOOLEAN: {
                    if (valueObj instanceof Boolean) {
                        return valueObj;
                    } else {
                        String str = valueObj.toString();
                        if ((str.equalsIgnoreCase("true")) || (str.equalsIgnoreCase("1"))) { // NOI18N
                            return Boolean.TRUE;
                        } else if ((str.equalsIgnoreCase("false")) || (str.equalsIgnoreCase("0"))) { // NOI18N
                            return Boolean.FALSE;
                        } else {
                            throw new DBException(NbBundle.getMessage(DBReadWriteHelper.class, "DBReadWriteHelper_Validate_Boolean")); // NOI18N
                        }
                    }
                }

                case Types.TIME:
                    return TimeType.convert(valueObj);
                
                case Types.DATE:
                    return DateType.convert(valueObj);

                case Types.TIMESTAMP:
                case SQL_TYPE_ORACLE_TIMESTAMP:
                case SQL_TYPE_ORACLE_TIMESTAMP_WITH_TZ:
                    return TimestampType.convert(valueObj);
                    
                case Types.BIGINT:
                    if(valueObj instanceof Long) {
                        return valueObj;
                    } else {
                        BigInteger value = new BigInteger(valueObj.toString());
                        if(value.compareTo(minLong) < 0  || value.compareTo(maxUnsignedLong) > 0) {
                            throw new NumberFormatException("Illegal value for BIGINT");
                        } else {
                            return value;
                        }
                    }
                        
                case Types.DOUBLE:
                    return valueObj instanceof Double ? valueObj : Double.valueOf(valueObj.toString());

                case Types.FLOAT:
                case Types.REAL:
                    return valueObj instanceof Float ? valueObj : Float.valueOf(valueObj.toString());

                case Types.DECIMAL:
                case Types.NUMERIC:
                    return valueObj instanceof BigDecimal ? valueObj : new BigDecimal(valueObj.toString());

                case Types.INTEGER: {
                    long ldata = Long.parseLong(valueObj.toString());
                        if(ldata >= ((long) Integer.MIN_VALUE) && ldata <= ((long) Integer.MAX_VALUE)) {
                        return Integer.valueOf((int) ldata);
                        } else if ( ldata < maxUnsignedInt ) {
                        return new Long(ldata);
                    } else {
                        throw new NumberFormatException("Illegal value for java.sql.Type.Integer");
                    }
                }

                case Types.SMALLINT: {
                    int idata = Integer.parseInt(valueObj.toString());
                        if(idata >= ((int) Short.MIN_VALUE) && idata <= ((int) Short.MAX_VALUE)) {
                        return (short) idata;
                        } else if ( idata < maxUnsignedShort ) {
                        return idata;
                    } else {
                        throw new NumberFormatException("Illegal value for java.sql.Type.SMALLINT");
                    }
                }

                case Types.TINYINT: {
                    short sdata = Short.parseShort(valueObj.toString());
                        if(sdata >= ((short) Byte.MIN_VALUE) && sdata <= ((short) Byte.MAX_VALUE)) {
                        return (byte) sdata;
                        } else if ( sdata < maxUnsignedByte ) {
                        return sdata;
                    } else {
                        throw new NumberFormatException("Illegal value for java.sql.Type.TINYINT");
                    }
                }

                case Types.CHAR:
                case Types.VARCHAR:
                case Types.LONGVARCHAR:
                case Types.NVARCHAR:  //NVARCHAR
                case Types.ROWID:  //ROWID
                case Types.NCHAR: //NCHAR
                    if (col.getPrecision() > 0 && valueObj.toString().length() > col.getPrecision()) {
                        String colName = col.getQualifiedName(false);
                        throw new DBException(NbBundle.getMessage(DBReadWriteHelper.class, "DBReadWriteHelper_Validate_TooLarge", valueObj, colName)); // NOI18N
                    }
                    return valueObj;

                case Types.BIT:
                    if (valueObj.toString().length() > col.getPrecision()) {
                        String colName = col.getQualifiedName(false);
                        throw new DBException(NbBundle.getMessage(DBReadWriteHelper.class, "DBReadWriteHelper_Validate_TooLarge", valueObj, colName)); // NOI18N
                    }
                    if (valueObj.toString().trim().length() == 0) {
                        String colName = col.getQualifiedName(false);
                        throw new DBException(NbBundle.getMessage(DBReadWriteHelper.class, "DBReadWriteHelper_Validate_Invalid", valueObj, colName)); // NOI18N
                    }
                    BinaryToStringConverter.convertBitStringToBytes(valueObj.toString());
                    return valueObj;

                case Types.BINARY:
                case Types.VARBINARY:
                case Types.LONGVARBINARY:
                case Types.BLOB:
                case Types.LONGNVARCHAR:
                case Types.CLOB:
                case Types.OTHER:
                case Types.NCLOB:
                default:
                    return valueObj;
            }
        } catch (RuntimeException | DBException e) {
            String type = col.getTypeName();
            String colName = col.getQualifiedName(false);
            int precision = col.getPrecision();
            throw new DBException(NbBundle.getMessage(DBReadWriteHelper.class, "DBReadWriteHelper_ErrLog", new Object[] {colName, type, precision, e.getLocalizedMessage()}));
        }
    }

    public static boolean isNullString(String str) {
        return (str == null || str.trim().length() == 0);
    }
}
