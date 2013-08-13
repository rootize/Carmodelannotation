package edu.cmu.cma;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseFile;
import com.parse.ParseObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import android.provider.MediaStore;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class MainActivity extends Activity {
	// HD image size
	private static final int dstWidth = 1280;
	private static final int dstHeight = 720;
	private static final int CAMERA_REQUEST = 1888;
	private static final String tmpImgname = "tmpimg.jpg";
	private MyImageView imageView;
	// private Point centerROI;
	// private Bitmap orgBitmap;
	private File tmpStorage;

	// GUI items
	private Spinner carmakeSpinner;
	private Spinner carmodelSpinner;
	private Button photoButton; // button : take a photo!
	private Button finishButton;
	private Button clear_rectButton;

	private int select_item_id;
	private String[] items = { "Use 4G anyway", "Abort image ,quit now",
			"Save image, remind me when wifi available" };
	private String selectedItem = "";
	private AlertDialog wifi_dialog;

	/*
	 * private String carmake = ""; private String carmodel = "";
	 */

	// Data to Send :
	private ParseObject pb_send;

	private File mMakeModel;

	// GPS information
	GPSTracker gps;
	// TODO database related!
	private static final String DATABASE_TABLE_NAME = "carmakemodel";
	private static final String DATABASE_NAME = "car.db";
	private static final String KEY_ID = "_id";
	private static final String CAR_MAKE = "carmake";
	private static final String CAR_MODEL = "carmodel";
	private SQLiteDatabase carDatabase;

	
	
	
	private String make_selectString = "";
	private String model_selectString = "";
	String model_select_SQL_String = "";
    String make_select_SQL_String="";
	/***********************************
	 * Things you should send to the server!
	 * 
	 ************************************/
	// check if it has GPS data
	// how long does it take to get gps data
	// check
	private Point Rect_TL; // rectangle top_left
	private Point Rect_BR; // rectangle bottom_right
	// private LocationManager mLocationManager=null;
	private Bitmap bitmap_toSend;
	private String car_Make = "";
	private String car_Model = "";
	private double latitude = 0;
	private double longitude = 0;
	private double altitude = 0;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		
		super.onResume();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		

		// Part 1: Location--get GPS information
		gps = new GPSTracker(MainActivity.this);
        
		// Part 2: connect to parse server
		Parse.initialize(this, "jAj3JiDwBuIvLzRejnBNBnF0nAY0pgNkbf01j6Ov",
				"xC51p1SSoacrunqRAYe2KnwHZ1QjvJyaRflwSNJt");
		ParseAnalytics.trackAppOpened(getIntent());

		
		
		setContentView(R.layout.activity_main);
		this.imageView = (MyImageView) this.findViewById(R.id.iview1);

		ViewStub stub = (ViewStub) findViewById(R.id.stub);
		stub.inflate();

		Button photoButton = (Button) this.findViewById(R.id.button_takephoto);
		photoButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// PackageManager pm=getPackageManager();

				String functionName = "photoButton related function";

				Log.d(functionName, "start called!");
				// if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
				tmpStorage = new File(getFilesDir(), tmpImgname);
				if (tmpStorage.exists()) {
					// Do nothing!
					// tmpStorage.delete();
				}

				Intent cameraIntent = new Intent(
						android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				// tmpStorage=Environment.getExternalStorageDirectory();
				/***********
				 * Key Part that leads to error!
				 ***********/
				cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,
						MyFileContentProvider.CONTENT_URI);

				// File out = Environment.getExternalStorageDirectory();
				// out = new File(out, tmpImgname);
				// cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,
				// Uri.fromFile(out));
				startActivityForResult(cameraIntent, CAMERA_REQUEST);

				// }else {
				// //Pop out a message box showing no camera available!
				// }

			}
		});

		// Build dialog for choosing different wifi
		AlertDialog.Builder wifi_perference = new AlertDialog.Builder(
				MainActivity.this);
		wifi_perference.setTitle("Wifi NOT available");
		wifi_perference.setIcon(R.drawable.warning);

		selectedItem = items[0];
		wifi_perference.setSingleChoiceItems(items, 0,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						selectedItem = items[which];
						select_item_id = which;
					}
				});
		wifi_perference.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				});
		wifi_perference.setCancelable(false);
		wifi_dialog = wifi_perference.create();
		Button finishButton = (Button) this.findViewById(R.id.button_finish);
		finishButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				// show if wifi available
				boolean send_flag = true;/* false */

				WifiManager wifi_condition = (WifiManager) getSystemService(Context.WIFI_SERVICE);
				if (!wifi_condition.isWifiEnabled()) {
					wifi_dialog.show();
				}

				switch (select_item_id) {
				case 0:
					// Send anyway
					// send_flag=false;
					send_flag = true;
					break;
				case 1:
					// not send
					android.os.Process.killProcess(android.os.Process.myPid());
					send_flag = false;
					break;
				default:
					break;
				}

				if (true == send_flag) {
					byte[] data = new byte[(int) tmpStorage.length()];

					try {

						FileInputStream in = new FileInputStream(tmpStorage);
						try {
							in.read(data);
							in.close();
						} catch (IOException e) {

							e.printStackTrace();
						}

					} catch (FileNotFoundException e) {

						e.printStackTrace();
					}
					// FIXME
					// Carefully Check!
					pb_send = new ParseObject("Annotation");
					pb_send.put("Make", make_selectString);
					pb_send.put("Model", model_selectString);
					/* pb_send.put("Img", orgBitmap); */
					pb_send.put("Location_x", centerROI.x);
					pb_send.put("Location_y", centerROI.y);
					pb_send.saveInBackground();
					ParseFile imgParseFile = new ParseFile("img.jpeg", data);
					imgParseFile.saveInBackground();
					AlertDialog.Builder Sending_Finish_Dialog = new AlertDialog.Builder(
							MainActivity.this).setCancelable(false);

					Sending_Finish_Dialog.setTitle("Done Sending!");
					Sending_Finish_Dialog
							.setMessage("Sent Successfully! Thanks");
					Sending_Finish_Dialog.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub

								}
							});
					Sending_Finish_Dialog.show();

				}

			}
		});

		Button clearButton = (Button) findViewById(R.id.clearDrawing);
		clearButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				imageView.clearrect();
				imageView.invalidate();

			}
		});

		// Create Spinner (Later you may move them to a onResume)
		// carmakeSpinner = (Spinner) findViewById(R.id.makespinner);

		// ArrayAdapter<string>adapter_make=
		// carmakeSpinner.setOnItemSelectedListener((OnItemSelectedListener)
		// this);/*?*/
		// TODO Create a database

		carDatabase = openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE,
				null);
		String create_table = "DROP TABLE IF EXISTS " + DATABASE_TABLE_NAME;
		carDatabase.execSQL(create_table);
		carDatabase.execSQL("CREATE TABLE " + DATABASE_TABLE_NAME + "("
				+ KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + CAR_MAKE
				+ " TEXT NOT NULL , " + CAR_MODEL + " TEXT NOT NULL )");

		// FIXME Generate data from raw file
		try {
			InputStream is = getResources().openRawResource(
					R.raw.car_make_model);
			File car_make_model_dir = getDir("make_model", Context.MODE_PRIVATE);
			mMakeModel = new File(car_make_model_dir, "car.csv");
			FileOutputStream os = new FileOutputStream(mMakeModel);
			byte[] buffer = new byte[4096];
			int bytesRead;
			while ((bytesRead = is.read(buffer)) != -1) {
				os.write(buffer, 0, bytesRead);

			}
			os.close();
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("OnCreate()::", "Failed to load csv file" + e);

		}

		// FIXME Insert data from file to database!
		// String insert_sentence=
		try {
			FileReader databasefile = new FileReader(
					mMakeModel.getAbsolutePath());
			BufferedReader database_bufferReader = new BufferedReader(
					databasefile);
			String line = "";
			String str1 = "INSERT INTO " + DATABASE_TABLE_NAME + "( "
					+ CAR_MAKE + " , " + CAR_MODEL + " )" + " VALUES (";
			String str2 = " );";

			carDatabase.beginTransaction();
			while ((line = database_bufferReader.readLine()) != null) {
				String[] all_elements = line.split(",");
				/*
				 * Log.d("Generate DATABASE",
				 * ""+all_elements.length+"   "+all_elements[1]); String[]
				 * test_elements=all_elements[0].split(":");
				 * Log.d(""+test_elements[0], test_elements[1]);
				 */
				String[] make_model_String = new String[2];
				for (int i = 0; i < all_elements.length; i++) {
					StringBuilder sb_insert = new StringBuilder(str1);
					make_model_String = all_elements[i].split(":");
					if (2 == make_model_String.length) {
						// Log.d(""+i, "  "+make_model_String.length);
						sb_insert.append("'" + make_model_String[0] + "',");
						sb_insert.append(" '" + make_model_String[1] + "'");
						sb_insert.append(str2);
						carDatabase.execSQL(sb_insert.toString());
					} else {
						Log.d("" + i, "" + make_model_String[0]);
					}

				}

			}
			database_bufferReader.close();
			carDatabase.setTransactionSuccessful();
			carDatabase.endTransaction();

		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}

		// select id, name from table group by name

		// Spinners
		carmakeSpinner = (Spinner) findViewById(R.id.makespinner);
		String select_array = "SELECT DISTINCT " + CAR_MAKE + " from "
				+ DATABASE_TABLE_NAME;
		// Cursor c_make=carDatabase.rawQuery(select_array, null);
		List<String> make_label = new ArrayList<String>();
		make_label = getAllLabels(select_array);
		// ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,
		// android.R.layout.simple_spinner_dropdown_item);
		// SimpleCursorAdapter adapter=new SimpleCursorAdapter(this,
		// android.R.layout.simple_dropdown_item_1line, c, from, to)
		ArrayAdapter<String> make_dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, make_label);
		make_dataAdapter
				.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		carmakeSpinner.setAdapter(make_dataAdapter);

		carmodelSpinner = (Spinner) findViewById(R.id.modelspinner);
		model_select_array = "SELECT DISTINCT " + CAR_MODEL + " from "
				+ DATABASE_TABLE_NAME;
		if ("" == make_selectString) {
			// Select all: model_select_array do nothing
		} else {
			// model_select_array+=" where "+CAR_MAKE+" ='"+make_selectString+"';";

		}
		// List<String>model_label=new ArrayList<String>();
		List<String> model_label = getAllLabels(model_select_array);
		ArrayAdapter<String> model_dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, model_label);
		model_dataAdapter
				.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		carmodelSpinner.setAdapter(model_dataAdapter);

		carmakeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				make_selectString = arg0.getItemAtPosition(arg2).toString();
				String refresh_model = model_select_array + " where "
						+ CAR_MAKE + " ='" + make_selectString + "';";
				setNewSpinner(refresh_model);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				make_selectString = "";
			}
		});
		carmodelSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				model_selectString = arg0.getItemAtPosition(arg2).toString();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				model_selectString = "";
			}
		});
	}

	private void setNewSpinner(String newSelect_String) {
		// TODO Auto-generated method stub

		List<String> model_label = getAllLabels(newSelect_String);
		ArrayAdapter<String> model_dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, model_label);
		model_dataAdapter
				.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		carmodelSpinner.setAdapter(model_dataAdapter);

	}

	private List<String> getAllLabels(String select_array) {
		List<String> labels = new ArrayList<String>();

		Cursor cursor = carDatabase.rawQuery(select_array, null);
		// Log.d("Cursor Size", "" + cursor.getCount());
		// Log.d("Function:getAllLabels() 0 column:::", cursor.getString(0));
		// Log.d("Function:getAllLabels() 1 column:::", cursor.getString(1));
		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			// do {
			// labels.add(cursor.getString(1));
			// } while (cursor.moveToNext());
			//         }
			//

			labels.add(cursor.getString(0));

			while (cursor.moveToNext()) {
				// type type = (type) en.nextElement();
				labels.add(cursor.getString(0));
			}

		}
		// closing connection
		cursor.close();
		return labels;
		// carDatabase.close();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		// super.onActivityResult(requestCode, resultCode, data);
		String functionName = "onActivityResult";
		Log.d(functionName, "Function Called!!!!");
		if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
			// if (tmpStorage.exists()) {
			//
			// }
			tmpStorage = new File(getFilesDir(), "newImage.jpg");
			if (!tmpStorage.exists()) {
				Log.d("OnActivityResult:", "  return at this point!");
				// return;
			} else {
				Log.d("OnActivityResult:", "tmpstorage actually exists!");
				Bitmap bm = BitmapFactory.decodeFile(tmpStorage
						.getAbsolutePath());
				orgBitmap = Bitmap.createScaledBitmap(bm, dstWidth, dstHeight,
						true);
				// check if the file(5th option is needed!)
				imageView.setImageBitmap(bm);
				// AfterAnnotation();
				imageView.setOnTouchListener(new View.OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {

						MyImageView drawView = (MyImageView) v;
						if (event.getAction() == MotionEvent.ACTION_DOWN) {
							drawView.left = event.getX();
							drawView.top = event.getY();
							// set end coords
						} else {
							drawView.right = event.getX();
							drawView.bottom = event.getY();
						}
						// draw
						drawView.invalidate();
						drawView.drawPoint = true;
						centerROI = new Point(
								(int) (drawView.left + drawView.right) / 2,
								(int) (drawView.top + drawView.bottom) / 2);
						// drawView.clearrect();
						return true;
					}
				});
				;

			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {

		Log.d("onPause:", "OnPause Called!");

		if (null != tmpStorage && tmpStorage.exists()) {
			tmpStorage.delete();
			if (null != imageView) {
				imageView.clearrect();
			}

		}
		make_selectString = "";
		model_selectString = "";

		// mLocationManager.removeUpdates(onLocationChange);
		super.onPause();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		gps.stopUsingGPS();// Stop Using GPS data
		super.onDestroy();
	}

}
