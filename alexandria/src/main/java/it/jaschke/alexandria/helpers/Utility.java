package it.jaschke.alexandria.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import it.jaschke.alexandria.R;

/**
 * Created by ryan.gilreath on 9/28/2015.
 */
public class Utility {

    /**
     * Returns true if the network is available or about to become available.
     *
     * @param c Context used to get the ConnectivityManager
     * @return
     */
    public static boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm =
                (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    /**
     * Returns the default starting fragment class name
     * @param context
     * @return
     */
    public static String getPreferredStartFragment(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String key = context.getString(R.string.pref_start_fragment_key);
        String def = context.getString(R.string.pref_start_fragment_default);
        return prefs.getString(key, def);
    }
}
