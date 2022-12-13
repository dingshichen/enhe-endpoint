// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2022-12-08

package com.enhe.endpoint

// Open Feigin
const val FEIGN_CLIENT = "org.springframework.cloud.openfeign.FeignClient"

// Spring MVC
const val POST_MAPPING = "org.springframework.web.bind.annotation.PostMapping"
const val DELETE_MAPPING = "org.springframework.web.bind.annotation.DeleteMapping"
const val PUT_MAPPING = "org.springframework.web.bind.annotation.PutMapping"
const val GET_MAPPING = "org.springframework.web.bind.annotation.GetMapping"

val REST_MAPPINGS = listOf(POST_MAPPING, DELETE_MAPPING, PUT_MAPPING, GET_MAPPING)