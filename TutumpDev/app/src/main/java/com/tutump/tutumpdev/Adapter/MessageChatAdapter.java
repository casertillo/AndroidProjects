package com.tutump.tutumpdev.Adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tutump.tutumpdev.Models.MessageChatModel;
import com.tutump.tutumpdev.R;

import java.util.List;

/**
 * Created by Marcel on 11/7/2015.
 */
public class MessageChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private final String TAG = "MESSAGECHATADAPTER";
    private List<MessageChatModel> mListOfFireChat;
    private static final int SENDER=0;
    private static final int RECIPIENT=1;

    public MessageChatAdapter(List<MessageChatModel> listOfFireChats) {
        mListOfFireChat=listOfFireChats;
    }

    @Override
    public int getItemViewType(int position) {
        if(mListOfFireChat.get(position).getRecipientOrSenderStatus()==SENDER){
            Log.e("Adapter", " sender");
            return SENDER;
        }else {
            return RECIPIENT;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
    try {

        switch (viewType) {
            case SENDER:
                View viewSender = inflater.inflate(R.layout.sender_message, viewGroup, false);
                viewHolder= new ViewHolderSender(viewSender);
                break;
            case RECIPIENT:
                View viewRecipient = inflater.inflate(R.layout.recipient_message, viewGroup, false);
                viewHolder=new ViewHolderRecipient(viewRecipient);
                break;
            default:
                View viewSenderDefault = inflater.inflate(R.layout.sender_message, viewGroup, false);
                viewHolder= new ViewHolderSender(viewSenderDefault);
                break;
        }
        return viewHolder;
    }catch (Exception e){
        Log.d(TAG, "ERROR ON onCreateViewHOlder");
    }
        return  null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

        switch (viewHolder.getItemViewType()){
            case SENDER:
                ViewHolderSender viewHolderSender=(ViewHolderSender)viewHolder;
                configureSenderView(viewHolderSender,position);
                break;
            case RECIPIENT:
                ViewHolderRecipient viewHolderRecipient=(ViewHolderRecipient)viewHolder;
                configureRecipientView(viewHolderRecipient,position);
                break;
        }


    }

    private void configureSenderView(ViewHolderSender viewHolderSender, int position) {
        MessageChatModel senderFireMessage=mListOfFireChat.get(position);
        viewHolderSender.getSenderMessageTextView().setText(senderFireMessage.getMessage());
    }

    private void configureRecipientView(ViewHolderRecipient viewHolderRecipient, int position) {
        MessageChatModel recipientFireMessage=mListOfFireChat.get(position);
        viewHolderRecipient.getRecipientMessageTextView().setText(recipientFireMessage.getMessage());
        if(recipientFireMessage.getEmotionalStatus()!= null)
        {
            switch (recipientFireMessage.getEmotionalStatus())
            {
                case "angry":
                    viewHolderRecipient.getRecipientMessageImage().setImageResource(R.drawable.angry);
                    break;
                case "happy":
                    viewHolderRecipient.getRecipientMessageImage().setImageResource(R.drawable.happy);
                    break;
                case "neutral":
                    viewHolderRecipient.getRecipientMessageImage().setImageResource(R.drawable.neutral);
                    break;
                case "sad":
                    viewHolderRecipient.getRecipientMessageImage().setImageResource(R.drawable.sad);
                    break;
                case "superhappy":
                    viewHolderRecipient.getRecipientMessageImage().setImageResource(R.drawable.superhappy);
                    break;
                default:
                    viewHolderRecipient.getRecipientMessageImage().setImageResource(R.drawable.neutral);
                    break;
            }


        }
    }

    @Override
    public int getItemCount() {
        return mListOfFireChat.size();
    }


    public void refillAdapter(MessageChatModel newFireChatMessage){

        /*add new message chat to list*/
        mListOfFireChat.add(newFireChatMessage);

        /*refresh view*/
        notifyItemInserted(getItemCount()-1);
    }

    public void refillFirsTimeAdapter(List<MessageChatModel> newFireChatMessage){

        /*add new message chat to list*/
        mListOfFireChat.clear();
        mListOfFireChat.addAll(newFireChatMessage);
        /*refresh view*/
        notifyItemInserted(getItemCount()-1);
    }

    public void cleanUp() {
        mListOfFireChat.clear();
    }


    /*==============ViewHolder===========*/

    /*ViewHolder for Sender*/

    public class ViewHolderSender extends RecyclerView.ViewHolder {

        private TextView mSenderMessageTextView;

        public ViewHolderSender(View itemView) {
            super(itemView);
            mSenderMessageTextView=(TextView)itemView.findViewById(R.id.senderMessage);
        }

        public TextView getSenderMessageTextView() {
            return mSenderMessageTextView;
        }

        public void setSenderMessageTextView(TextView senderMessage) {
            mSenderMessageTextView = senderMessage;
        }
    }


    /*ViewHolder for Recipient*/
    public class ViewHolderRecipient extends RecyclerView.ViewHolder {

        private TextView mRecipientMessageTextView;
        private ImageView mRecipientMessageImage;

        public ViewHolderRecipient(View itemView) {
            super(itemView);
            mRecipientMessageTextView=(TextView)itemView.findViewById(R.id.recipientMessage);
            mRecipientMessageImage = (ImageView)itemView.findViewById(R.id.imageSignal);
        }

        public TextView getRecipientMessageTextView() {
            return mRecipientMessageTextView;
        }

        public ImageView getRecipientMessageImage(){
            return mRecipientMessageImage;
        }

        public void setRecipientMessageImageView(ImageView recipientImage)
        {
            mRecipientMessageImage = recipientImage;
        }
        public void setRecipientMessageTextView(TextView recipientMessage) {
            mRecipientMessageTextView = recipientMessage;
        }
    }
}