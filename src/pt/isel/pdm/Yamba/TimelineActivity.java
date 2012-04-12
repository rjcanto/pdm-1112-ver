package pt.isel.pdm.Yamba;

import java.util.List;
import winterwell.jtwitter.Twitter;
import android.app.Activity;
import android.os.Bundle;

public class TimelineActivity extends Activity{

	List<Message> elems ;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.timeline);
		
		
	}

}
