package bd.org.basis.library;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
	public static final String DB_NAME = "library3";
	public static final int VERSION = 1;

	public static  final String TABLE_NAME="books";
	public static  final String ID_FIELD="id";        
	public static  final String TITLE="title";
	public static  final String AUTHOR="author";
	public static  final String CATEGORY="category";
	public static  final String ISBN="ISBN";
	public static  final String PRICE="price";	
	
	
	public static final String TABLE_SQL="CREATE TABLE " +TABLE_NAME+ " ( "+ ID_FIELD+ " INTEGER PRIMARY KEY AUTOINCREMENT , "+TITLE+
			" TEXT,"+AUTHOR+ " TEXT,"+CATEGORY+" TEXT,"+ISBN+" TEXT,"+PRICE+" NUMBER )";
	
	public DBHelper(Context context) {
		
	
		super(context, DB_NAME, null, VERSION);
	
		// TODO Auto-generated constructor stub
	}
//create db
	@Override
	public void onCreate(SQLiteDatabase db) {
		// create table
		try
		{
		Log.d("TABLE_SQL",TABLE_SQL);
		db.execSQL(TABLE_SQL);
		}
		catch(Exception e)
		{
			
			
		}
		//getWritableDatabase();
		//getReadableDatabase();
		

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
		// upgrade logic
		
		

	}

}
