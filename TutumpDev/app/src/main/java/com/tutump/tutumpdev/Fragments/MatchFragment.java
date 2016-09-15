package com.tutump.tutumpdev.Fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.os.Bundle;

import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.daprlabs.cardstack.SwipeDeck;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.microsoft.band.sensors.BandRRIntervalEvent;
import com.microsoft.band.sensors.BandRRIntervalEventListener;
import com.tutump.tutumpdev.Activities.TabsActivity;
import com.tutump.tutumpdev.Adapter.SwipeDeskAdapter;
import com.tutump.tutumpdev.Models.CurrentUser;
import com.tutump.tutumpdev.Models.Match;
import com.tutump.tutumpdev.Models.User;
import com.tutump.tutumpdev.R;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.BandIOException;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.UserConsent;
import com.microsoft.band.sensors.BandGsrEvent;
import com.microsoft.band.sensors.BandGsrEventListener;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.HeartRateConsentListener;

import weka.attributeSelection.AttributeTransformer;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.meta.Bagging;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;


/**
 * Created by casertillo on 19/06/16.
 */
public class MatchFragment extends Fragment{

    private static final String TAG = "MatchFragment";
    SwipeDeck swipeDeck;
    private DatabaseReference mDatabase;
    private ProgressBar mprogress;
    private TextView textMatchMessage;
    private ArrayList<User> users = new ArrayList<User>();
    private static boolean Visibility;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    Query queryRef;
    View view;
    TextView textSignals;
    private ImageButton heartPermission;


    //Band variables
    private BandClient client = null;
    private Integer heartRate;

    //MachineLearning values
    private Double hrr;
    DecimalFormat df = new DecimalFormat("#.#####");
    private Integer resistance;
    //1) ATRIBUTES
    //List of clases
    ArrayList<String> classList = new ArrayList<String>();
    //List of Attributes
    ArrayList<Attribute> attributeList = new ArrayList<Attribute>(4);

    //Attribute
    Attribute HR = new Attribute("hr");
    Attribute interval = new Attribute("rr");
    Attribute GSR = new Attribute("gsr");

    Instances data;
    Instance vitalSigns;
    //hr attribute

    //Classifier
    LibSVM textClass;

