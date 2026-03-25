package com.khumomashapa.mywallpapers.fragments

import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.AcknowledgePurchaseResponseListener
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.khumomashapa.mywallpapers.R
import com.khumomashapa.mywallpapers.billing.Security
import java.io.IOException
import kotlin.collections.ArrayList


class AbstractPaywallFragment : BottomSheetDialogFragment() {
    private var billingClient: BillingClient? = null
    private lateinit var subButton: MaterialButton
    private lateinit var subPrice: TextView
    private val sharedPreferences by lazy {
        requireContext().getSharedPreferences("abstract_subscription_prefs", Context.MODE_PRIVATE)
    }

    var isSuccess = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? { // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_abstract_paywall, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val titleTextView = view.findViewById<TextView>(R.id.subscription_title)
        // Add an underline to the title for better visual emphasis.
        titleTextView.paintFlags = titleTextView.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        subButton = view.findViewById(R.id.abstract_subscribe_button)
        subPrice = view.findViewById(R.id.subscription_price_textview)

        val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
            if (!isAdded) return@PurchasesUpdatedListener
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (purchase in purchases) {
                    handlePurchase(purchase)
                }
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                dismiss()
                Toast.makeText(requireContext(), "Already Subscribed", Toast.LENGTH_SHORT).show()
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                Toast.makeText(requireContext(), "Transaction canceled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Error: " + billingResult.debugMessage, Toast.LENGTH_SHORT).show()
            }
        }

        billingClient = BillingClient.newBuilder(requireContext())
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
            .enableAutoServiceReconnection()
            .build()

        // Start a connection to the billing client to query the price
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (!isAdded) return
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryPriceAndDisplay()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Handle disconnection
            }
        })

        subButton.setOnClickListener {
            if (!isAdded) return@setOnClickListener

            if (sharedPreferences.getBoolean("is_subscribed", false)) {
                dismiss()
            } else {
                launchBillingFlow()
            }
        }
    }

    private fun queryPriceAndDisplay() {
        // First, check if the user is already subscribed. If so, update the UI and stop.
        if (sharedPreferences.getBoolean("is_subscribed", false)) {
            activity?.runOnUiThread {
                subButton.text = "Subscribed"
                subPrice.text = "Subscription Active"
            }
            return
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId("abstract_wallpapers")
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            ).build()

        billingClient?.queryProductDetailsAsync(params) { br, productDetailsList ->
            if (!isAdded) return@queryProductDetailsAsync
            val productDetails = productDetailsList.productDetailsList.firstOrNull()
            if (br.responseCode == BillingClient.BillingResponseCode.OK && productDetails != null) {
                val offerDetails = productDetails.subscriptionOfferDetails?.firstOrNull()
                // Find the phase where the user is charged (price > 0).
                val billingPhase = offerDetails?.pricingPhases?.pricingPhaseList?.find { it.priceAmountMicros > 0L }
                // Check if any phase has a price of 0, which indicates a free trial.
                val hasFreeTrial = offerDetails?.pricingPhases?.pricingPhaseList?.any { it.priceAmountMicros == 0L } == true

                // This robust logic constructs the correct price string whether there is a free trial or not.
                val priceText = if (billingPhase != null) {
                    if (hasFreeTrial) {
                        "${billingPhase.formattedPrice} per month after your free trial"
                    } else {
                        "${billingPhase.formattedPrice} per month"
                    }
                } else {
                    "Price not available"
                }
                activity?.runOnUiThread {
                    subPrice.text = priceText
                }

            }
        }
    }

    private fun launchBillingFlow() {
        // Launch the billing flow
        billingClient!!.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (!isAdded) return
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val params = QueryProductDetailsParams.newBuilder()
                        .setProductList(
                            listOf(
                                QueryProductDetailsParams.Product.newBuilder()
                                    .setProductId("abstract_wallpapers")
                                    .setProductType(BillingClient.ProductType.SUBS)
                                    .build()
                            )
                        ).build()

                    billingClient!!.queryProductDetailsAsync(params) { br, productDetailsList ->
                        if (!isAdded) return@queryProductDetailsAsync
                        val productDetails = productDetailsList.productDetailsList.firstOrNull()
                        if (br.responseCode == BillingClient.BillingResponseCode.OK && productDetails != null) {
                            val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
                            if (offerToken == null) {
                                Toast.makeText(requireContext(), "Offer not found.", Toast.LENGTH_SHORT).show()
                                return@queryProductDetailsAsync
                            }
                            val productDetailsParamsList = listOf(
                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                    .setProductDetails(productDetails)
                                    .setOfferToken(offerToken)
                                    .build()
                            )
                            val billingFlowParams = BillingFlowParams.newBuilder()
                                .setProductDetailsParamsList(productDetailsParamsList)
                                .build()
                            billingClient!!.launchBillingFlow(requireActivity(), billingFlowParams)
                        }
                    }
                }
            }
            override fun onBillingServiceDisconnected() {}
        })
    }

    private fun handlePurchase(purchase: Purchase) {
        if (!isAdded) return
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!verifyValidSignature(purchase.originalJson, purchase.signature)) {
                Toast.makeText(requireContext(), "Error: invalid Purchase", Toast.LENGTH_SHORT).show()
                return
            }

            with(sharedPreferences.edit()) {
                putBoolean("is_subscribed", true)
                apply()
            }

            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingClient!!.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener)

            } else {
                // If the purchase is already acknowledged, send a signal to the previous fragment
                // to update its UI (e.g., show the full-access button).
                parentFragmentManager.setFragmentResult("purchase_completed", bundleOf())
                Toast.makeText(requireContext(), "Already Subscribed", Toast.LENGTH_SHORT).show()
                dismiss()
            }

        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            Toast.makeText(requireContext(), "Subscription Pending", Toast.LENGTH_SHORT).show()
        } else if (purchase.purchaseState == Purchase.PurchaseState.UNSPECIFIED_STATE) {
            Toast.makeText(requireContext(), "UNSPECIFIED_STATE", Toast.LENGTH_SHORT).show()
        }
    }

    var acknowledgePurchaseResponseListener = AcknowledgePurchaseResponseListener { billingResult ->
        if (!isAdded) return@AcknowledgePurchaseResponseListener
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            // When the purchase is successfully acknowledged, send a signal to the previous fragment
            // to update its UI (e.g., swap the paywall bu fritton for the full-access button).
            parentFragmentManager.setFragmentResult("purchase_completed", bundleOf())
            Toast.makeText(requireActivity(), "Subscribed", Toast.LENGTH_LONG).show()
            isSuccess = true
            dismiss()
        }
    }

    private fun verifyValidSignature(signedData: String, signature: String): Boolean {
        return try {
            val base64Key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhRMePsW1k9sOprr8m7hsuDhL+pM" +
                    "HkSq1IFiaZTFpmWhN//qqqZJST4/DIYxBGuhtpxWwH7NG+tBKkMY203XmtLJFJJ/RwFzClaSVvW3dyr" +
                    "QEp2cOimjuUbHlDKwnEO6MUxwmDQCNv/JSYOER68PFuOHYSNBp560wyUyV6ciA8modZu0u0nDwGR/17" +
                    "LanttcV/El0MbfAAu4LFjll5J7IOv0DNw/k65/NuHSKxb1z52dGjGFblngD6skHKohwIx6hgMFtIklY" +
                    "+YXucLaMhwBr82vQSYhkJvpxJhpNfHscWRL0Tnbvnl1dPHR79kvv5S7V7N8aj3+idcy1nbuvFLPuuQIDAQAB"
            Security.verifyPurchase(base64Key, signedData, signature)
        } catch (e: IOException) {
            false
        }
    }
}
