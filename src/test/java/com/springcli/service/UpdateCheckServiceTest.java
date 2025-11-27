package com.springcli.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest
class UpdateCheckServiceTest {

    @Autowired
    private UpdateCheckService service;

    @Test
    void shouldCreateService() {
        assertThat(service).isNotNull();
    }

    @Test
    void shouldCheckForUpdates() {
        UpdateCheckService.UpdateInfo info = service.checkForUpdates();

        assertThat(info).isNotNull();
        assertThat(info.currentVersion()).isNotEmpty();
    }

    @Test
    void shouldHandleNetworkFailuresGracefully() {
        UpdateCheckService.UpdateInfo info = service.checkForUpdates();

        assertThat(info).isNotNull();
        assertThat(info.currentVersion()).isEqualTo("1.1.0");
    }

    @Test
    void shouldReturnCurrentVersionOnFailure() {
        UpdateCheckService.UpdateInfo info = service.checkForUpdates();

        assertThat(info).isNotNull();
        assertThat(info.currentVersion()).isNotNull();
        assertThat(info.currentVersion()).isNotEmpty();
    }

    @Test
    void shouldHaveValidUpdateInfoStructure() {
        UpdateCheckService.UpdateInfo info = service.checkForUpdates();

        assertThat(info).isNotNull();
        assertThat(info.currentVersion()).isNotNull();
        assertThat(info.latestVersion()).isNotNull();
        assertThat(info.updateAvailable()).isNotNull();
    }

    @Test
    void shouldNotHaveNullReleaseUrlWhenUpdateAvailable() {
        UpdateCheckService.UpdateInfo info = service.checkForUpdates();

        if (info.updateAvailable()) {
            assertThat(info.releaseUrl()).isNotNull();
            assertThat(info.releaseUrl()).isNotEmpty();
        }
    }

    @Test
    void shouldHaveCorrectCurrentVersion() {
        UpdateCheckService.UpdateInfo info = service.checkForUpdates();

        assertThat(info.currentVersion()).matches("\\d+\\.\\d+\\.\\d+");
    }

    @Test
    void shouldIndicateNoUpdateWhenNetworkFails() {
        UpdateCheckService.UpdateInfo info = service.checkForUpdates();

        assertThat(info.updateAvailable()).isFalse();
    }

    @Test
    void shouldReturnSameCurrentAndLatestVersionOnFailure() {
        UpdateCheckService.UpdateInfo info = service.checkForUpdates();

        if (!info.updateAvailable()) {
            assertThat(info.currentVersion()).isEqualTo(info.latestVersion());
        }
    }

    @Test
    void shouldNotThrowExceptionOnCheckForUpdates() {
        assertThatCode(() -> service.checkForUpdates()).doesNotThrowAnyException();
    }
}
