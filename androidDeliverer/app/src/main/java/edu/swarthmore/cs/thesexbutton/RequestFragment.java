package edu.swarthmore.cs.thesexbutton;

import android.app.Activity;
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
    private String mDeliveryEstimateString, mOrderNumberString , mSessionToken;
    private CheckBox mAcceptedCheckBox, mDeliveredCheckBox, mFailedCheckBox;
    private Button mConfirm;

    boolean deliveryIsChecked, acceptedIsChecked;
    private static final String TAG = "RequestFragment";
    private static final String API = "http://tsb.sccs.swarthmore.edu:8080/api/";

    List<NameValuePair> mParams;

    // accepts an order number, creates an argument bundle, creates fragment instance, then
    // attaches the arguments to the fragment
    public static RequestFragment newInstance(String orderNumber, String sessionToken) {
        Bundle bundle = new Bundle();
        bundle.putString("order_number", orderNumber);
        bundle.putString("session_token", sessionToken);

        RequestFragment fragment = new RequestFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSessionToken = getArguments().getString("session_token");
        String orderNumber = getArguments().getString("order_number");

        CondomRequestStore store = CondomRequestStore.get(mSessionToken);
        mCondomRequest = store.getCondomRequest(orderNumber);

        Log.d(TAG, mCondomRequest.getOrderNumber());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_request, parent, false);

        // fill condom request details
        mOrderNumber = (TextView) v.findViewById(R.id.order_number);
        mDeliveryDestination = (TextView) v.findViewById(R.id.delivery_destination);
        mDateRequested = (TextView) v.findViewById(R.id.date_requested);

        mOrderNumber.setText(mCondomRequest.getOrderNumber());
        mOrderNumberString = mOrderNumber.getText().toString();

        mDeliveryDestination.setText(mCondomRequest.getDeliveryDestination());
        mDateRequested.setText(mCondomRequest.getDateRequested().toString());

        // fill deliverer options
        mDeliveryEstimate = (EditText) v.findViewById(R.id.delivery_estimate);

        mDateAccepted = (TextView) v.findViewById(R.id.date_accepted);
        mDateDelivered = (TextView) v.findViewById(R.id.date_delivered);

        mAcceptedCheckBox = (CheckBox) v.findViewById(R.id.order_accepted);
        mDeliveredCheckBox = (CheckBox) v.findViewById(R.id.order_delivered);
        mFailedCheckBox = (CheckBox) v. findViewById(R.id.order_failed);

        String dateAccepted = mCondomRequest.getDateAccepted();
        String dateDelivered = mCondomRequest.getDateDelivered();

        if (dateAccepted!=null) {
            mDateAccepted.setText(dateAccepted);
        }
        if (dateDelivered!=null) {
            mDateDelivered.setText(dateDelivered);
        }

        // checkbox
        mAcceptedCheckBox.setChecked(mCondomRequest.isOrderAccepted());
        mDeliveredCheckBox.setChecked(mCondomRequest.isOrderDelivered());
        mFailedCheckBox.setChecked(mCondomRequest.isOrderFailed());

        mAcceptedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                acceptedIsChecked = isChecked;
                Log.d(TAG, mOrderNumberString + " checked accepted");
            }
        });

        mDeliveredCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                deliveryIsChecked = isChecked;
                Log.d(TAG, mOrderNumberString + " checked delivered");
            }
        });

        mFailedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCondomRequest.setOrderDelivered(isChecked);
                Log.d(TAG, mOrderNumberString + " checked delivered");
            }
        });


        mConfirm = (Button) v.findViewById(R.id.confirm_button);
        mConfirm.setOnClickListener(new View.OnClickListener() {
            Activity activity = getActivity();

            @Override
            public void onClick(View view) {
                // set up POST
                Log.i(TAG, "Confirm button clicked");
                mParams = new ArrayList<NameValuePair>();
                final ServerRequest serverRequest = new ServerRequest();
                mParams.add(new BasicNameValuePair("session_token", mSessionToken));
                mParams.add(new BasicNameValuePair("order_number", mOrderNumberString));

                // update condom request fields
                if(acceptedIsChecked) {
                    mCondomRequest.setOrderAccepted(acceptedIsChecked);
                    mDeliveryEstimateString = mDeliveryEstimate.getText().toString();
                    mParams.add(new BasicNameValuePair("delivery_estimate ", mDeliveryEstimateString));

                    JSONObject json = serverRequest.getJSON(API + "delivery/request/accept", mParams);
                    if (json != null) {
                        try {
                            String jsonString = json.getString("response");
                            Toast.makeText(activity,jsonString,Toast.LENGTH_LONG).show();
                            Log.d(TAG, mOrderNumberString + " POST accepted");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                if(deliveryIsChecked) {
                    mCondomRequest.setOrderDelivered(deliveryIsChecked);
                    JSONObject json = serverRequest.getJSON(API + "delivery/request/deliver", mParams);
                    if(json!=null) {
                        try{
                            String jsonString = json.getString("response");
                            Toast.makeText(activity,jsonString,Toast.LENGTH_LONG).show();
                            Log.d(TAG, mOrderNumberString + " POST delivered");
                        }catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        return v;
    }
}
