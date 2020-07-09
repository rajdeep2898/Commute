package com.example.myapp2.commute;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private TextView mProfileName,mProfileStatus,mProfileFriendsCount;
    private ImageView mProfileImage;
    private Button mProfileSendReqBtn,mDeclineBtn;

    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;

    private DatabaseReference mRootRef;



    private FirebaseUser mCurrent_user;

    private ProgressDialog mProcessDialog;

    private String mcurrent_state;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id=getIntent().getStringExtra("from_user_id");


        mRootRef = FirebaseDatabase.getInstance().getReference();
        mUsersDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendReqDatabase=FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase=FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase=FirebaseDatabase.getInstance().getReference().child("notifications");


        mCurrent_user= FirebaseAuth.getInstance().getCurrentUser();

        mProfileName=(TextView) findViewById(R.id.profile_displayName);
        mProfileStatus=(TextView) findViewById(R.id.profile_status);
        mProfileFriendsCount=(TextView) findViewById(R.id.profile_totalFriends);
        mProfileImage=(ImageView)findViewById(R.id.profile_image);
        mProfileSendReqBtn=(Button)findViewById(R.id.profile_send_Req_Btn);
        mDeclineBtn=(Button)findViewById(R.id.profile_decline_Btn);

        mcurrent_state="not friends";

        mDeclineBtn.setVisibility(View.INVISIBLE);
        mDeclineBtn.setEnabled(false);

        mProcessDialog=new ProgressDialog(this);
        mProcessDialog.setTitle("Loding User Data");
        mProcessDialog.setMessage("Wait while we load User Data");
        mProcessDialog.setCanceledOnTouchOutside(false);
        mProcessDialog.show();

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String display_name=dataSnapshot.child("name").getValue().toString();
                String status=dataSnapshot.child("status").getValue().toString();
                String image=dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(display_name);
                mProfileStatus.setText(status);

                Picasso.get().load(image).placeholder(R.drawable.a).into(mProfileImage);


                //---------------FRIEND REQUEST FEATURE-------------------



                mFriendReqDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(user_id)){
                            String req_type=dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if(req_type.equals("received")){
                                mProfileSendReqBtn.setEnabled(true);
                                mcurrent_state="req_received";
                                mProfileSendReqBtn.setText("Accept friend request");

                                mDeclineBtn.setVisibility(View.VISIBLE);
                                mDeclineBtn.setEnabled(true);

                            }else if(req_type.equals("sent")){

                                mProfileSendReqBtn.setEnabled(true);
                                mcurrent_state="req_sent";
                                mProfileSendReqBtn.setText("Cancel friend request");
                                Toast.makeText(ProfileActivity.this, "hello", Toast.LENGTH_SHORT).show();

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);

                            }
                            mProcessDialog.dismiss();

                        }else{
                            mFriendDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(user_id)){

                                        mProfileSendReqBtn.setEnabled(true);
                                        mcurrent_state="friends";
                                        mProfileSendReqBtn.setText("Unfriend");

                                        mDeclineBtn.setVisibility(View.INVISIBLE);
                                        mDeclineBtn.setEnabled(false);

                                    }
                                    mProcessDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    mProcessDialog.dismiss();

                                }
                            });
                        }
                        mProcessDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProfileSendReqBtn.setEnabled(false);
                //----------REQUEST SENT-------------NOT FRIENDS STATE----

                if(mcurrent_state.equals("not friends"))
                {
                    DatabaseReference newNotificationref = mRootRef.child("notifications").child(user_id).push();
                    String newNotificationId = newNotificationref.getKey();

                    Map notificationData = new HashMap();
                    notificationData.put("from", mCurrent_user.getUid());
                    notificationData.put("type", "request");

                    Map requestSentAddMap = new HashMap();
                    requestSentAddMap.put("request_type", "sent");
                    requestSentAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map requestRecievedAddMap = new HashMap();
                    requestRecievedAddMap.put("request_type", "received");
                    requestRecievedAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map requestMap = new HashMap();
                    requestMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id , requestSentAddMap);
                    requestMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid(), requestRecievedAddMap);
                    requestMap.put("notifications/" + user_id + "/" + newNotificationId, notificationData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null){

                                Toast.makeText(ProfileActivity.this, "There was some error in sending request", Toast.LENGTH_SHORT).show();

                            }

                                mcurrent_state = "req_sent";
                                mProfileSendReqBtn.setText("Cancel Friend Request");



                             mProfileSendReqBtn.setEnabled(true);


                        }
                    });
                }

                //-------------CANCEL REQUEST STATE-----------
                if(mcurrent_state.equals("req_sent"))
                {
                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mProfileSendReqBtn.setEnabled(true);
                                    mcurrent_state="not friends";
                                    mProfileSendReqBtn.setText("Send friend request");

                                    mDeclineBtn.setVisibility(View.INVISIBLE);
                                    mDeclineBtn.setEnabled(false);

                                }
                            });
                        }
                    });
                }

                //-------------Request received state-----------------

                if(mcurrent_state.equals("req_received")){

                    final String currentDate= DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id + "/date", currentDate);
                    friendsMap.put("Friends/" + user_id + "/"  + mCurrent_user.getUid() + "/date", currentDate);


                    friendsMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id, null);
                    friendsMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid(), null);


                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {


                            if(databaseError == null){

                                mProfileSendReqBtn.setEnabled(true);
                                mcurrent_state = "friends";
                                mProfileSendReqBtn.setText("Unfriend this Person");

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);

                            } else {

                                String error = databaseError.getMessage();

                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();


                            }

                        }
                    });

                }
                //------------UNFRIEND----------------
                if(mcurrent_state.equals("friends")){

                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id, null);
                    unfriendMap.put("Friends/" + user_id + "/" + mCurrent_user.getUid(), null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {


                            if(databaseError == null){

                                mcurrent_state = "not_friends";
                                mProfileSendReqBtn.setText("Send Friend Request");

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);

                            } else {

                                String error = databaseError.getMessage();

                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();


                            }

                            mProfileSendReqBtn.setEnabled(true);

                        }
                    });

                }

            }
        });
    }
}
