package com.example.lun.pocket_health_advisor

import ai.api.AIDataService
import ai.api.AIListener
import ai.api.AIServiceException
import ai.api.android.AIConfiguration
import ai.api.android.AIService
import ai.api.model.AIRequest
import ai.api.model.AIResponse
import ai.api.model.ResponseMessage
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.android.synthetic.main.activity_chatbot_acvitity.*
import java.util.*

class ChatbotActivity : AppCompatActivity(), AIListener {

    //create empty constructor for firestore recycleview
    data class ChatMessage(var message: String = "", var user: String = "")

    lateinit var user : MainActivity.User
    lateinit var db: FirebaseFirestore
    lateinit var adapter: FirestoreRecyclerAdapter<ChatMessage, ChatRecord>
    internal var flagFab: Boolean? = true

    private var aiService: AIService? = null

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatbot_acvitity)
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECORD_AUDIO), 1)

        var user  = intent.getSerializableExtra(MainActivity.USER_DETAILS) as MainActivity.User

        recyclerView.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.stackFromEnd = true
        linearLayoutManager.isAutoMeasureEnabled = true
        recyclerView.layoutManager = linearLayoutManager

        db = FirebaseFirestore.getInstance()


        val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        db.firestoreSettings = settings

        val config = AIConfiguration("47836bc8e2494eabb7ea945d1b227d29",
                ai.api.AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System)

        aiService = AIService.getService(this, config)
        aiService!!.setListener(this)

        val aiDataService = AIDataService(config)

        val aiRequest = AIRequest()


        addBtn.setOnClickListener {
            val message = editText.text.toString().trim { it <= ' ' }

            if (message != "") {

                val chatMessage = ChatMessage(message, user.name)
                val data = HashMap<String, Any>()
                data.put("message", chatMessage.message)
                data.put("user", chatMessage.user)
                data.put("timestamp", FieldValue.serverTimestamp())
                db.collection("patients").document(user.id).collection("chat_data")
                        .add(data)


                aiRequest.setQuery(message)
                object : AsyncTask<AIRequest, Void, AIResponse>() {

                    override fun doInBackground(vararg aiRequests: AIRequest): AIResponse? {
                        val request = aiRequests[0]
                        try {
                            return aiDataService.request(aiRequest)
                        } catch (e: AIServiceException) {
                        }

                        return null
                    }

                    override fun onPostExecute(response: AIResponse?) {
                        if (response != null) {

                            val result = response.result
                            val messages = result.fulfillment.messages
                            for (message1 in messages) {
                                if (message1 is ResponseMessage.ResponseSpeech) {
                                    for (message3 in message1.getSpeech()) {
                                        val chatMessage = ChatMessage(message3, "bot")
                                        Log.d("message3: ", message3)
                                        val data = HashMap<String, Any>()
                                        data.put("message", chatMessage.message)
                                        data.put("user", chatMessage.user)
                                        data.put("timestamp", FieldValue.serverTimestamp())
                                        db.collection("patients").document(user.id).collection("chat_data")
                                                .add(data)
                                                .addOnFailureListener { e -> Log.w("Db", "Error updating document", e) }
                                    }

                                }

                            }


                        }
                    }
                }.execute(aiRequest)
            } else {
                aiService!!.startListening()
            }

            editText.setText("")
        }


        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val fab_img = findViewById(R.id.fab_img) as ImageView
                val img = BitmapFactory.decodeResource(resources, R.drawable.ic_send_white_24dp)
                val img1 = BitmapFactory.decodeResource(resources, R.drawable.ic_mic_white_24dp)


                if (s.toString().trim { it <= ' ' }.length != 0 && flagFab!!) {
                    ImageViewAnimatedChange(this@ChatbotActivity, fab_img, img)
                    flagFab = false

                } else if (s.toString().trim { it <= ' ' }.length == 0) {
                    ImageViewAnimatedChange(this@ChatbotActivity, fab_img, img1)
                    flagFab = true

                }


            }

            override fun afterTextChanged(s: Editable) {

            }
        })

        val query = FirebaseFirestore.getInstance()
                .collection("patients").document(user.id).collection("chat_data").limit(100)
                .orderBy("timestamp")

        val options = FirestoreRecyclerOptions.Builder<ChatMessage>()
                .setQuery(query, ChatMessage::class.java)
                .build()

        adapter = object : FirestoreRecyclerAdapter<ChatMessage, ChatRecord>(options) {
            override fun onBindViewHolder(viewHolder: ChatRecord, position: Int, model: ChatMessage) {
                Log.d("user", "" + model.user)

                if (model.user == user.name) {
                    Log.d("model", "" + model.message)
                    viewHolder.rightText.text = model.message

                    viewHolder.rightText.visibility = View.VISIBLE
                    viewHolder.leftText.visibility = View.GONE
                } else {
                    viewHolder.leftText.text = model.message

                    viewHolder.rightText.visibility = View.GONE
                    viewHolder.leftText.visibility = View.VISIBLE
                }


            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRecord {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.message_view, parent, false)

                return ChatRecord(view)
            }
        }

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)

                val msgCount = adapter.itemCount
                val lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition()

                if (lastVisiblePosition == -1 || positionStart >= msgCount - 1 && lastVisiblePosition == positionStart - 1) {
                    recyclerView.scrollToPosition(positionStart)

                }

            }
        })

        recyclerView.adapter = adapter

    }

    fun ImageViewAnimatedChange(c: Context, v: ImageView, new_image: Bitmap) {
        val anim_out = AnimationUtils.loadAnimation(c, R.anim.zoom_out)
        val anim_in = AnimationUtils.loadAnimation(c, R.anim.zoom_in)
        anim_out.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationRepeat(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                v.setImageBitmap(new_image)
                anim_in.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {}

                    override fun onAnimationRepeat(animation: Animation) {}

                    override fun onAnimationEnd(animation: Animation) {}
                })
                v.startAnimation(anim_in)
            }
        })
        v.startAnimation(anim_out)
    }

    override fun onResult(response: ai.api.model.AIResponse) {


        val result = response.result

        val message = result.resolvedQuery
        val chatMessage0 = ChatMessage(message, "user")
        val data = HashMap<String, Any>()
        data.put("message", chatMessage0.message)
        data.put("user", chatMessage0.user)
        data.put("timestamp", FieldValue.serverTimestamp())
        db.collection("patients").document(user.id).collection("chat_data")
                .add(data)

        val reply = result.fulfillment.speech
        val chatMessage = ChatMessage(reply, "bot")
        val data2 = HashMap<String, Any>()
        data.put("message", chatMessage.message)
        data.put("user", chatMessage.user)
        data.put("timestamp", FieldValue.serverTimestamp())
        db.collection("patients").document(user.id).collection("chat_data")
                .add(data2)
    }

    override fun onError(error: ai.api.model.AIError) {

    }

    override fun onAudioLevel(level: Float) {

    }

    override fun onListeningStarted() {

    }

    override fun onListeningCanceled() {

    }

    override fun onListeningFinished() {

    }
}
