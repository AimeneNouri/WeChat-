package com.example.wechat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
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
import androidx.recyclerview.widget.RecyclerView;

import com.example.wechat.activities.ImageViewer;
import com.example.wechat.activities.MainActivity;
import com.example.wechat.activities.PdfReader;
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

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupMessageAdapter extends RecyclerView.Adapter<GroupMessageAdapter.MessageViewHolder> {

    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, GroupsRef;

    public GroupMessageAdapter(Context context, List<Messages> userMessagesList)
    {
        this.userMessagesList = userMessagesList;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView receiverName, senderMsgText, receiverMsgText, senTime, receive_time, sent_time_video, receiver_time_video,sent_time_image, receiver_time_image, sentPdfMessage, receivePdfMessage, sent_time_receiver_pdf, sent_time_sender_pdf, sentAudioMessage, receiveAudioMessage, sent_time_audio, receive_time_audio;
        public CircleImageView receiverProfileImage;
        public ImageView messageSenderImage, messageReceiverImage, playTwo, playOne, iconReceiverPdf, iconSenderPdf, icon_audio, icon_audio_receiver;
        public VideoView messageSenderVideo, messageReceiverVideo;
        public RelativeLayout messageSender, messageReceiver, videoSenderLayout, videoReceiverLayout, imageSenderLayout, imageReceiverLayout, pdfReceiverLayout, pdfSenderLayout, overflow_pdf_receiver, overflow_pdf_sender, receiver_audio_overflow, receiver_message_audio_layout, sender_audio_overflow, sender_message_audio_Layout;

        public MessageViewHolder(@NonNull View itemView) {
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

            sent_time_video = itemView.findViewById(R.id.sent_time_video);
            receiver_time_video = itemView.findViewById(R.id.receive_time_video);
            videoSenderLayout = itemView.findViewById(R.id.video_sender_layout);
            videoReceiverLayout = itemView.findViewById(R.id.video_receiver_layout);

            sent_time_image = itemView.findViewById(R.id.sent_time_image);
            receiver_time_image = itemView.findViewById(R.id.receive_time_image);
            imageReceiverLayout = itemView.findViewById(R.id.image_receiver_layout);
            imageSenderLayout = itemView.findViewById(R.id.image_sender_layout);

            sentPdfMessage = itemView.findViewById(R.id.sender_message_pdf);
            receivePdfMessage = itemView.findViewById(R.id.receiver_message_pdf);
            sent_time_sender_pdf = itemView.findViewById(R.id.sent_time_pdf);
            sent_time_receiver_pdf = itemView.findViewById(R.id.receive_time_pdf);
            pdfReceiverLayout = itemView.findViewById(R.id.receiver_message_pdf_layout);
            pdfSenderLayout= itemView.findViewById(R.id.sender_message_pdf_Layout);
            iconSenderPdf= itemView.findViewById(R.id.icon_pdf);
            iconReceiverPdf= itemView.findViewById(R.id.icon_pdf_receiver);
            overflow_pdf_receiver= itemView.findViewById(R.id.receiver_pdf_overflow);
            overflow_pdf_sender= itemView.findViewById(R.id.sender_pdf_overflow);

            playOne = itemView.findViewById(R.id.playVideoReceiver);
            playTwo = itemView.findViewById(R.id.playVideoSender);
            senTime = itemView.findViewById(R.id.sent_time);
            receive_time = itemView.findViewById(R.id.receive_time);

            sentAudioMessage = itemView.findViewById(R.id.sender_message_audio);
            icon_audio = itemView.findViewById(R.id.icon_audio);
            sender_audio_overflow = itemView.findViewById(R.id.sender_audio_overflow);
            sender_message_audio_Layout = itemView.findViewById(R.id.sender_message_audio_Layout);
            sent_time_audio = itemView.findViewById(R.id.sent_time_audio);
            receiveAudioMessage = itemView.findViewById(R.id.receiver_message_audio);
            receiver_message_audio_layout = itemView.findViewById(R.id.receiver_message_audio_layout);
            receiver_audio_overflow = itemView.findViewById(R.id.receiver_audio_overflow);
            receive_time_audio = itemView.findViewById(R.id.receive_time_audio);
            icon_audio_receiver = itemView.findViewById(R.id.icon_audio_receiver);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_custom_message, parent, false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        String msgSenderId = mAuth.getCurrentUser().getUid();
        com.example.wechat.Messages messages = userMessagesList.get(position);

        String fromUserId = messages.getFrom();
        String fromMsgType = messages.getType();

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);
        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("image")) {
                    String ReceiverImage = dataSnapshot.child("image").getValue(String.class);
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

        holder.sent_time_video.setVisibility(View.GONE);
        holder.receiver_time_video.setVisibility(View.GONE);
        holder.videoReceiverLayout.setVisibility(View.GONE);
        holder.videoSenderLayout.setVisibility(View.GONE);

        holder.sent_time_image.setVisibility(View.GONE);
        holder.receiver_time_image.setVisibility(View.GONE);
        holder.imageSenderLayout.setVisibility(View.GONE);
        holder.imageReceiverLayout.setVisibility(View.GONE);

        holder.sent_time_sender_pdf.setVisibility(View.GONE);
        holder.sent_time_receiver_pdf.setVisibility(View.GONE);
        holder.pdfSenderLayout.setVisibility(View.GONE);
        holder.pdfReceiverLayout.setVisibility(View.GONE);
        holder.iconReceiverPdf.setVisibility(View.GONE);
        holder.iconSenderPdf.setVisibility(View.GONE);
        holder.receivePdfMessage.setVisibility(View.GONE);
        holder.sentPdfMessage.setVisibility(View.GONE);
        holder.overflow_pdf_receiver.setVisibility(View.GONE);
        holder.overflow_pdf_sender.setVisibility(View.GONE);

        holder.sentAudioMessage.setVisibility(View.GONE);
        holder.receiveAudioMessage.setVisibility(View.GONE);
        holder.sent_time_audio.setVisibility(View.GONE);
        holder.receive_time_audio.setVisibility(View.GONE);
        holder.receiver_audio_overflow.setVisibility(View.GONE);
        holder.sender_audio_overflow.setVisibility(View.GONE);
        holder.icon_audio.setVisibility(View.GONE);
        holder.icon_audio_receiver.setVisibility(View.GONE);
        holder.receiver_message_audio_layout.setVisibility(View.GONE);
        holder.sender_message_audio_Layout.setVisibility(View.GONE);

        if (fromMsgType.equals("text"))
        {
            if (fromUserId.equals(msgSenderId))
            {
                holder.messageSender.setVisibility(View.VISIBLE);
                holder.senderMsgText.setVisibility(View.VISIBLE);
                holder.receive_time.setVisibility(View.INVISIBLE);
                holder.senderMsgText.setBackgroundResource(R.drawable.sender_message);
                holder.senderMsgText.setText(messages.getMessage());
                holder.senTime.setText(messages.getTime());
                //holder.senTime.setTextColor(Color.WHITE);
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

                //check if email
                if (isValid(messages.getMessage()))
                {
                    holder.senderMsgText.setText(Html.fromHtml("<u>"+ messages.getMessage() +"</u>"));
                    holder.senderMsgText.setTextColor(Color.parseColor("#009AFF"));
                    holder.senderMsgText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String recipient = messages.getMessage().trim();

                            Intent mEmailIntent = new Intent(Intent.ACTION_SEND);
                            mEmailIntent.setData(Uri.parse("mailto:"));
                            mEmailIntent.setType("text/plain");
                            mEmailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{recipient});

                            try {
                                holder.itemView.getContext().startActivity(Intent.createChooser(mEmailIntent, "Choose an Email Client"));
                            }catch (Exception e){
                                Toast.makeText(holder.itemView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
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
                    holder.receiverMsgText.setTextColor(Color.parseColor("#ffffff"));
                    holder.receiverMsgText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            holder.itemView.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(messages.getMessage())));
                        }
                    });
                }

                //check if email
                if (isValid(messages.getMessage()))
                {
                    holder.receiverMsgText.setText(Html.fromHtml("<u>"+ messages.getMessage() +"</u>"));
                    holder.receiverMsgText.setTextColor(Color.parseColor("#ffffff"));
                    holder.receiverMsgText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String recipient = messages.getMessage().trim();

                            Intent mEmailIntent = new Intent(Intent.ACTION_SEND);
                            mEmailIntent.setData(Uri.parse("mailto:"));
                            mEmailIntent.setType("text/plain");
                            mEmailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{recipient});

                            try {
                                holder.itemView.getContext().startActivity(Intent.createChooser(mEmailIntent, "Choose an Email Client"));
                            }catch (Exception e){
                                Toast.makeText(holder.itemView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
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
                holder.senTime.setVisibility(View.GONE);
                holder.receive_time.setVisibility(View.GONE);
                holder.sent_time_image.setVisibility(View.VISIBLE);
                holder.receiver_time_image.setVisibility(View.GONE);
                holder.imageSenderLayout.setVisibility(View.VISIBLE);
                holder.imageReceiverLayout.setVisibility(View.GONE);

                Picasso.get().load(messages.getMessage()).into(holder.messageSenderImage);
                holder.sent_time_image.setText( messages.getTime());

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
                holder.sent_time_image.setVisibility(View.GONE);
                holder.receiver_time_image.setVisibility(View.VISIBLE);
                holder.receiverName.setVisibility(View.VISIBLE);
                holder.imageSenderLayout.setVisibility(View.GONE);
                holder.imageReceiverLayout.setVisibility(View.VISIBLE);


                holder.receiver_time_image.setText( messages.getTime());
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
                holder.sent_time_video.setVisibility(View.VISIBLE);
                holder.videoSenderLayout.setVisibility(View.VISIBLE);
                holder.receiver_time_video.setVisibility(View.GONE);
                holder.videoReceiverLayout.setVisibility(View.GONE);

                holder.sent_time_video.setText( messages.getTime());
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
                holder.sent_time_video.setVisibility(View.GONE);
                holder.videoSenderLayout.setVisibility(View.GONE);
                holder.receiverName.setVisibility(View.VISIBLE);
                holder.receiver_time_video.setVisibility(View.VISIBLE);
                holder.videoReceiverLayout.setVisibility(View.VISIBLE);

                holder.receiver_time_video.setText( messages.getTime());
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
                holder.sent_time_sender_pdf.setVisibility(View.VISIBLE);
                holder.sent_time_receiver_pdf.setVisibility(View.GONE);
                holder.pdfSenderLayout.setVisibility(View.VISIBLE);
                holder.pdfReceiverLayout.setVisibility(View.GONE);
                holder.iconReceiverPdf.setVisibility(View.GONE);
                holder.iconSenderPdf.setVisibility(View.VISIBLE);
                holder.receivePdfMessage.setVisibility(View.GONE);
                holder.sentPdfMessage.setVisibility(View.VISIBLE);
                holder.overflow_pdf_receiver.setVisibility(View.GONE);
                holder.overflow_pdf_sender.setVisibility(View.VISIBLE);

                holder.sentPdfMessage.setText("PDF File");
                holder.sent_time_sender_pdf.setText(messages.getTime());

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(holder.itemView.getContext(), PdfReader.class);
                        intent.putExtra("url", userMessagesList.get(position).getMessage());
                        intent.putExtra("id", userMessagesList.get(position).getMessageID());
                        holder.itemView.getContext().startActivity(intent);
                    }
                });

            }
            else
            {
                holder.sent_time_sender_pdf.setVisibility(View.GONE);
                holder.sent_time_receiver_pdf.setVisibility(View.VISIBLE);
                holder.pdfSenderLayout.setVisibility(View.GONE);
                holder.pdfReceiverLayout.setVisibility(View.VISIBLE);
                holder.iconReceiverPdf.setVisibility(View.VISIBLE);
                holder.iconSenderPdf.setVisibility(View.GONE);
                holder.receivePdfMessage.setVisibility(View.VISIBLE);
                holder.receiverName.setVisibility(View.VISIBLE);
                holder.sentPdfMessage.setVisibility(View.GONE);
                holder.overflow_pdf_receiver.setVisibility(View.VISIBLE);
                holder.overflow_pdf_sender.setVisibility(View.GONE);

                holder.receivePdfMessage.setText("PDF File");
                holder.sent_time_receiver_pdf.setText(messages.getTime());

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(holder.itemView.getContext(), PdfReader.class);
                        intent.putExtra("url", userMessagesList.get(position).getMessage());
                        intent.putExtra("id", userMessagesList.get(position).getMessageID());
                        holder.itemView.getContext().startActivity(intent);
                    }
                });
            }
        }
        else if (fromMsgType.equals("docx"))
        {
            if (fromUserId.equals(msgSenderId))
            {
                holder.sent_time_sender_pdf.setVisibility(View.VISIBLE);
                holder.sent_time_receiver_pdf.setVisibility(View.GONE);
                holder.pdfSenderLayout.setVisibility(View.VISIBLE);
                holder.pdfReceiverLayout.setVisibility(View.GONE);
                holder.iconReceiverPdf.setVisibility(View.GONE);
                holder.iconSenderPdf.setVisibility(View.VISIBLE);
                holder.receivePdfMessage.setVisibility(View.GONE);
                holder.sentPdfMessage.setVisibility(View.VISIBLE);
                holder.overflow_pdf_receiver.setVisibility(View.GONE);
                holder.overflow_pdf_sender.setVisibility(View.VISIBLE);

                holder.iconSenderPdf.setBackgroundResource(R.drawable.word_icon);
                holder.sentPdfMessage.setText("docx File");
                holder.sent_time_sender_pdf.setText(messages.getTime());

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(holder.itemView.getContext(), PdfReader.class);
                        intent.putExtra("url", userMessagesList.get(position).getMessage());
                        intent.putExtra("id", userMessagesList.get(position).getMessageID());
                        holder.itemView.getContext().startActivity(intent);
                    }
                });
            }
            else
            {
                holder.sent_time_sender_pdf.setVisibility(View.GONE);
                holder.sent_time_receiver_pdf.setVisibility(View.VISIBLE);
                holder.pdfSenderLayout.setVisibility(View.GONE);
                holder.pdfReceiverLayout.setVisibility(View.VISIBLE);
                holder.iconReceiverPdf.setVisibility(View.VISIBLE);
                holder.iconSenderPdf.setVisibility(View.GONE);
                holder.receivePdfMessage.setVisibility(View.VISIBLE);
                holder.sentPdfMessage.setVisibility(View.GONE);
                holder.receiverName.setVisibility(View.VISIBLE);
                holder.overflow_pdf_receiver.setVisibility(View.VISIBLE);
                holder.overflow_pdf_sender.setVisibility(View.GONE);

                holder.iconReceiverPdf.setBackgroundResource(R.drawable.word2);
                holder.receivePdfMessage.setText("docx File");
                holder.sent_time_receiver_pdf.setText(messages.getTime());

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(holder.itemView.getContext(), PdfReader.class);
                        intent.putExtra("url", userMessagesList.get(position).getMessage());
                        intent.putExtra("id", userMessagesList.get(position).getMessageID());
                        holder.itemView.getContext().startActivity(intent);
                    }
                });
            }
        }

        else if (fromMsgType.equals("audio"))
        {
            if (fromUserId.equals(msgSenderId))
            {
                holder.sentAudioMessage.setVisibility(View.VISIBLE);
                holder.receiveAudioMessage.setVisibility(View.GONE);
                holder.sent_time_audio.setVisibility(View.VISIBLE);
                holder.receive_time_audio.setVisibility(View.GONE);
                holder.receiver_audio_overflow.setVisibility(View.GONE);
                holder.sender_audio_overflow.setVisibility(View.VISIBLE);
                holder.icon_audio.setVisibility(View.VISIBLE);
                holder.icon_audio_receiver.setVisibility(View.GONE);
                holder.receiver_message_audio_layout.setVisibility(View.GONE);
                holder.sender_message_audio_Layout.setVisibility(View.VISIBLE);

                holder.sentAudioMessage.setText("Audio ");
                holder.sent_time_audio.setText(messages.getTime());
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MediaPlayer mediaPlayer = new MediaPlayer();
                        try {
                            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            mediaPlayer.setDataSource(userMessagesList.get(position).getMessage());
                            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    mp.start();
                                }
                            });
                            mediaPlayer.prepareAsync();
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                });
            }
            else
            {
                holder.sentAudioMessage.setVisibility(View.GONE);
                holder.receiveAudioMessage.setVisibility(View.VISIBLE);
                holder.sent_time_audio.setVisibility(View.GONE);
                holder.receive_time_audio.setVisibility(View.VISIBLE);
                holder.receiver_audio_overflow.setVisibility(View.VISIBLE);
                holder.sender_audio_overflow.setVisibility(View.GONE);
                holder.icon_audio.setVisibility(View.GONE);
                holder.icon_audio_receiver.setVisibility(View.VISIBLE);
                holder.receiver_message_audio_layout.setVisibility(View.VISIBLE);
                holder.sender_message_audio_Layout.setVisibility(View.GONE);
                holder.receiverProfileImage.setVisibility(View.VISIBLE);

                holder.receiveAudioMessage.setText("Audio ");
                holder.receive_time_audio.setText(messages.getTime());
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MediaPlayer mediaPlayer = new MediaPlayer();
                        try {
                            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            mediaPlayer.setDataSource(userMessagesList.get(position).getMessage());
                            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    mp.start();
                                }
                            });
                            mediaPlayer.prepareAsync();
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    public static boolean isValid(String email)
    {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }

}
