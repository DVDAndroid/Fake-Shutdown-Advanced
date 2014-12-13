package com.dvd.android.advanced_fake_shutdown;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceActivity;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends PreferenceActivity {

	static final int RESULT_ENABLE = 1;
	private static int myProgress;
	private final Handler myHandler = new Handler();
	Dialog dialog;
	DevicePolicyManager deviceManger;
	ActivityManager activityManager;
	ComponentName compName;
	private ProgressDialog progressDialog;
	private int progressStatus = 0;

	@SuppressLint("InlinedApi")
	private static boolean isAirplaneModeOn(Context context) {
		return Settings.System.getInt(context.getContentResolver(),
				Global.AIRPLANE_MODE_ON, 0) != 0;

	}

	@SuppressWarnings({ "static-access", "deprecation" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final SharedPreferences prefs = getPreferenceManager()
				.getDefaultSharedPreferences(this);

		if (prefs.getBoolean("welcome", true)) {
			prefs.edit().putInt("ms", 600).commit();
			prefs.edit().putBoolean("welcome", false).commit();
		}

		dialog = new Dialog(MainActivity.this, R.style.Dialog);

		dialog.setContentView(R.layout.activity_main);

		dialog.setCancelable(true);
		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface arg0) {
				finish();

			}

		});

		ImageView img = (ImageView) dialog.findViewById(R.id.pow_icon);

		if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP)
			setColorFilter(img, 0, 0, 0);

		PendingIntent RESTART_INTENT = PendingIntent.getActivity(this
				.getBaseContext(), 0, new Intent(getIntent()), getIntent()
				.getFlags());

		deviceManger = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		compName = new ComponentName(this, MyAdmin.class);

		boolean active = deviceManger.isAdminActive(compName);
		if (active) {
			dialog.show();
		}

		else {
			Intent intent = new Intent(
					DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
			intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
			intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
					(this.getString(R.string.perm)));
			startActivityForResult(intent, RESULT_ENABLE);
			AlarmManager mgr = (AlarmManager) this
					.getSystemService(Context.ALARM_SERVICE);
			mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000,
					RESTART_INTENT);
			System.exit(2);
			finish();
		}

		LinearLayout ll = (LinearLayout) dialog.findViewById(R.id.linearlayout);

		ll.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				dialog.dismiss();
				if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
					start();
				} else {
					AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
							MainActivity.this);

					alertDialogBuilder.setTitle(getApplicationContext()
							.getString(R.string.power_off));
					alertDialogBuilder.setMessage(getApplicationContext()
							.getString(R.string.warn));

					alertDialogBuilder.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									start();
								}
							});

					alertDialogBuilder.setNegativeButton(
							android.R.string.cancel,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									finish();
								}
							});

					AlertDialog alertDialog = alertDialogBuilder.create();
					alertDialog.setCancelable(true);
					alertDialog
							.setOnCancelListener(new DialogInterface.OnCancelListener() {

								@Override
								public void onCancel(DialogInterface arg0) {
									finish();

								}

							});

					alertDialog.show();
				}
			}
		});

		ll.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {

				AlertDialog.Builder alert = new AlertDialog.Builder(
						MainActivity.this);

				alert.setTitle(getString(R.string.title_ms));

				final EditText input = new EditText(MainActivity.this);
				input.setHint(getString(R.string.curr_val) + " "
						+ (prefs.getInt("ms", 0)));

				alert.setView(input);

				alert.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								String value = input.getText().toString();
								if (!value.equals(""))
									prefs.edit()
											.putInt("ms",
													Integer.valueOf(value))
											.commit();
							}
						});

				alert.setNegativeButton(android.R.string.cancel, null);

				alert.show();
				return false;
			}
		});

		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
			LinearLayout silent = (LinearLayout) dialog.findViewById(R.id.mute);
			silent.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					AudioManager am = (AudioManager) MainActivity.this
							.getSystemService(Context.AUDIO_SERVICE);
					am.setRingerMode(AudioManager.RINGER_MODE_SILENT);

					finish();
				}
			});

			LinearLayout normal = (LinearLayout) dialog
					.findViewById(R.id.normal);
			normal.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					AudioManager am = (AudioManager) MainActivity.this
							.getSystemService(Context.AUDIO_SERVICE);
					am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

					finish();
				}
			});

			LinearLayout vibrate = (LinearLayout) dialog
					.findViewById(R.id.vibrate);
			vibrate.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					AudioManager am = (AudioManager) MainActivity.this
							.getSystemService(Context.AUDIO_SERVICE);
					am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);

					finish();

				}
			});

			ImageView fly_icon = (ImageView) dialog.findViewById(R.id.fly_icon);
			TextView status = (TextView) dialog.findViewById(R.id.status);

			if (isAirplaneModeOn(this)) {
				fly_icon.setImageResource(R.drawable.ic_lock_airplane_mode);
				status.setText(getString(R.string.airplane_mode_on));
			} else {
				fly_icon.setImageResource(R.drawable.ic_lock_airplane_mode_off);
				status.setText(getString(R.string.airplane_mode_off));
			}

		}

	}

	public void setColorFilter(ImageView iv, float redValue, float greenValue,
			float blueValue) {

		redValue = redValue / 255;
		blueValue = blueValue / 255;
		greenValue = greenValue / 255;

		float[] colorMatrix = { 0.5f, 0, 0, redValue, 0, 0, 0.5f, 0,
				greenValue, 0, 0, 0, 0.5f, blueValue, 0, 0, 0, 0, 1, 0 };

		ColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix);

		iv.setColorFilter(colorFilter);

	}

	public void start() {
		progressDialog = new ProgressDialog(MainActivity.this);
		progressDialog.setCancelable(false);

		progressDialog.setTitle(getString(R.string.power_off));

		progressDialog.setMessage(getString(R.string.descr));
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setCancelable(false);
		progressDialog.setProgress(0);
		progressDialog.setMax(100);
		progressDialog.show();
		progressStatus = 0;
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (progressStatus < 10) {
					progressStatus = performTask();

				}
				myHandler.post(new Runnable() {

					@Override
					public void run() {

						progressDialog.dismiss();

						progressStatus = 0;
						myProgress = 0;
						deviceManger = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);

						try {
							Thread.sleep(120);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						deviceManger.lockNow();

						AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
								MainActivity.this);

						alertDialogBuilder.setTitle(getApplicationContext()
								.getString(R.string.hi));
						alertDialogBuilder.setMessage(getApplicationContext()
								.getString(R.string.cred));
						alertDialogBuilder.setPositiveButton(
								android.R.string.ok,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int id) {
										finish();
									}
								});
						AlertDialog alertDialog = alertDialogBuilder.create();
						alertDialog.setCancelable(false);
						alertDialog.show();

					}
				});

			}

			@SuppressWarnings({ "static-access", "deprecation" })
			private int performTask() {
				try {

					SharedPreferences prefs = getPreferenceManager()
							.getDefaultSharedPreferences(MainActivity.this);

					Thread.sleep(prefs.getInt("ms", 0));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return ++myProgress;
			}
		}).start();
	}

}