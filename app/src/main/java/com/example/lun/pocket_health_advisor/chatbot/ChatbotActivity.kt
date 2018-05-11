package com.example.lun.pocket_health_advisor.chatbot

import android.app.LoaderManager
import android.content.Context
import android.content.Intent
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
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.example.lun.pocket_health_advisor.MainActivity.Companion.USER_DETAILS
import com.example.lun.pocket_health_advisor.R
import com.example.lun.pocket_health_advisor.R.id.medic_report
import com.example.lun.pocket_health_advisor.medicReport.MedicReportActivity
import com.example.lun.pocket_health_advisor.ulti.DataClassWrapper.AuthUser
import com.example.lun.pocket_health_advisor.ulti.DataClassWrapper.ChatMessage
import com.example.lun.pocket_health_advisor.ulti.DialogflowAsyncWorker
import com.example.lun.pocket_health_advisor.ulti.DialogflowAsyncWorker.Companion.BOT
import com.example.lun.pocket_health_advisor.ulti.DialogflowAsyncWorker.Companion.GET
import com.example.lun.pocket_health_advisor.ulti.DialogflowAsyncWorker.Companion.POST
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.android.synthetic.main.activity_chatbot_acvitity.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.yesButton
import org.json.JSONObject
import java.io.Serializable

// TODO: solve the chat wont get response when in background
class ChatbotActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<ArrayList<ChatMessage>> {
    companion object {
        const val DIALOGFLOW_URL = "https://api.dialogflow.com/v1/query?v=20170712"
        const val LOADER_ID = 1
    }

    private var requestMethod = GET
    private lateinit var authUser: AuthUser
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: FirestoreRecyclerAdapter<ChatMessage, ChatRecord>
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
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECORD_AUDIO), 1)
        title = "Chatbot"
        recyclerView.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.stackFromEnd = true // to add new chat to bottom of recycleview
        linearLayoutManager.isAutoMeasureEnabled = true
        recyclerView.layoutManager = linearLayoutManager
        //make recycleview scroll to bottom when keyboard shows
        recyclerView.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom < oldBottom) {
                //to avoid clash when adapter doesn't have item
                if (recyclerView.adapter.itemCount != 0) {
                    recyclerView.smoothScrollToPosition(recyclerView.adapter.itemCount - 1)
                }
            }
        }

        val auth = FirebaseAuth.getInstance().currentUser
        auth?.displayName?.let { authUser = AuthUser(auth.uid, auth.displayName as String) }

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
                requestMethod = GET
                loaderManager.restartLoader(LOADER_ID, null, this)
                val chatMessage = ChatMessage(message, authUser.name)
                db.collection(getString(R.string.first_col))
                        .document(authUser.id)
                        .collection(getString(R.string.second_col))
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
                    imageViewAnimatedChange(this@ChatbotActivity, fab_img, img)
                    flagFab = false

                } else if (s.toString().trim { it <= ' ' }.length == 0) {
                    imageViewAnimatedChange(this@ChatbotActivity, fab_img, img1)
                    flagFab = true
                }
            }

            override fun afterTextChanged(s: Editable) {

            }
        })

        val query = FirebaseFirestore.getInstance()
                .collection(getString(R.string.first_col))
                .document(authUser.id)
                .collection(getString(R.string.second_col))
                .limit(500)
                .orderBy(getString(R.string.timestamp))
        // TODO: make user to load previous message

        val options = FirestoreRecyclerOptions.Builder<ChatMessage>()
                .setQuery(query, ChatMessage::class.java)
                .build()

        adapter = object : FirestoreRecyclerAdapter<ChatMessage, ChatRecord>(options) {
            override fun onBindViewHolder(viewHolder: ChatRecord, position: Int, model: ChatMessage) {
                Log.d("authUser", "" + model.user)

                if (model.user != BOT) {
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

        alert("post") {
            yesButton { restartLoader() }
        }.show()
    }

    private fun restartLoader() {
        requestMethod = POST
        loaderManager.restartLoader(LOADER_ID, null, this)
    }

    fun imageViewAnimatedChange(c: Context, v: ImageView, new_image: Bitmap) {
        val animOut = AnimationUtils.loadAnimation(c, R.anim.zoom_out)
        val animIn = AnimationUtils.loadAnimation(c, R.anim.zoom_in)
        animOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationRepeat(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                v.setImageBitmap(new_image)
                animIn.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {}

                    override fun onAnimationRepeat(animation: Animation) {}

                    override fun onAnimationEnd(animation: Animation) {}
                })
                v.startAnimation(animIn)
            }
        })
        v.startAnimation(animOut)
    }

    private fun getMap(chatMessage: ChatMessage): HashMap<String, Any> {
        val data = HashMap<String, Any>()
        data[getString(R.string.message_field)] = chatMessage.message
        data[getString(R.string.user_field)] = chatMessage.user
        data[getString(R.string.timestamp)] = FieldValue.serverTimestamp()
        return data
    }

    private fun constructPostData(): String {
        return JSONObject()
                .put("lang", "en")
                .put("event", JSONObject().put("name", "WELCOME"))
                .put("sessionId", authUser.id)
                .toString()
    }

    private fun constructUrl(): String {
        val baseUri = Uri.parse(DIALOGFLOW_URL)
        val uriBuilder = baseUri.buildUpon()
        return uriBuilder.appendQueryParameter("lang", "en")
                .appendQueryParameter("query", queryText)
                .appendQueryParameter("sessionId", authUser.id)
                .build()
                .toString()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chatbot_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId

        when (id) {
            medic_report -> {
                val intent = Intent(this, MedicReportActivity::class.java)
                intent.putExtra(USER_DETAILS, authUser as Serializable)
                intent.putExtras(intent)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onLoaderReset(loader: Loader<ArrayList<ChatMessage>>?) {
        Log.d("Load Reset", "abc")
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<ArrayList<ChatMessage>> {
        Log.d("onCreateLoader", "loader created")
        return if (requestMethod == GET) {
            DialogflowAsyncWorker(applicationContext, constructUrl(), GET)
        } else {
            DialogflowAsyncWorker(applicationContext, DIALOGFLOW_URL, POST, constructPostData())
        }
    }

    override fun onLoadFinished(loader: Loader<ArrayList<ChatMessage>>?, data: ArrayList<ChatMessage>?) {
        for (chatMessage in data.orEmpty()) {
            Log.d("OnLoadFinish", chatMessage.message)
            db.collection(getString(R.string.first_col))
                    .document(authUser.id)
                    .collection(getString(R.string.second_col))
                    .add(getMap(chatMessage))
        }
    }
}