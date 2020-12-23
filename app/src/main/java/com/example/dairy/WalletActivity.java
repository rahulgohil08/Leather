package com.example.dairy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.razorpay.Checkout;
import com.razorpay.PaymentData;
import com.razorpay.PaymentResultWithDataListener;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WalletActivity extends AppCompatActivity implements PaymentResultWithDataListener {

    private Button button;
    private EditText editText;
    private TextView wallet;
    String UserId;
    SharedPreferences preferences;
    private Context context = this;

    private static final String TAG = "WalletActivity";

    private int money = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);


        button = findViewById(R.id.btn_add);
        editText = findViewById(R.id.edt_add_money);
        wallet = findViewById(R.id.tv_wallet);

        preferences = context.getSharedPreferences("login", Context.MODE_PRIVATE);
        UserId = preferences.getString("user_id", null);
        init();

        Checkout.preload(context);

    }

    private void init() {

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Wallet");

        getMoney();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String amount = editText.getText().toString().trim();
                money = Integer.parseInt(amount);

                if (money <= 0) {
                    Toast.makeText(WalletActivity.this, "Invalid Amount", Toast.LENGTH_SHORT).show();
                    return;
                }


                startPayment();

            }
        });
    }


    /*------------------------------ Start Payment --------------------------------*/

    public void startPayment() {

        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_test_OYPofJwjN32SPE");

        try {
            JSONObject options = new JSONObject();
            options.put("name", "Test");
            options.put("description", "Subscription");
            options.put("currency", "INR");
            options.put("amount", String.valueOf(money * 100));

            JSONObject preFill = new JSONObject();

            options.put("prefill", preFill);


            checkout.open(this, options);
        } catch (Exception e) {
            Toast.makeText(this, "Error in payment: " + e.getMessage(), Toast.LENGTH_SHORT)
                    .show();
            e.printStackTrace();
        }

    }

    @Override
    public void onPaymentSuccess(String s, PaymentData paymentData) {
        try {
            Log.d(TAG, "onPaymentSuccess: O _ Id " + paymentData.getData());
            Log.d(TAG, "onPaymentSuccess: P _ Id " + paymentData.getPaymentId());
            Log.d(TAG, "onPaymentSuccess: Signature" + paymentData.getSignature());

            addMoney(String.valueOf(money));


        } catch (Exception e) {
            Log.d(TAG, "Exception in onPaymentSuccess", e);
        }

    }

    @Override
    public void onPaymentError(int i, String s, PaymentData paymentData) {
        try {
            Toast.makeText(this, s, Toast.LENGTH_SHORT).show();

            Log.d(TAG, "onPaymentFailed: O _ Id" + paymentData.getOrderId());
            Log.d(TAG, "onPaymentFailed: P _ Id" + paymentData.getPaymentId());
            Log.d(TAG, "onPaymentFailed: Signature" + paymentData.getSignature());

        } catch (Exception e) {
            Log.e(TAG, "Exception in onPaymentError", e);
        }

    }



    /*------------------------------ Add Money --------------------------------*/


    private void addMoney(String amount) {

        String url = Api.URL_ADD_MONEY;

        Log.d(TAG, "Amount - " + amount + "---ID  " + UserId + "----Url  " + url);

        Call<Final_Model1> call = Api.getPostService().AddMoney(url, UserId, amount);
        call.enqueue(new Callback<Final_Model1>() {
            @Override
            public void onResponse(Call<Final_Model1> call, Response<Final_Model1> response) {

                if (response.isSuccessful()) {

                    if (!response.body().getError()) {
                        Final_Model1 model1 = response.body();

                        Toast.makeText(context, model1.getMessage(), Toast.LENGTH_SHORT).show();
                        editText.setText(null);
                        getMoney();
                    }
                }

            }

            @Override
            public void onFailure(Call<Final_Model1> call, Throwable t) {

                Toast.makeText(context, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getMoney() {

        String url = Api.URL_GET_MONEY;

        Call<Final_Model1> call = Api.getPostService().getMoney(url, UserId);
        call.enqueue(new Callback<Final_Model1>() {
            @Override
            public void onResponse(Call<Final_Model1> call, Response<Final_Model1> response) {

                if (response.isSuccessful()) {

                    if (!response.body().getError()) {
                        Final_Model1 model1 = response.body();

                        wallet.setText(String.valueOf(model1.getRecords().get(0).getWallet()));

                    }
                }

            }

            @Override
            public void onFailure(Call<Final_Model1> call, Throwable t) {

                Toast.makeText(context, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }


    private void openActivity(Class aclass) {

        startActivity(new Intent(context, aclass));
    }


}