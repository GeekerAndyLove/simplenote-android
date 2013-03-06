package com.automattic.simplenote;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.automattic.simplenote.models.Note;
import com.simperium.client.*;

/**
 * An activity representing a list of Notes. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link NoteEditorActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link NoteListFragment} and the item details (if present) is a
 * {@link NoteEditorFragment}.
 * <p>
 * This activity also implements the required {@link NoteListFragment.Callbacks}
 * interface to listen for item selections.
 */
public class NoteListActivity extends SherlockFragmentActivity implements
		NoteListFragment.Callbacks, OnNavigationListener, User.AuthenticationListener {

	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	private boolean mTwoPane;
	private Bucket<Note> mNotesBucket;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_note_list);
		
        Simplenote application = (Simplenote)getApplication();
		mNotesBucket = application.getNotesBucket();

		ActionBar ab = getSupportActionBar();
		ab.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		ab.setDisplayShowTitleEnabled(false);

		String[] items = { "Notes", "Trash", "RADWHIMPS" };
		SpinnerAdapter mSpinnerAdapter = new ArrayAdapter<String>(
				getSupportActionBar().getThemedContext(),
				R.layout.sherlock_spinner_dropdown_item, items);
		ab.setListNavigationCallbacks(mSpinnerAdapter, this);

		if (findViewById(R.id.note_detail_container) != null) {
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			mTwoPane = true;

			// In two-pane mode, list items should be given the
			// 'activated' state when touched.
			((NoteListFragment) getSupportFragmentManager().findFragmentById(
					R.id.note_list)).setActivateOnItemClick(true);
		}

		Simplenote currentApp = (Simplenote) getApplication();
		if( currentApp.getSimperium().getUser() == null || currentApp.getSimperium().getUser().needsAuthentication() ){
			startLoginActivity();
		}
		currentApp.getSimperium().setAuthenticationListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.notes_list, menu);
		return true;
	}
	
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		if (item.getItemId() == R.id.menu_preferences) {
			Intent i = new Intent(this, PreferencesActivity.class);
			this.startActivity(i);
		} else if (item.getItemId() == R.id.menu_create_note) {
			
			Note note = mNotesBucket.newObject();
			
			((NoteListFragment) getSupportFragmentManager().findFragmentById(
					R.id.note_list)).refreshList();
			
			// TODO: select the new note here
		}

		
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Callback method from {@link NoteListFragment.Callbacks} indicating that
	 * the item with the given ID was selected.
	 */
	@Override
	public void onNoteSelected(Note note) {
		if (mTwoPane) {
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putString(NoteEditorFragment.ARG_ITEM_ID, note.getSimperiumKey());
			NoteEditorFragment fragment = new NoteEditorFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.note_detail_container, fragment).commit();

		} else {
			// In single-pane mode, simply start the detail activity
			// for the selected item ID.
			Intent detailIntent = new Intent(this, NoteEditorActivity.class);
			detailIntent.putExtra(NoteEditorFragment.ARG_ITEM_ID, note.getSimperiumKey());
			startActivity(detailIntent);
		}
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	public void onAuthenticationStatusChange(User.AuthenticationStatus status){
		if ( status == User.AuthenticationStatus.NOT_AUTHENTICATED ) {
			startLoginActivity();
		}
	}
	
	public void startLoginActivity(){
		Intent loginIntent = new Intent(this, LoginActivity.class);
		startActivityForResult(loginIntent, 0);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (data != null) {
			Bundle bundle = data.getExtras();
			int status = bundle.getInt("returnStatus");
			if (status == -1) {
				finish();
			} 
		}
	}
}
