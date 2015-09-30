package it.jaschke.alexandria;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import it.jaschke.alexandria.api.Callback;

public class MainActivity extends AppCompatActivity
        implements OnNavigationItemSelectedListener, Callback {

    private static final String TAG                     = MainActivity.class.getSimpleName();
    private static final String LISTBOOKSFRAGMENT_TAG   = "LBFT";
    private static final String ADDBOOKFRAGMENT_TAG     = "ABFT";
    private static final String ABOUTFRAGMENT_TAG       = "AFT";

    public static final String MESSAGE_EVENT            = "MESSAGE_EVENT";
    public static final String MESSAGE_KEY              = "MESSAGE_EXTRA";

    private Toolbar mToolbar;
    private NavigationView mNavigationDrawer;
    private DrawerLayout mDrawerLayout;
    private BroadcastReceiver mMessageReciever;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initializing Toolbar and setting it as the ActionBar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        // Initialize NavigationView
        mNavigationDrawer = (NavigationView) findViewById(R.id.navigation_drawer);

        // Setting NavigationView Item Selected Listener to handle the item click
        // of the navigation menu
        mNavigationDrawer.setNavigationItemSelectedListener(this);

        // Initialize Drawer Layout and ActionBarToggle
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                mToolbar,
                R.string.open_drawer,
                R.string.close_drawer) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we don't want anyting
                // to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer opens as we don't want anything
                // to happen so we leave this blank
                super.onDrawerOpened(drawerView);
            }
        };

        // Setting the ActionBarToggle to drawer layout
        mDrawerLayout.setDrawerListener(actionBarDrawerToggle);

        // Calling sync state is necessary or else your hamburger icon won't show up
        actionBarDrawerToggle.syncState();

        mMessageReciever    = new MessageReciever();
        IntentFilter filter = new IntentFilter(MESSAGE_EVENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReciever, filter);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment               = null;
        String tag                      = null;

        switch (menuItem.getItemId()) {
            case R.id.list_books:
                tag         = LISTBOOKSFRAGMENT_TAG;
                fragment    = fragmentManager.findFragmentByTag(tag);
                if (null == fragment) {
                    fragment = new ListOfBooks();
                }
                break;
            case R.id.add_book:
                tag         = ADDBOOKFRAGMENT_TAG;
                fragment    = fragmentManager.findFragmentByTag(tag);
                if (null == fragment) {
                    fragment = new AddBook();
                }
                break;
            case R.id.about:
                tag         = ABOUTFRAGMENT_TAG;
                fragment    = fragmentManager.findFragmentByTag(tag);
                if (null == fragment) {
                    fragment = new About();
                }
                break;
        }

        // Close drawer on item click
        mDrawerLayout.closeDrawers();

        if (null != fragment) {
            // Checking if the item is in checked state or not, if not make it in checked state
            menuItem.setChecked(!menuItem.isChecked());

            fragmentManager
                    .beginTransaction()
                    .replace(R.id.container, fragment, tag)
                    .addToBackStack(null)
                    .commit();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReciever);
        super.onDestroy();
    }

    @Override
    public void onItemSelected(String ean) {
        Bundle args = new Bundle();
        args.putString(BookDetail.EAN_KEY, ean);

        BookDetail fragment = new BookDetail();
        fragment.setArguments(args);

        int id = R.id.container;
        if(findViewById(R.id.right_container) != null){
            id = R.id.right_container;
        }
        getSupportFragmentManager().beginTransaction()
                .replace(id, fragment)
                .addToBackStack("Book Detail")
                .commit();
    }

    private class MessageReciever extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getStringExtra(MESSAGE_KEY)!=null){
                Toast.makeText(MainActivity.this, intent.getStringExtra(MESSAGE_KEY), Toast.LENGTH_LONG).show();
            }
        }
    }
}