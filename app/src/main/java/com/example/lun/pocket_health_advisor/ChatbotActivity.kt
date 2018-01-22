package com.example.lun.pocket_health_advisor

import android.app.LoaderManager
import android.content.Context
import android.content.Loader
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.android.synthetic.main.activity_chatbot_acvitity.*

class ChatbotActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<ArrayList<ChatbotActivity.ChatMessage>> {
    companion object {
        val DIALOGFLOW_URL = "https://api.dialogflow.com/v1/query?v=20170712&lang=en"
        val LOADER_ID = 1
    }
    //create empty constructor for firestore recycleview
    data class ChatMessage(var message: String = "", var user: String = "")

    lateinit var user: MainActivity.User
    lateinit var db: FirebaseFirestore
    lateinit var adapter: FirestoreRecyclerAdapter<ChatMessage, ChatRecord>
    internal var flagFab: Boolean? = true
    private lateinit var queryText: String

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

        user = intent.getSerializableExtra(MainActivity.USER_DETAILS) as MainActivity.User

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

        addBtn.setOnClickListener {
            val message = editText.text.toString().trim { it <= ' ' }

            if (message != "") {
                queryText = message
                Log.d("Init loader", "loader initiated")
                loaderManager.restartLoader(LOADER_ID, null, this)
                val chatMessage = ChatMessage(message, user.name)
                db.collection("patients").document(user.id).collection("chat_data")
                        .add(getMap(chatMessage))
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

    fun getMap(chatMessage: ChatMessage): HashMap<String, Any> {
        val data = HashMap<String, Any>()
        data.put("message", chatMessage.message)
        data.put("user", chatMessage.user)
        data.put("timestamp", FieldValue.serverTimestamp())
        return data
    }

    override fun onLoaderReset(loader: Loader<ArrayList<ChatMessage>>?) {
        Log.d("Load Reset", "abc")
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<ArrayList<ChatMessage>> {
        Log.d("onCreateLoader", "loader created")
        var baseUri = Uri.parse(DIALOGFLOW_URL)
        var uriBuilder = baseUri.buildUpon()
        uriBuilder.appendQueryParameter("query", queryText)
                .appendQueryParameter("sessionId", user.id)

        return DialogflowAsyncWorker(applicationContext, uriBuilder.build().toString())
    }

    override fun onLoadFinished(loader: Loader<ArrayList<ChatMessage>>?, data: ArrayList<ChatMessage>?) {
        for (i in data.orEmpty()) {
            Log.d("OnLoadFinish", i.message)
            var chatMessage = i
            db.collection("patients").document(user.id).collection("chat_data")
                    .add(getMap(chatMessage))
        }
    }
}