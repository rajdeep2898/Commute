package com.example.myapp2.commute;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout mDisplayName;
    private TextInputLayout mEmail;
    private TextInputLayout mPassword;
    private Button mCreateBtn;

    private FirebaseAuth mAuth;
    private DatabaseReference mdatabase;

    private ProgressDialog mRegProgress;

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        mToolbar=(Toolbar) findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth=FirebaseAuth.getInstance();

        mRegProgress=new ProgressDialog(this);

        mDisplayName=(TextInputLayout) findViewById(R.id.reg_display_name);
        mEmail=(TextInputLayout) findViewById(R.id.reg_email);
        mPassword=(TextInputLayout) findViewById(R.id.reg_password);
        mCreateBtn=(Button) findViewById(R.id.reg_create_button);

        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String display_name=mDisplayName.getEditText().getText().toString();
                String email=mEmail.getEditText().getText().toString();
                String password=mPassword.getEditText().getText().toString();
                if(!TextUtils.isEmpty(display_name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
                    mRegProgress.setTitle("Registering User");
                    mRegProgress.setMessage("Registering your account");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();
                    register_user(display_name, email, password);
                }
            }
        });

    }

    private void register_user(final String display_name, String email, String password) {

        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful())
                {
//                    FirebaseUser current_user=FirebaseAuth.getInstance().getCurrentUser();
//                    String uid=current_user.getUid();
//
//                    mdatabase=FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
//
//                    HashMap<String,String> userMap=new HashMap<String, String>();
//
//                    userMap.put("name",display_name);
//                    userMap.put("status","hi there..i am using commute");
//                    userMap.put("image","default");
//                    userMap.put("thumb_image","default");
//
//                    for (String v:userMap.values()){
//                        Log.d("MainActivity",v);
//                    }
//
//                    mdatabase.setValue(userMap);

                      String user_id=mAuth.getCurrentUser().getUid();
                      DatabaseReference current_user_db=FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
                    String deviceToken= FirebaseInstanceId.getInstance().getToken();


                    Map newPost=new HashMap();
                    newPost.put("devide token",deviceToken);
                    newPost.put("name",display_name);
                    newPost.put("status","HI there...i am using Commute");
                    newPost.put("image","default");
                    newPost.put("thumb_image","default");

                    current_user_db.setValue(newPost).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                mRegProgress.dismiss();
                                Intent mainIntent=new Intent(RegisterActivity.this,MainActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainIntent);
                                finish();

                            }
                        }
                    });




                    mRegProgress.dismiss();
                    Intent mainIntent=new Intent(RegisterActivity.this,MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntent);
                    finish();
                }else{
                    mRegProgress.hide();
                    Toast.makeText(RegisterActivity.this,"Failed sign-in..",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
