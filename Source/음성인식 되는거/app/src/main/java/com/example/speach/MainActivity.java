package com.example.speach;
import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    Intent intent;
    SpeechRecognizer speechRecognizer;
    TextView textView, textView2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 10004);
        //권한 대충주고

        textView = findViewById(R.id.textview1);
        textView2 = findViewById(R.id.textview2);

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); //인텐트 액션 음성으로가기 생성
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR"); //사용자 언어설정 (나라)
                //intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "말을하세요 말을 제발.."); //작동안함.. 사용자에게 보여줄 글자라고함

                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext()); //음성인식 객체
                speechRecognizer.setRecognitionListener(recognitionListener); //음성인식 리스너 등록

                speechRecognizer.startListening(intent); //인텐트 ㄱㄱ
            }
        });

    }
    // 이벤트는 RecognitionListener를 통해 받을 수 있다..
    private RecognitionListener recognitionListener = new RecognitionListener() { //음성 진행상황이벤트
        @Override
        public void onReadyForSpeech(Bundle bundle) { //음성 시작할때
            textView2.setText("현재사황 onReadyForSpeech");  //사황 샹크스>>>>>>카이도우>빅맘>검은보털
        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(float v) {
        }

        @Override
        public void onBufferReceived(byte[] bytes) {
        }

        @Override
        public void onEndOfSpeech() {
        }

        @Override
        public void onError(int i) { //실패했을때 음성인식
            textView2.setText("현재사황 : onError");

        }

        @Override
        public void onResults(Bundle bundle) {
            //음성의 결과는 매개변수에 있는 ArrayList형태로 Bundle에 담겨 넘어오게 됨
            //bundle의 키와 값은 SpeechRecognizer.RESULTS_RECOGNITION



            textView2.setText("현재사황 : onResults");


            //결과 받아오기
            String key= SpeechRecognizer.RESULTS_RECOGNITION;

            ArrayList<String> result = bundle.getStringArrayList(key); // 음성데이터 list로 받아옴

            String[] rs = new String[result.size()]; //배열생성~ 출력 하기위해  //뭐라고 시부릴지 모르니 결과값 사이즈만큼 배열 크기만듬
            result.toArray(rs);                      // list배열로 변신    //toArray() == list값 배열에 넣기

            textView.setText(rs[0]);
            //textView.setText(key);
        }

        @Override
        public void onPartialResults(Bundle bundle) {
        }

        @Override
        public void onEvent(int i, Bundle bundle) {
        }
    };
}