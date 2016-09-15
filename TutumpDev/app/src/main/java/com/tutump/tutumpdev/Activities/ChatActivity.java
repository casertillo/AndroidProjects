package com.tutump.tutumpdev.Activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.vision.text.Text;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandIOException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.UserConsent;
import com.microsoft.band.sensors.BandGsrEvent;
import com.microsoft.band.sensors.BandGsrEventListener;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.BandRRIntervalEvent;
import com.microsoft.band.sensors.BandRRIntervalEventListener;
import com.microsoft.band.sensors.HeartRateConsentListener;
import com.tutump.tutumpdev.Adapter.MatchAdapter;
import com.tutump.tutumpdev.Adapter.MessageChatAdapter;
import com.tutump.tutumpdev.ChatListDecoration;
import com.tutump.tutumpdev.Models.Match;
import com.tutump.tutumpdev.Models.MessageChatModel;
import com.tutump.tutumpdev.Models.Signals;
import com.tutump.tutumpdev.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import de.hdodenhof.circleimageview.CircleImageView;
import weka.classifiers.functions.LibSVM;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class ChatActivity extends AppCompatActivity {

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private final String TAG ="CHATACTIVITY";
    private ActionBar toolbar;
    private String chatId;
    private String token;

    //CHAT VARIABLES
    private RecyclerView mChatRecyclerView;
    private TextView     mUserMessageChatText;
    private MessageChatAdapter mMessageChatAdapter;
    //TextView textSignals;
    private String currentStatus;

    /* Sender and Recipient status*/
    private static final int SENDER_STATUS=0;
    private static final int RECIPIENT_STATUS=1;

    /* Recipient uid */
    private String mRecipientUid;

    /* Sender uid */
    private String mSenderUid;

    /* unique Firebase ref for this chat */
    private DatabaseReference mDatabase;

    /* Listen to change in chat in firabase-remember to remove it */
    private ChildEventListener listener;

    Query queryRef;

    //Band variables
    private BandClient client = null;
    private Integer heartRate;

    //MachineLearning values
    private Double hrr;
    DecimalFormat df = new DecimalFormat("#.#####");
    private Integer resistance;
    //-----------------------------------------------
    //MACHINE LEARNING MODEL IMPREMENTATION VARIABLES

    //1) ATRIBUTES
    //List of classes
    ArrayList<String> classList = new ArrayList<String>();
    //List of Attributes
    ArrayList<Attribute> attributeList = new ArrayList<Attribute>(4);

    Attribute HR = new Attribute("hr");
    Attribute interval = new Attribute("rr");
    Attribute GSR = new Attribute("gsr");

    Instances data;
    Instance vitalSigns;
    //hr attribute

    //Classifier
    LibSVM textClass;
    //-----------------------------------------------
    //Testing arrayList
    ArrayList<Signals> randomSignals = new ArrayList<Signals>();
    private Random randomGenerator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //COnsent the usage of the heartrate
        final WeakReference<Activity> reference = new WeakReference<Activity>(ChatActivity.this);
        new HeartRateConsentTask().execute(reference);
        new HeartRateSubscriptionTask().execute();
        new GsrSubscriptionTask().execute();
        new RRIntervalSubscriptionTask().execute();

        //SETUP ACTION BAR
        toolbar = getSupportActionBar();
        //REMOVE DEFAULT CHARACTERISTICS
        toolbar.setDisplayHomeAsUpEnabled(false);
        toolbar.setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(this);
        //Add custom actionBar
        View mCustomView = mInflater.inflate(R.layout.custom_action_bar, null);
        //Define the Name
        TextView mTitleTextView = (TextView) mCustomView.findViewById(R.id.title_chat_text);
        mTitleTextView.setText(getIntent().getStringExtra("userName"));
        //Define image from user
        CircleImageView circleImageView = (CircleImageView) mCustomView.findViewById(R.id.title_chat_image);
        Glide.with(this)
                .load(getIntent().getStringExtra("userPhoto"))
                .into(circleImageView);
        toolbar.setCustomView(mCustomView);
        toolbar.setDisplayShowCustomEnabled(true);
        //Keep the chat ID
        chatId = getIntent().getStringExtra("chatId");

        //Set recipient uid
        mRecipientUid = getIntent().getStringExtra("userId");

        //Set sender uid
        mSenderUid = user.getUid();

        //Set token
        token = getIntent().getStringExtra("token");
        // Reference to recyclerView and text view
        mChatRecyclerView=(RecyclerView)findViewById(R.id.chat_recycler_view);
        mUserMessageChatText=(TextView)findViewById(R.id.chat_user_message);

        // Set recyclerView and adapter
        mChatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mChatRecyclerView.setHasFixedSize(true);

        // Initialize adapter
        List<MessageChatModel> emptyMessageChat=new ArrayList<MessageChatModel>();
        mMessageChatAdapter=new MessageChatAdapter(emptyMessageChat);

        // Set adapter to recyclerView
        mChatRecyclerView.setAdapter(mMessageChatAdapter);

        //Database
        // [START initialize_database_ref]
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //textSignals = (TextView) findViewById(R.id.text_chat_signals);

        //---------------------------------
        //region MACHINE LEARNING SETTINGS
        try {
            textClass = (LibSVM) weka.core.SerializationHelper.read(Environment.getExternalStorageDirectory().getPath() +
                    "/Android/data/Sensors/SVM5ClassesText.model");
        }catch(Exception ex){
            Log.d(TAG,"Error loading model: " + ex.toString());
        }
        classList.add("hvla");
        classList.add("black");
        classList.add("hvha");
        classList.add("lvha");
        classList.add("lvla");

        //fill the attribute list
        attributeList.add(HR);
        attributeList.add(interval);
        attributeList.add(GSR);
        attributeList.add(new Attribute("@@class@@",classList));
        //End attribute declaration

        data = new Instances("TestInstances",attributeList,0);

        //INSTANCE
        vitalSigns =  new DenseInstance(data.numAttributes());
        data.setClassIndex(data.numAttributes()-1);
        //endregion
        //---------------------------------

        //TEST LIST OF VALUES TO CHECK EVERY EMOTIONAL RESPONSE
        Signals newSignal = new Signals(65, 0.91256, 4063);
        randomSignals.add(newSignal);
        Signals newSignal2 = new Signals(65, 0.81301, 3929);
        randomSignals.add(newSignal2);
        Signals newSignal3 = new Signals(66, 0.89597, 3946);
        randomSignals.add(newSignal3);
        Signals newSignal4 = new Signals(69, 0.87938, 3751);
        randomSignals.add(newSignal4);
        Signals newSignal5 = new Signals(70, 0.66666, 3718);
        randomSignals.add(newSignal5);
        randomGenerator = new Random();
    }

    @Override
    protected void onStart() {
        super.onStart();

        queryRef = mDatabase.child("chats").child(chatId);

        listener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                if(dataSnapshot.exists())
                {
                    MessageChatModel newMessage = dataSnapshot.getValue(MessageChatModel.class);
                    if(newMessage.getSender().equals(mSenderUid)){
                        newMessage.setRecipientOrSenderStatus(SENDER_STATUS);
                    }else{
                        newMessage.setRecipientOrSenderStatus(RECIPIENT_STATUS);
                    }
                    mMessageChatAdapter.refillAdapter(newMessage);
                    mChatRecyclerView.scrollToPosition(mMessageChatAdapter.getItemCount()-1);
                }
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        queryRef.addChildEventListener(listener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (client != null) {
            try {
                client.getSensorManager().unregisterHeartRateEventListener(mHeartRateEventListener);
                client.getSensorManager().unregisterGsrEventListener(mGsrEventListener);
                client.getSensorManager().unregisterRRIntervalEventListener(mRRIntervalEventListener);
            } catch (BandIOException e) {
                appendToUI(e.getMessage());
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (client != null) {
            try {
                client.disconnect().await();
            } catch (InterruptedException e) {
                // Do nothing as this is happening during destroy
            } catch (BandException e) {
                // Do nothing as this is happening during destroy
            }
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        // Remove listener
        if(listener !=null) {
            // Remove listener
            queryRef.removeEventListener(listener);
        }
        // Clean chat message
        mMessageChatAdapter.cleanUp();

        if (client != null) {
            try {
                client.disconnect().await();
            } catch (InterruptedException e) {
                // Do nothing as this is happening during destroy
            } catch (BandException e) {
                // Do nothing as this is happening during destroy
            }
        }
    }
    public void sendMessageToFireChat(View sendButton){
        String senderMessage=mUserMessageChatText.getText().toString();
        senderMessage=senderMessage.trim();
        if(!senderMessage.isEmpty()){

            // Log.e(TAG, "send message");

            // Send message to firebase
            Map<String, String> newMessage = new HashMap<String, String>();
            newMessage.put("sender", mSenderUid); // Sender uid
            newMessage.put("recipient",mRecipientUid); // Recipient uid
            newMessage.put("message",senderMessage); // Message
            newMessage.put("emotionalStatus", currentStatus);

            mDatabase.child("chats").child(chatId).push().setValue(newMessage);
            // Clear text
            mUserMessageChatText.setText("");

            //SEND NOTIFICATION TO THE USER
            try {
                RequestQueue queue = Volley.newRequestQueue(this);
                String url ="https://fcm.googleapis.com/fcm/send";
                JSONObject json = new JSONObject();
                JSONObject notification = new JSONObject();
                try{
                    notification.put("title", getIntent().getStringExtra("userName"));
                    notification.put("text", senderMessage);
                    json.put("to",token);
                    json.put("notification", notification);
                }catch(JSONException e){
                    e.printStackTrace();
                }
                // Request a string response from the provided URL.
                JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, url,json,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                // Display the first 500 characters of the response string.
                                Log.d("CHATACTIVITY", "Response is: "+ response.toString());
                            }

                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("CHATACTIVITY", "That didn't work!");
                            }

                        }){

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String,String> params = new HashMap<String, String>();
                        params.put("Content-Type","application/json");
                        params.put("Authorization","key=AIzaSyBW6ozNyuD1mMu2XymwpmDLaewMpaiU5tM");
                        return params;
                    }
                };
                // Add the request to the RequestQueue.
                queue.add(stringRequest);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
    }

    //region Band listeners
    //CREATE BAND INFROMATION
    //1 - HEART RATE LISTENER
    private BandHeartRateEventListener mHeartRateEventListener = new BandHeartRateEventListener() {
        @Override
        public void onBandHeartRateChanged(final BandHeartRateEvent event) {
            if (event != null) {
                heartRate =  event.getHeartRate();
                //appendToUI(String.format("Heart Rate = %d \n"
                //        + "Quality = %s\n", event.getHeartRate(), event.getQuality()));
                appendToUI("");
            }
        }
    };
    //22 - HRR
    private BandRRIntervalEventListener mRRIntervalEventListener = new BandRRIntervalEventListener() {
        @Override
        public void onBandRRIntervalChanged(final BandRRIntervalEvent event) {
            if (event != null) {
                //appendToUI(String.format("RR Interval = %.3f s\n", event.getInterval()));

                hrr = Double.parseDouble(df.format(event.getInterval()));

                appendToUI("");
            }
        }
    };
    //3 - GALVANIC SKIN RESPONSE
    private BandGsrEventListener mGsrEventListener = new BandGsrEventListener() {
        @Override
        public void onBandGsrChanged(final BandGsrEvent event) {
            if (event != null) {
                resistance = event.getResistance();
                //appendToUIGsr(String.format("Resistance = %d kOhms\n", event.getResistance()));
                appendToUI("");
            }
        }
    };
    private boolean getConnectedBandClient() throws InterruptedException, BandException {
        if (client == null) {
            BandInfo[] devices = BandClientManager.getInstance().getPairedBands();
            if (devices.length == 0) {
                appendToUI("Band isn't paired with your phone.\n");
                return false;
            }
            client = BandClientManager.getInstance().create(this, devices[0]);
        } else if (ConnectionState.CONNECTED == client.getConnectionState()) {
            return true;
        }

        appendToUI("Band is connecting...\n");
        return ConnectionState.CONNECTED == client.connect().await();
    }
    //endregion ---------------------------------------------
    //region Band values for measuring function
    private void appendToUI(final String string) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if(string == "" & heartRate != null & hrr != null & resistance != null) {
                    //TESTING DATA
                    int index = randomGenerator.nextInt(randomSignals.size());
                    Signals sign = randomSignals.get(index);
/*
                    vitalSigns.setValue(attributeList.get(0), sign.getHr());
                    vitalSigns.setValue(attributeList.get(1), sign.getRr());
                    vitalSigns.setValue(attributeList.get(2), sign.getGsr());
                    vitalSigns.setMissing(attributeList.get(3));
*/
                    vitalSigns.setValue(attributeList.get(0), heartRate);
                    vitalSigns.setValue(attributeList.get(1), hrr);
                    vitalSigns.setValue(attributeList.get(2), resistance);
                    vitalSigns.setMissing(attributeList.get(3));

                    data.add(vitalSigns);
                    try{
                        double pred = textClass.classifyInstance(data.firstInstance());
                        //Log.d(TAG, "PREDICTED: " +Double.toString(pred)+ "value: "+classList.get((int)pred));

                        switch ((int)pred){
                            case 0:
                                currentStatus = "happy";
                                break;
                            case 1:
                                currentStatus = "neutral";
                                break;
                            case 2:
                                currentStatus = "superhappy";
                                break;
                            case 3:
                                currentStatus = "angry";
                                break;
                            case 4:
                                currentStatus = "sad";
                                break;
                            default:
                                currentStatus ="neutral";
                                break;
                        }
                        data.clear();
                    }catch(Exception ex)
                    {
                        Log.d(TAG, "Error on prediction: " +ex);
                    }
                }
            }
        });
    }
    //endregion
    private class RRIntervalSubscriptionTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (getConnectedBandClient()) {
                    int hardwareVersion = Integer.parseInt(client.getHardwareVersion().await());
                    if (hardwareVersion >= 20) {
                        if (client.getSensorManager().getCurrentHeartRateConsent() == UserConsent.GRANTED) {
                            client.getSensorManager().registerRRIntervalEventListener(mRRIntervalEventListener);
                        } else {
                            appendToUI("You have not given this application consent to access heart rate data yet."
                                    + " Please press the Heart Rate Consent button.\n");
                        }
                    } else {
                        appendToUI("The RR Interval sensor is not supported with your Band version. Microsoft Band 2 is required.\n");
                    }
                } else {
                    appendToUI("Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");
                }
            } catch (BandException e) {
                String exceptionMessage="";
                switch (e.getErrorType()) {
                    case UNSUPPORTED_SDK_VERSION_ERROR:
                        exceptionMessage = "Microsoft Health BandService doesn't support your SDK Version. Please update to latest SDK.\n";
                        break;
                    case SERVICE_ERROR:
                        exceptionMessage = "Microsoft Health BandService is not available. Please make sure Microsoft Health is installed and that you have the correct permissions.\n";
                        break;
                    default:
                        exceptionMessage = "Unknown error occured: " + e.getMessage() + "\n";
                        break;
                }
                appendToUI(exceptionMessage);

            } catch (Exception e) {
                appendToUI(e.getMessage());
            }
            return null;
        }
    }
    private class HeartRateSubscriptionTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (getConnectedBandClient()) {
                    if (client.getSensorManager().getCurrentHeartRateConsent() == UserConsent.GRANTED) {
                        client.getSensorManager().registerHeartRateEventListener(mHeartRateEventListener);

                    } else {
                        appendToUI("You have not given this application consent to access heart rate data yet."
                                + " Please press the watch icon button.\n");

                    }
                } else {
                    appendToUI("Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");
                }
            } catch (BandException e) {
                String exceptionMessage="";
                switch (e.getErrorType()) {
                    case UNSUPPORTED_SDK_VERSION_ERROR:
                        exceptionMessage = "Microsoft Health BandService doesn't support your SDK Version. Please update to latest SDK.\n";
                        break;
                    case SERVICE_ERROR:
                        exceptionMessage = "Microsoft Health BandService is not available. Please make sure Microsoft Health is installed and that you have the correct permissions.\n";
                        break;
                    default:
                        exceptionMessage = "Unknown error occured: " + e.getMessage() + "\n";
                        break;
                }
                appendToUI(exceptionMessage);

            } catch (Exception e) {
                appendToUI(e.getMessage());
            }
            return null;
        }
    }
    private class HeartRateConsentTask extends AsyncTask<WeakReference<Activity>, Void, Void> {
        @Override
        protected Void doInBackground(WeakReference<Activity>... params) {
            try {
                if (getConnectedBandClient()) {
                    if (client.getSensorManager().getCurrentHeartRateConsent() != UserConsent.GRANTED) {
                        if (params[0].get() != null) {
                            client.getSensorManager().requestHeartRateConsent(params[0].get(), new HeartRateConsentListener() {
                                @Override
                                public void userAccepted(boolean consentGiven) {
                                    new HeartRateSubscriptionTask().execute();
                                }
                            });
                        }
                    }
                } else {
                    appendToUI("Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");
                }
            } catch (BandException e) {
                String exceptionMessage="";
                switch (e.getErrorType()) {
                    case UNSUPPORTED_SDK_VERSION_ERROR:
                        exceptionMessage = "Microsoft Health BandService doesn't support your SDK Version. Please update to latest SDK.\n";
                        break;
                    case SERVICE_ERROR:
                        exceptionMessage = "Microsoft Health BandService is not available. Please make sure Microsoft Health is installed and that you have the correct permissions.\n";
                        break;
                    default:
                        exceptionMessage = "Unknown error occured: " + e.getMessage() + "\n";
                        break;
                }
                appendToUI(exceptionMessage);

            } catch (Exception e) {
                appendToUI(e.getMessage());
            }
            return null;
        }
    }
    private class GsrSubscriptionTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (getConnectedBandClient()) {
                    int hardwareVersion = Integer.parseInt(client.getHardwareVersion().await());
                    if (hardwareVersion >= 20) {
                        appendToUI("Band is connected.\n");
                        client.getSensorManager().registerGsrEventListener(mGsrEventListener);
                    } else {
                        appendToUI("The Gsr sensor is not supported with your Band version. Microsoft Band 2 is required.\n");
                    }
                } else {
                    appendToUI("Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");
                }
            } catch (BandException e) {
                String exceptionMessage="";
                switch (e.getErrorType()) {
                    case UNSUPPORTED_SDK_VERSION_ERROR:
                        exceptionMessage = "Microsoft Health BandService doesn't support your SDK Version. Please update to latest SDK.\n";
                        break;
                    case SERVICE_ERROR:
                        exceptionMessage = "Microsoft Health BandService is not available. Please make sure Microsoft Health is installed and that you have the correct permissions.\n";
                        break;
                    default:
                        exceptionMessage = "Unknown error occured: " + e.getMessage() + "\n";
                        break;
                }
                appendToUI(exceptionMessage);

            } catch (Exception e) {
                appendToUI(e.getMessage());
            }
            return null;
        }
    }
}
