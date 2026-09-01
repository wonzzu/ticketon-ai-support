package com.ticketon.ai;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class TicketonAiSupportApplicationTests {

	@MockitoBean
	private ChatModel chatModel;

	@MockitoBean
	private VectorStore vectorStore;

	@Test
	void contextLoads() {
	}

}
