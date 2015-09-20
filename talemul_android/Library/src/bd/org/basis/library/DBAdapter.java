package bd.org.basis.library;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DBAdapter {
	private DBHelper dbHelper;
	private Context context;
	private SQLiteDatabase db;
	
	public DBAdapter(Context context) 
	{
		
		this.context=context;
		dbHelper=new DBHelper(context);
	}
	
	public void open()
	{
		db=dbHelper.getWritableDatabase();
		
	}
	
	public void close()
	{
	db.close();
		
	}
	public long insertBook(Book book)
	{
		ContentValues values=new ContentValues();
		values.put(DBHelper.TITLE, "TALEMUL");
		values.put(DBHelper.AUTHOR, "2");
		values.put(DBHelper.CATEGORY, book.getCategory());
		values.put(DBHelper.ISBN, book.getISBN());
		values.put(DBHelper.PRICE, book.getPrice());
		
		long inserted = db.insert(DBHelper.TABLE_NAME, null, values);
		return inserted;
	}
	public ArrayList<Book> getAllBooks()
	{
		 ArrayList<Book> allBooks =null;
		 Cursor cursor =  db.query(DBHelper.TABLE_NAME, null, null, null, null, null, null, null);
		 //select * from books
		 allBooks=new ArrayList<Book>(); 
		 if(cursor!=null && cursor.getCount()>0)
		 {
			 int size=cursor.getCount();
			 cursor.moveToLast();
			 allBooks=new ArrayList<Book>();   
			 for(int i=0;i<size;i++)
			 {
				 String title=cursor.getString(cursor.getColumnIndex(DBHelper.TITLE));
				 String author=cursor.getString(cursor.getColumnIndex(DBHelper.AUTHOR));
				 String category=cursor.getString(cursor.getColumnIndex(DBHelper.CATEGORY));
				 String ISBN=cursor.getString(cursor.getColumnIndex(DBHelper.ISBN));
				 double price =cursor.getDouble(cursor.getColumnIndex(DBHelper.PRICE));
				 allBooks.add(new Book(title, author, category, ISBN, price));
				 cursor.moveToPrevious();
				 
			 }
		 }
		 cursor.close();
		return allBooks;
		
	}

}
