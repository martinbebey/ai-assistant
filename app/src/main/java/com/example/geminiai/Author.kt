package com.example.geminiai

import com.stfalcon.chatkit.commons.models.IUser

/**
 * ChatKit author for a message bubble (you vs Gemini).
 * Different [id] values make outgoing vs incoming bubbles render on opposite sides.
 */
data class Author(
    private val id: String,
    private val name: String,
    private val avatar: String? = null,
) : IUser {
    override fun getId(): String = id
    override fun getName(): String = name
    override fun getAvatar(): String? = avatar
}
