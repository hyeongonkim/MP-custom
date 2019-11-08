package com.example.custom;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;

public class AddtraceActivity extends AppCompatActivity {
    private EditText product_name_input;
    private EditText trace_number_input;
    private Button save_btn;

    private Spinner trace_year;
    ArrayList<String> arrayList;
    ArrayAdapter<String> arrayAdapter;

    private Spinner trace_company;
    ArrayList<String> companyList;
    ArrayAdapter<String> companyAdapter;

    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("송장추가");
        setContentView(R.layout.activity_addtrace);

        product_name_input = (EditText) findViewById(R.id.product_name_input);
        trace_number_input = (EditText) findViewById(R.id.trace_number_input);
        trace_number_input.setFilters(new InputFilter[]{filterTrace});
        save_btn = (Button) findViewById(R.id.save);

        arrayList = new ArrayList<>();
        arrayList.add("2019");
        arrayList.add("2018");

        arrayAdapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_spinner_dropdown_item,
                arrayList);

        trace_year = (Spinner)findViewById(R.id.trace_year_spinner);
        trace_year.setAdapter(arrayAdapter);
        trace_year.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        companyList = new ArrayList<>();
        companyList.add("우체국");
        companyList.add("대한통운");
        companyList.add("한진택배");
        companyList.add("EMS");
        companyList.add("DHL");
        companyList.add("FedEx");
        companyList.add("UPS");
        companyList.add("로젠택배");
        companyList.add("천일택배");
        companyList.add("경동택배");
        companyList.add("대신택배");

        companyAdapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_spinner_dropdown_item,
                companyList);

        trace_company = (Spinner)findViewById(R.id.trace_company_spinner);
        trace_company.setAdapter(companyAdapter);
        trace_company.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        final Pattern ps = Pattern.compile("^[A-Z]{2}+[0-9]{9}+[A-Z]{2}+$");

        trace_number_input.addTextChangedListener(new TextWatcher() {
                                    @Override
                                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                                        if(ps.matcher(trace_number_input.getText().toString()).matches()) {
                                            companyList.clear();
                                            companyList.add("EMS");
                                        } else if(trace_number_input.getText().toString().length() == 10) {
                                            companyList.clear();
                                            companyList.add("대한통운");
                                            companyList.add("한진택배");
                                            companyList.add("DHL");
                                            companyList.add("경동택배");
                                        } else if(trace_number_input.getText().toString().length() == 11) {
                                            companyList.clear();
                                            companyList.add("로젠택배");
                                            companyList.add("천일택배");
                                            companyList.add("경동택배");
                                        } else if(trace_number_input.getText().toString().length() == 12) {
                                            companyList.clear();
                                            companyList.add("한진택배");
                                            companyList.add("FedEx");
                                        } else if(trace_number_input.getText().toString().length() == 13) {
                                            companyList.clear();
                                            companyList.add("우체국");
                                            companyList.add("대신택배");
                                        } else {
                                            companyList.clear();
                                            companyList.add("우체국");
                                            companyList.add("대한통운");
                                            companyList.add("한진택배");
                                            companyList.add("EMS");
                                            companyList.add("DHL");
                                            companyList.add("FedEx");
                                            companyList.add("UPS");
                                            companyList.add("로젠택배");
                                            companyList.add("천일택배");
                                            companyList.add("경동택배");
                                            companyList.add("대신택배");
                                        }
                                        trace_company.setAdapter(companyAdapter);
                                        trace_company.setSelection(0);
                                    }

                                    @Override
                                    public void afterTextChanged(Editable arg0) {
                                    }

                                    @Override
                                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                    }
                                });

        save_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String p_name = product_name_input.getText().toString();
                String t_number = trace_number_input.getText().toString();
                String t_year = trace_year.getSelectedItem().toString();
                String t_company = trace_company.getSelectedItem().toString();
                Map n_time = ServerValue.TIMESTAMP;
                AddTraceClass forSave = new AddTraceClass(p_name, t_number, t_year, t_company, n_time);

                mAuth = FirebaseAuth.getInstance();
                currentUser = mAuth.getCurrentUser();
                String cu = currentUser.getUid();
                mDatabase.child("users").child(cu).child(p_name).setValue(forSave);

                Toast.makeText(getApplicationContext(), "저장되었습니다", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    protected InputFilter filterTrace = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence charSequence, int i, int i1, Spanned spanned, int i2, int i3) {
            Pattern ps = Pattern.compile("^[A-Z0-9]+$");
            if(!ps.matcher(charSequence).matches()) {
                return"";
            }
            return null;
        }
    };
}
