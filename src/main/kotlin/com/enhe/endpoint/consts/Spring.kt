// @author  ding.shichen
// @email   foreverhuiqiao@126.com
// @date    2022-12-08

package com.enhe.endpoint.consts

// OpenFeign
const val FEIGN_CLIENT = "org.springframework.cloud.openfeign.FeignClient"

// SpringMVC
const val POST_MAPPING = "org.springframework.web.bind.annotation.PostMapping"
const val DELETE_MAPPING = "org.springframework.web.bind.annotation.DeleteMapping"
const val PUT_MAPPING = "org.springframework.web.bind.annotation.PutMapping"
const val GET_MAPPING = "org.springframework.web.bind.annotation.GetMapping"

// Spring
const val SERVICE = "org.springframework.stereotype.Service"
const val PRIMARY = "org.springframework.context.annotation.Primary"
const val AUTOWIRED = "org.springframework.beans.factory.annotation.Autowired"
const val TRANS = "org.springframework.transaction.annotation.Transactional"
const val REPO = "org.springframework.stereotype.Repository"

val REST_MAPPINGS = listOf(POST_MAPPING, DELETE_MAPPING, PUT_MAPPING, GET_MAPPING)

const val EVENT_LISTENER = "org.springframework.context.event.EventListener"