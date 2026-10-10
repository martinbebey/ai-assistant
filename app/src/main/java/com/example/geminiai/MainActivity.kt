package com.example.geminiai

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.ImageViewCompat
import androidx.lifecycle.lifecycleScope
import com.stfalcon.chatkit.commons.ImageLoader
import com.stfalcon.chatkit.messages.MessagesList
import com.stfalcon.chatkit.messages.MessagesListAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.UUID

/**
 * Main chat screen: text/image input, speech-to-text mic, streaming Gemini replies,
 * and a toolbar toggle for text-to-speech (spoken replies).
 *
 * Speech recognition flow:
 * 1. [startRecognition] launches Google's [RecognizerIntent.ACTION_RECOGNIZE_SPEECH] UI.
 * 2. When that UI finishes, [speechRecognition] receives the result Intent.
 * 3. Spoken text is read from [RecognizerIntent.EXTRA_RESULTS], put in the input field,
 *    then [sendMessage] sends it to Gemini.
 *
 * Note: [startRecognition] itself returns nothing — it only starts the recognizer Activity.
 */
class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var searchField: EditText
    private lateinit var sendBtn: ImageButton
    private lateinit var attachImageBtn: ImageButton
    private lateinit var micBtn: ImageButton
    private lateinit var sendCard: CardView
    private lateinit var micCard: CardView
    private lateinit var ttsToggleBtn: ImageButton
    private lateinit var messagesList: MessagesList
    private lateinit var messagesAdapter: MessagesListAdapter<Message>

    private val chatViewModel: ChatViewModel by viewModels()
    private var selectedBitmap: Bitmap? = null

    lateinit var tts: TextToSpeech
    /** True if the device TTS engine initialized and supports the chosen language. */
    private var ttsIsAvailable: Boolean = true
    /** User preference from the toolbar speaker icon — whether replies should be spoken. */
    private var ttsEnabled: Boolean = true
    /** Explicit British voice chosen at init; re-applied before each speak. */
    private var britishVoice: Voice? = null
    /** All usable UK English voices (for cycling until the accent sounds right). */
    private var britishVoices: List<Voice> = emptyList()
    private var britishVoiceIndex: Int = 0

    /**
     * System photo picker. On success, decodes the chosen image into [selectedBitmap].
     */
    private val pickMedia = registerForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) {
            Log.d(TAG, "Selected image URI: $uri")
            loadBitmapFromUri(uri)
        } else {
            Log.d(TAG, "No image selected")
        }
    }

    /**
     * Callback for the speech-recognition Activity.
     * This is where transcribed text is actually retrieved and sent — not in [startRecognition].
     */
    private val speechRecognition = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            Log.d(TAG, "Speech recognition cancelled or failed (resultCode=${result.resultCode})")
            Toast.makeText(this, "No speech captured", Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }

        // Google returns a ranked list of possible transcriptions; take the top one
        val spokenText = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
            ?.trim()

        if (spokenText.isNullOrEmpty()) {
            Log.d(TAG, "Speech recognition returned empty EXTRA_RESULTS")
            Toast.makeText(this, "Could not understand speech", Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }

        Log.d(TAG, "Speech recognized: $spokenText")
        searchField.setText(spokenText)
        sendMessage()
    }

    /**
     * Requests [Manifest.permission.RECORD_AUDIO] before opening the recognizer.
     * Without this grant on modern Android, the recognizer often returns nothing.
     */
    private val requestMicPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchSpeechRecognizer()
        } else {
            Toast.makeText(this, "Microphone permission is required for voice input", Toast.LENGTH_LONG)
                .show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Pad for status/nav bars and keep the input row above the keyboard
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                maxOf(systemBars.bottom, ime.bottom),
            )
            insets
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        searchField = findViewById(R.id.editText)
        sendBtn = findViewById(R.id.imageButton)
        attachImageBtn = findViewById(R.id.attachImageButton)
        micBtn = findViewById(R.id.micButton)
        sendCard = findViewById(R.id.cardView2)
        micCard = findViewById(R.id.micCard)
        ttsToggleBtn = findViewById(R.id.ttsToggleButton)
        messagesList = findViewById(R.id.messagesList)
        updateTtsButton()

        setupMessagesList()
        observeChatEvents()

        attachImageBtn.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        }

        sendBtn.setOnClickListener { sendMessage() }
        sendCard.setOnClickListener { sendMessage() }

        // Bottom mic = speech-to-text (listen). Toolbar speaker = TTS (talk).
        micBtn.setOnClickListener { startRecognition() }
        micCard.setOnClickListener { startRecognition() }
        ttsToggleBtn.setOnClickListener {
            ttsEnabled = !ttsEnabled
            if (!ttsEnabled && ::tts.isInitialized) {
                tts.stop()
            }
            updateTtsButton()
        }

        // Force Google's TTS engine — system default may ignore locale/voice changes
        tts = TextToSpeech(application, this, GOOGLE_TTS_ENGINE)
    }

    /**
     * Entry point for the bottom mic button.
     * Checks mic permission, then opens the system speech UI via [launchSpeechRecognizer].
     * Does not return transcribed text — that arrives later in [speechRecognition].
     */
    private fun startRecognition() {
        val hasMicPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED

        if (hasMicPermission) {
            launchSpeechRecognizer()
        } else {
            requestMicPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    /**
     * Builds and launches [RecognizerIntent.ACTION_RECOGNIZE_SPEECH].
     * Result is delivered to [speechRecognition].
     */
    private fun launchSpeechRecognizer() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "GeminiAi")
        }
        try {
            speechRecognition.launch(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                this,
                "Speech recognition not available on this device",
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    /**
     * Sends the current text field (and optional attached image) to Gemini via [ChatViewModel].
     */
    private fun sendMessage() {
        val typed = searchField.text.toString().trim()
        val bitmap = selectedBitmap
        if (typed.isEmpty() && bitmap == null) return

        searchField.text.clear()
        val imagePath = bitmap?.let { saveBitmapToCache(it) }
        clearSelectedImage()
        chatViewModel.send(
            prompt = typed.ifEmpty { "What is in this image?" },
            bitmap = bitmap,
            // Image-only: empty bubble text so ChatKit shows the photo, not the default prompt
            bubbleText = typed,
            imagePath = imagePath,
        )
    }

    /** Green speaker when TTS is on; red speaker with strikethrough when off. */
    private fun updateTtsButton() {
        if (ttsEnabled) {
            ttsToggleBtn.setImageResource(R.drawable.ic_tts_speaker)
            ImageViewCompat.setImageTintList(
                ttsToggleBtn,
                ColorStateList.valueOf(
                    ContextCompat.getColor(this, android.R.color.holo_green_light),
                ),
            )
        } else {
            // Off icon is already red + strikethrough in the vector
            ttsToggleBtn.setImageResource(R.drawable.ic_tts_speaker_off)
            ImageViewCompat.setImageTintList(ttsToggleBtn, null)
        }
    }

    /**
     * Loads a [Bitmap] from a content [Uri] on a background thread and marks the attach button.
     */
    private fun loadBitmapFromUri(uri: Uri) {
        lifecycleScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            }
            if (bitmap != null) {
                selectedBitmap = bitmap
                attachImageBtn.imageTintList = ContextCompat.getColorStateList(
                    this@MainActivity,
                    android.R.color.holo_green_dark,
                )
                Toast.makeText(this@MainActivity, "Image attached", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "Could not load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Writes [bitmap] to app cache and returns the absolute file path for ChatKit image bubbles.
     */
    private fun saveBitmapToCache(bitmap: Bitmap): String {
        val dir = File(cacheDir, "chat_images").apply { mkdirs() }
        val file = File(dir, "${UUID.randomUUID()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        return file.absolutePath
    }

    /**
     * Clears any pending image attachment and resets the attach-button tint.
     */
    private fun clearSelectedImage() {
        selectedBitmap = null
        attachImageBtn.imageTintList = ContextCompat.getColorStateList(
            this,
            android.R.color.darker_gray,
        )
    }

    /**
     * Wires ChatKit's [MessagesList] to an adapter and restores bubbles from the ViewModel
     * (needed after rotation).
     */
    private fun setupMessagesList() {
        val imageLoader = ImageLoader { imageView, url, _ ->
            if (url.isNullOrBlank()) {
                imageView.setImageDrawable(null)
            } else {
                imageView.setImageBitmap(BitmapFactory.decodeFile(url))
            }
        }
        messagesAdapter = MessagesListAdapter(ChatViewModel.SENDER_ID, imageLoader)
        messagesList.setAdapter(messagesAdapter)

        // MessagesList is reverse-layout: index 0 = bottom (newest).
        // addToStart in oldest→newest order keeps the welcome message at the top.
        chatViewModel.messages.forEach { message ->
            messagesAdapter.addToStart(message, false)
        }
    }

    /**
     * Collects UI events from [ChatViewModel]: new bubbles, streamed text updates, and TTS.
     */
    private fun observeChatEvents() {
        lifecycleScope.launch {
            chatViewModel.events.collect { event ->
                when (event) {
                    is ChatViewModel.ChatEvent.Added ->
                        messagesAdapter.addToStart(event.message, true)
                    is ChatViewModel.ChatEvent.Updated ->
                        messagesAdapter.update(event.message)
                    is ChatViewModel.ChatEvent.Speak ->
                        speakReply(event.text)
                }
            }
        }
    }

    /**
     * Speaks a finished Gemini reply if the engine is ready and the user left TTS enabled.
     */
    private fun speakReply(text: String) {
        if (!ttsEnabled || !ttsIsAvailable || !::tts.isInitialized) return
        applyBritishVoice()
        val ok = tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "geminiReply")
        Log.d(
            TAG,
            "speak()=$ok voice=${tts.voice?.name} locale=${tts.voice?.locale} " +
                "engine=${tts.defaultEngine}",
        )
    }

    private fun applyBritishVoice() {
        val voice = britishVoice ?: return
        // setLanguage after setVoice can wipe the accent — voice only here
        val result = tts.setVoice(voice)
        tts.setSpeechRate(1.0f)
        tts.setPitch(1.0f)
        if (result != TextToSpeech.SUCCESS) {
            Log.w(TAG, "setVoice(${voice.name}) failed: $result")
        }
    }

    /**
     * [TextToSpeech.OnInitListener] callback — forces an en-GB Google voice when installed.
     */
    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) {
            Log.e(TAG, "TTS engine failed to initialize (status=$status)")
            ttsIsAvailable = false
            return
        }

        Log.d(TAG, "TTS engine ready: ${tts.defaultEngine}")

        val allVoices = tts.voices.orEmpty()
        val englishVoices = allVoices.filter {
            it.locale.language.equals("en", ignoreCase = true)
        }
        Log.d(TAG, "English voices available (${englishVoices.size}):")
        englishVoices.forEach { voice ->
            Log.d(
                TAG,
                "  ${voice.name} locale=${voice.locale} " +
                    "network=${voice.isNetworkConnectionRequired} quality=${voice.quality} " +
                    "features=${voice.features}",
            )
        }

        britishVoices = listBritishVoices(allVoices)
        Log.d(TAG, "Ordered UK voice list: ${britishVoices.map { it.name }}")

        if (britishVoices.isNotEmpty()) {
            britishVoiceIndex = 0
            britishVoice = britishVoices[0]
            applyBritishVoice()
            Log.d(
                TAG,
                "Active UK voice=${tts.voice?.name} features=${tts.voice?.features}",
            )
        } else {
            val languageResult = tts.setLanguage(Locale.UK)
            ttsIsAvailable = languageResult != TextToSpeech.LANG_MISSING_DATA &&
                languageResult != TextToSpeech.LANG_NOT_SUPPORTED
            Log.w(TAG, "No UK voices found; setLanguage(UK)=$languageResult")
            Toast.makeText(
                this,
                "No UK TTS voices found. Open Speech Recognition and Synthesis settings.",
                Toast.LENGTH_LONG,
            ).show()
        }
    }

    /**
     * Builds an ordered list of English (UK) voices.
     *
     * Prefer voices that do **not** carry [TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED]
     * (those often fall back to a non-British synthesizer). Local before network;
     * known Google UK ids first.
     */
    private fun listBritishVoices(voices: Collection<Voice>): List<Voice> {
        fun isEnglishUk(voice: Voice): Boolean {
            val name = voice.name.lowercase(Locale.ROOT)
            if (name.startsWith("cy-") || name.contains("cy-gb")) return false // Welsh
            val country = voice.locale.country
            return country.equals("GB", ignoreCase = true) ||
                name.contains("en-gb") ||
                name.contains("eng-gbr")
        }

        fun isMarkedInstalled(voice: Voice) =
            !voice.features.contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED)

        // Prefer network UK voices when local packs are broken/missing (notInstalled).
        // On this device local en-gb superpacks fail to sync and TTS silently falls back to en-US.
        val preferredOrder = listOf(
            "en-gb-x-gbb-network",
            "en-gb-x-gba-network",
            "en-gb-x-gbc-network",
            "en-gb-x-gbd-network",
            "en-gb-x-gbg-network",
            "en-gb-x-rjs-network",
            "en-gb-x-gbb-local",
            "en-gb-x-gba-local",
            "en-gb-x-gbc-local",
            "en-gb-x-gbd-local",
            "en-gb-x-gbg-local",
            "en-gb-x-rjs-local",
            "en-GB-language",
        )

        val uk = voices.filter(::isEnglishUk)
        val installed = uk.filter(::isMarkedInstalled)
        // If nothing is marked installed (common when en-gb packs are missing), still use UK
        // voices — prefer network so Google synthesizes British audio on the server.
        val pool = uk
        Log.d(
            TAG,
            "UK voices installed-flag=${installed.map { it.name }} " +
                "using pool=${pool.map { it.name }}",
        )

        val ranked = ArrayList<Voice>()
        preferredOrder.forEach { preferred ->
            pool.firstOrNull { it.name.equals(preferred, ignoreCase = true) }?.let { ranked.add(it) }
        }
        // Installed locals first among leftovers, then other locals, then remaining network
        pool.filter { candidate ->
            ranked.none { it.name.equals(candidate.name, ignoreCase = true) }
        }
            .sortedWith(
                compareBy<Voice> { !isMarkedInstalled(it) }
                    .thenBy { !it.isNetworkConnectionRequired } // after preferred network list, prefer local leftovers
                    .thenByDescending { it.quality }
                    .thenBy { it.name },
            )
            .forEach { ranked.add(it) }
        return ranked
    }

    /**
     * Releases the TTS engine when the Activity is destroyed.
     */
    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val GOOGLE_TTS_ENGINE = "com.google.android.tts"
    }
}
