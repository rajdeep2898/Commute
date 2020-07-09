package com.example.myapp2.commute;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {
    private RecyclerView mRequestList;
    private DatabaseReference mRequestDatabase;
    private DatabaseReference mMessageDatabase;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;
    private String mCurrent_user_id;
    private View mMainView;


    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_request, container, false);
        mMainView = inflater.inflate(R.layout.fragment_request, container, false);
        mRequestList = mMainView.findViewById(R.id.request_list);
        mAuth = FirebaseAuth.getInstance();
        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req").child(mCurrent_user_id);

        mRequestDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);



        mRequestList.setHasFixedSize(true);
        mRequestList.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inflate the layout for this fragment
        return mMainView;
    }
    @Override
    public void onStart() {
        super.onStart();
        //
        FirebaseRecyclerOptions<Request> options =

                new FirebaseRecyclerOptions.Builder<Request>()
                        .setQuery( mRequestDatabase.orderByChild("timestamp"), Request.class)
                        .build();

        FirebaseRecyclerAdapter requestRecyclerViewAdapter = new FirebaseRecyclerAdapter<Request, RequestViewHolder>(options) {


            @Override
            protected void onBindViewHolder(@NonNull final RequestViewHolder friendsViewHolder, int i, @NonNull Request request) {
                friendsViewHolder.setRequest_type(request.getRequest_type());

                final String list_user_id = getRef(i).getKey();

                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String userThumb = dataSnapshot.child("thumb_image").getValue().toString();

                        if(dataSnapshot.hasChild("online")) {

                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            friendsViewHolder.setUserOnline(userOnline);

                        }

                        friendsViewHolder.setName(userName);
                        friendsViewHolder.setUserImage(userThumb, getContext());
                        // When Click to Friends View
                        friendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                profileIntent.putExtra("from_user_id", list_user_id);
                                startActivity(profileIntent);

                            }
                        });


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }


            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_layout, parent, false);
                return new RequestViewHolder(view);
            }


        };

        mRequestList.setAdapter(requestRecyclerViewAdapter);
        requestRecyclerViewAdapter.startListening();

    }


    public class RequestViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public RequestViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        public void setRequest_type(String request_type){

            TextView userStatusView = mView.findViewById(R.id.user_single_status);
            userStatusView.setText(request_type);
//            Log.e("raju",request_type);
//            if(request_type.equals("sent"))
//            userStatusView.setText("received");
//            else {
//                if (request_type.equals("received"))
//                    userStatusView.setText("sent");
//            }


        }

        public void setName(String name){

            TextView userNameView = mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);

        }

        public void setUserImage(String thumb_image, Context ctx){

            CircleImageView userImageView = mView.findViewById(R.id.user_single_image);
            Picasso.get().load(thumb_image).placeholder(R.drawable.a).into(userImageView);

        }

        public void setUserOnline(String online_status) {

            ImageView userOnlineView =mView.findViewById(R.id.user_single_online_icon);

            if(online_status.equals("true")){

                userOnlineView.setVisibility(View.VISIBLE);

            } else {

                userOnlineView.setVisibility(View.INVISIBLE);

            }

        }


    }

}