    String prediction;
    //region Unregister band when not visible
    //---------------------------------------------------------
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Visibility = isVisibleToUser;
        if(!Visibility)
        {
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
    }
   //endregion ---------------------------------------------

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
            client = BandClientManager.getInstance().create(getActivity().getBaseContext(), devices[0]);
        } else if (ConnectionState.CONNECTED == client.getConnectionState()) {
            return true;
        }

        appendToUI("Band is connecting...\n");
        return ConnectionState.CONNECTED == client.connect().await();
    }
    //endregion ---------------------------------------------

    //region Band values for measuring function
    private void appendToUI(final String string) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if(string == "" & heartRate != null & hrr != null & resistance != null) {
                   // if (game) {
                        vitalSigns.setValue(attributeList.get(0), heartRate);
                        vitalSigns.setValue(attributeList.get(1), hrr);
                        vitalSigns.setValue(attributeList.get(2), resistance);
                        vitalSigns.setMissing(attributeList.get(3));
                    //    game = false;
                    //}
                    //else{
                    //    vitalSigns.setValue(attributeList.get(0), 70);
                    //    vitalSigns.setValue(attributeList.get(1), 0.87938);
                    //    vitalSigns.setValue(attributeList.get(2), 8000);
                    //    vitalSigns.setMissing(attributeList.get(3));
                    //    game = true;
                    //}
                    data.add(vitalSigns);
                    try{
                        double pred = textClass.classifyInstance(data.firstInstance());
                        //Log.d(TAG, "PREDICTED: " +Double.toString(pred)+ "value: "+classList.get((int)pred));
                        if(classList.get((int)pred) == "mh") {
                            textSignals.setText("LIKE!!");
                            prediction = "positive";
                        }
                        else
                        {
                            textSignals.setText("NOT LIKE!!");
                            prediction = "negative";
                        }
                        data.clear();
                    }catch(Exception ex)
                    {
                        Log.d(TAG, "Error on prediction: " +ex);
                    }
                }else
                    textSignals.setText(string);
            }
        });
    }
    //endregion

    //region BAND TASKS
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
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                heartPermission.setVisibility(View.GONE);
                                textSignals.setGravity(Gravity.CENTER | Gravity.BOTTOM);
                            }
                        });

                    } else {
                        appendToUI("You have not given this application consent to access heart rate data yet."
                                + " Please press the watch icon button.\n");
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(heartPermission.getVisibility() == View.GONE)
                                {
                                    heartPermission.setVisibility(View.VISIBLE);
                                }
                            }
                        });

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
                        appendToUIGsr("Band is connected.\n");
                        client.getSensorManager().registerGsrEventListener(mGsrEventListener);
                    } else {
                        appendToUIGsr("The Gsr sensor is not supported with your Band version. Microsoft Band 2 is required.\n");
                    }
                } else {
                    appendToUIGsr("Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");
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
                appendToUIGsr(exceptionMessage);

            } catch (Exception e) {
                appendToUIGsr(e.getMessage());
            }
            return null;
        }
    }
    private void appendToUIGsr(final String string) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textSignals.setText(string);
            }
        });
    }
    //endregion---------------------------------------

    @Override
    public void onResume() {
        super.onResume();
        double min, max;
        swipeDeck.destroyDrawingCache();
        users.clear();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
        Set<String> AgeRange = settings.getStringSet("age_range", null);
        if(AgeRange != null)
        {
            List list = new ArrayList(AgeRange);
            double val1 = Integer.parseInt(list.get(0).toString());
            double val2 = Integer.parseInt(list.get(1).toString());
            min = Math.min(val1, val2);
            max = Math.max(val1, val2);
        }
        else{
            min = 18;
            max = 100;
        }

        Log.d("MATCHFRAGMENT",  "Minimum: " + min + ", Max: "+max);

        final Boolean male = settings.getBoolean("search_men", false);
        final Boolean female = settings.getBoolean("search_women", true);

        queryRef = mDatabase.child("users")
                .orderByChild("Age")
                .startAt(min).endAt(max);
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    Map<String, Object> values = (HashMap<String, Object>) dataSnapshot.getValue();
                    CurrentUser.getCurrentUser().addViewedProfiles(user.getUid());
                    Map<String, Object> removed = removeViewed(values);
                    for(Map.Entry<String, Object> child : removed.entrySet())
                    {
                        Map<String, Object> userValues = (Map<String, Object>) child.getValue();

                        if(userValues.get("gender").toString().contains("f") && female && !male)
                            addUser(userValues, child.getKey());
                        else if(userValues.get("gender").toString().equals("male") && !female && male)
                            addUser(userValues, child.getKey());
                        else if(male && female)
                            addUser(userValues, child.getKey());
                        else
                            continue;

                    }
                    if(users.isEmpty()) {
                        mprogress.setVisibility(View.GONE);
                        textMatchMessage.setText("No more profiles found");
                        textMatchMessage.setVisibility(View.VISIBLE);
                    }
                    else{
                        mprogress.setVisibility(View.GONE);
                        textMatchMessage.setVisibility(View.GONE);
                    }
                    SwipeDeskAdapter adapter = new SwipeDeskAdapter(getContext(), users, getFragmentManager());
                    swipeDeck.setAdapter(adapter);

                    swipeDeck.setEventCallback(new SwipeDeck.SwipeEventCallback() {
                        @Override
                        public void cardSwipedLeft(int position) {
                            appendLog("reject");
                            //Log.i(TAG, "card was swiped left, position in adapter: " + users.get(position).getId()+", "+position);
                            mDatabase.child("users").child(user.getUid()).child("dislike").push().setValue(users.get(position).getId());
                            mDatabase.child("users").child(user.getUid()).child("viewed").push().setValue(users.get(position).getId());;
                        }

                        @Override
                        public void cardSwipedRight(int position) {
                            appendLog("accept");
                            //DIALOG TO SHOW THAT IS A MATCH
                            Bundle bundle = new Bundle();
                            bundle.putString("username", users.get(position).getName());
                            FragmentManager fm = getFragmentManager();
                            myDialogFragment editNameDialog = new myDialogFragment();

                            //Log.i(TAG, "card was swiped right, position in adapter: " + users.get(position).getId());
                            mDatabase.child("users").child(user.getUid()).child("like").push().setValue(users.get(position).getId());
                            mDatabase.child("users").child(user.getUid()).child("viewed").push().setValue(users.get(position).getId());
                            if(users.get(position).getLikes() != null)
                            {
                                if(users.get(position).getLikes().contains(user.getUid())){

                                    //POPUP MATCH
                                    editNameDialog.setArguments(bundle);
                                    editNameDialog.show(fm, "new Match!");

                                    //ADD the new match in the current user
                                    Match newMatch = new Match(users.get(position).getId(), users.get(position).getName(), users.get(position).getProfileImage(), users.get(position).getToken());
                                    //Add a new match to the current user
                                    //1 - get the key
                                    String key = mDatabase.child("users").child(user.getUid()).child("matches").push().getKey();
                                    newMatch.setChatId(key);
                                    //2 - assign the newMatch to the map
                                    Map<String, Object> newMatchValues = newMatch.toMap();
                                    //3 - add the child to Update
                                    Map<String, Object> childUpdates = new HashMap<>();
                                    //4 - Define the new match to the matches part in the current user
                                    childUpdates.put("/users/"+user.getUid()+"/matches/"+key, newMatchValues);
                                    //5 - Add the value
                                    mDatabase.updateChildren(childUpdates);

                                    //ADD the new match in the selected user
                                    newMatch.setId(user.getUid());
                                    newMatch.setName(CurrentUser.getCurrentUser().getName());
                                    newMatch.setPhotoUrl(user.getPhotoUrl().toString());
                                    newMatch.setToken(FirebaseInstanceId.getInstance().getToken());
                                    newMatchValues = newMatch.toMap();
                                    childUpdates.put("/users/"+users.get(position).getId()+"/matches/"+key, newMatchValues);
                                    mDatabase.updateChildren(childUpdates);


                                }
                            }

                        }

                        @Override
                        public void cardsDepleted() {
                            Log.i(TAG, "no more cards");
                            mprogress.setVisibility(View.GONE);
                            textMatchMessage.setText("No more profiles found");
                            textMatchMessage.setVisibility(View.VISIBLE);
                        }
                        @Override
                        public void cardActionDown(){
                            //Log.i(TAG, "Card was swiped down");
                        }
                        @Override
                        public void cardActionUp(){
                            //Log.i(TAG, "Card was swiped up");
                        }
                    });
                }
                else{
                    Log.d(TAG, "empty set");
                    mprogress.setVisibility(View.GONE);
                    textMatchMessage.setText("No more profiles found");
                    textMatchMessage.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "getUser:onCancelled", databaseError.toException());
            }
        };
        queryRef.addListenerForSingleValueEvent(listener);

        FloatingActionButton leftButton = (FloatingActionButton) view.findViewById(R.id.fableft);
        leftButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                swipeDeck.swipeTopCardLeft(Toast.LENGTH_LONG);
            }
        });

        FloatingActionButton rightButton = (FloatingActionButton) view.findViewById(R.id.fabright);
        rightButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                swipeDeck.swipeTopCardRight(Toast.LENGTH_LONG);
            }
        });
    }

    public MatchFragment(){

    }
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //COnsent the usage of the heartrate
        final WeakReference<Activity> reference = new WeakReference<Activity>(getActivity());
        new HeartRateConsentTask().execute(reference);
        new HeartRateSubscriptionTask().execute();
        new GsrSubscriptionTask().execute();
        new RRIntervalSubscriptionTask().execute();


        //Attribute declaration
        //classList.add("black");
        //classList.add("hvha");
        //classList.add("hvla");
        //classList.add("lvha");
        //classList.add("lvla");
        try {
            textClass = (LibSVM) weka.core.SerializationHelper.read(Environment.getExternalStorageDirectory().getPath() + "/Android/data/Sensors/SVMPicturesMen.model");
        }catch(Exception ex){
            Log.d(TAG,"Error loading model: " + ex.toString());
        }
        classList.add("mh");
        classList.add("mn");

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
        //Copy model from asssets to the device
        String destFile = getContext().getApplicationInfo().dataDir+"/textModel.model";

        File dest = new File(destFile);
        //if(!dest.exists())
            //copyModel();

        // [START initialize_database_ref]
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    //METHOD TO ADD THE MODEL INTO THE PHONE
    //NOT USED TODO
    //IN ORDER TO KEEP THE .model file secure it should be in assets, and then installed on the phone
    private void copyModel()
    {
        String destFile = getContext().getApplicationInfo().dataDir+"/textModel.model";
        try {
            File f2 = new File(destFile);
            InputStream in = getContext().getAssets().open("textInstance.model");
            OutputStream out = new FileOutputStream(f2);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            System.out.println("File copied.");
        } catch (FileNotFoundException ex) {
            System.out
                    .println(ex.getMessage() + " in the specified directory.");

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        view = inflater.inflate(R.layout.fragment_match, container, false);
        swipeDeck = (SwipeDeck) view.findViewById(R.id.swipe_deck);
        mprogress = (ProgressBar) view.findViewById(R.id.progressMatch);
        textMatchMessage = (TextView) view.findViewById(R.id.textMatchMessage);
        textSignals = (TextView) view.findViewById(R.id.text_signals);
        heartPermission = (ImageButton) view.findViewById(R.id.fabheart);

        heartPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final WeakReference<Activity> reference = new WeakReference<Activity>(getActivity());
                new HeartRateConsentTask().execute(reference);
            }
        });
        return view;
    }

    @Override
    public void onDestroy() {
        if (client != null) {
            try {
                client.disconnect().await();
            } catch (InterruptedException e) {
                // Do nothing as this is happening during destroy
            } catch (BandException e) {
                // Do nothing as this is happening during destroy
            }
        }
        super.onDestroy();
    }

    public Map<String, Object> removeViewed(Map<String, Object> complete)
    {
        complete.keySet().removeAll(CurrentUser.getCurrentUser().getViewedProfiles());
        return  complete;
    }

    public void addUser(Map<String, Object> userValues, String key)
    {
        User newUser = new User();
        newUser.setId(key);
        if(userValues.containsKey("name"))
            newUser.setName(userValues.get("name").toString());
        if(userValues.containsKey("Age"))
            newUser.setAge(userValues.get("Age").toString());
        if(userValues.containsKey("education"))
            newUser.setEducation(userValues.get("education").toString());
        if(userValues.containsKey("work"))
            newUser.setWork(userValues.get("work").toString());
        if(userValues.containsKey("about"))
            newUser.setAbout(userValues.get("about").toString());
        if(userValues.containsKey("pictures")) {
            Map<String, Object> pictures = (Map<String, Object>) userValues.get("pictures");
            newUser.setPictures(pictures);
        }
        else {
            HashMap<String, String> h = new HashMap<String, String>() {{
                put("a","b");
            }};
            HashMap<String, Object> pictures = new HashMap<String, Object>(){{put("iv_1","defaultprofile");}};

            newUser.setPictures(pictures);
        }
        if(userValues.containsKey("like"))
        {
            Map<String, String> likes = (Map<String, String>) userValues.get("like");
            newUser.setLikes(likes.values());
        } else
        {
            newUser.setLikes(null);
        }
        if(userValues.containsKey("profileImage"))
        {
            newUser.setProfileImage(userValues.get("profileImage").toString());
        }
        if(userValues.containsKey("token"))
        {
            newUser.setToken(userValues.get("token").toString());
        }
        users.add(newUser);
    }

    public void appendLog(String selected)
    {
        File backupPath = Environment.getExternalStorageDirectory();

        backupPath = new File(backupPath.getPath() + "/Android/data/Sensors");
        if(!backupPath.exists()){
            try{
                backupPath.mkdirs();
            } catch(Exception e){
                e.printStackTrace();
            }

        }

        File logFile = new File(backupPath + "/data2.txt");
        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(heartRate+", "+ hrr+ ", "+resistance+", "+ System.currentTimeMillis()+", "+selected+", "+prediction);
            buf.newLine();
            buf.close();

        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}


