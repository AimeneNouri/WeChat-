package com.example.wechat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wechat.activities.ImageViewer;
import com.example.wechat.activities.MainActivity;
import com.example.wechat.activities.videoView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessagesViewHolder> {

    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;

    public MessagesAdapter (List<Messages> userMessagesList)
    {
        this.userMessagesList = userMessagesList;
    }

    public class MessagesViewHolder extends RecyclerView.ViewHolder
    {
        public TextView receiverName, senderMsgText, receiverMsgText, senTime, receive_time;
        public CircleImageView receiverProfileImage;
        public ImageView messageSenderImage, messageReceiverImage, playOne, playTwo;
        public VideoView messageSenderVideo, messageReceiverVideo;
        public RelativeLayout messageSender, messageReceiver;

        public MessagesViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMsgText = itemView.findViewById(R.id.sender_message_text);
            receiverMsgText = itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = itemView.findViewById(R.id.message_profile_image);
            receiverName = itemView.findViewById(R.id.receiverUsername);
            messageSenderImage = itemView.findViewById(R.id.message_sender_image);
            messageReceiverImage = itemView.findViewById(R.id.message_receiver_image);
            messageSenderVideo = itemView.findViewById(R.id.message_sender_video);
            messageReceiverVideo = itemView.findViewById(R.id.message_receiver_video);
            messageSender = itemView.findViewById(R.id.sender_message_text_Layout);
            messageReceiver = itemView.findViewById(R.id.receiver_message_text_layout);
            playOne = itemView.findViewById(R.id.playVideoReceiver);
            playTwo = itemView.findViewById(R.id.playVideoSender);
            senTime = itemView.findViewById(R.id.sent_time);
            receive_time = itemView.findViewById(R.id.receive_time);
        }
    }

    @NonNull
    @Override
    public MessagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_messages_layout, parent, false);

        mAuth = FirebaseAuth.getInstance();

        return new MessagesViewHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull final MessagesViewHolder holder, final int position)
    {
        String msgSenderId = mAuth.getCurrentUser().getUid();
        com.example.wechat.Messages messages = userMessagesList.get(position);

        String fromUserId = messages.getFrom();
        String fromMsgType = messages.getType();

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);
        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.hasChild("image"))
                {
                    String ReceiverImage = dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(ReceiverImage).placeholder(R.drawable.profile_image).into(holder.receiverProfileImage);
                }
                String ReceiverName = dataSnapshot.child("name").getValue().toString();
                holder.receiverName.setText(ReceiverName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.receiverMsgText.setVisibility(View.GONE);
        holder.receiverProfileImage.setVisibility(View.GONE);
        holder.receiverName.setVisibility(View.GONE);
        holder.senderMsgText.setVisibility(View.GONE);
        holder.messageReceiverImage.setVisibility(View.GONE);
        holder.messageSenderImage.setVisibility(View.GONE);
        holder.messageReceiverVideo.setVisibility(View.GONE);
        holder.messageSenderVideo.setVisibility(View.GONE);
        holder.playTwo.setVisibility(View.GONE);
        holder.playOne.setVisibility(View.GONE);
        holder.messageSender.setVisibility(View.GONE);
        holder.messageReceiver.setVisibility(View.GONE);

        if (fromMsgType.equals("text"))
        {
            if (fromUserId.equals(msgSenderId))
            {
                holder.messageSender.setVisibility(View.VISIBLE);
                holder.senderMsgText.setVisibility(View.VISIBLE);
                holder.receive_time.setVisibility(View.INVISIBLE);
                holder.senTime.setVisibility(View.VISIBLE);

                holder.senderMsgText.setBackgroundResource(R.drawable.sender_message);
                holder.senderMsgText.setText(messages.getMessage());
                holder.senTime.setText(messages.getTime());
                holder.senderMsgText.setTextColor(Color.BLACK);

                //check if url
                if (URLUtil.isValidUrl(messages.getMessage()))
                {
                    holder.senderMsgText.setText(Html.fromHtml("<u>"+ messages.getMessage() +"</u>"));
                    holder.senderMsgText.setTextColor(Color.parseColor("#009AFF"));
                    holder.senderMsgText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            holder.itemView.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(messages.getMessage())));
                        }
                    });
                }
            }
            else
            {
                holder.messageReceiver.setVisibility(View.VISIBLE);
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.receiverName.setVisibility(View.VISIBLE);
                holder.receiverMsgText.setVisibility(View.VISIBLE);
                holder.senTime.setVisibility(View.INVISIBLE);

                holder.receiverMsgText.setBackgroundResource(R.drawable.receiver_message);
                holder.receiverMsgText.setText(messages.getMessage());
                holder.receive_time.setText( messages.getTime());
                holder.senderMsgText.setTextColor(Color.WHITE);

                //check if url
                if (URLUtil.isValidUrl(messages.getMessage()))
                {
                    holder.receiverMsgText.setText(Html.fromHtml("<u>"+ messages.getMessage() +"</u>"));
                    holder.receiverMsgText.setTextColor(Color.parseColor("#FFFFC800"));
                    holder.receiverMsgText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            holder.itemView.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(messages.getMessage())));
                        }
                    });
                }
            }
        }
        else if (fromMsgType.equals("image"))
        {
            if (fromUserId.equals(msgSenderId))
            {
                holder.messageSenderImage.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.messageSenderImage);
                holder.senTime.setVisibility(View.GONE);
                holder.receive_time.setVisibility(View.GONE);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(holder.itemView.getContext(), ImageViewer.class);
                        intent.putExtra("url", userMessagesList.get(position).getMessage());
                        holder.itemView.getContext().startActivity(intent); 
                    }
                });
            }
            else
            {
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.messageReceiverImage.setVisibility(View.VISIBLE);
                holder.senTime.setVisibility(View.GONE);
                holder.receive_time.setVisibility(View.GONE);

                Picasso.get().load(messages.getMessage()).into(holder.messageReceiverImage);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(holder.itemView.getContext(), ImageViewer.class);
                        intent.putExtra("url", userMessagesList.get(position).getMessage());
                        holder.itemView.getContext().startActivity(intent);
                    }
                });
            }
        }
        else if (fromMsgType.equals("video"))
        {
            if (fromUserId.equals(msgSenderId))
            {
                holder.messageSenderVideo.setVisibility(View.VISIBLE);
                holder.senTime.setVisibility(View.GONE);
                holder.receive_time.setVisibility(View.GONE);
                holder.playTwo.setVisibility(View.VISIBLE);
                holder.playOne.setVisibility(View.GONE);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(holder.itemView.getContext(), videoView.class);
                        intent.putExtra("url", userMessagesList.get(position).getMessage());
                        holder.itemView.getContext().startActivity(intent);
                    }
                });
            }
            else
            {
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.messageReceiverVideo.setVisibility(View.VISIBLE);
                holder.senTime.setVisibility(View.GONE);
                holder.receive_time.setVisibility(View.GONE);
                holder.playTwo.setVisibility(View.GONE);
                holder.playOne.setVisibility(View.VISIBLE);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(holder.itemView.getContext(), videoView.class);
                        intent.putExtra("url", userMessagesList.get(position).getMessage());
                        holder.itemView.getContext().startActivity(intent);
                    }
                });
            }
        }
        else if (fromMsgType.equals("pdf"))
        {
            if (fromUserId.equals(msgSenderId))
            {
                holder.messageSenderImage.setVisibility(View.VISIBLE);
                holder.senTime.setVisibility(View.GONE);
                holder.receive_time.setVisibility(View.GONE);

                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/wechat-6ffe4.appspot.com/o/Image%20Files%2Fpdf_file.png?alt=media&token=1d7d5481-9702-4972-b9a1-852420130f2d")
                        .into(holder.messageSenderImage);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                        holder.itemView.getContext().startActivity(intent);
                    }
                });

            }
            else
            {
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.messageReceiverImage.setVisibility(View.VISIBLE);
                holder.senTime.setVisibility(View.GONE);
                holder.receive_time.setVisibility(View.GONE);

                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/wechat-6ffe4.appspot.com/o/Image%20Files%2Fpdf_file.png?alt=media&token=1d7d5481-9702-4972-b9a1-852420130f2d")
                        .into(holder.messageReceiverImage);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                        holder.itemView.getContext().startActivity(intent);
                    }
                });
            }
        }
        else if (fromMsgType.equals("docx"))
        {
            if (fromUserId.equals(msgSenderId))
            {
                holder.messageSenderImage.setVisibility(View.VISIBLE);
                holder.senTime.setVisibility(View.GONE);
                holder.receive_time.setVisibility(View.GONE);

                holder.messageSenderImage.setBackgroundResource(R.drawable.word_icon);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                        holder.itemView.getContext().startActivity(intent);
                    }
                });
            }
            else
            {
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.messageReceiverImage.setVisibility(View.VISIBLE);
                holder.senTime.setVisibility(View.GONE);
                holder.receive_time.setVisibility(View.GONE);

                holder.messageReceiverImage.setBackgroundResource(R.drawable.word_icon);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                        holder.itemView.getContext().startActivity(intent);
                    }
                });
            }
        }

        if (fromUserId.equals(msgSenderId))
        {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v)
                {
                    if (userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx")) {
                        CharSequence options[] = new CharSequence[]{
                                "Delete For me",
                                "Delete For everyone",
                                "Download and View this Document",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Files Options:");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                //Delete For me
                                if (which == 0)
                                {
                                    deleteSentMessages(position, holder);

                                    userMessagesList.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position,userMessagesList.size());
                                }

                                //Delete For everyone
                                else if (which == 1)
                                {
                                    deleteMessagesForEveryOne(position, holder);

                                    userMessagesList.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position,userMessagesList.size());

                                }

                                //Download and View this Document
                                else if (which == 2)
                                {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                    else if (userMessagesList.get(position).getType().equals("text")) {
                        CharSequence options[] = new CharSequence[]{
                                "Delete For me",
                                "Delete For everyone",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Files Options:");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                //Delete For me
                                if (which == 0)
                                {
                                    deleteSentMessages(position, holder);

                                    userMessagesList.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position,userMessagesList.size());

                                }

                                //Delete For everyone
                                else if (which == 1)
                                {
                                    deleteMessagesForEveryOne(position, holder);

                                    userMessagesList.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position,userMessagesList.size());

                                }

                            }
                        });
                        builder.show();
                    }
                    else if (userMessagesList.get(position).getType().equals("image") ) {
                        CharSequence options[] = new CharSequence[]{
                                "Delete For me",
                                "Delete For everyone",
                                "Download this Image",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Message Options:");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                //Delete For me
                                if (which == 0)
                                {
                                    deleteSentMessages(position, holder);

                                    userMessagesList.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position,userMessagesList.size());

                                }

                                //Delete For everyone
                                else if (which == 1)
                                {
                                    deleteMessagesForEveryOne(position, holder);

                                    userMessagesList.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position,userMessagesList.size());

                                }
                                //Download this Image
                                else if (which == 2)
                                {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });
                        builder.show();
                    }
                    return false;
                }
            });
        }

        else
        {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v)
                {
                    if (userMessagesList.get(position).getType().equals("pdf") || userMessagesList.get(position).getType().equals("docx")) {
                        CharSequence options[] = new CharSequence[]{
                                "Delete For me",
                                "Download and View this Document",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Files Options:");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                //Delete For me
                                if (which == 0)
                                {
                                    deleteReceiveMessages(position, holder);

                                    userMessagesList.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position,userMessagesList.size());

                                }

                                //Download and View this Document
                                else if (which == 1)
                                {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
                        builder.show();
                    }
                    else if (userMessagesList.get(position).getType().equals("text")) {
                        CharSequence options[] = new CharSequence[]{
                                "Delete For me",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Files Options:");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                //Delete For me
                                if (which == 0)
                                {
                                    deleteReceiveMessages(position, holder);

                                    userMessagesList.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position,userMessagesList.size());

                                }
                            }
                        });
                        builder.show();
                    }
                    else if (userMessagesList.get(position).getType().equals("image") ) {
                        CharSequence options[] = new CharSequence[]{
                                "Delete For me",
                                "Download this Image",
                                "Cancel"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Message Options:");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                //Delete For me
                                if (which == 0)
                                {
                                    deleteReceiveMessages(position, holder);

                                    userMessagesList.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position,userMessagesList.size());

                                }

                                //Download this Image
                                else if (which == 1)
                                {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                    holder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });
                        builder.show();
                    }
                    return false;
                }
            });
        }
    }


    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    private void deleteSentMessages(final int position, final MessagesViewHolder messageHolder)
    {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

        rootRef.child("Messages").child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    Toast.makeText(messageHolder.itemView.getContext(), "Deleted Successfully.", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(messageHolder.itemView.getContext(), "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteReceiveMessages(final int position, final MessagesViewHolder messageHolder)
    {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages").child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    Toast.makeText(messageHolder.itemView.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(messageHolder.itemView.getContext(), "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteMessagesForEveryOne(final int position, final MessagesViewHolder messageHolder)
    {
        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        if (!userMessagesList.get(position).getFrom().equals(userMessagesList.get(position).getTo()))
        {
            rootRef.child("Messages")
                    .child(userMessagesList.get(position).getTo())
                    .child(userMessagesList.get(position).getFrom())
                    .child(userMessagesList.get(position).getMessageID())
                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    if (task.isSuccessful())
                    {
                        rootRef.child("Messages").child(userMessagesList.get(position).getFrom())
                                .child(userMessagesList.get(position).getTo())
                                .child(userMessagesList.get(position).getMessageID())
                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task)
                            {
                                if (task.isSuccessful())
                                {
                                    Toast.makeText(messageHolder.itemView.getContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    else {
                        Toast.makeText(messageHolder.itemView.getContext(), "Error", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

}
