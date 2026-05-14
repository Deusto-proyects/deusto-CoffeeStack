package com.deusto.coffeestack.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.AuditorAware;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JpaAuditingConfig — tests unitarios")
class JpaAuditingConfigTest {

    @Test
    @DisplayName("auditorAware bean se crea correctamente")
    void auditorAware_returnsAuditorAware() {
        JpaAuditingConfig config = new JpaAuditingConfig();
        AuditorAware<String> auditorAware = config.auditorAware();

        assertThat(auditorAware).isNotNull();
        assertThat(auditorAware).isInstanceOf(SpringSecurityAuditorAware.class);
    }
}
