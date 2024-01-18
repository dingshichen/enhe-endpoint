// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2023-12-28

package com.enhe.endpoint.extend

import com.enhe.endpoint.consts.*
import com.enhe.endpoint.doc.JavaDataTypeConvertor
import com.enhe.endpoint.doc.model.LangDataType
import com.intellij.psi.PsiType

/**
 * 转换成 Java 基础类型枚举
 */
fun PsiType.ofJavaBaseType(): JavaBaseType? {
    return JavaBaseType.of(canonicalText)
}

/**
 * 是否是 Java 基础类型
 */
fun PsiType.isJavaBaseType(): Boolean {
   return JavaBaseType.entries.toTypedArray().any { it.baseName == canonicalText || it.qualifiedName == canonicalText }
}

/**
 * 是否是 Java 简单类型
 */
fun PsiType.isJavaSimpleType(): Boolean {
    return JavaSimpleType.entries.toTypedArray().any { it.qualifiedName == canonicalText }
}

/**
 * 是否是 Java 基础类型集合
 */
fun PsiType.isJavaBaseList(): Boolean {
    return JavaBaseType.entries.toTypedArray().any { it.listName == canonicalText }
}

/**
 * 是否是 Java 简单类型集合
 */
fun PsiType.isJavaSimpleList(): Boolean {
    return JavaSimpleType.entries.toTypedArray().any { it.listName == canonicalText }
}

/**
 * 是否是 Java 日志类型
 */
fun PsiType.isJavaLogType(): Boolean {
    return JavaLogType.entries.toTypedArray().any { it.qualifierName == canonicalText }
}

/**
 * 是否是 Java 带泛型的集合
 */
fun PsiType.isJavaGenericList(): Boolean {
    return canonicalText.startsWith("java.util.List<")
            || canonicalText.startsWith("java.util.ArrayList<")
            || canonicalText.startsWith("java.util.LinkedList<")
            || canonicalText.startsWith("java.util.Set<")
            || canonicalText.startsWith("java.util.HashSet<")
}

/**
 * 是否是 Java Object 类型
 */
fun PsiType.isJavaObjectType(): Boolean {
    return canonicalText == JAVA_OBJ
}

/**
 * 是否是 Java 序列化接口
 */
fun PsiType.isJavaSerializableType(): Boolean {
    return canonicalText == IO_SERIAL
}

/**
 * 是否是任意一种 Java List 类型
 */
fun PsiType.isAnyJavaList(): Boolean {
    return canonicalText.startsWith("java.util.Collection") || canonicalText.startsWith("java.lang.Iterable")
}

/**
 * 是否是上传文件类型
 */
fun PsiType.isMultipartFileType(): Boolean {
    return canonicalText == MULTIPART_FILE
}

/**
 * 转换成 API 数据类型
 */
fun PsiType.convertApiDataType(): LangDataType {
    return JavaDataTypeConvertor.convert(presentableText)
}

/**
 * 是否是 JSON 类型
 */
fun PsiType.isJsonType(): Boolean {
    return canonicalText.endsWith(".JsonObject", true) || canonicalText.endsWith(".JsonArray", true)
}
