package com.ticketon.ai.policy;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PolicyMarkdownParserTest {

    private final PolicyMarkdownParser parser = new PolicyMarkdownParser();

    @Test
    void 정책_마크다운을_백육십육_개의_고유_Chunk로_분리한다() throws IOException {
        List<PolicyChunk> chunks;

        try (var paths = Files.list(Path.of("policies"))) {
            chunks = paths
                    .filter(path -> path.getFileName().toString().endsWith(".md"))
                    .sorted()
                    .flatMap(path -> parser.parse(path).stream())
                    .toList();
        }

        assertThat(chunks).hasSize(166);
        assertThat(chunks).extracting(PolicyChunk::chunkId).doesNotHaveDuplicates();
        assertThat(chunks).allSatisfy(chunk -> {
            assertThat(chunk.chunkId()).isNotBlank();
            assertThat(chunk.policyId()).isNotBlank();
            assertThat(chunk.domain()).isNotBlank();
            assertThat(chunk.title()).isNotBlank();
            assertThat(chunk.content()).isNotBlank();
            assertThat(chunk.version()).isNotBlank();
            assertThat(chunk.effectiveFrom()).isNotNull();
            assertThat(chunk.status()).isNotBlank();
        });
    }

    @Test
    void 파일_메타데이터를_각_정책_조항에_전달한다() {
        PolicyChunk chunk = parser.parse(Path.of("policies", "queue-seat-v1.md")).stream()
                .filter(it -> it.policyId().equals("SEAT-02"))
                .findFirst()
                .orElseThrow();

        assertThat(chunk.domain()).isEqualTo("QUEUE_SEAT");
        assertThat(chunk.version()).isEqualTo("1.0");
        assertThat(chunk.effectiveFrom()).isEqualTo(LocalDate.of(2026, 9, 1));
        assertThat(chunk.status()).isEqualTo("ACTIVE");
    }

    @Test
    void 정책_제목_아래_본문을_다음_정책_전까지만_담는다() {
        PolicyChunk chunk = parser.parse(Path.of("policies", "queue-seat-v1.md")).stream()
                .filter(it -> it.policyId().equals("SEAT-02"))
                .findFirst()
                .orElseThrow();

        assertThat(chunk.title()).isEqualTo("좌석 임시 선점 시간");
        assertThat(chunk.content())
                .contains("7분간 임시로 보호됩니다")
                .contains("예매 요청이 성공한 시점부터 계산합니다")
                .doesNotContain("동일 좌석에 대한 동시 요청");
    }
}
