package com.abdulaziz.androidwear;

import android.content.Intent;
import android.widget.Toast;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class ListenerService extends WearableListenerService {

    private static final String MESSAGE1 = "Hello Watch! You're in Control";
    private static final String MESSAGE2 = "I'm running late, but I'll be there soon.";
    private static final String MESSAGE3 = "Sorry, I missed your call.";
    private static final String MESSAGE4 = "Please call me when you get this message.";
    private static final String MESSAGE5 = "I'm in the airport now, I'll talk to you later.";


    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        if (messageEvent.getPath().equals(MESSAGE1)) {
            showToast(messageEvent.getPath());
            Intent startIntent = new Intent(this, Msg1Activity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startIntent);
        }

        if (messageEvent.getPath().equals(MESSAGE2)) {
            showToast(messageEvent.getPath());
            Intent startIntent = new Intent(this, Msg2Activity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startIntent);
        }

        if (messageEvent.getPath().equals(MESSAGE3)) {
            showToast(messageEvent.getPath());
            Intent startIntent = new Intent(this, Msg3Activity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startIntent);
        }

        if (messageEvent.getPath().equals(MESSAGE4)) {
            showToast(messageEvent.getPath());
            Intent startIntent = new Intent(this, Msg4Activity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startIntent);
        }

        if (messageEvent.getPath().equals(MESSAGE5)) {
            showToast(messageEvent.getPath());
            Intent startIntent = new Intent(this, Msg5Activity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startIntent);
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}