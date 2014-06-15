/*
 * Copyright (c) 2012. HappyDroids LLC, All rights reserved.
 */

package com.happydroids.droidtowers;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.DisplayMetrics;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.happydroids.HappyDroidConsts;
import com.happydroids.droidtowers.gamestate.server.TowerGameService;
import com.happydroids.droidtowers.platform.Display;
import com.happydroids.platform.*;
import com.happydroids.platform.purchase.GooglePlayPurchaseManager;
import net.robotmedia.billing.BillingController;
import net.robotmedia.billing.BillingRequest;
import net.robotmedia.billing.helper.AbstractBillingObserver;
import net.robotmedia.billing.model.Transaction;

import java.util.List;

public class DroidTowersGooglePlay extends AndroidApplication implements BillingController.IConfiguration {
  private static final String TAG = DroidTowersGooglePlay.class.getSimpleName();

  private AbstractBillingObserver mBillingObserver;

  public void onCreate(android.os.Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    DisplayMetrics metrics = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(metrics);

    Display.setXHDPI(metrics.densityDpi == DisplayMetrics.DENSITY_XHIGH);
    Display.setScaledDensity(metrics.densityDpi == DisplayMetrics.DENSITY_XHIGH ? 1.5f : 1f);

    TowerGameService.setDeviceType("android");
    TowerGameService.setDeviceOSMarketName("google-play");
    TowerGameService.setDeviceOSVersion("sdk" + getVersion());

    AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
    config.useGL20 = true;
    config.useWakelock = true;

    initialize(new DroidTowersGame(new Runnable() {
      @Override
      public void run() {
        Platform.setDialogOpener(new AndroidDialogOpener(DroidTowersGooglePlay.this));
        Platform.setConnectionMonitor(new PlatformConnectionMonitor());
        Platform.setUncaughtExceptionHandler(new AndroidUncaughtExceptionHandler());
        Platform.setBrowserUtil(new AndroidBrowserUtil(DroidTowersGooglePlay.this));
        Platform.setPurchaseManager(new GooglePlayPurchaseManager(DroidTowersGooglePlay.this));

        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            setupAndroidBilling();
          }
        });
      }
    }), config);

    Gdx.input.setCatchBackKey(true);
    Gdx.input.setCatchMenuKey(true);
  }

  private void setupAndroidBilling() {
    mBillingObserver = new AbstractBillingObserver(this) {
      @Override
      public void onBillingChecked(boolean supported) {
        DroidTowersGooglePlay.this.onBillingChecked(supported);
      }

      @Override
      public void onPurchaseStateChanged(String itemId, Transaction.PurchaseState state) {
        DroidTowersGooglePlay.this.onPurchaseStateChanged(itemId, state);
      }

      @Override
      public void onRequestPurchaseResponse(String itemId, BillingRequest.ResponseCode response) {
        DroidTowersGooglePlay.this.onRequestPurchaseResponse(itemId, response);
      }

	@Override
	public void onSubscriptionChecked(boolean supported) {
		// TODO Auto-generated method stub
		
	}
    };
    BillingController.registerObserver(mBillingObserver);
    BillingController.setConfiguration(this); // This activity will provide
    BillingController.setDebug(HappyDroidConsts.DEBUG);
    // the public key and salt
    this.checkBillingSupported();
    if (!mBillingObserver.isTransactionsRestored()) {
      BillingController.restoreTransactions(this);
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    BillingController.unregisterObserver(mBillingObserver); // Avoid
    // receiving
    // notifications after
    // destroy
    BillingController.setConfiguration(null);
  }

  private void onRequestPurchaseResponse(String itemId, BillingRequest.ResponseCode response) {

  }

  private void onPurchaseStateChanged(final String itemId, Transaction.PurchaseState state) {
    PlatformPurchaseManger purchaseManager = Platform.getPurchaseManager();

    Gdx.app.error(TAG, "Purchase of: " + itemId + " state: " + state.name());

    switch (state) {
      case PURCHASED:
        List<Transaction> transactions = BillingController.getTransactions(this, itemId);
        for (Transaction transaction : transactions) {
          purchaseManager.purchaseItem(transaction.productId, transaction.orderId);
        }
        break;
      default:
        purchaseManager.revokeItem(itemId);
        break;
    }
  }

  private void onBillingChecked(boolean supported) {
    if (supported) {
      restoreTransactions();
      Platform.getPurchaseManager().enablePurchases();
    } else {
      new AlertDialog.Builder(this)
              .setTitle("Purchases via Google Play")
              .setMessage("Sorry but this device is unable to make purchases via Google Play.")
              .setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                  dialogInterface.dismiss();
                }
              })
              .show();
    }
  }

  public BillingController.BillingStatus checkBillingSupported() {
    return BillingController.checkBillingSupported(this);
  }

  public void requestPurchase(String itemId) {
    BillingController.requestPurchase(this, itemId);
  }

  /**
   * Requests to restore all transactions.
   */
  public void restoreTransactions() {
    if (!mBillingObserver.isTransactionsRestored()) {
      BillingController.restoreTransactions(this);
    }
  }

  @Override
  public byte[] getObfuscationSalt() {
    return HappyDroidConsts.OBFUSCATION_SALT;
  }

  @Override
  public String getPublicKey() {
    return HappyDroidConsts.OBFUSCATION_KEY;
  }
}
