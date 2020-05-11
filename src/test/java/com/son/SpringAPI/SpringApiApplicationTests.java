package com.son.SpringAPI;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class SpringApiApplicationTests {

	@Test
	public void contextLoads() {
		/*
		 ActiveProfiles 를 설정하지 않는 경우 BaseControllerTest처럼
		 application.properties 을 application-test.properties로 오버라이딩 하지 않고
		 기존의 외부 설정파일을 사용하게 된다 = 테스트 전용 h2 설정을 사용하지 않고 PostgreSQL 을 쓰게 된다
		 */
	}

}
