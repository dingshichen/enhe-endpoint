// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-01-07

package com.enhe.endpoint.database

enum class MysqlColumnType {
    // 数字类型
    TINYINT,
    SMALLINT,
    MEDIUMINT,
    INT,
    INTEGER,
    BIGINT,
    FLOAT,
    DOUBLE,
    DECIMAL,
    // 时间类型
    DATE,
    TIME,
    YEAR,
    DATETIME,
    TIMESTAMP,
    // 字符串类型
    CHAR,
    VARCHAR,
    TINYBLOB,
    TINYTEXT,
    BLOB,
    TEXT,
    MEDIUMBLOB,
    MEDIUMTEXT,
    LONGBLOB,
    LONGTEXT,
    // 其他
    BIT,
    JSON,
    ;

    companion object {

        @JvmStatic
        fun of(typeName: String): MysqlColumnType {
            return valueOf(typeName.uppercase())
        }
    }

    fun toJavaType(): JavaMapperType {
        return when(this) {
            TINYINT -> JavaMapperType.INTEGER
            SMALLINT -> JavaMapperType.INTEGER
            MEDIUMINT -> JavaMapperType.INTEGER
            INT -> JavaMapperType.INTEGER
            INTEGER -> JavaMapperType.INTEGER
            BIGINT -> JavaMapperType.LONG
            FLOAT -> JavaMapperType.FLOAT
            DOUBLE -> JavaMapperType.DOUBLE
            DECIMAL -> JavaMapperType.BIGDECIMAL
            DATE -> JavaMapperType.DATE
            TIME -> JavaMapperType.DATE
            YEAR -> JavaMapperType.DATE
            DATETIME -> JavaMapperType.DATE
            TIMESTAMP -> JavaMapperType.DATE
            CHAR -> JavaMapperType.STRING
            VARCHAR -> JavaMapperType.STRING
            TINYBLOB -> JavaMapperType.STRING
            TINYTEXT -> JavaMapperType.STRING
            BLOB -> JavaMapperType.STRING
            TEXT -> JavaMapperType.STRING
            MEDIUMBLOB -> JavaMapperType.STRING
            MEDIUMTEXT -> JavaMapperType.STRING
            LONGBLOB -> JavaMapperType.STRING
            LONGTEXT -> JavaMapperType.STRING
            BIT -> JavaMapperType.BOOLEAN
            JSON -> JavaMapperType.JSONOBJECT
        }
    }

}