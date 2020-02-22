package com.eungu.lineplusnote.StaticMethod;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class ImageOpenerTest {

    Context appContext;
    File f;

    @Before
    public void setUp(){
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    // 영어 평문을 이미지 주소로 불렀을때 예외처리되었는지 체크
    @Test
    public void openImageUrl1() {
        f = ImageOpener.openImage(appContext, "This is not a Image URL");
        assertTrue(f == null);
    }

    // 일반 URL을 이미지 주소로 불렀을때 예외처리되었는지 체크
    @Test
    public void openImageUrl() {
        f = ImageOpener.openImage(appContext, "http://www.naver.com");
        assertTrue(f == null);
    }

    // 제대로 된 이미지 주소로 불렀을때 파일이 전달되었는지 체크
    @Test
    public void openImageUrl3() {
        f = ImageOpener.openImage(appContext, "https://d.line-scdn.net/stf/line-lp/ko_2016_01.png");
        assertTrue(f != null);
    }

    // 한글 평문을 이미지 주소로 불렀을때 예외처리되었는지 체크
    @Test
    public void openImageUrl4() {
        f = ImageOpener.openImage(appContext, "이 문장은 한글로 입력되었습니다.");
        assertTrue(f == null);
    }

    // 만약 이미지를 저장했다면 삭제
    @After
    public void deleteImage() {
        if(f != null) f.delete();
    }
}