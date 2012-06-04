package pt.isel.pdm.Yamba.activity;

import java.util.Date;

import pt.isel.pdm.Yamba.R;
import pt.isel.pdm.Yamba.R.id;
import pt.isel.pdm.Yamba.R.layout;

import android.app.Activity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.widget.TextView;

public class DetailActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detail);
		Bundle bundle = getIntent().getExtras();
				
		TextView detailTextUser = (TextView) findViewById(R.id.detail_textUser);
		detailTextUser.setText(bundle.getString("detailTextUser")) ;
		
		TextView detailTextMessage = (TextView) findViewById(R.id.detail_textMessage);
		detailTextMessage.setText(bundle.getString("detailTextMessage")) ;
		
		TextView detailTextTime = (TextView) findViewById(R.id.detail_textTime);
		long createdAt = bundle.getLong("detailTextTime");
		detailTextTime.setText(new Date(createdAt).toLocaleString());
		
		TextView detailTextId = (TextView) findViewById(R.id.detail_textId);
		detailTextId.setText(bundle.getString("detailTextId")) ;
	}

}
