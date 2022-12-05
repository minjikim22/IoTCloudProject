package com.example.android_resapi.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.android_resapi.R;
import com.example.android_resapi.ui.apicall.GetThingShadow;
import com.example.android_resapi.ui.apicall.UpdateShadow;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Timer;
import java.util.TimerTask;

public class DeviceActivity extends AppCompatActivity {
    String urlStr;
    final static String TAG = "AndroidAPITest";
    Timer timer;
    Button startGetBtn;
    Button stopGetBtn;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        Intent intent = getIntent();
        urlStr = intent.getStringExtra("thingShadowURL"); // 받아온 URL 정보를 urlStr에 저장

        startGetBtn = findViewById(R.id.startGetBtn); // 조회시작 버튼
        startGetBtn.setEnabled(true);
        startGetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        new GetThingShadow(DeviceActivity.this, urlStr).execute();
                    }
                },
                        0,2000);

                startGetBtn.setEnabled(false);
                stopGetBtn.setEnabled(true);
            }
        });

        stopGetBtn = findViewById(R.id.stopGetBtn);
        stopGetBtn.setEnabled(false);
        stopGetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (timer != null)
                    timer.cancel();
                clearTextView();
                startGetBtn.setEnabled(true);
                stopGetBtn.setEnabled(false);
            }
        });

        Button updateBtn = findViewById(R.id.updateBtn);
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText edit_waterlv = findViewById(R.id.edit_waterlv);
                EditText edit_bar = findViewById(R.id.edit_bar);

                JSONObject payload = new JSONObject();

                try {
                    JSONArray jsonArray = new JSONArray();
                    String waterlv_input = edit_waterlv.getText().toString();
                    if (waterlv_input != null && !waterlv_input.equals("")) {
                        JSONObject tag1 = new JSONObject();
                        tag1.put("tagName", "waterlevel");
                        tag1.put("tagValue", waterlv_input);

                        jsonArray.put(tag1);
                    }

                    String bar_input = edit_bar.getText().toString();
                    if (bar_input != null && !bar_input.equals("")) {
                        JSONObject tag2 = new JSONObject();
                        tag2.put("tagName", "BAR");
                        tag2.put("tagValue", bar_input);

                        jsonArray.put(tag2);
                    }

                    if (jsonArray.length() > 0)
                        payload.put("tags", jsonArray);
                } catch (JSONException e) {
                    Log.e(TAG, "JSONEXception");
                }
                Log.i(TAG,"payload="+payload);
                if (payload.length() >0 )
                    new UpdateShadow(DeviceActivity.this,urlStr).execute(payload);
                else
                    Toast.makeText(DeviceActivity.this,"변경할 상태 정보 입력이 필요합니다", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void clearTextView() {
        TextView reported_barTV = findViewById(R.id.reported_bar);
        TextView reported_waterlvTV = findViewById(R.id.reported_waterlv);
        reported_waterlvTV.setText("");
        reported_barTV.setText("");

        TextView desired_barTV = findViewById(R.id.desired_bar);
        TextView desired_waterlvTV = findViewById(R.id.desired_waterlv);
        desired_waterlvTV.setText("");
        desired_barTV.setText("");
    }

}


