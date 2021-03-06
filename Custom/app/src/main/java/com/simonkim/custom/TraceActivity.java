package com.simonkim.custom;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;


public class TraceActivity extends AppCompatActivity {

    private ListView listView;
    private ImageView empty_img;
    private ImageView loading_img;
    ArrayList<TraceListItemClass> traceList;
    ListAdapter adapter;

    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    String device_token;
    String cu;

    private AdView mAdView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("배송목록");
        setContentView(R.layout.activity_trace);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        cu = currentUser.getUid();

        listView = (ListView) findViewById(R.id.tracelist);
        empty_img = (ImageView) findViewById(R.id.empty_img);
        loading_img = (ImageView) findViewById(R.id.loading_img);
        loading_img.setVisibility(View.VISIBLE);

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("TAG", "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        final String token = task.getResult().getToken();

                        mDatabase.child("devices").child(cu).child(token).setValue(" ");
                        device_token = token;
                    }
                });

        mDatabase.child("users").child(cu).orderByChild("nowTime").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int cntFinish = 0;
                loading_img.setVisibility(View.INVISIBLE);
                traceList = new ArrayList<>();
                for (DataSnapshot fileSnapshot : dataSnapshot.getChildren()) {
                    TraceListItemClass temp = new TraceListItemClass();
                    temp.product_name = fileSnapshot.child("productName").getValue(String.class);
                    temp.trace_company = fileSnapshot.child("traceCompany").getValue(String.class);
                    temp.trace_number = fileSnapshot.child("traceNumber").getValue(String.class);
                    temp.now_status = fileSnapshot.child("nowStatus").getValue(String.class);
                    if(temp.now_status.equals("배달완료")) {
                        if(cntFinish == 0)
                            traceList.add(temp);
                        else
                            traceList.add(traceList.size() - cntFinish, temp);
                        cntFinish++;
                    } else {
                        traceList.add(0, temp);
                    }
                }
                if(!traceList.isEmpty()) {
                    empty_img.setVisibility(View.INVISIBLE);
                } else {
                    empty_img.setVisibility(View.VISIBLE);
                }
                adapter = new ListAdapter(traceList);
                adapter.notifyDataSetChanged();
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("TAG: ", "Failed to read value", databaseError.toException());
                loading_img.setVisibility(View.VISIBLE);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String toDetail = traceList.get(position).product_name;
                Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
                intent.putExtra("toDetail", toDetail);
                startActivity(intent);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final String toDelete = traceList.get(position).product_name;
                AlertDialog.Builder builder = new AlertDialog.Builder(TraceActivity.this);

                builder.setTitle("삭제").setMessage(toDelete + "을(를) 정말 지우시겠습니까?");

                builder.setPositiveButton("닫기", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                    }
                });

                builder.setNegativeButton("삭제", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                        mDatabase.child("users").child(cu).child(toDelete).removeValue().addOnSuccessListener(
                                new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(getApplicationContext(), "삭제되었습니다", Toast.LENGTH_SHORT).show();
                                    }
                                }
                        );
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_setting:
                final String PREFERENCE = "com.simonkim.custom";
                final SharedPreferences pref = getSharedPreferences(PREFERENCE, MODE_PRIVATE);

                final EditText edittext = new EditText(this);

                FrameLayout container = new FrameLayout(this);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.leftMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
                params.rightMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);

                edittext.setLayoutParams(params);

                container.addView(edittext);
                edittext.setText(pref.getString("personalCode", ""));

                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("설정").setMessage("개인통관고유부호를 메모하실 수 있습니다").setView(container);

                builder.setPositiveButton("닫기", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                    }
                });

                builder.setNegativeButton("저장", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("personalCode", edittext.getText().toString());
                        editor.commit();
                    }
                });

                builder.setNeutralButton("로그아웃", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                        FirebaseAuth.getInstance().signOut();
                        SharedPreferences.Editor editor = pref.edit();
                        edittext.setText("");
                        editor.putString("personalCode", edittext.getText().toString());
                        editor.commit();
                        mDatabase.child("devices").child(cu).child(device_token).removeValue().addOnSuccessListener(
                                new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                    }
                                }
                        );
                        Toast.makeText(getApplicationContext(), "로그아웃 되었습니다", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(TraceActivity.this,
                                MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                return true;
            case R.id.action_add:
                Intent intent = new Intent(getApplicationContext(),
                        AddtraceActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
