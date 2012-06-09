package pt.isel.pdm.Yamba.activity;

import java.util.Date;

import pt.isel.pdm.Yamba.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class DetailActivity extends Activity{
	TextView detailTextMessage ;
	TextView detailTextUser ;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
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
		
		/*
		ImageView imgShare = (ImageView) findViewById(R.id.imgShare);
		imgShare.setOnClickListener(new View.OnClickListener() {

			  public void onClick(View view) {
			    // do stuff
			  }

		});*/
	}

	
	public void onClickShare(View v) {
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.setType("plain/text"); 
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, detailTextMessage.getText());   
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, 
				getString(R.string.detail_share_subject)+detailTextUser.getText()); 		
		startActivity(emailIntent); 
	}

}
