package com.example.geminiai

import com.stfalcon.chatkit.commons.models.IMessage
import com.stfalcon.chatkit.commons.models.MessageContentType
import java.util.Date

/**
 * One ChatKit bubble. Implements [MessageContentType.Image] so a non-null [imageUrl]
 * renders as an image message; otherwise ChatKit shows [text].
 */
class Message(
    private val id: String,
    private val user: Author,
    private var text: String,
    private val createdAt: Date = Date(),
    private val imageUrl: String? = null,
) : IMessage, MessageContentType.Image {
    override fun getId(): String = id
    override fun getText(): String = text
    override fun getUser(): Author = user
    override fun getCreatedAt(): Date = createdAt

    /** Non-null → ChatKit shows an image bubble instead of a text bubble. */
    override fun getImageUrl(): String? = imageUrl

    /** Updates bubble text (used while streaming Gemini's reply). */
    fun setText(value: String) {
        text = value
    }
}
