package com.example.geminiai

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Tool
import com.google.firebase.ai.type.content
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Holds chat UI state and talks to Gemini.
 *
 * - [_messages] backs the on-screen ChatKit bubbles (survives rotation via this ViewModel).
 * - [chat] is Firebase's multi-turn session (in-memory history for the model).
 * - [events] tells [MainActivity] when to add/update bubbles or speak a finished reply.
 */
class ChatViewModel : ViewModel() {
    val user = Author(id = SENDER_ID, name = "You")
    val assistant = Author(id = AI_ID, name = "Gemini")

    private val _messages = mutableListOf<Message>()
    /** Chronological list of bubbles shown in the UI (oldest first). */
    val messages: List<Message> get() = _messages

    private val _events = MutableSharedFlow<ChatEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<ChatEvent> = _events.asSharedFlow()

    private val generativeModel by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(
                modelName = "gemini-3.1-flash-lite",
                tools = listOf(Tool.googleSearch()),
            )
    }

    /** Multi-turn chat — Firebase keeps prior user/model turns as context for later sends. */
    private val chat by lazy { generativeModel.startChat() }

    init {
        if (_messages.isEmpty()) {
            _messages.add(
                Message(
                    id = UUID.randomUUID().toString(),
                    user = assistant,
                    text = "Welcome! I am Martin, your personal AI agent.",
                )
            )
        }
    }

    /**
     * Adds user bubbles to the UI, then streams a Gemini reply into an assistant bubble.
     *
     * ChatKit shows either an image bubble **or** a text bubble per [Message]
     * (non-null [Message.getImageUrl] selects the image holder). So image + caption
     * are emitted as two messages: image first, then text underneath.
     *
     * @param prompt text sent to Gemini (may be a default when only an image is attached)
     * @param bitmap optional image for multimodal prompts
     * @param bubbleText text shown in the user's text bubble (empty if image-only)
     * @param imagePath local file path for an image bubble, or null for text-only
     */
    fun send(
        prompt: String,
        bitmap: Bitmap? = null,
        bubbleText: String = prompt,
        imagePath: String? = null,
    ) {
        if (imagePath != null) {
            val imageMessage = Message(
                id = UUID.randomUUID().toString(),
                user = user,
                text = "",
                imageUrl = imagePath,
            )
            _messages.add(imageMessage)
            _events.tryEmit(ChatEvent.Added(imageMessage))
        }

        if (bubbleText.isNotBlank()) {
            val textMessage = Message(
                id = UUID.randomUUID().toString(),
                user = user,
                text = bubbleText,
            )
            _messages.add(textMessage)
            _events.tryEmit(ChatEvent.Added(textMessage))
        }

        val reply = Message(
            id = UUID.randomUUID().toString(),
            user = assistant,
            text = "Thinking…",
        )
        _messages.add(reply)
        _events.tryEmit(ChatEvent.Added(reply))

        viewModelScope.launch {
            try {
                // --- CHAT MODE (history + streaming) ---
                val stream = if (bitmap != null) {
                    chat.sendMessageStream(
                        content {
                            image(bitmap)
                            text(prompt)
                        }
                    )
                } else {
                    chat.sendMessageStream(prompt)
                }

                // --- ONE-SHOT MODE (no conversation history) ---
                // Uncomment this block and comment out CHAT MODE above to switch.
                /*
                val stream = if (bitmap != null) {
                    generativeModel.generateContentStream(
                        content {
                            image(bitmap)
                            text(prompt)
                        }
                    )
                } else {
                    generativeModel.generateContentStream(prompt)
                }
                */

                var firstChunk = true
                val built = StringBuilder()
                stream.collect { chunk ->
                    val piece = chunk.text ?: return@collect
                    if (firstChunk) {
                        built.clear()
                        firstChunk = false
                    }
                    built.append(piece)
                    reply.setText(built.toString())
                    _events.tryEmit(ChatEvent.Updated(reply))
                }

                // Full reply ready — ask the UI to speak it (if TTS is enabled)
                if (built.isNotBlank()) {
                    _events.tryEmit(ChatEvent.Speak(built.toString()))
                }
            } catch (e: Exception) {
                reply.setText("Error: ${e.message}")
                _events.tryEmit(ChatEvent.Updated(reply))
            }
        }
    }

    /** One-way events from ViewModel → Activity UI. */
    sealed class ChatEvent {
        data class Added(val message: Message) : ChatEvent()
        data class Updated(val message: Message) : ChatEvent()
        data class Speak(val text: String) : ChatEvent()
    }

    companion object {
        const val SENDER_ID = "user"
        const val AI_ID = "gemini"
    }
}
