// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-01-19

package com.enhe.endpoint

import com.google.common.base.CaseFormat
import kotlin.test.Test
import kotlin.test.assertEquals

class MybatisGenerateTest {

    @Test
    fun caseTableToModel() {
        val table = "std_obj_type"
        val result = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, table)
        assertEquals("StdObjType", result)
    }
}