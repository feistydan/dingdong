package edu.swarthmore.cs.thesexbutton;

import android.app.DownloadManager;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.AvoidXfermode;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;

/**
 * Created by wngo1 on 12/1/14.
 */
public class RequestFragment extends Fragment {
    private CondomRequest mCondomRequest;

    private TextView mOrderNumber, mDeliveryDestination, mDateRequested, mDateAccepted, mDateDelivered;
    private EditText mDeliveryEstimate;
    private String mDeliveryEstimateString, mSessionToken, mOrderNumberString;
    private CheckBox mAcceptedCheckBox, mDeliveredCheckBox, mFailedCheckBox;
    private Button mConfirm;

    private static final String TAG = "RequestFragment";
    private static final String API = "http:///tsb.sccs.swarthmore.edu:8080/api/";
    SharedPreferences mSharedPreferences;
    List<NameValuePair> mParams;

    // accepts an order number, creates an argument bundle, creates fragment instance, then
    // attaches the arguments to the fragment
    public static RequestFragment newInstance(String orderNumber) {
        Bundle args = new Bundle();
        args.putSerializable("order_number", orderNumber);

        RequestFragment fragment = new RequestFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO RequestEvent
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_request, parent, false);

        // fill condom request details
        mOrderNumber = (TextView) v.findViewById(R.id.order_number);


        mDeliveryDestination = (TextView) v.findViewById(R.id.delivery_destination);
        mDateRequested = (TextView) v.findViewById(R.id.date_requested);

        mOrderNumber.setText(mCondomRequest.getOrderNumber());
        mDeliveryDestination.setText(mCondomRequest.getDeliveryDestination());
        mDateRequested.setText(mCondomRequest.getDateRequested().toString());

        // fill deliverer options
        mDeliveryEstimate = (EditText) v.findViewById(R.id.delivery_estimate);

        mDateAccepted = (TextView) v.findViewById(R.id.date_accepted);
        mDateDelivered = (TextView) v.findViewById(R.id.date_delivered);

        mAcceptedCheckBox = (CheckBox) v.findViewById(R.id.order_accepted);
        mDeliveredCheckBox = (CheckBox) v.findViewById(R.id.order_delivered);
        mFailedCheckBox = (CheckBox) v. findViewById(R.id.order_failed);

        mDateAccepted.setText(mCondomRequest.getDateAccepted().toString());
        mDateDelivered.setText(mCondomRequest.getDateDelivered().toString());
        mAcceptedCheckBox.setChecked(mCondomRequest.isOrderAccepted());
        mDeliveredCheckBox.setChecked(mCondomRequest.isOrderDelivered());
        mFailedCheckBox.setChecked(mCondomRequest.isOrderFailed());


        mConfirm = (Button) v.findViewById(R.id.confirm_button);
        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // set up POST
                mParams = new ArrayList<NameValuePair>();
                final ServerRequest serverRequest = new ServerRequest();
                // TODO get mSessionToken from sharedPreferences
                mOrderNumberString = mOrderNumber.getText().toString();

                // update condom request fields
                mDeliveryEstimateString = mDeliveryEstimate.getText().toString();

                mAcceptedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mCondomRequest.setOrderAccepted(isChecked);
                        mParams.add(new BasicNameValuePair("session_token", mSessionToken));
                        mParams.add(new BasicNameValuePair("order_number", mOrderNumberString));
                        mParams.add(new BasicNameValuePair("delivery_estimate", mDeliveryEstimateString));

                        JSONObject json = serverRequest.getJSON(API + "delivery/status/accepted", mParams);
                        if(json!=null) {
                            try{
                                String jsonString = json.getString("response");
                            }catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        Log.d(TAG, "accepted");
                    }

                });
                mDeliveredCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mCondomRequest.setOrderDelivered(isChecked);
                        mParams.add(new BasicNameValuePair("session_token", mSessionToken));
                        mParams.add(new BasicNameValuePair("order_number", mOrderNumberString));

                        JSONObject json = serverRequest.getJSON(API + "delivery/status/delivered", mParams);
                        if(json!=null) {
                            try{
                                String jsonString = json.getString("response");
                            }catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        Log.d(TAG, "delivered");
                    }
                });
                mFailedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mCondomRequest.setOrderDelivered(isChecked);
                        Log.d(TAG, "failed");
                    }
                });
            }
        });

        return v;
    }
}