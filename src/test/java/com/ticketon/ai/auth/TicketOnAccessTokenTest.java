package com.ticketon.ai.auth;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TicketOnAccessTokenTest {

    @Test
    void Authorization_헤더가_없으면_공개_질문을_위해_빈_값을_반환한다() {
        assertThat(TicketOnAccessToken.fromOptional(null)).isEmpty();
        assertThat(TicketOnAccessToken.fromOptional(" ")).isEmpty();
    }

    @Test
    void Bearer_헤더가_있으면_ToolContext에_넣을_토큰을_반환한다() {
        assertThat(TicketOnAccessToken.fromOptional("Bearer user-token"))
                .contains(new TicketOnAccessToken("user-token"));
    }

    @Test
    void 잘못된_Authorization_형식은_인증_실패로_처리한다() {
        assertThatThrownBy(() -> TicketOnAccessToken.fromOptional("Basic credential"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401");
    }
}
