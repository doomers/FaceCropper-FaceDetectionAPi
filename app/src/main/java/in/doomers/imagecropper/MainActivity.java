package in.doomers.imagecropper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

 BitmapView bitmapView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bitmapView = (BitmapView) findViewById(R.id.bitmapview);

        InputStream stream = getResources().openRawResource(R.raw.image04);
        Bitmap mbitmap = BitmapFactory.decodeStream(stream);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        int width = options.outWidth;
        int height = options.outHeight;
        Rect rect = new Rect(0,height,width,0);
        Bitmap bm =BitmapFactory.decodeStream(stream,rect,options);

       bitmapView.setBitmap(bm);



    }


}
