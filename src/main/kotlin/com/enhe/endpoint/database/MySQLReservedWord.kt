// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-01-20

package com.enhe.endpoint.database

object MySQLReservedWord {

    private val words = setOf("add","all","alter","analyze","and","as","asc","asensitive","before","between","bigint",
        "binary","blob","both","by","call","cascade","case","change","char","character","check","collate","column",
        "condition","connection","constraint","continue","convert","create","cross","current_date","current_time",
        "current_timestamp","current_user","cursor","database","databases","day_hour","day_microsecond","day_minute",
        "day_second","dec","decimal","declare","default","delayed","delete","desc","describe","deterministic",
        "distinct","distinctrow","div","double","drop","dual","each","else","elseif","enclosed","escaped","exists",
        "exit","explain","false","fetch","float","float4","float8","for","force","foreign","from","fulltext","goto",
        "grant","group","having","high_priority","hour_microsecond","hour_minute","hour_second","if","ignore","in",
        "index","infile","inner","inout","insensitive","insert","int","int1","int2","int3","int4","int8","integer",
        "interval","into","is","iterate","join","key","keys","kill","label","leading","leave","left","like","limit",
        "linear","lines","load","localtime","localtimestamp","lock","long","longblob","longtext","loop","low_priority",
        "match","mediumblob","mediumint","mediumtext","middleint","minute_microsecond","minute_second","mod","modifies",
        "natural","not","no_write_to_binlog","null","numeric","on","optimize","option","optionally","or","order","out",
        "outer","outfile","precision","primary","procedure","purge","raid0","range","read","reads","real","references",
        "regexp","release","rename","repeat","replace","require","restrict","return","revoke","right","rlike","schema",
        "schemas","second_microsecond","select","sensitive","separator","set","show","smallint","spatial","specific",
        "sql","sqlexception","sqlstate","sqlwarning","sql_big_result","sql_calc_found_rows","sql_small_result","ssl",
        "starting","straight_join","table","terminated","then","tinyblob","tinyint","tinytext","to","trailing",
        "trigger","true","undo","union","unique","unlock","unsigned","update","usage","use","using","utc_date",
        "utc_time","utc_timestamp","values","varbinary","varchar","varcharacter","varying","when","where","while",
        "with","write","x509","xor","year_month","zerofill")

    /**
     * 是否是保留字
     */
    fun isReservedWord(columnName: String) = words.contains(columnName.lowercase())

    /**
     * 包装保留字
     */
    fun wrapReservedWord(columnName: String) = if (isReservedWord(columnName)) "`$columnName`" else columnName
}