package net.sf.andpdf.pdfviewer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import net.sf.andpdf.nio.ByteBuffer;
import net.sf.andpdf.pdfviewer.gui.FullScrollView;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFImage;
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PDFPaint;
import com.sun.pdfview.font.PDFFont;


/**
 * U:\Android\android-sdk-windows-1.5_r1\tools\adb push u:\Android\simple_T.pdf /data/test.pdf
 * @author ferenc.hechler
 */
public class PdfViewerActivity extends Activity {

	private static final int STARTPAGE = 1;
	private static final float STARTZOOM = 1.0f;
	
	private static final String TAG = "PDFVIEWER";
	
	private final static int MEN_NEXT_PAGE = 1;
	private final static int MEN_PREV_PAGE = 2;
	private final static int MEN_GOTO_PAGE = 3;
	private final static int MEN_ZOOM_IN   = 4;
	private final static int MEN_ZOOM_OUT  = 5;
	private final static int MEN_BACK      = 6;
	
	private final static int DIALOG_PAGENUM = 1;
	
	private GraphView mGraphView;
	private String pdffilename;
	private PDFFile mPdfFile;
	private int mPage;
	private float mZoom;
    private File mTmpFile;
    
    private PDFPage mPdfPage; 
    
    private Thread backgroundThread;
    private Handler uiHandler;

	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        uiHandler = new Handler();
        if (mGraphView == null) {
	        mGraphView = new GraphView(this);
	        
	        Intent intent = getIntent();
	        Log.i(TAG, ""+intent);

	        boolean showImages = getIntent().getBooleanExtra(PdfFileSelectActivity.EXTRA_SHOWIMAGES, PdfFileSelectActivity.DEFAULTSHOWIMAGES);
	        PDFImage.sShowImages = showImages;
	        boolean antiAlias = getIntent().getBooleanExtra(PdfFileSelectActivity.EXTRA_ANTIALIAS, PdfFileSelectActivity.DEFAULTANTIALIAS);
	        PDFPaint.s_doAntiAlias = antiAlias;
	    	boolean useFontSubstitution = getIntent().getBooleanExtra(PdfFileSelectActivity.EXTRA_USEFONTSUBSTITUTION, PdfFileSelectActivity.DEFAULTUSEFONTSUBSTITUTION);
	        PDFFont.sUseFontSubstitution= useFontSubstitution;
		        
	        if (intent != null) {
	        	if ("android.intent.action.VIEW".equals(intent.getAction())) {
        			pdffilename = storeUriContentToFile(intent.getData());
	        	}
	        	else {
	                pdffilename = getIntent().getStringExtra(PdfFileSelectActivity.EXTRA_PDFFILENAME);
	        	}
	        }
	        
	        if (pdffilename == null)
	        	pdffilename = "no file selected";
	        
	        parsePDF(pdffilename);
	        
	        setContentView(mGraphView);
	        
			mPage = STARTPAGE;
			mZoom = STARTZOOM;
	        startRenderThread(mPage, mZoom);
        }
    }

	private synchronized void startRenderThread(final int page, final float zoom) {
		if (backgroundThread != null)
			return;
		mGraphView.showText("reading page "+ page+", zoom:"+zoom);
        backgroundThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
			        if (mPdfFile != null) {
//			        	File f = new File("/sdcard/andpdf.trace");
//			        	f.delete();
//			        	Log.e(TAG, "DEBUG.START");
//			        	Debug.startMethodTracing("andpdf");
			        	showPage(page, zoom);
//			        	Debug.stopMethodTracing();
//			        	Log.e(TAG, "DEBUG.STOP");
			        }
				} catch (Exception e) {
					Log.e(TAG, e.getMessage(), e);
				}
		        backgroundThread = null;
			}
		});
        updateImageStatus();
        backgroundThread.start();
	}


	private void updateImageStatus() {
//		Log.i(TAG, "updateImageStatus: " +  (System.currentTimeMillis()&0xffff));
		if (backgroundThread == null) {
			mGraphView.updateUi();
			return;
		}
		mGraphView.updateUi();
		mGraphView.postDelayed(new Runnable() {
			@Override public void run() {
				updateImageStatus();
			}
		}, 1000);
	}

	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, MEN_PREV_PAGE, Menu.NONE, "Previous Page");
        menu.add(Menu.NONE, MEN_NEXT_PAGE, Menu.NONE, "Next Page");
        menu.add(Menu.NONE, MEN_GOTO_PAGE, Menu.NONE, "Goto Page");
        menu.add(Menu.NONE, MEN_ZOOM_OUT, Menu.NONE, "Zoom Out");
        menu.add(Menu.NONE, MEN_ZOOM_IN, Menu.NONE, "Zoom In");
        menu.add(Menu.NONE, MEN_BACK, Menu.NONE, "Back");
        return true;
    }
    
    /**
     * Called when a menu item is selected.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
    	switch (item.getItemId()) {
    	case MEN_NEXT_PAGE: {
    		nextPage();
    		break;
    	}
    	case MEN_PREV_PAGE: {
    		prevPage();
    		break;
    	}
    	case MEN_GOTO_PAGE: {
    		gotoPage();
    		break;
    	}
    	case MEN_ZOOM_IN: {
    		zoomIn();
    		break;
    	}
    	case MEN_ZOOM_OUT: {
    		zoomOut();
    		break;
    	}
    	case MEN_BACK: {
            finish();
            break;
    	}
    	}
    	return true;
    }
    
    
    private void zoomIn() {
    	if (mPdfFile != null) {
    		if (mZoom < 4) {
    			mZoom *= 1.5;
    			if (mZoom > 4)
    				mZoom = 4;
    			startRenderThread(mPage, mZoom);
    		}
    	}
	}

    private void zoomOut() {
    	if (mPdfFile != null) {
    		if (mZoom > 0.25) {
    			mZoom /= 1.5;
    			if (mZoom < 0.25)
    				mZoom = 0.25f;
    			startRenderThread(mPage, mZoom);
    		}
    	}
	}

	private void nextPage() {
    	if (mPdfFile != null) {
    		if (mPage < mPdfFile.getNumPages()) {
    			mPage += 1;
    			startRenderThread(mPage, mZoom);
    		}
    	}
	}

    private void prevPage() {
    	if (mPdfFile != null) {
    		if (mPage > 1) {
    			mPage -= 1;
    			startRenderThread(mPage, mZoom);
    		}
    	}
	}
    
	private void gotoPage() {
    	if (mPdfFile != null) {
            showDialog(DIALOG_PAGENUM);
    	}
	}

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DIALOG_PAGENUM:
	        LayoutInflater factory = LayoutInflater.from(this);
	        final View pagenumView = factory.inflate(R.layout.dialog_pagenumber, null);
			final EditText edPagenum = (EditText)pagenumView.findViewById(R.id.pagenum_edit);
			edPagenum.setText(Integer.toString(mPage));
	        return new AlertDialog.Builder(this)
	            .setIcon(R.drawable.icon)
	            .setTitle("Jump to page")
	            .setView(pagenumView)
	            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {
	            		String strPagenum = edPagenum.getText().toString();
	            		int pageNum = mPage;
	            		try {
	            			pageNum = Integer.parseInt(strPagenum);
	            		}
	            		catch (NumberFormatException ignore) {}
	            		if ((pageNum!=mPage) && (pageNum>=1) && (pageNum <= mPdfFile.getNumPages())) {
	            			mPage = pageNum;
	            			startRenderThread(mPage, mZoom);
	            		}
	                }
	            })
	            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {
	                }
	            })
	            .create();
        }        
        return null;
    }
    
	private class GraphView extends FullScrollView {
    	private String mText;
    	private long fileMillis;
    	private long pageMillis;
    	private Bitmap mBi;
    	private String mLine1;
    	private String mLine2;
    	private String mLine3;
    	private ImageView mImageView;
    	private TextView mLine1View; 
    	private TextView mLine2View; 
    	private TextView mLine3View; 
    	private Button mBtPage;
    	private Button mBtPage2;
        
        public GraphView(Context context) {
            super(context);

            // layout params
			LinearLayout.LayoutParams lpChild1 = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,1);
			LinearLayout.LayoutParams lpWrap1 = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,1);
			LinearLayout.LayoutParams lpChild10 = new LinearLayout.LayoutParams(100,100,1);
			LinearLayout.LayoutParams lpWrap10 = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,10);

            // vertical layout
			LinearLayout vl=new LinearLayout(context);
			vl.setLayoutParams(lpWrap10);
			vl.setOrientation(LinearLayout.VERTICAL);

				mLine1 = "PDF Viewer initializing";
				mLine1View = new TextView(context);
		        mLine1View.setLayoutParams(lpWrap1);
		        mLine1View.setText(mLine1);
		        mLine1View.setTextColor(Color.BLACK);
		        vl.addView(mLine1View);
			
				mLine2 = "unknown number of pages";
				mLine2View = new TextView(context);
		        mLine2View.setLayoutParams(lpWrap1);
		        mLine2View.setText(mLine2);
		        mLine2View.setTextColor(Color.BLACK);
		        vl.addView(mLine2View);
			
				mLine3 = "unknown timestamps";
				mLine3View = new TextView(context);
		        mLine3View.setLayoutParams(lpWrap1);
		        mLine3View.setText(mLine3);
		        mLine3View.setTextColor(Color.BLACK);
		        vl.addView(mLine3View);
			
		        addNavButtons(vl);
		        // remember page button for updates
		        mBtPage2 = mBtPage;
		        
		        mImageView = new ImageView(context);
		        setPageBitmap(null);
		        updateImage();
		        mImageView.setLayoutParams(lpWrap1);
		        mImageView.setPadding(5, 5, 5, 5);
		        vl.addView(mImageView);	
			
		        addNavButtons(vl);
			    
			setLayoutParams(new LayoutParams(
					LayoutParams.FILL_PARENT,
					LayoutParams.FILL_PARENT,
					100));
			setBackgroundColor(Color.LTGRAY);
			setHorizontalScrollBarEnabled(true);
			setHorizontalFadingEdgeEnabled(true);
			setVerticalScrollBarEnabled(true);
			setVerticalFadingEdgeEnabled(true);
			addView(vl);
        }

        private void addNavButtons(ViewGroup vg) {
        	
	        addSpace(vg, 6, 6);
	        
			LinearLayout.LayoutParams lpChild1 = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,1);
			LinearLayout.LayoutParams lpWrap10 = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,10);
        	
        	Context context = vg.getContext();
			LinearLayout hl=new LinearLayout(context);
			hl.setLayoutParams(lpWrap10);
			hl.setOrientation(LinearLayout.HORIZONTAL);

				// zoom out button
				Button bZoomOut=new Button(context);
				bZoomOut.setLayoutParams(lpChild1);
				bZoomOut.setText("-");
				bZoomOut.setWidth(40);
				bZoomOut.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
			            zoomOut();
					}
				});
		        hl.addView(bZoomOut);
		        
				// zoom in button
				Button bZoomIn=new Button(context);
				bZoomIn.setLayoutParams(lpChild1);
		        bZoomIn.setText("+");
		        bZoomIn.setWidth(40);
		        bZoomIn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
			            zoomIn();
					}
				});
		        hl.addView(bZoomIn);
	    
		        addSpace(hl, 6, 6);
		        
				// prev button
				Button bPrev=new Button(context);
		        bPrev.setLayoutParams(lpChild1);
		        bPrev.setText("<");
		        bPrev.setWidth(40);
		        bPrev.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
			            prevPage();
					}
				});
		        hl.addView(bPrev);
        
				// page button
				mBtPage=new Button(context);
				mBtPage.setLayoutParams(lpChild1);
				String maxPage = ((mPdfFile==null)?"?":Integer.toString(mPdfFile.getNumPages()));
				mBtPage.setText(mPage+"/"+maxPage);
				mBtPage.setWidth(60);
				mBtPage.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
			    		gotoPage();
					}
				});
		        hl.addView(mBtPage);
        
				// next button
				Button bNext=new Button(context);
		        bNext.setLayoutParams(lpChild1);
		        bNext.setText(">");
		        bNext.setWidth(40);
		        bNext.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
			    		nextPage();
					}
				});
		        hl.addView(bNext);
        
		        addSpace(hl, 20, 20);
        
				// exit button
				Button bExit=new Button(context);
		        bExit.setLayoutParams(lpChild1);
		        bExit.setText("Back");
		        bExit.setWidth(60);
		        bExit.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
			            finish();
					}
				});
		        hl.addView(bExit);
        	        
		        vg.addView(hl);
		    
	        addSpace(vg, 6, 6);
		}

		private void addSpace(ViewGroup vg, int width, int height) {
			TextView tvSpacer=new TextView(vg.getContext());
			tvSpacer.setLayoutParams(new LinearLayout.LayoutParams(width,height,1));
			tvSpacer.setText("");
//			tvSpacer.setWidth(width);
//			tvSpacer.setHeight(height);
	        vg.addView(tvSpacer);
    
		}

		private void showText(String text) {
        	Log.i(TAG, "ST='"+text+"'");
        	mText = text;
        	updateUi();
		}
        
        private void updateUi() {
        	uiHandler.post(new Runnable() {
				@Override
				public void run() {
		        	updateTexts();
				}
			});
		}

        private void updateImage() {
        	uiHandler.post(new Runnable() {
				@Override
				public void run() {
		        	mImageView.setImageBitmap(mBi);
				}
			});
		}

		private void setPageBitmap(Bitmap bi) {
			if (bi != null)
				mBi = bi;
			else {
				mBi = Bitmap.createBitmap(100, 100, Config.RGB_565);
	            Canvas can = new Canvas(mBi);
	            can.drawColor(Color.RED);
	            
				Paint paint = new Paint();
	            paint.setColor(Color.BLUE);
	            can.drawCircle(50, 50, 50, paint);
	            
	            paint.setStrokeWidth(0);
	            paint.setColor(Color.BLACK);
	            can.drawText("Bitmap", 10, 50, paint);
			}
		}
        
		protected void updateTexts() {
            mLine1 = "PdfViewer: "+mText;
            float fileTime = fileMillis*0.001f;
            float pageTime = pageMillis*0.001f;
            mLine2 = "seconds: parse="+fileTime+" show="+pageTime;
    		int maxCmds = PDFPage.getParsedCommands();
    		int curCmd = PDFPage.getLastRenderedCommand()+1;
    		mLine3 = "PDF-Commands: "+curCmd+"/"+maxCmds;
    		mLine1View.setText(mLine1);
    		mLine2View.setText(mLine2);
    		mLine3View.setText(mLine3);
    		if (mPdfPage != null) {
	    		if (mBtPage != null)
	    			mBtPage.setText(mPdfPage.getPageNumber()+"/"+mPdfFile.getNumPages());
	    		if (mBtPage2 != null)
	    			mBtPage2.setText(mPdfPage.getPageNumber()+"/"+mPdfFile.getNumPages());
    		}
        }
    }

	
	
    private void showPage(int page, float zoom) throws Exception {
        long startTime = System.currentTimeMillis();
    	try {
	        // free memory from previous page
	        mGraphView.setPageBitmap(null);
	        mGraphView.updateImage();
	        
	        mPdfPage = mPdfFile.getPage(page, true);
	        int num = mPdfPage.getPageNumber();
	        int maxNum = mPdfFile.getNumPages();
	        float wi = mPdfPage.getWidth();
	        float hei = mPdfPage.getHeight();
	        String pageInfo= new File(pdffilename).getName() + " - " + num +"/"+maxNum+ ": " + wi + "x" + hei;
	        mGraphView.showText(pageInfo);
	        Log.i(TAG, pageInfo);
	        RectF clip = null;
	        Bitmap bi = mPdfPage.getImage((int)(wi*zoom), (int)(hei*zoom), clip, true, true);
	        mGraphView.setPageBitmap(bi);
	        mGraphView.updateImage();
		} catch (Throwable e) {
			Log.e(TAG, e.getMessage(), e);
			mGraphView.showText("Exception: "+e.getMessage());
		}
        long stopTime = System.currentTimeMillis();
        mGraphView.pageMillis = stopTime-startTime;
    }
    
    private void parsePDF(String filename) {
        long startTime = System.currentTimeMillis();
    	try {
        	File f = new File(filename);
        	long len = f.length();
        	if (len == 0) {
        		mGraphView.showText("file '" + filename + "' not found");
        	}
        	else {
        		mGraphView.showText("file '" + filename + "' has " + len + " bytes");
    	    	openFile(f);
        	}
		} catch (Throwable e) {
			e.printStackTrace();
			mGraphView.showText("Exception: "+e.getMessage());
		}
        long stopTime = System.currentTimeMillis();
        mGraphView.fileMillis = stopTime-startTime;
	}

    
    /**
     * <p>Open a specific pdf file.  Creates a DocumentInfo from the file,
     * and opens that.</p>
     *
     * <p><b>Note:</b> Mapping the file locks the file until the PDFFile
     * is closed.</p>
     *
     * @param file the file to open
     * @throws IOException
     */
    public void openFile(File file) throws IOException {
        // first open the file for random access
        RandomAccessFile raf = new RandomAccessFile(file, "r");

        // extract a file channel
        FileChannel channel = raf.getChannel();

        // now memory-map a byte-buffer
        ByteBuffer bb =
                ByteBuffer.NEW(channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size()));
        // create a PDFFile from the data
        mPdfFile = new PDFFile(bb);
	        
        mGraphView.showText("Anzahl Seiten:" + mPdfFile.getNumPages());
    }
    
     
    private byte[] readBytes(File srcFile) throws IOException {
    	long fileLength = srcFile.length();
    	int len = (int)fileLength;
    	byte[] result = new byte[len];
    	FileInputStream fis = new FileInputStream(srcFile);
    	int pos = 0;
		int cnt = fis.read(result, pos, len-pos);
    	while (cnt > 0) {
    		pos += cnt;
    		cnt = fis.read(result, pos, len-pos);
    	}
		return result;
	}

	private String storeUriContentToFile(Uri uri) {
    	String result = null;
    	try {
	    	if (mTmpFile == null) {
				File root = Environment.getExternalStorageDirectory();
				if (root == null)
					throw new Exception("external storage dir not found");
				mTmpFile = new File(root,"AndroidPdfViewer/AndroidPdfViewer_temp.pdf");
				mTmpFile.getParentFile().mkdirs();
	    		mTmpFile.delete();
	    	}
	    	else {
	    		mTmpFile.delete();
	    	}
	    	InputStream is = getContentResolver().openInputStream(uri);
	    	OutputStream os = new FileOutputStream(mTmpFile);
	    	byte[] buf = new byte[1024];
	    	int cnt = is.read(buf);
	    	while (cnt > 0) {
	    		os.write(buf, 0, cnt);
		    	cnt = is.read(buf);
	    	}
	    	os.close();
	    	is.close();
	    	result = mTmpFile.getCanonicalPath();
	    	mTmpFile.deleteOnExit();
    	}
    	catch (Exception e) {
    		Log.e(TAG, e.getMessage(), e);
		}
		return result;
	}

    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	if (mTmpFile != null) {
    		mTmpFile.delete();
    		mTmpFile = null;
    	}
    }
    
    Runnable mRenderTask = new Runnable() {
        public void run() {
        	
        }
    };


}