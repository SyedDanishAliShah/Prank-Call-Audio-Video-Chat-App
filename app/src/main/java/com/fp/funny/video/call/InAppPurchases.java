package com.fp.funny.video.call;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.common.collect.ImmutableList;

import java.util.List;

public class InAppPurchases {

    public static BillingClient billingClient;
    public static String ONE_WEEK, ONE_MONTH, ONE_YEAR;
    public static boolean isPurchase = false, isBpClientReady = false;
    @SuppressLint("StaticFieldLeak")
    public static Context contextGlobal;

    public static ProductDetails week1Detail = null , month1detail = null, yearlyDetail = null;

    public static PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {

        @Override
        public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
// updatePurchases(list);
                for (Purchase purchase : list) {
                    handlePurchase(purchase, contextGlobal);
                }
            } else {
                try {
                    if (!billingResult.getDebugMessage().isEmpty()) {
                        Toast.makeText(contextGlobal, billingResult.getDebugMessage(), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public static BillingClientStateListener billingClientStateListener = new BillingClientStateListener() {
        @Override
        public void onBillingSetupFinished(@NonNull BillingResult billingResult) {

            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {

//               Toast.makeText(contextGlobal, "connected", Toast.LENGTH_SHORT).show();
                Log.d("TAG_111", "onBillingSetupFinished: ");

                isBpClientReady = true;

                get_onetime_purchases();
                get_subscription_purchases();
                query_subscriptions();
               // query_products();

            }
// test in App for ads by un-commenting below line
            //     isPurchase = true;
        }

        @Override
        public void onBillingServiceDisconnected() {


            //     Toast.makeText(contextGlobal, "disconnected", Toast.LENGTH_SHORT).show();


        }
    };

    public static void setupBillingClient(Context context) {
        contextGlobal = context;

       // ONE_WEEK = context.getString(R.string.voice_changer_one_week_subscription);
        ONE_MONTH = context.getString(R.string.prank_call_monthly_subscription);
       // ONE_YEAR = context.getString(R.string.voice_changer_one_year_subscription);


        billingClient = BillingClient.newBuilder(context)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();
        if (!billingClient.isReady()) {
            billingClient.startConnection(billingClientStateListener);
        }


        Log.d("TAG_111", "setup_billing_client: ");

    }

    public static void launch_Subscription_billing_flow(Activity activity, ProductDetails productDetails) {
        Log.d("launch", productDetails.getDescription());


        assert productDetails.getSubscriptionOfferDetails() != null;
        ImmutableList<BillingFlowParams.ProductDetailsParams> productDetailsParamsList =
                ImmutableList.of(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .setOfferToken(productDetails.getSubscriptionOfferDetails().get(0).getOfferToken())
                                .build()
                );
        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build();


//        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
//                .setSkuDetails(skuDetails)
//                .build();
        int responseCode = billingClient.launchBillingFlow(activity, billingFlowParams).getResponseCode();
        if (responseCode == BillingClient.BillingResponseCode.OK) {

        } else if (responseCode == BillingClient.BillingResponseCode.BILLING_UNAVAILABLE) {
            Toast.makeText(activity, "Billing Unavailable!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(activity, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
    }

    public static void launch_Purchase_billing_flow(Activity activity, ProductDetails productDetails, boolean isonetimePur) {
        ImmutableList<BillingFlowParams.ProductDetailsParams> productDetailsParamsList = null;

        if (isonetimePur) {


            assert productDetails.getOneTimePurchaseOfferDetails() != null;
            productDetailsParamsList = ImmutableList.of(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .build());


        } else {


            assert productDetails.getSubscriptionOfferDetails() != null;
            productDetailsParamsList = ImmutableList.of(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .setOfferToken(productDetails.getSubscriptionOfferDetails().get(0).getOfferToken())
                            .build()
            );


        }


        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build();


        int responseCode = billingClient.launchBillingFlow(activity, billingFlowParams).getResponseCode();
        if (responseCode == BillingClient.BillingResponseCode.OK) {

        } else if (responseCode == BillingClient.BillingResponseCode.BILLING_UNAVAILABLE) {
            Toast.makeText(activity, "Billing Unavailable!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(activity, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
    }


    public static void query_subscriptions() {
//        QueryProductDetailsParams.Product p=                QueryProductDetailsParams.Product.newBuilder()
//                .setProductId(SPECIAL_OFFER_SCREEN4)
//                .setProductType(BillingClient.ProductType.SUBS)
//                .build();
//        Log.d("product123",p.toString());
//        QueryProductDetailsParams.Product p1=                QueryProductDetailsParams.Product.newBuilder()
//                .setProductId(SPECIAL_OFFER)
//                .setProductType(BillingClient.ProductType.SUBS)
//                .build();
//        Log.d("product123",p1.toString());


        ImmutableList<QueryProductDetailsParams.Product> productList = ImmutableList.of(
                //Product 1
                /*QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(ONE_WEEK)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build(),*/

                //Product 2
                QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(ONE_MONTH)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()

                //Product 3
//                QueryProductDetailsParams.Product.newBuilder()
//                        .setProductId(ONE_YEAR)
//                        .setProductType(BillingClient.ProductType.SUBS)
//                        .build()
                //Special Product

        );


        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build();


        billingClient.queryProductDetailsAsync(
                params,
                (billingResult, prodDetailsList) -> {
                    // Process the result
                    try {
                        if (prodDetailsList == null) {
                            Log.d("productList", "null if");


                        } else {
                            if (prodDetailsList.size() > 0) {
                                for (ProductDetails productDetails : prodDetailsList) {
                                    Log.d("productList", productDetails.getProductId());
                                    Log.d("productList", productDetails.toString());
                                   /* if (productDetails.getProductId().equals(ONE_WEEK)) {
                                        week1Detail = productDetails;
                                    }*/

                                    if (productDetails.getProductId().equals(ONE_MONTH)) {
                                        month1detail = productDetails;
                                    }


                                   /* if (productDetails.getProductId().equals(ONE_YEAR)) {
                                        yearlyDetail = productDetails;
                                    }
*/
                                }
                            } else {

                                month1detail = null;



                            }
                        }
                    } catch (Exception w) {
                    }

                }
        );


    }

//    public static void query_products() {
//        ImmutableList<QueryProductDetailsParams.Product> productList = ImmutableList.of(
//                //Product 1
//                QueryProductDetailsParams.Product.newBuilder()
//                        .setProductId(LIFETIME)
//                        .setProductType(BillingClient.ProductType.INAPP)
//                        .build());
//
//
//        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
//                .setProductList(productList)
//                .build();


//        billingClient.queryProductDetailsAsync(
//                params,
//                (billingResult, prodDetailsList) -> {
//                    // Process the result
//                    try {
//                        if (prodDetailsList == null) {
//                            Log.d("prodDetailsList", "null if");
//
//
//                        } else {
//                            if (prodDetailsList.size() > 0) {
//                                for (ProductDetails productDetails : prodDetailsList) {
//                                    Log.d("productList", productDetails.getProductId());
//                                    if (productDetails.getProductId().equals(LIFETIME)) {
//                                        lifetimeDetail = productDetails;
//                                    }
//                                }
//                            } else {
//                                lifetimeDetail = null;
//                            }
//                        }
//                    } catch (Exception w) {
//                    }
//                }
//        );
//   }


    static void verifySubPurchase(Purchase purchases) {


        AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams
                .newBuilder()
                .setPurchaseToken(purchases.getPurchaseToken())
                .build();

        billingClient.acknowledgePurchase(acknowledgePurchaseParams, billingResult -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                //user prefs to set premium


                Log.d("TAG_1", "verifySubPurchase: " + purchases.getProducts());

                isPurchase = true;




            }
        });

    }


    public static void get_subscription_purchases() {

        // List<Purchase> purchasesInApp, purchasesSub;

        if (!isPurchase) {


            billingClient.queryPurchasesAsync(
                    QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build(),
                    (billingResult, list) -> {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            for (Purchase purchase : list) {
                                //   if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged()) {
                                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                                    verifySubPurchase(purchase);
                                }
                            }
                        }
                    }

            );

        }

    }

    public static void get_onetime_purchases() {

        // List<Purchase> purchasesInApp, purchasesSub;

        if (!isPurchase) {

            billingClient.queryPurchasesAsync(
                    QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(),
                    (billingResult, list) -> {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            for (Purchase purchase : list) {
                                //  if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged())
                                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                                    verifySubPurchase(purchase);
                                }
                            }
                        }
                    }

            );

        }
    }


    public static void onPurchasedItemDialog(Context context) {
        get_subscription_purchases();
    }

    public static void handlePurchase(Purchase purchase, Context context) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {

            Log.d("TAG_1", "handlePurchase: if ");

            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
                    @Override
                    public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
// Handle the success of the consume operation.
                            onPurchasedItemDialog(context);

                        }
                    }
                };
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
            } else {

                Log.d("TAG_1", "handlePurchase: else ");
                onPurchasedItemDialog(context);
            }
        }
    }
}


