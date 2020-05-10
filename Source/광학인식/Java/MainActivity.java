package com.example.ocrproject;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.icu.text.SimpleDateFormat;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     *  true  : Camera On  : 카메라로 직접 찍어 문자 인식
     *  false : Camera Off : 샘플이미지를 로드하여 문자 인식
     */
    private boolean CameraOnOffFlag = true;


    private final String[] supportLanguageList = {"eng","kor"}; // 언어
    private TessBaseAPI tessBaseAPI;

    // 언어데이터 경로
    private String dataPath;

    // 사진 경로
    private String photoPath;


    private long startTime ;
    private long endTime ;




    private Button m_btnOCR; 
    private TextView m_ocrTextView; 
    private ImageView m_ivImage; 
    private Bitmap image;
    private TextView m_tvTime; 

    private ProgressCircleDialog m_objProgressCircle = null; // 원형 프로그레스바
    private MessageHandler m_messageHandler;
    private boolean ProgressFlag = false; // 프로그레스바 상태 플래그


    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new JeongLog();
        context = this;
        //m_messageHandler = new MessageHandler();

        //m_objProgressCircle = new ProgressCircleDialog(this);


        //m_ivImage = findViewById(R.id.iv_image);
        //m_ocrTextView = findViewById(R.id.tv_view);
        //m_tvTime = findViewById(R.id.tv_time);
        //m_btnOCR = findViewById(R.id.btn_OCR);

        //m_btnOCR.setOnClickListener(this);


        if(CameraOnOffFlag)
        {
            PermissionCheck();
            Tesseract();
        }
        else
        {
            // 이미지 디코딩을 위한  초기화
            image = BitmapFactory.decodeResource(getResources(), R.drawable.korlite);
            Test();
        }


    }


    //region button of Start
    // 시작 의 버튼
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.btn_OCR:
                if(CameraOnOffFlag)
                {
                    // 카메라로 찍음
                    dispatchTakePictureIntent();
                }
                else
                {
                    startTime = System.currentTimeMillis();
                    processImage(v);
                }
                m_tvTime.setText("처리 시간 ");
                break;
        }
    }
    //endregion



    //region ActivityResult

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ConstantDefine.PERMISSION_CODE:
                Toast.makeText(this, "권한이 허용되었습니다.", Toast.LENGTH_LONG).show();
                break;
            case ConstantDefine.ACT_TAKE_PIC:
          
                if ((resultCode == RESULT_OK) ) {

                    try {
                        startTime = System.currentTimeMillis(); 

                        File file = new File(photoPath);  // 사진의 경로로 파일 객체 얻어옴

                        Bitmap rotatedBitmap = null;

//                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),
//                                FileProvider.getUriForFile(MainActivity.this,
//                                        getApplicationContext().getPackageName() + ".fileprovider", file));

                        ImageDecoder.Source iamb = ImageDecoder.createSource(getContentResolver(), FileProvider.getUriForFile(MainActivity.this,
                                getApplicationContext().getPackageName() + ".fileprovider", file));
                       // Bitmap bitmap = ARGBBitmap(ImageDecoder.decodeBitmap(iamb));

                        Bitmap bitmap;
                        BitmapFactory.Options op = new BitmapFactory.Options();
                        op.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        bitmap = BitmapFactory.decodeFile(photoPath, op);


                        //
//                        JeongLog.log.logD("bitmap ::::    "+ iamb);
//                        ImageView iv = findViewById(R.id.iv_image2);
//                        iv.setImageBitmap(ImageDecoder.decodeBitmap(iamb));
                        // 회전된 사진을 원래대로 돌려 표시한다.
                        if (bitmap != null) {
                            ExifInterface ei = new ExifInterface(photoPath);
                            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                                    ExifInterface.ORIENTATION_UNDEFINED);
                            switch (orientation) {

                                case ExifInterface.ORIENTATION_ROTATE_90:
                                    rotatedBitmap = rotateImage(bitmap, 90);
                                    break;

                                case ExifInterface.ORIENTATION_ROTATE_180:
                                    rotatedBitmap = rotateImage(bitmap, 180);
                                    break;

                                case ExifInterface.ORIENTATION_ROTATE_270:
                                    rotatedBitmap = rotateImage(bitmap, 270);
                                    break;

                                case ExifInterface.ORIENTATION_NORMAL:
                                default:
                                    rotatedBitmap = bitmap;
                            }


                            OCRThread ocrThread = new OCRThread(rotatedBitmap);
                            ocrThread.setDaemon(true);
                            ocrThread.start();
                            m_ivImage.setImageBitmap(rotatedBitmap);
                            m_ocrTextView.setText(getResources().getString(R.string.LoadingMessage)); //인식된텍스트 표시
                        }
                    } catch (Exception e) {
                    }
                }
                break;
        }
    }
    private Bitmap ARGBBitmap(Bitmap img) {
        return img.copy(Bitmap.Config.ARGB_8888,true);
    }




   
    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }


    //endregion



    //region camera Start
   
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);


       
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // 사진파일을 생성한다.
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {

            }
            // 사진파일이 정상적으로 생성되었을때
            if (photoFile != null) {
                
                Uri photoURI = FileProvider.getUriForFile(this,
                        this.getApplicationContext().getPackageName()+".fileprovider",
                        photoFile);

              
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                startActivityForResult(takePictureIntent, ConstantDefine.ACT_TAKE_PIC);
            }
        }
    }
    //https://hjiee.tistory.com/entry/Android-TessTwo%EB%A5%BC-%EC%9D%B4%EC%9A%A9%ED%95%9C-OCR-%EC%95%B1-%EB%A7%8C%EB%93%A4%EA%B8%B0%EB%AC%B8%EC%9E%90%EC%9D%B8%EC%8B%9D
    //https://github.com/hjiee/Android-TessTwo/blob/master/app/src/main/java/com/example/android_tesstwo/MainActivity.java


    @RequiresApi(api = Build.VERSION_CODES.N)
    
    private File createImageFile()throws IOException {
        // Create an image file name
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(date);
        String imageFileName = "JPEG_" + timeStamp + "_";

      
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
      

    
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // 저장된 파일 :의 경로는 인텐트 뷰에 서 사용됨
        photoPath = image.getAbsolutePath();
        JeongLog.log.logD(" 포토 경로 " + photoPath);
        return image;
    }

    //endregion





    //region OCR Start

    // 권한 체크
    public void PermissionCheck()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED &&
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED &&
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {

                // 권한 없음
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, ConstantDefine.PERMISSION_CODE);
            } else {
                // 권한 있음
                JeongLog.log.logD("권한 있음 ");
            }
        }
    }

    // tess Ocr 시작 진입점
    public void Tesseract()
    {
        dataPath = getFilesDir() + "/tesseract/";
        JeongLog.log.logD("일단 데이터 경로 찎음  "+dataPath);
        JeongLog.log.logD("일단 데이터 경로 찎음  "+getFilesDir().getName());

        String lang = "";
        for (String Language : supportLanguageList)
        {
            checkFile(new File(dataPath + "tessdata/"), Language);
            lang += Language + "+";
        }
        tessBaseAPI = new TessBaseAPI();
        tessBaseAPI.init(dataPath, lang);
    }

    private void checkFile(File dir, String Language)
    {
        //디렉토리가 없으면 디렉토리를 만들고 그후에 파일을 카피
        // 디렉토리가 없으면~
        if (!dir.exists())
        {
            // 디렉토리 생성
            dir.mkdirs();
            copyFiles(Language);
        }


        // 디렉토리가 있니?
        if(dir.exists())
        {
            String filePath = dataPath+ "tessdata/"+Language+".traineddata";

            File dataFile = new File(filePath);

            // 데이터 파일이 없으면~~
            if( !dataFile.exists() )
            {
                copyFiles(Language);
            }

        }

    }

    private void copyFiles(String language)
    {
        // 복사 합니다 .

        try {
            String filepath = dataPath + "/tessdata/" + language + ".traineddata";
            AssetManager assetManager = getAssets();
            InputStream instream = assetManager.open("tessdata/"+language+".traineddata");
            OutputStream outstream = new FileOutputStream(filepath);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }
            outstream.flush();
            outstream.close();
            instream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //endregion


    //region Thread
    public class OCRThread extends Thread
    {
        private Bitmap rotatedImage;
        OCRThread(Bitmap rotatedImage)
        {
            this.rotatedImage = rotatedImage;
            if(!ProgressFlag)
                m_objProgressCircle = ProgressCircleDialog.show(context, "", "", true);
            ProgressFlag = true;
        }

        @Override
        public void run() {
            super.run();
            // 사진의 글자를 인식해서 옮긴다
            String OCRresult = null;
            tessBaseAPI.setImage(rotatedImage);
            OCRresult = tessBaseAPI.getUTF8Text();

            //  메시지 큐에 담을 메시지 하나를 생성한다.
            Message message = Message.obtain();
            // 무엇을 실행하는 메시지인지 구분하기 위해 구분자 설정
            message.what = ConstantDefine.RESULT_OCR;

            // 메시지가 실행될떄 참조하는 Object 형 데이터 설정
            message.obj = OCRresult;

            // 핸들러를 통해 메시지를 메시지 큐에 보냄
            m_messageHandler.sendMessage(message);

        }
    }
    //endregion


    //region Handler
    public class MessageHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what)
            {
                case ConstantDefine.RESULT_OCR:
                    TextView OCRTextView = findViewById(R.id.tv_view);
                    OCRTextView.setText(String.valueOf(msg.obj)); //텍스트 변경

                    // 원형 프로그레스바 종료
                    if(m_objProgressCircle.isShowing() && m_objProgressCircle !=null)
                        m_objProgressCircle.dismiss();

                    ProgressFlag = false;



                    endTime = System.currentTimeMillis();
                    long time = (endTime - startTime)/1000;
                    m_tvTime.setText("처리시간 : "+time+"초");
                    Toast.makeText(context, getResources().getString(R.string.CompleteMessage),Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
    //endregion



    //region Test

    public void Test()
    {
//        String lang = "eng";
        image = BitmapFactory.decodeResource(getResources(), R.drawable.korlite);


        dataPath = getFilesDir()+"/tesseract/";

//        checkFile2(new File(mDataPath + "tessdata/"),lang);

        String lang = "";
        for (String Language : supportLanguageList) {
            checkFile(new File(dataPath + "tessdata/"), Language);
            lang += Language + "+";
        }
        lang = lang.substring(0,lang.length()-1);
        tessBaseAPI = new TessBaseAPI();
        tessBaseAPI.init(dataPath, lang);
    }
    private void copyFiles2(String lang) {
        try {
            //location we want the file to be at
            String filepath = dataPath + "/tessdata/"+lang+".traineddata";

            //get access to AssetManager
            AssetManager assetManager = getAssets();

            //open byte streams for reading/writing
            InputStream instream = assetManager.open("tessdata/"+lang+".traineddata");
            OutputStream outstream = new FileOutputStream(filepath);

            //copy the file to the location specified by filepath
            byte[] buffer = new byte[1024];
            int read;
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }
            outstream.flush();
            outstream.close();
            instream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //endregion



    //region 이미지 처리 시작

    public void processImage(View v)
    {
        OCRThread ocrThread = new OCRThread(image);
        ocrThread.setDaemon(true);
        ocrThread.start();
    }

    //endregion




}
