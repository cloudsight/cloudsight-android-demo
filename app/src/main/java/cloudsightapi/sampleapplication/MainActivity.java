package cloudsightapi.sampleapplication;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int IMAGE_REQUEST_CODE = 1;
    private static final int REQUEST_TIME_OUT = 60 * 1000;
    private static final String IMAGE_REQUEST_URL = "http://api.cloudsight.ai/image_requests";
    private static final String IMAGE_RESPONSE_URL = "http://api.cloudsight.ai/image_responses/";

    private Uri mOutputFileUri;
    private static ViewHolder mViewHolder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mViewHolder = new ViewHolder();
        if (PermissionUtils.checkReadStoragePermission(this)) {
            mViewHolder.initializeViews();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btn_capture:
                mViewHolder.setTvRecognitionText("");
                openImageIntent();
                break;

            case R.id.btn_send_image:
                mViewHolder.setTvRecognitionText("");
                createAndSendImageRequest();
                break;

        }
    }

    private void createAndSendImageRequest() {
        mViewHolder.showProgressDialog(getString(R.string.processing_message));
        Bitmap bitmap = mViewHolder.getIvImageBitmap();

        if (bitmap == null) {
            Toast.makeText(getApplicationContext(), R.string.no_image_selected_message, Toast.LENGTH_LONG).show();
            return;
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        //compressing bitmap by 50%
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bos);
        byte[] bitmapdata = bos.toByteArray();
        InputStream bs = new ByteArrayInputStream(bitmapdata);

        AsyncHttpClient asyncClient = new AsyncHttpClient();
        asyncClient.setResponseTimeout(REQUEST_TIME_OUT);
        asyncClient.addHeader(getString(R.string.auth_header), "CloudSight " + getString(R.string.api_key));

        RequestParams params = new RequestParams();
        params.put(getString(R.string.param_image), bs, getString(R.string.image_name));
        params.put(getString(R.string.param_locale), getString(R.string.en_us));
        params.setForceMultipartEntityContentType(true);

        asyncClient.post(IMAGE_REQUEST_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    Toast.makeText(getApplicationContext(), R.string.upload_success_message, Toast.LENGTH_SHORT).show();
                    Toast.makeText(getApplicationContext(), "Image URL : " + response.getString("url"), Toast.LENGTH_SHORT).show();
                    String token = response.getString("token");
                    new FetchTask(token).execute();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Toast.makeText(getApplicationContext(), R.string.upload_failure_message, Toast.LENGTH_SHORT).show();
                mViewHolder.hideProgressDialog();
            }
        });
    }

    private class FetchTask extends AsyncTask<Void, Void, Void> {

        String token;

        public FetchTask(String token) {
            super();
            this.token = token;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            getImageResponse(token);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mViewHolder.hideProgressDialog();
        }
    }


    public void showImageResponseText(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mViewHolder.setTvRecognitionText(message);
            }
        });

    }

    private void getImageResponse(String pToken) {
        final boolean[] flag = {false};
        final SyncHttpClient syncHttpClient = new SyncHttpClient();
        syncHttpClient.setResponseTimeout(REQUEST_TIME_OUT);
        syncHttpClient.addHeader(getString(R.string.auth_header), "CloudSight " + getString(R.string.api_key));
        while (!flag[0]) {
            syncHttpClient.get(IMAGE_RESPONSE_URL + pToken, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    try {
                        if (!response.getString("status").equals("not completed")) {
                            flag[0] = true;
                            String status = response.getString("status");
                            if (!status.equals("completed")) {
                                showImageResponseText(getString(R.string.fail_message));
                            } else {
                                showImageResponseText(getString(R.string.image_recognized_as) + response.getString("name"));
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                }
            });
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionUtils.PERMISSION_STORAGE_TAG) {
            if (PermissionUtils.checkReadStoragePermission(this)) {
                mViewHolder.initializeViews();
            }
        }
    }

    private void openImageIntent() {
        Uri photoFileUri = null;
        File outputDir = getApplicationContext().getExternalFilesDir(null);
        if (outputDir != null) {
            File photoFile = new File(outputDir, "imageFile" + ".png");
            photoFileUri = Uri.fromFile(photoFile);
        }
        // Determine Uri of camera image to save.
        mOutputFileUri = photoFileUri;
        final List<Intent> intentList = new ArrayList<Intent>();
        // Camera Intent.
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mOutputFileUri);
        intentList.add(cameraIntent);
        // Filesystem Intent.
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, getString(R.string.chooser_title));
        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new Parcelable[intentList.size()]));
        startActivityForResult(chooserIntent, IMAGE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == IMAGE_REQUEST_CODE) {

            boolean isCamera;

            if (data == null || data.getData() == null)
                isCamera = true;
            else
                isCamera = false;

            Bitmap bitmap;
            if (isCamera) {
                bitmap = loadCameraImage(mOutputFileUri.getPath());
            } else {
                if (data == null)
                    bitmap = null;
                else
                    bitmap = loadGalleryImage(data.getData());
            }

            if (bitmap != null) {
                mViewHolder.setIvImageBitmap(bitmap);
            }
        }
    }

    public Bitmap loadCameraImage(String path) {

        try {
            File f = new File(path);
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            return b;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;

    }

    private Bitmap loadGalleryImage(Uri uri) {
        try {
            ParcelFileDescriptor parcelFileDescriptor;
            parcelFileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
            return image;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    class ViewHolder {
        private ImageView mIvImage;
        private Button mBtnCapture;
        private Button mBtnSend;
        private TextView mTvRecognitionText;
        private ProgressDialog mProgressDialog;

        public void initializeViews() {
            mIvImage = (ImageView) findViewById(R.id.iv_image);
            mBtnCapture = (Button) findViewById(R.id.btn_capture);
            mBtnSend = (Button) findViewById(R.id.btn_send_image);
            mTvRecognitionText = (TextView) findViewById(R.id.tv_recognition_text);

            mBtnCapture.setOnClickListener(MainActivity.this);
            mBtnSend.setOnClickListener(MainActivity.this);
        }

        public void showProgressDialog(String pMessage) {
            if (mProgressDialog == null || !mProgressDialog.isShowing()) {
                mProgressDialog = new ProgressDialog(MainActivity.this);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setMessage(pMessage);
                mProgressDialog.show();
            }
        }

        public void hideProgressDialog() {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
        }

        public void setTvRecognitionText(String pText) {
            mTvRecognitionText.setText(pText);
        }

        public Bitmap getIvImageBitmap() {
            if (mIvImage.getDrawable() == null || ((BitmapDrawable) mIvImage.getDrawable()).getBitmap() == null)
                return null;
            else
                return ((BitmapDrawable) mIvImage.getDrawable()).getBitmap();
        }

        public void setIvImageBitmap(Bitmap bitmap) {
            mIvImage.setImageBitmap(bitmap);
        }
    }
}
