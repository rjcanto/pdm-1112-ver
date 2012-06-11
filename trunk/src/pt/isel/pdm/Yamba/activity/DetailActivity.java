package pt.isel.pdm.Yamba.activity;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Date;

import pt.isel.pdm.Yamba.R;
import pt.isel.pdm.Yamba.util.Utils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import winterwell.jtwitter.Twitter;

import pt.isel.pdm.Yamba.App;

public class DetailActivity extends Activity{
	TextView detailTextMessage ;
	TextView detailTextUser ;
	ImageView detailUserImage ;
	App _app;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		_app = (App) getApplication() ;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detail);
		Bundle bundle = getIntent().getExtras();
				
		detailTextUser = (TextView) findViewById(R.id.detail_textUser);
		detailTextUser.setText(bundle.getString("detailTextUser")) ;
		
		detailTextMessage = (TextView) findViewById(R.id.detail_textMessage);
		detailTextMessage.setText(bundle.getString("detailTextMessage")) ;
		
		TextView detailTextTime = (TextView) findViewById(R.id.detail_textTime);
		long createdAt = bundle.getLong("detailTextTime");
		detailTextTime.setText(new Date(createdAt).toLocaleString());
		
		TextView detailTextId = (TextView) findViewById(R.id.detail_textId);
		detailTextId.setText(bundle.getString("detailTextId")) ;
		
		detailUserImage = (ImageView) findViewById(R.id.detail_userImage) ;
		try {
			Twitter.User _twUser = _app.twitter().getUser(bundle.getString("detailTextUser")) ;
			InputStream imageSrc;
			imageSrc = _twUser.getProfileImageUrl().toURL().openStream();
			detailUserImage.setImageBitmap((Bitmap) BitmapFactory.decodeStream(imageSrc));					
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}					
	}

	
	public void onClickShare(View v) {
		try {
			Utils.Log("DetailActivity.onClickShare") ;
			Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
			emailIntent.setType("plain/text"); 
			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, detailTextMessage.getText());   
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, 
					getString(R.string.detail_share_subject)+detailTextUser.getText()); 		
			startActivity(emailIntent);
		} catch (android.content.ActivityNotFoundException e) {
			Utils.Log("DetailActivity.onClickShare - Error:"+ e.toString()) ;
			Utils.showToast(getApplicationContext(), getString(R.string.detail_error_noemail));
		}
	}

}
