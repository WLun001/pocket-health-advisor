package com.example.lun.pocket_health_advisor;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import ai.api.AIDataService;
import ai.api.AIListener;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.ResponseMessage;
import ai.api.model.Result;

public class ChatbotActivity extends AppCompatActivity implements AIListener {
    RecyclerView recyclerView;
    EditText editText;
    RelativeLayout addBtn;
    FirebaseFirestore db;
    FirestoreRecyclerAdapter<ChatMessage, ChatRecord> adapter;
    Boolean flagFab = true;

    private AIService aiService;

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot_acvitity);
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO}, 1);


        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        editText = (EditText) findViewById(R.id.editText);
        addBtn = (RelativeLayout) findViewById(R.id.addBtn);

        recyclerView.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        db = FirebaseFirestore.getInstance();

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        final AIConfiguration config = new AIConfiguration("47836bc8e2494eabb7ea945d1b227d29",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        aiService = AIService.getService(this, config);
        aiService.setListener(this);

        final AIDataService aiDataService = new AIDataService(config);

        final AIRequest aiRequest = new AIRequest();


        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String message = editText.getText().toString().trim();

                if (!message.equals("")) {

                    ChatMessage chatMessage = new ChatMessage(message, "user");
                    db.collection("patients").document("patient1").collection("chat_data")
                            .add(chatMessage);


                    aiRequest.setQuery(message);
                    new AsyncTask<AIRequest, Void, AIResponse>() {

                        @Override
                        protected AIResponse doInBackground(AIRequest... aiRequests) {
                            final AIRequest request = aiRequests[0];
                            try {
                                final AIResponse response = aiDataService.request(aiRequest);
                                return response;
                            } catch (AIServiceException e) {
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(AIResponse response) {
                            if (response != null) {

                                Result result = response.getResult();
                                List<ResponseMessage> messages = result.getFulfillment().getMessages();
                                for (ResponseMessage message1 : messages) {
                                    if (message1 instanceof ResponseMessage.ResponseSpeech) {
                                        ResponseMessage.ResponseSpeech message2 = (ResponseMessage.ResponseSpeech) message1;
                                        for (String message3 : message2.getSpeech()) {
                                            ChatMessage chatMessage = new ChatMessage(message3, "bot");
                                            Log.d("message3: ", message3);
                                            db.collection("patients").document("patient1").collection("chat_data")
                                                    .add(
                                                            chatMessage
                                                    )
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.w("Db", "Error updating document", e);
                                                        }
                                                    });
                                        }

                                    }

                                }


                            }
                        }
                    }.execute(aiRequest);
                } else {
                    aiService.startListening();
                }

                editText.setText("");

            }
        });


        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ImageView fab_img = (ImageView) findViewById(R.id.fab_img);
                Bitmap img = BitmapFactory.decodeResource(getResources(), R.drawable.ic_send_white_24dp);
                Bitmap img1 = BitmapFactory.decodeResource(getResources(), R.drawable.ic_mic_white_24dp);


                if (s.toString().trim().length() != 0 && flagFab) {
                    ImageViewAnimatedChange(ChatbotActivity.this, fab_img, img);
                    flagFab = false;

                } else if (s.toString().trim().length() == 0) {
                    ImageViewAnimatedChange(ChatbotActivity.this, fab_img, img1);
                    flagFab = true;

                }


            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        Query query = FirebaseFirestore.getInstance()
                .collection("patients").document("patient1").collection("chat_data").limit(100)
                .orderBy(FieldPath.documentId());

        FirestoreRecyclerOptions<ChatMessage> options = new FirestoreRecyclerOptions.Builder<ChatMessage>()
                .setQuery(query, ChatMessage.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<ChatMessage, ChatRecord>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ChatRecord viewHolder, int position, @NonNull ChatMessage model) {
                Log.d("user", "" + model.getMsgUser());

                    if (model.getMsgUser().equals("user")) {
                        Log.d("model", "" + model.getMsgText());
                        viewHolder.rightText.setText(model.getMsgText());

                        viewHolder.rightText.setVisibility(View.VISIBLE);
                        viewHolder.leftText.setVisibility(View.GONE);
                    } else {
                        viewHolder.leftText.setText(model.getMsgText());

                        viewHolder.rightText.setVisibility(View.GONE);
                        viewHolder.leftText.setVisibility(View.VISIBLE);
                    }



            }

            @Override
            public ChatRecord onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.message_view, parent, false);

                return new ChatRecord(view);
            }
        };

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);

                int msgCount = adapter.getItemCount();
                int lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();

                if (lastVisiblePosition == -1 ||
                        (positionStart >= (msgCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    recyclerView.scrollToPosition(positionStart);

                }

            }
        });

        recyclerView.setAdapter(adapter);

    }

    public void ImageViewAnimatedChange(Context c, final ImageView v, final Bitmap new_image) {
        final Animation anim_out = AnimationUtils.loadAnimation(c, R.anim.zoom_out);
        final Animation anim_in = AnimationUtils.loadAnimation(c, R.anim.zoom_in);
        anim_out.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                v.setImageBitmap(new_image);
                anim_in.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                    }
                });
                v.startAnimation(anim_in);
            }
        });
        v.startAnimation(anim_out);
    }

    @Override
    public void onResult(ai.api.model.AIResponse response) {


        Result result = response.getResult();

        String message = result.getResolvedQuery();
        ChatMessage chatMessage0 = new ChatMessage(message, "user");
        db.collection("patients").document("patient1").collection("chat_data")
                .add(
                        chatMessage0
                );

        String reply = result.getFulfillment().getSpeech();
        ChatMessage chatMessage = new ChatMessage(reply, "bot");
        db.collection("patients").document("patient1").collection("chat_data")
                .add(chatMessage);

    }

    @Override
    public void onError(ai.api.model.AIError error) {

    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {

    }

    @Override
    public void onListeningCanceled() {

    }

    @Override
    public void onListeningFinished() {

    }
}
