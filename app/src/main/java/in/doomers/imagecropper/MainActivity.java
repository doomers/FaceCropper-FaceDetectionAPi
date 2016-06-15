package in.doomers.imagecropper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

 BitmapView bitmapView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bitmapView = (BitmapView) findViewById(R.id.bitmapview);

        InputStream stream = getResources().openRawResource(R.raw.image01);
        Bitmap mbitmap = BitmapFactory.decodeStream(stream);

       bitmapView.setBitmap(mbitmap);

    }


}
