/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.core.media.session;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import androidx.core.media.session.MediaControllerCompat;
import androidx.core.media.session.MediaSessionCompat;
import android.view.KeyEvent;

import java.util.List;

/**
 * A media button receiver receives and helps translate hardware media playback buttons,
 * such as those found on wired and wireless headsets, into the appropriate callbacks
 * in your app.
 * <p />
 * You can add this MediaButtonReceiver to your app by adding it directly to your
 * AndroidManifest.xml:
 * <pre>
 * &lt;receiver android:name="android.support.v4.media.session.MediaButtonReceiver" &gt;
 *   &lt;intent-filter&gt;
 *     &lt;action android:name="android.intent.action.MEDIA_BUTTON" /&gt;
 *   &lt;/intent-filter&gt;
 * &lt;/receiver&gt;
 * </pre>
 * This class assumes you have a {@link Service} in your app that controls
 * media playback via a {@link MediaSessionCompat}. That {@link Service} must
 * include an intent filter that also handles {@link Intent#ACTION_MEDIA_BUTTON}:
 * <pre>
 * &lt;service android:name="com.example.android.MediaPlaybackService" &gt;
 *   &lt;intent-filter&gt;
 *     &lt;action android:name="android.intent.action.MEDIA_BUTTON" /&gt;
 *   &lt;/intent-filter&gt;
 * &lt;/service&gt;
 * </pre>
 *
 * All {@link Intent}s sent to this MediaButtonReceiver will then be forwarded
 * to the {@link Service}. Events can then be handled in
 * {@link Service#onStartCommand(Intent, int, int)} by calling
 * {@link MediaButtonReceiver#handleIntent(MediaSessionCompat, Intent)}, passing in
 * your current {@link MediaSessionCompat}:
 * <pre>
 * private MediaSessionCompat mMediaSessionCompat = ...;
 *
 * public int onStartCommand(Intent intent, int flags, int startId) {
 *   MediaButtonReceiver.handleIntent(mMediaSessionCompat, intent);
 *   return super.onStartCommand(intent, flags, startId);
 * }
 * </pre>
 *
 * This ensures that the correct callbacks to {@link MediaSessionCompat.Callback}
 * will be triggered based on the incoming {@link KeyEvent}.
 */
public class MediaButtonReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent queryIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        queryIntent.setPackage(context.getPackageName());
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfos = pm.queryIntentServices(queryIntent, 0);
        if (resolveInfos.size() != 1) {
            throw new IllegalStateException("Expected 1 Service that handles " +
                    Intent.ACTION_MEDIA_BUTTON + ", found " + resolveInfos.size());
        }
        ResolveInfo resolveInfo = resolveInfos.get(0);
        ComponentName componentName = new ComponentName(resolveInfo.serviceInfo.packageName,
                resolveInfo.serviceInfo.name);
        intent.setComponent(componentName);
        context.startService(intent);
    }

    /**
     * Extracts any available {@link KeyEvent} from an {@link Intent#ACTION_MEDIA_BUTTON}
     * intent, passing it onto the {@link MediaSessionCompat} using
     * {@link MediaControllerCompat#dispatchMediaButtonEvent(KeyEvent)}, which in turn
     * will trigger callbacks to the {@link MediaSessionCompat.Callback} registered via
     * {@link MediaSessionCompat#setCallback(MediaSessionCompat.Callback)}.
     * <p />
     * The returned {@link KeyEvent} is non-null if any {@link KeyEvent} is found and can
     * be used if any additional processing is needed beyond what is done in the
     * {@link MediaSessionCompat.Callback}. An example of is to prevent redelivery of a
     * {@link KeyEvent#KEYCODE_MEDIA_PLAY_PAUSE} Intent in the case of the Service being
     * restarted (which, by default, will redeliver the last received Intent).
     * <pre>
     * KeyEvent keyEvent = MediaButtonReceiver.handleIntent(mediaSession, intent);
     * if (keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
     *   Intent emptyIntent = new Intent(intent);
     *   emptyIntent.setAction("");
     *   startService(emptyIntent);
     * }
     * </pre>
     * @param mediaSessionCompat A {@link MediaSessionCompat} that has a
     *            {@link MediaSessionCompat.Callback} set.
     * @param intent The intent to parse.
     * @return The extracted {@link KeyEvent} if found, or null.
     */
    public static KeyEvent handleIntent(MediaSessionCompat mediaSessionCompat, Intent intent) {
        if (mediaSessionCompat == null || intent == null
                || !Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())
                || !intent.hasExtra(Intent.EXTRA_KEY_EVENT)) {
            return null;
        }
        KeyEvent ke = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        MediaControllerCompat mediaController = mediaSessionCompat.getController();
        mediaController.dispatchMediaButtonEvent(ke);
        return ke;
    }
}

