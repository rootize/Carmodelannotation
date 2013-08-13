package edu.cmu.cma;

import android.R.string;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper{

	
	private static final int db_verison=1;
	private static final String DB_NAME="CAR_MAKE_MODEL";
	private static final String TABLE_NAME="YAIR";
	private static final String car_make="cmake";
	private static final String car_model="cmodel";
	public DatabaseHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

}



/*FileReader file = new FileReader(fileName);
BufferedReader buffer = new BufferedReader(file);
String line = "";
String tableName ="TABLE_NAME";
String columns = "_id, name, dt1, dt2, dt3";
String str1 = "INSERT INTO " + tableName + " (" + columns + ") values(";
String str2 = ");";

db.beginTransaction();
while ((line = buffer.readLine()) != null) {
    StringBuilder sb = new StringBuilder(str1);
    String[] str = line.split(",");
    sb.append("'" + str[0] + "',");
    sb.append(str[1] + "',");
    sb.append(str[2] + "',");
    sb.append(str[3] + "'");
    sb.append(str[4] + "'");
    sb.append(str2);
    db.execSQL(sb.toString());
}
db.setTransactionSuccessful();
db.endTransaction();*/