package com.example.android_resapi.ui.apicall;

import android.app.Activity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.example.android_resapi.R;
import com.example.android_resapi.httpconnection.GetRequest;

public class GetThingShadow extends GetRequest {
    final static String TAG = "AndroidAPITest";
    String urlStr;
    public GetThingShadow(Activity activity, String urlStr) {
        super(activity);
        this.urlStr = urlStr;
    }

    @Override
    protected void onPreExecute() {
        try {
            Log.e(TAG, urlStr);
            url = new URL(urlStr); // url 객체 초기화

        } catch (MalformedURLException e) {
            Toast.makeText(activity,"URL is invalid:"+urlStr, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            activity.finish();
        }
    }

    @Override
    protected void onPostExecute(String jsonString) {
        if (jsonString == null)
            return;
        Map<String, String> state = getStateFromJSONString(jsonString); // 서버로부터 받은 jsonString을 Map형식으로 반환한다.
        TextView reported_barTV = activity.findViewById(R.id.reported_bar);
        TextView reported_waterlvTV = activity.findViewById(R.id.reported_waterlv);
        reported_waterlvTV.setText(state.get("reported_waterlevel"));
        reported_barTV.setText(state.get("reported_BAR"));

        TextView desired_barTV = activity.findViewById(R.id.desired_bar);
        desired_barTV.setText(state.get("desired_BAR"));

    }

    protected Map<String, String> getStateFromJSONString(String jsonString) {
        Map<String, String> output = new HashMap<>();
        try {
            // 처음 double-quote와 마지막 double-quote 제거
            jsonString = jsonString.substring(1,jsonString.length()-1);
            // \\\" 를 \"로 치환
            jsonString = jsonString.replace("\\\"","\"");
            Log.i(TAG, "jsonString="+jsonString);
            JSONObject root = new JSONObject(jsonString);
            JSONObject state = root.getJSONObject("state");
            JSONObject reported = state.getJSONObject("reported");
            String waterlvValue = reported.getString("waterlevel");
            String barValue = reported.getString("BAR");
            output.put("reported_waterlevel", waterlvValue);
            output.put("reported_BAR",barValue);

            JSONObject desired = state.getJSONObject("desired");
            String desired_waterlvValue = desired.getString("waterlevel");
            String desired_barValue = desired.getString("BAR");
            output.put("desired_waterlevel", desired_waterlvValue);
            output.put("desired_BAR",desired_barValue);

        } catch (JSONException e) {
            Log.e(TAG, "Exception in processing JSONString.", e);
            e.printStackTrace();
        }
        return output;
    }
}
