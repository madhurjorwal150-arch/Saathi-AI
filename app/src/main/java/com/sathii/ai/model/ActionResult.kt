package com.sathii.ai.model

data class ActionResult(
    val success: Boolean,
    val actionName: String,
    val message: String,
    val error: String? = null,
    val requiresPermission: Boolean = false
)
